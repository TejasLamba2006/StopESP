package com.github.tejaslamba2006.stopesp.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.manager.AntiXrayManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AntiXrayPacketListener {
   private final StopESP plugin;
   private final AntiXrayManager manager;

   public AntiXrayPacketListener(StopESP plugin, AntiXrayManager manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   public void register() {
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, ListenerPriority.HIGHEST, new PacketType[]{Server.MAP_CHUNK}) {
         public void onPacketSending(PacketEvent event) {
            if (AntiXrayPacketListener.this.manager.isEnabled()) {
               Player player = event.getPlayer();
               if (AntiXrayPacketListener.this.manager.getDsConfig().isEnabledWorld(player.getWorld())) {
                  PacketContainer packet = event.getPacket();
                  Integer chunkX = (Integer)packet.getIntegers().readSafely(0);
                  Integer chunkZ = (Integer)packet.getIntegers().readSafely(1);
                  if (chunkX != null && chunkZ != null) {
                     AntiXrayPacketListener.this.manager.hideAllInChunk(player, chunkX, chunkZ);
                  }
               }
            }
         }
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, ListenerPriority.NORMAL, new PacketType[]{Server.NAMED_SOUND_EFFECT}) {
         public void onPacketSending(PacketEvent event) {
            if (AntiXrayPacketListener.this.manager.isEnabled()) {
               if (AntiXrayPacketListener.this.manager.getDsConfig().isEnabledWorld(event.getPlayer().getWorld())) {
                  PacketContainer packet = event.getPacket();
                  Integer rawX = (Integer)packet.getIntegers().readSafely(0);
                  Integer rawY = (Integer)packet.getIntegers().readSafely(1);
                  Integer rawZ = (Integer)packet.getIntegers().readSafely(2);
                  if (rawX != null && rawY != null && rawZ != null) {
                     double x = (double)rawX / (double)8.0F;
                     double y = (double)rawY / (double)8.0F;
                     double z = (double)rawZ / (double)8.0F;
                     Location soundLoc = new Location(event.getPlayer().getWorld(), x, y, z);
                     if (!AntiXrayPacketListener.this.manager.hasLoSAsync(event.getPlayer(), event.getPlayer().getEyeLocation(), soundLoc, false)) {
                        event.setCancelled(true);
                     }

                  }
               }
            }
         }
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, ListenerPriority.NORMAL, new PacketType[]{Server.WORLD_EVENT}) {
         public void onPacketSending(PacketEvent event) {
            if (AntiXrayPacketListener.this.manager.isEnabled()) {
               if (AntiXrayPacketListener.this.manager.getDsConfig().isEnabledWorld(event.getPlayer().getWorld())) {
                  PacketContainer packet = event.getPacket();
                  Integer effectId = (Integer)packet.getIntegers().readSafely(0);
                  if (effectId != null) {
                     if (effectId == 1021 || effectId == 1022) {
                        BlockPosition pos = (BlockPosition)packet.getBlockPositionModifier().readSafely(0);
                        if (pos == null) {
                           return;
                        }

                        Location loc = new Location(event.getPlayer().getWorld(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                        if (!AntiXrayPacketListener.this.manager.hasLoSAsync(event.getPlayer(), event.getPlayer().getEyeLocation(), loc, false)) {
                           event.setCancelled(true);
                        }
                     }

                  }
               }
            }
         }
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, ListenerPriority.NORMAL, new PacketType[]{Server.WORLD_PARTICLES}) {
         public void onPacketSending(PacketEvent event) {
            if (AntiXrayPacketListener.this.manager.isEnabled()) {
               if (AntiXrayPacketListener.this.manager.getDsConfig().isEnabledWorld(event.getPlayer().getWorld())) {
                  PacketContainer packet = event.getPacket();
                  Double x = (Double)packet.getDoubles().readSafely(0);
                  Double y = (Double)packet.getDoubles().readSafely(1);
                  Double z = (Double)packet.getDoubles().readSafely(2);
                  if (x != null && y != null && z != null) {
                     Location particleLoc = new Location(event.getPlayer().getWorld(), x, y, z);
                     if (!(event.getPlayer().getEyeLocation().distanceSquared(particleLoc) < (double)1.0F)) {
                        if (!AntiXrayPacketListener.this.manager.hasLoSAsync(event.getPlayer(), event.getPlayer().getEyeLocation(), particleLoc, false)) {
                           event.setCancelled(true);
                        }

                     }
                  }
               }
            }
         }
      });
   }
}
