package com.github.tejaslamba2006.stopesp.module;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.listener.AntiEspListener;
import com.github.tejaslamba2006.stopesp.manager.AntiEspManager;
import com.github.tejaslamba2006.stopesp.manager.DeepShieldConfig;
import org.bukkit.Bukkit;

// ponytail: reconstructed from usage — reuses AntiXray's already-loaded config instead of re-reading the file.
public class AntiEspModule implements Module {
   private final StopESP plugin;
   private AntiEspManager manager;
   private boolean enabled;

   public AntiEspModule(StopESP plugin) {
      this.plugin = plugin;
   }

   public String getName() {
      return "AntiESP";
   }

   public void onEnable() {
      DeepShieldConfig dsConfig = this.plugin.getAntiXrayModule().getDsConfig();
      if (!dsConfig.isAntiEspEnabled()) {
         return;
      }
      this.manager = new AntiEspManager(this.plugin, dsConfig);
      Bukkit.getPluginManager().registerEvents(new AntiEspListener(this.plugin, this.manager), this.plugin);
      this.manager.startScan();
      this.enabled = true;
   }

   public void onDisable() {
      if (this.manager != null) {
         this.manager.stopTask();
      }
      this.enabled = false;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public AntiEspManager getManager() {
      return this.manager;
   }
}
