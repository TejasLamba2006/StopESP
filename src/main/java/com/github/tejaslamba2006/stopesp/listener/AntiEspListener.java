package com.github.tejaslamba2006.stopesp.listener;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.manager.AntiEspManager;
import com.github.tejaslamba2006.stopesp.util.Scheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class AntiEspListener implements Listener {
   private final StopESP plugin;
   private final AntiEspManager manager;

   public AntiEspListener(StopESP plugin, AntiEspManager manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      Scheduler.runEntity(this.plugin, event.getPlayer(), () -> this.manager.cleanupPlayer(event.getPlayer()));
   }
}
