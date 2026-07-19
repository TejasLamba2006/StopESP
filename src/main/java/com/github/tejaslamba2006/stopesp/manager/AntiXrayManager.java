package com.github.tejaslamba2006.stopesp.manager;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.util.Scheduler;
import com.github.tejaslamba2006.stopesp.util.TransparentBlocks;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AntiXrayManager {
   private final StopESP plugin;
   private final DeepShieldConfig dsConfig;
   private boolean enabled = true;
   private final Map<UUID, Map<Long, ChunkData>> chunkMap = new ConcurrentHashMap();
   private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap();
   private final ExecutorService workerPool;
   private Scheduler.Task obfuscationTask;
   private int tickCounter = 0;

   public AntiXrayManager(StopESP plugin, DeepShieldConfig dsConfig) {
      this.plugin = plugin;
      this.dsConfig = dsConfig;
      int threads = dsConfig.getWorkerThreads();
      if (threads <= 0) {
         threads = Runtime.getRuntime().availableProcessors() * 2;
      }

      this.workerPool = Executors.newFixedThreadPool(threads);
   }

   public void startScan() {
      this.enabled = true;
      if (this.obfuscationTask != null) {
         this.obfuscationTask.cancel();
      }

      if (!Scheduler.isFolia()) {
         for(World world : Bukkit.getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
               this.indexChunk(chunk);
            }
         }
      } else {
         for(Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;

            for(int x = -8; x <= 8; ++x) {
               for(int z = -8; z <= 8; ++z) {
                  int tx = cx + x;
                  int tz = cz + z;
                  World world = player.getWorld();
                  Scheduler.runRegion(this.plugin, new Location(world, (double)(tx << 4), (double)0.0F, (double)(tz << 4)), () -> {
                     if (world.isChunkLoaded(tx, tz)) {
                        this.indexChunk(world.getChunkAt(tx, tz));
                     }

                  });
               }
            }
         }
      }

      long interval = this.dsConfig.getScanInterval();
      this.obfuscationTask = Scheduler.runTimer(this.plugin, () -> {
         if (this.enabled) {
            ++this.tickCounter;
            Map<UUID, PlayerUpdateSnapshot> snapshots = new HashMap();

            for(Player player : Bukkit.getOnlinePlayers()) {
               if (this.dsConfig.isEnabledWorld(player.getWorld()) && !player.hasPermission("stopesp.bypass")) {
                  Location eye = player.getEyeLocation();
                  snapshots.put(player.getUniqueId(), new PlayerUpdateSnapshot(player, eye, eye.getBlockX() >> 4, eye.getBlockZ() >> 4, false));
               }
            }

            for(PlayerUpdateSnapshot snap : snapshots.values()) {
               PlayerData data = (PlayerData)this.playerData.computeIfAbsent(snap.player.getUniqueId(), (k) -> new PlayerData());
               if (data.processing.compareAndSet(false, true)) {
                  this.workerPool.submit(() -> {
                     try {
                        this.updateForPlayerAsync(snap);
                     } finally {
                        data.processing.set(false);
                     }

                  });
               }
            }

         }
      }, 1L, interval);
   }

   public void stopTask() {
      this.enabled = false;
      if (this.obfuscationTask != null) {
         this.obfuscationTask.cancel();
      }

      this.workerPool.shutdown();

      try {
         if (!this.workerPool.awaitTermination(2L, TimeUnit.SECONDS)) {
            this.workerPool.shutdownNow();
         }
      } catch (InterruptedException var3) {
         this.workerPool.shutdownNow();
      }

      for(Player player : Bukkit.getOnlinePlayers()) {
         this.revealAll(player);
      }

   }

   public void updateForPlayerAsync(PlayerUpdateSnapshot snap) {
      Player player = snap.player;
      if (!player.hasPermission("stopesp.bypass")) {
         Location pLoc = snap.eyeLoc;
         PlayerData data = (PlayerData)this.playerData.computeIfAbsent(player.getUniqueId(), (k) -> new PlayerData());
         long now = System.currentTimeMillis();
         boolean force = snap.force || data.forceUpdate.getAndSet(false);
         if (!force) {
            if (now - data.lastUpdateTime < 50L) {
               return;
            }

            if (data.lastUpdateLoc != null && data.lastUpdateLoc.getWorld() == pLoc.getWorld()) {
               double distSq = data.lastUpdateLoc.distanceSquared(pLoc);
               double yawDiff = (double)Math.abs(data.lastUpdateLoc.getYaw() - pLoc.getYaw());
               double pitchDiff = (double)Math.abs(data.lastUpdateLoc.getPitch() - pLoc.getPitch());
               if (distSq < 0.0025 && yawDiff < (double)1.0F && pitchDiff < (double)1.0F) {
                  return;
               }
            }
         }

         data.lastUpdateLoc = pLoc.clone();
         data.lastUpdateTime = now;
         Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.get(player.getWorld().getUID());
         if (worldIndex != null) {
            double worldRevealRadius = this.dsConfig.getRevealRadius(player.getWorld());
            int radius = (int)Math.ceil(worldRevealRadius / (double)16.0F) + 1;
            int minHeight = player.getWorld().getMinHeight();
            int maxHeight = player.getWorld().getMaxHeight();
            double revealRadSq = worldRevealRadius * worldRevealRadius;
            Map<Location, BlockData> totalChanges = new HashMap();
            boolean hasVelocity = player.getVelocity().lengthSquared() > 0.01;
            Location futureLoc = hasVelocity ? pLoc.clone().add(player.getVelocity().multiply(3)) : null;
            Vector playerDir = pLoc.getDirection();
            Map<Long, Location> allProtected = new HashMap();
            Map<Long, ChunkData> allProtectedChunk = new HashMap();
            Set<Long> directlyVisible = new HashSet();

            for(int x = -radius; x <= radius; ++x) {
               for(int z = -radius; z <= radius; ++z) {
                  int chunkX = snap.chunkX + x;
                  int chunkZ = snap.chunkZ + z;
                  long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);
                  ChunkData chunkData = (ChunkData)worldIndex.get(chunkKey);
                  if (chunkData != null && !chunkData.protectedBlocks.isEmpty()) {
                     BitSet protectedBits = chunkData.protectedBlocks;

                     for(int i = protectedBits.nextSetBit(0); i >= 0; i = protectedBits.nextSetBit(i + 1)) {
                        int relY = i >> 8;
                        int blockY = relY + minHeight;
                        int relX = i >> 4 & 15;
                        int relZ = i & 15;
                        int blockX = (chunkX << 4) + relX;
                        int blockZ = (chunkZ << 4) + relZ;
                        long blockKey = (long)blockX & 67108863L | ((long)blockZ & 67108863L) << 26 | ((long)blockY & 4095L) << 52;
                        Location loc = new Location(player.getWorld(), (double)blockX, (double)blockY, (double)blockZ);
                        allProtected.put(blockKey, loc);
                        allProtectedChunk.put(blockKey, chunkData);
                        double distSq = pLoc.distanceSquared(loc);
                        if (!(distSq > revealRadSq) && !this.isBuried(worldIndex, chunkX, chunkZ, relX, blockY, relZ, minHeight, maxHeight)) {
                           if (this.dsConfig.isFrustumCulling() && distSq > (double)9.0F) {
                              Vector blockDir = (new Vector((double)blockX + (double)0.5F - pLoc.getX(), (double)blockY + (double)0.5F - pLoc.getY(), (double)blockZ + (double)0.5F - pLoc.getZ())).normalize();
                              if (playerDir.dot(blockDir) < -0.2) {
                                 continue;
                              }
                           }

                           boolean canSee = this.hasLoSAsync(worldIndex, pLoc, loc, true);
                           if (!canSee && hasVelocity) {
                              canSee = this.hasLoSAsync(worldIndex, futureLoc, loc, true);
                           }

                           if (canSee) {
                              directlyVisible.add(blockKey);
                           }
                        }
                     }
                  }
               }
            }

            Set<Long> finalVisible = new HashSet(directlyVisible);
            Set<Long> currentLayer = new HashSet(directlyVisible);

            Set<Long> nextLayer;
            for(int[][] offsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}; !currentLayer.isEmpty(); currentLayer = nextLayer) {
               nextLayer = new HashSet();

               for(long key : currentLayer) {
                  int bx = (int)(key & 67108863L);
                  if (bx >= 33554432) {
                     bx -= 67108864;
                  }

                  int bz = (int)(key >> 26 & 67108863L);
                  if (bz >= 33554432) {
                     bz -= 67108864;
                  }

                  int by = (int)(key >> 52 & 4095L);
                  if (by >= 2048) {
                     by -= 4096;
                  }

                  for(int[] off : offsets) {
                     int ax = bx + off[0];
                     int ay = by + off[1];
                     int az = bz + off[2];
                     long adjKey = (long)ax & 67108863L | ((long)az & 67108863L) << 26 | ((long)ay & 4095L) << 52;
                     if (!finalVisible.contains(adjKey) && allProtected.containsKey(adjKey)) {
                        finalVisible.add(adjKey);
                        nextLayer.add(adjKey);
                     }
                  }
               }
            }

            int bufferThreshold = this.dsConfig.getVisibilityBuffer();

            for(Map.Entry<Long, Location> entry : allProtected.entrySet()) {
               long blockKey = (Long)entry.getKey();
               Location loc = (Location)entry.getValue();
               ChunkData chunkData = (ChunkData)allProtectedChunk.get(blockKey);
               if (chunkData != null) {
                  if (finalVisible.contains(blockKey)) {
                     data.visibilityBuffer.remove(loc);
                     if (data.visibleBlocks.add(loc)) {
                        BlockData realData = (BlockData)chunkData.originalBlockData.get(loc);
                        totalChanges.put(loc, realData != null ? realData : loc.getBlock().getBlockData());
                     }
                  } else {
                     int failCount = (Integer)data.visibilityBuffer.getOrDefault(loc, 0) + 1;
                     data.visibilityBuffer.put(loc, failCount);
                     if (failCount >= bufferThreshold && data.visibleBlocks.remove(loc)) {
                        totalChanges.put(loc, this.getFakeMaterial(loc).createBlockData());
                     }
                  }
               }
            }

            if (!totalChanges.isEmpty()) {
               player.sendMultiBlockChange(totalChanges);
            }

         }
      }
   }

   private boolean isBuried(Map<Long, ChunkData> worldIndex, int cx, int cz, int relX, int y, int relZ, int minHeight, int maxHeight) {
      if (y > minHeight && y < maxHeight - 1) {
         ChunkData center = (ChunkData)worldIndex.get(Chunk.getChunkKey(cx, cz));
         if (center == null) {
            return false;
         } else if (center.isOccluding(relX, y + 1, relZ) && center.isOccluding(relX, y - 1, relZ)) {
            if (relX < 15) {
               if (!center.isOccluding(relX + 1, y, relZ)) {
                  return false;
               }
            } else {
               ChunkData east = (ChunkData)worldIndex.get(Chunk.getChunkKey(cx + 1, cz));
               if (east == null || !east.isOccluding(0, y, relZ)) {
                  return false;
               }
            }

            if (relX > 0) {
               if (!center.isOccluding(relX - 1, y, relZ)) {
                  return false;
               }
            } else {
               ChunkData west = (ChunkData)worldIndex.get(Chunk.getChunkKey(cx - 1, cz));
               if (west == null || !west.isOccluding(15, y, relZ)) {
                  return false;
               }
            }

            if (relZ < 15) {
               if (!center.isOccluding(relX, y, relZ + 1)) {
                  return false;
               }
            } else {
               ChunkData south = (ChunkData)worldIndex.get(Chunk.getChunkKey(cx, cz + 1));
               if (south == null || !south.isOccluding(relX, y, 0)) {
                  return false;
               }
            }

            if (relZ > 0) {
               if (!center.isOccluding(relX, y, relZ - 1)) {
                  return false;
               }
            } else {
               ChunkData north = (ChunkData)worldIndex.get(Chunk.getChunkKey(cx, cz - 1));
               if (north == null || !north.isOccluding(relX, y, 15)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean hasLoSAsync(Player player, Location eye, Location target, boolean isBlock) {
      if (eye != null && target != null && eye.getWorld() == target.getWorld()) {
         Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.get(eye.getWorld().getUID());
         return worldIndex == null ? false : this.hasLoSAsync(worldIndex, eye, target, isBlock);
      } else {
         return false;
      }
   }

   public boolean hasLoSAsync(Map<Long, ChunkData> worldIndex, Location eye, Location target, boolean isBlock) {
      if (eye.getWorld() != target.getWorld()) {
         return false;
      } else {
         if (isBlock) {
            if (this.isPathClearAsync(worldIndex, eye, target.getX() + (double)0.5F, target.getY() + (double)0.5F, target.getZ() + (double)0.5F)) {
               return true;
            }

            double[][] faces = new double[][]{{-0.05, (double)0.5F, (double)0.5F}, {1.05, (double)0.5F, (double)0.5F}, {(double)0.5F, -0.05, (double)0.5F}, {(double)0.5F, 1.05, (double)0.5F}, {(double)0.5F, (double)0.5F, -0.05}, {(double)0.5F, (double)0.5F, 1.05}};

            for(double[] off : faces) {
               if (this.isPathClearAsync(worldIndex, eye, target.getX() + off[0], target.getY() + off[1], target.getZ() + off[2])) {
                  return true;
               }
            }

            double[][] corners = new double[][]{{-0.05, -0.05, -0.05}, {1.05, -0.05, -0.05}, {-0.05, 1.05, -0.05}, {-0.05, -0.05, 1.05}, {1.05, 1.05, -0.05}, {1.05, -0.05, 1.05}, {-0.05, 1.05, 1.05}, {1.05, 1.05, 1.05}};

            for(double[] off : corners) {
               if (this.isPathClearAsync(worldIndex, eye, target.getX() + off[0], target.getY() + off[1], target.getZ() + off[2])) {
                  return true;
               }
            }
         } else {
            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();
            if (this.isPathClearAsync(worldIndex, eye, tx + (double)0.5F, ty + (double)0.5F, tz + (double)0.5F)) {
               return true;
            }

            double lo = 0.1;
            double hi = 0.9;
            if (this.isPathClearAsync(worldIndex, eye, tx + lo, ty + lo, tz + lo)) {
               return true;
            }

            if (this.isPathClearAsync(worldIndex, eye, tx + hi, ty + hi, tz + hi)) {
               return true;
            }

            if (this.isPathClearAsync(worldIndex, eye, tx + lo, ty + hi, tz + lo)) {
               return true;
            }

            if (this.isPathClearAsync(worldIndex, eye, tx + hi, ty + lo, tz + hi)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isPathClearAsync(Map<Long, ChunkData> worldIndex, Location start, double tx, double ty, double tz) {
      double x = start.getX();
      double y = start.getY();
      double z = start.getZ();
      int ix = (int)Math.floor(x);
      int iy = (int)Math.floor(y);
      int iz = (int)Math.floor(z);
      int targetX = (int)Math.floor(tx);
      int targetY = (int)Math.floor(ty);
      int targetZ = (int)Math.floor(tz);
      double dx = tx - x;
      double dy = ty - y;
      double dz = tz - z;
      double rayLength = Math.sqrt(dx * dx + dy * dy + dz * dz);
      int stepX = dx > (double)0.0F ? 1 : (dx < (double)0.0F ? -1 : 0);
      int stepY = dy > (double)0.0F ? 1 : (dy < (double)0.0F ? -1 : 0);
      int stepZ = dz > (double)0.0F ? 1 : (dz < (double)0.0F ? -1 : 0);
      double deltaX = stepX != 0 ? Math.abs((double)1.0F / dx) : Double.MAX_VALUE;
      double deltaY = stepY != 0 ? Math.abs((double)1.0F / dy) : Double.MAX_VALUE;
      double deltaZ = stepZ != 0 ? Math.abs((double)1.0F / dz) : Double.MAX_VALUE;
      double maxX = stepX > 0 ? (Math.floor(x) + (double)1.0F - x) * deltaX : (x - Math.floor(x)) * deltaX;
      double maxY = stepY > 0 ? (Math.floor(y) + (double)1.0F - y) * deltaY : (y - Math.floor(y)) * deltaY;
      double maxZ = stepZ > 0 ? (Math.floor(z) + (double)1.0F - z) * deltaZ : (z - Math.floor(z)) * deltaZ;
      int limit = 120;
      int minHeight = start.getWorld().getMinHeight();
      int maxHeight = start.getWorld().getMaxHeight();
      ChunkData lastChunk = null;
      long lastChunkKey = Long.MIN_VALUE;
      boolean firstStep = true;
      double currentT = (double)0.0F;

      while(limit-- > 0) {
         if (ix == targetX && iy == targetY && iz == targetZ) {
            return true;
         }

         if (iy < minHeight || iy >= maxHeight) {
            return false;
         }

         long chunkKey = Chunk.getChunkKey(ix >> 4, iz >> 4);
         ChunkData chunkData;
         if (chunkKey == lastChunkKey) {
            chunkData = lastChunk;
         } else {
            chunkData = (ChunkData)worldIndex.get(chunkKey);
            lastChunk = chunkData;
            lastChunkKey = chunkKey;
         }

         double nextT = Math.min(maxX, Math.min(maxY, maxZ));
         if (chunkData != null && chunkData.isOccluding(ix & 15, iy, iz & 15)) {
            double distInVoxel = (nextT - currentT) * rayLength;
            if (distInVoxel > 0.15 && (!firstStep || ix != (int)Math.floor(x) || iy != (int)Math.floor(y) || iz != (int)Math.floor(z))) {
               return ix == targetX && iy == targetY && iz == targetZ;
            }
         }

         firstStep = false;
         currentT = nextT;
         if (maxX < maxY) {
            if (maxX < maxZ) {
               ix += stepX;
               maxX += deltaX;
            } else {
               iz += stepZ;
               maxZ += deltaZ;
            }
         } else if (maxY < maxZ) {
            iy += stepY;
            maxY += deltaY;
         } else {
            iz += stepZ;
            maxZ += deltaZ;
         }
      }

      return true;
   }

   public void hideAllInChunk(Player player, int cx, int cz) {
      World world = player.getWorld();
      Scheduler.runRegion(this.plugin, new Location(world, (double)(cx << 4), (double)0.0F, (double)(cz << 4)), () -> {
         if (world.isChunkLoaded(cx, cz)) {
            Chunk chunk = world.getChunkAt(cx, cz);
            this.indexChunk(chunk);
            if (player.isOnline()) {
               Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.get(world.getUID());
               if (worldIndex != null) {
                  long chunkKey = Chunk.getChunkKey(cx, cz);
                  ChunkData chunkData = (ChunkData)worldIndex.get(chunkKey);
                  if (chunkData != null && !chunkData.protectedBlocks.isEmpty()) {
                     int minHeight = world.getMinHeight();
                     Map<Location, BlockData> changes = new HashMap();
                     PlayerData data = (PlayerData)this.playerData.computeIfAbsent(player.getUniqueId(), (k) -> new PlayerData());
                     BitSet protectedBits = chunkData.protectedBlocks;

                     for(int i = protectedBits.nextSetBit(0); i >= 0; i = protectedBits.nextSetBit(i + 1)) {
                        int relY = i >> 8;
                        int blockY = relY + minHeight;
                        int relX = i >> 4 & 15;
                        int relZ = i & 15;
                        Location loc = new Location(world, (double)((cx << 4) + relX), (double)blockY, (double)((cz << 4) + relZ));
                        if (!data.visibleBlocks.contains(loc)) {
                           changes.put(loc, this.getFakeMaterial(loc).createBlockData());
                        }
                     }

                     if (!changes.isEmpty()) {
                        player.sendMultiBlockChange(changes);
                     }

                     data.forceUpdate.set(true);
                  }
               }
            }
         }
      });
   }

   public void hideAllInChunk(Player player, Chunk chunk) {
      this.hideAllInChunk(player, chunk.getX(), chunk.getZ());
   }

   public void revealAll(Player player) {
      if (player.isOnline() && this.dsConfig.isEnabledWorld(player.getWorld())) {
         Location eye = player.getEyeLocation();
         this.updateForPlayerAsync(new PlayerUpdateSnapshot(player, eye, player.getChunk().getX(), player.getChunk().getZ(), true));
      }
   }

   public void revealInstant(Location origin) {
      if (origin != null && origin.getWorld() != null) {
         for(Player player : origin.getWorld().getPlayers()) {
            if (!(player.getLocation().distanceSquared(origin) > (double)1600.0F)) {
               PlayerData data = (PlayerData)this.playerData.computeIfAbsent(player.getUniqueId(), (k) -> new PlayerData());
               data.forceUpdate.set(true);
               if (data.processing.compareAndSet(false, true)) {
                  Location eye = player.getEyeLocation();
                  PlayerUpdateSnapshot snap = new PlayerUpdateSnapshot(player, eye, player.getChunk().getX(), player.getChunk().getZ(), true);
                  this.workerPool.submit(() -> {
                     try {
                        this.updateForPlayerAsync(snap);
                     } finally {
                        data.processing.set(false);
                     }

                  });
               }
            }
         }

      }
   }

   public void indexChunk(Chunk chunk) {
      if (this.dsConfig.isEnabledWorld(chunk.getWorld())) {
         UUID worldUid = chunk.getWorld().getUID();
         Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.computeIfAbsent(worldUid, (k) -> new ConcurrentHashMap());
         long chunkKey = chunk.getChunkKey();
         if (!worldIndex.containsKey(chunkKey)) {
            int minHeight = chunk.getWorld().getMinHeight();
            int maxHeight = chunk.getWorld().getMaxHeight();
            ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            World world = chunk.getWorld();
            this.workerPool.submit(() -> {
               if (!worldIndex.containsKey(chunkKey)) {
                  ChunkData chunkData = new ChunkData(chunkKey, minHeight);
                  boolean hasProtected = false;

                  for(int y = minHeight; y < maxHeight; ++y) {
                     int relY = y - minHeight;

                     for(int x = 0; x < 16; ++x) {
                        for(int z = 0; z < 16; ++z) {
                           Material type = snapshot.getBlockType(x, y, z);
                           int bitIndex = relY << 8 | x << 4 | z;
                           if (type.isOccluding() && !TransparentBlocks.isTransparent(type)) {
                              chunkData.occludingBlocks.set(bitIndex);
                           }

                           if (this.dsConfig.getProtectedBlocks().contains(type)) {
                              chunkData.protectedBlocks.set(bitIndex);
                              hasProtected = true;
                              Location loc = new Location(world, (double)((chunkX << 4) + x), (double)y, (double)((chunkZ << 4) + z));
                              chunkData.originalBlockData.put(loc, snapshot.getBlockData(x, y, z));
                           }
                        }
                     }
                  }

                  if (hasProtected || !chunkData.occludingBlocks.isEmpty()) {
                     worldIndex.put(chunkKey, chunkData);
                  }

               }
            });
         }
      }
   }

   public void removeChunk(Chunk chunk) {
      Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.get(chunk.getWorld().getUID());
      if (worldIndex != null) {
         worldIndex.remove(chunk.getChunkKey());
      }

   }

   public void addBlock(Player player, Block block) {
      if (this.dsConfig.isEnabledWorld(block.getWorld())) {
         UUID worldUid = block.getWorld().getUID();
         Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.computeIfAbsent(worldUid, (k) -> new ConcurrentHashMap());
         long chunkKey = block.getChunk().getChunkKey();
         ChunkData chunkData = (ChunkData)worldIndex.computeIfAbsent(chunkKey, (k) -> new ChunkData(chunkKey, block.getWorld().getMinHeight()));
         int relY = block.getY() - block.getWorld().getMinHeight();
         int bitIndex = relY << 8 | (block.getX() & 15) << 4 | block.getZ() & 15;
         if (block.getType().isOccluding() && !TransparentBlocks.isTransparent(block.getType())) {
            chunkData.occludingBlocks.set(bitIndex);
         } else {
            chunkData.occludingBlocks.clear(bitIndex);
         }

         if (this.dsConfig.getProtectedBlocks().contains(block.getType())) {
            chunkData.protectedBlocks.set(bitIndex);
            chunkData.originalBlockData.put(block.getLocation(), block.getBlockData());
            if (player != null) {
               PlayerData data = (PlayerData)this.playerData.get(player.getUniqueId());
               if (data != null) {
                  data.visibleBlocks.add(block.getLocation());
               }
            }
         }

         this.revealInstant(block.getLocation());
      }
   }

   public void removeBlock(Block block) {
      if (this.dsConfig.isEnabledWorld(block.getWorld())) {
         Map<Long, ChunkData> worldIndex = (Map)this.chunkMap.get(block.getWorld().getUID());
         if (worldIndex != null) {
            long chunkKey = block.getChunk().getChunkKey();
            ChunkData chunkData = (ChunkData)worldIndex.get(chunkKey);
            if (chunkData != null) {
               int relY = block.getY() - block.getWorld().getMinHeight();
               int bitIndex = relY << 8 | (block.getX() & 15) << 4 | block.getZ() & 15;
               chunkData.occludingBlocks.clear(bitIndex);
               chunkData.protectedBlocks.clear(bitIndex);
               chunkData.originalBlockData.remove(block.getLocation());
            }

            this.revealInstant(block.getLocation());
         }
      }
   }

   private Material getFakeMaterial(Location loc) {
      String mode = this.dsConfig.getFakeBlockMode();
      if (!mode.equalsIgnoreCase("AUTO")) {
         try {
            return Material.valueOf(mode.toUpperCase());
         } catch (IllegalArgumentException var4) {
         }
      }

      World world = loc.getWorld();
      if (world == null) {
         return Material.STONE;
      } else {
         switch (world.getEnvironment()) {
            case NETHER -> {
               return Material.NETHERRACK;
            }
            case THE_END -> {
               return Material.END_STONE;
            }
            default -> {
               return loc.getBlockY() < 0 ? Material.DEEPSLATE : Material.STONE;
            }
         }
      }
   }

   public void cleanupPlayer(Player player) {
      this.playerData.remove(player.getUniqueId());
   }

   public void cleanupChunk(Chunk chunk) {
      long chunkKey = chunk.getChunkKey();
      UUID worldUid = chunk.getWorld().getUID();
      this.workerPool.submit(() -> {
         for(PlayerData data : this.playerData.values()) {
            data.visibleBlocks.removeIf((loc) -> loc.getWorld().getUID().equals(worldUid) && Chunk.getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4) == chunkKey);
            data.visibilityBuffer.keySet().removeIf((loc) -> loc.getWorld().getUID().equals(worldUid) && Chunk.getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4) == chunkKey);
         }

      });
   }

   public String[] getDebugInfo(Player player) {
      PlayerData data = (PlayerData)this.playerData.get(player.getUniqueId());
      if (data == null) {
         return new String[]{"No tracking data (player never scanned)."};
      } else {
         String[] var10000 = new String[]{"Visible blocks: " + data.visibleBlocks.size(), "Visibility buffer entries: " + data.visibilityBuffer.size(), "Scan processing: " + data.processing.get(), "Force update queued: " + data.forceUpdate.get(), "Has bypass perm: " + player.hasPermission("stopesp.bypass"), null, null};
         DeepShieldConfig var10003 = this.dsConfig;
         var10000[5] = "World enabled: " + var10003.isEnabledWorld(player.getWorld());
         var10003 = this.dsConfig;
         var10000[6] = "Reveal radius: " + var10003.getRevealRadius(player.getWorld()) + " blocks";
         return var10000;
      }
   }

   public DeepShieldConfig getDsConfig() {
      return this.dsConfig;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public static class ChunkData {
      final long chunkKey;
      final int worldMinHeight;
      final BitSet protectedBlocks = new BitSet();
      final BitSet occludingBlocks = new BitSet();
      final Map<Location, BlockData> originalBlockData = new ConcurrentHashMap();

      public ChunkData(long chunkKey, int worldMinHeight) {
         this.chunkKey = chunkKey;
         this.worldMinHeight = worldMinHeight;
      }

      public boolean isOccluding(int x, int y, int z) {
         if (y < this.worldMinHeight) {
            return true;
         } else {
            int relY = y - this.worldMinHeight;
            return this.occludingBlocks.get(relY << 8 | x << 4 | z);
         }
      }
   }

   private static class PlayerData {
      Location lastUpdateLoc;
      long lastUpdateTime;
      final Set<Location> visibleBlocks = ConcurrentHashMap.newKeySet();
      final Map<Location, Integer> visibilityBuffer = new ConcurrentHashMap();
      final AtomicBoolean processing = new AtomicBoolean(false);
      final AtomicBoolean forceUpdate = new AtomicBoolean(false);
   }

   private static class PlayerUpdateSnapshot {
      final Player player;
      final Location eyeLoc;
      final int chunkX;
      final int chunkZ;
      final boolean force;

      PlayerUpdateSnapshot(Player player, Location eyeLoc, int chunkX, int chunkZ, boolean force) {
         this.player = player;
         this.eyeLoc = eyeLoc;
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         this.force = force;
      }
   }
}
