package com.github.tejaslamba2006.stopesp.module;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.listener.AntiXrayListener;
import com.github.tejaslamba2006.stopesp.listener.AntiXrayPacketListener;
import com.github.tejaslamba2006.stopesp.manager.AntiXrayManager;
import com.github.tejaslamba2006.stopesp.manager.DeepShieldConfig;
import org.bukkit.Bukkit;

// ponytail: reconstructed from usage in StopESP/AntiXrayCommand — owns the one DeepShieldConfig both modules share.
public class AntiXrayModule implements Module {
   private final StopESP plugin;
   private DeepShieldConfig dsConfig;
   private AntiXrayManager manager;
   private boolean enabled;

   public AntiXrayModule(StopESP plugin) {
      this.plugin = plugin;
   }

   public String getName() {
      return "AntiXray";
   }

   public void onEnable() {
      this.dsConfig = new DeepShieldConfig(this.plugin);
      this.manager = new AntiXrayManager(this.plugin, this.dsConfig);
      Bukkit.getPluginManager().registerEvents(new AntiXrayListener(this.plugin, this.manager), this.plugin);
      new AntiXrayPacketListener(this.plugin, this.manager).register();
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

   public AntiXrayManager getManager() {
      return this.manager;
   }

   public DeepShieldConfig getDsConfig() {
      return this.dsConfig;
   }
}
