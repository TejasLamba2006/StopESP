package com.github.tejaslamba2006.stopesp;

import com.github.tejaslamba2006.stopesp.command.AntiXrayCommand;
import com.github.tejaslamba2006.stopesp.module.AntiEspModule;
import com.github.tejaslamba2006.stopesp.module.AntiXrayModule;
import com.github.tejaslamba2006.stopesp.util.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class StopESP extends JavaPlugin {
   private AntiXrayModule antiXrayModule;
   private AntiEspModule antiEspModule;

   public void onEnable() {
      this.saveDefaultConfig();
      if (Scheduler.isFolia()) {
         this.getLogger().info("Folia detected! Enabling regional multithreading support.");
      } else {
         this.getLogger().info("Bukkit/Paper detected! Using standard scheduler.");
      }

      this.antiXrayModule = new AntiXrayModule(this);
      this.antiXrayModule.onEnable();
      this.antiEspModule = new AntiEspModule(this);
      this.antiEspModule.onEnable();
      if (this.getCommand("stopesp") != null) {
         this.getCommand("stopesp").setExecutor(new AntiXrayCommand(this));
      }

      this.getLogger().info("StopESP has been enabled!");
   }

   public void onDisable() {
      if (this.antiXrayModule != null) {
         this.antiXrayModule.onDisable();
      }

      if (this.antiEspModule != null) {
         this.antiEspModule.onDisable();
      }

      this.getLogger().info("StopESP has been disabled!");
   }

   public AntiXrayModule getAntiXrayModule() {
      return this.antiXrayModule;
   }

   public AntiEspModule getAntiEspModule() {
      return this.antiEspModule;
   }
}
