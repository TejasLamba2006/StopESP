package com.github.tejaslamba2006.stopesp.manager;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.util.Scheduler;
import com.github.tejaslamba2006.stopesp.util.TransparentBlocks;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class AntiEspManager {
   private final StopESP plugin;
   private final DeepShieldConfig dsConfig;
   private Scheduler.Task scanTask;
   private boolean enabled = true;
   private final ConcurrentHashMap<UUID, Map<UUID, Entity>> hiddenEntities = new ConcurrentHashMap();
   private final ConcurrentHashMap<UUID, Scoreboard> nameplateBoards = new ConcurrentHashMap();

   public AntiEspManager(StopESP plugin, DeepShieldConfig dsConfig) {
      this.plugin = plugin;
      this.dsConfig = dsConfig;
   }

   public void startScan() {
      this.enabled = true;
      long interval = this.dsConfig.getScanInterval();
      this.scanTask = Scheduler.runTimer(this.plugin, () -> {
         if (this.enabled) {
            for(Player player : Bukkit.getOnlinePlayers()) {
               if (this.dsConfig.isEnabledWorld(player.getWorld()) && !player.hasPermission("stopesp.bypass")) {
                  Scheduler.runEntity(this.plugin, player, () -> {
                     if (player.isOnline()) {
                        double radius = this.dsConfig.getAntiEspRevealRadius();
                        Collection<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
                        Map<UUID, Entity> playerHidden = (Map)this.hiddenEntities.computeIfAbsent(player.getUniqueId(), (k) -> new ConcurrentHashMap());
                        Set<UUID> nearbyUuids = new HashSet();

                        for(Entity entity : nearby) {
                           if (!entity.equals(player) && this.shouldObfuscate(entity)) {
                              UUID entityUuid = entity.getUniqueId();
                              nearbyUuids.add(entityUuid);
                              boolean canSee = this.canSeeEntity(player, entity);
                              if (canSee && this.dsConfig.isAntiEspHideInvisibleEntities() && this.isConsideredInvisible(entity)) {
                                 canSee = false;
                              }

                              if (canSee) {
                                 if (playerHidden.remove(entityUuid) != null) {
                                    player.showEntity(this.plugin, entity);
                                 }
                              } else if (playerHidden.put(entityUuid, entity) == null) {
                                 player.hideEntity(this.plugin, entity);
                              }
                           }
                        }

                        playerHidden.entrySet().removeIf((entry) -> {
                           UUID uuid = (UUID)entry.getKey();
                           if (!nearbyUuids.contains(uuid)) {
                              Entity entity = (Entity)entry.getValue();
                              if (entity.isValid()) {
                                 player.showEntity(this.plugin, entity);
                              }

                              return true;
                           } else {
                              return false;
                           }
                        });

                        if (this.dsConfig.isAntiEspHideNameplates() && this.dsConfig.isAntiEspObfuscatePlayers()) {
                           this.applyNameplateHiding(player, playerHidden);
                        }
                     }
                  });
               }
            }

         }
      }, 1L, interval);
   }

   private boolean shouldObfuscate(Entity entity) {
      if (this.dsConfig.getAntiEspExtraEntityHide().contains(entity.getType())) {
         return true;
      } else if (entity instanceof Player) {
         return this.dsConfig.isAntiEspObfuscatePlayers();
      } else {
         return entity instanceof LivingEntity ? this.dsConfig.isAntiEspObfuscateMobs() : false;
      }
   }

   private boolean canSeeEntity(Player player, Entity entity) {
      Location eyeLocation = player.getEyeLocation();
      if (entity instanceof LivingEntity living) {
         Location entityEye = living.getEyeLocation();
         if (this.hasLineOfSightThroughTransparents(eyeLocation, entityEye)) {
            return true;
         }

         Location entityFeet = entity.getLocation().clone().add((double)0.0F, 0.1, (double)0.0F);
         if (this.hasLineOfSightThroughTransparents(eyeLocation, entityFeet)) {
            return true;
         }

         double halfHeight = (living.getEyeHeight() + 0.1) / (double)2.0F;
         Location entityMid = entity.getLocation().clone().add((double)0.0F, halfHeight, (double)0.0F);
         if (this.hasLineOfSightThroughTransparents(eyeLocation, entityMid)) {
            return true;
         }
      } else {
         Location entityLoc = entity.getLocation();
         double height = entity.getHeight();
         Location center = entityLoc.clone().add((double)0.0F, height / (double)2.0F, (double)0.0F);
         if (this.hasLineOfSightThroughTransparents(eyeLocation, center)) {
            return true;
         }

         if (this.hasLineOfSightThroughTransparents(eyeLocation, entityLoc)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasLineOfSightThroughTransparents(Location start, Location end) {
      if (start.getWorld() != end.getWorld()) {
         return false;
      } else {
         Vector direction = end.toVector().subtract(start.toVector());
         double totalDistance = direction.length();
         if (totalDistance < 0.01) {
            return true;
         } else {
            direction.normalize();
            Location current = start.clone();
            double distanceTraveled = (double)0.0F;
            int maxIterations = 100;

            while(true) {
               if (distanceTraveled < totalDistance && maxIterations-- > 0) {
                  double remaining = totalDistance - distanceTraveled;
                  RayTraceResult result = start.getWorld().rayTraceBlocks(current, direction, remaining, FluidCollisionMode.NEVER, false);
                  if (result != null && result.getHitBlock() != null) {
                     Block hitBlock = result.getHitBlock();
                     Material hitMaterial = hitBlock.getType();
                     if (!TransparentBlocks.isTransparent(hitMaterial)) {
                        return false;
                     }

                     Vector hitPoint = result.getHitPosition();
                     double hitDistance = hitPoint.distance(current.toVector());
                     distanceTraveled += hitDistance + 0.05;
                     current = start.clone().add(direction.clone().multiply(distanceTraveled));
                     continue;
                  }

                  return true;
               }

               return true;
            }
         }
      }
   }

   private boolean isConsideredInvisible(Entity entity) {
      if (entity instanceof Player p) {
         return p.isInvisible() || p.getGameMode() == GameMode.SPECTATOR;
      }
      return entity instanceof LivingEntity living && living.isInvisible();
   }

   // ponytail: server-side nametag suppression via a per-viewer scoreboard team (NEVER visibility).
   // A cheat that force-shows the entity still can't read the floating name. Runs on the main thread.
   private void applyNameplateHiding(Player viewer, Map<UUID, Entity> hidden) {
      Set<String> hiddenNames = new HashSet();
      for(Entity entity : hidden.values()) {
         if (entity instanceof Player p && p.isValid()) {
            hiddenNames.add(p.getName());
         }
      }

      Scheduler.run(this.plugin, () -> {
         if (!viewer.isOnline()) {
            return;
         }
         Scoreboard board = viewer.getScoreboard();
         this.nameplateBoards.put(viewer.getUniqueId(), board);
         Team team = board.getTeam("stopesp_hidden");
         if (team == null) {
            team = board.registerNewTeam("stopesp_hidden");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
         }

         for(String entry : new HashSet<>(team.getEntries())) {
            if (!hiddenNames.contains(entry)) {
               team.removeEntry(entry);
            }
         }

         for(String name : hiddenNames) {
            if (!team.hasEntry(name)) {
               team.addEntry(name);
            }
         }
      });
   }

   private void clearNameplateHiding(Player viewer) {
      Scoreboard board = this.nameplateBoards.remove(viewer.getUniqueId());
      if (board != null) {
         Scheduler.run(this.plugin, () -> {
            Team team = board.getTeam("stopesp_hidden");
            if (team != null) {
               team.unregister();
            }
         });
      }
   }

   public void stopTask() {
      this.enabled = false;
      if (this.scanTask != null) {
         this.scanTask.cancel();
      }

      for(Map.Entry<UUID, Map<UUID, Entity>> entry : this.hiddenEntities.entrySet()) {
         Player player = Bukkit.getPlayer((UUID)entry.getKey());
         if (player != null && player.isOnline()) {
            for(Entity entity : entry.getValue().values()) {
               if (entity.isValid()) {
                  player.showEntity(this.plugin, entity);
               }
            }

            this.clearNameplateHiding(player);
         }
      }

      this.hiddenEntities.clear();
   }

   public void cleanupPlayer(Player player) {
      this.clearNameplateHiding(player);
      Map<UUID, Entity> playerHidden = (Map)this.hiddenEntities.remove(player.getUniqueId());
      if (playerHidden != null && player.isOnline()) {
         for(Entity entity : playerHidden.values()) {
            if (entity.isValid()) {
               player.showEntity(this.plugin, entity);
            }
         }
      }

   }
}
