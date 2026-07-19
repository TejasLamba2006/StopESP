package com.github.tejaslamba2006.stopesp.manager;

import com.github.tejaslamba2006.stopesp.StopESP;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class DeepShieldConfig {
   private final StopESP plugin;
   private File configFile;
   private FileConfiguration config;
   private double revealRadius;
   private boolean showThanksMessage;
   private boolean frustumCulling;
   private int visibilityBuffer;
   private long scanInterval;
   private int workerThreads;
   private String fakeBlockMode;
   private final Map<String, Double> worldRadiusOverrides = new HashMap();
   private final EnumSet<Material> protectedBlocks = EnumSet.noneOf(Material.class);
   private final Set<String> enabledWorlds = new HashSet();
   private boolean antiEspEnabled;
   private double antiEspRevealRadius;
   private boolean antiEspObfuscatePlayers;
   private boolean antiEspObfuscateMobs;
   private boolean antiEspHideNameplates;
   private boolean antiEspHideInvisibleEntities;
   private final EnumSet<EntityType> antiEspExtraEntityHide = EnumSet.noneOf(EntityType.class);

   public DeepShieldConfig(StopESP plugin) {
      this.plugin = plugin;
      this.configFile = new File(plugin.getDataFolder(), "config.yml");
      this.saveDefaultConfig();
      this.reload();
   }

   public void saveDefaultConfig() {
      if (!this.configFile.exists()) {
         this.plugin.saveResource("config.yml", false);
      }

   }

   public void reload() {
      this.config = YamlConfiguration.loadConfiguration(this.configFile);
      this.revealRadius = this.config.getDouble("reveal-radius", (double)20.0F);
      this.showThanksMessage = this.config.getBoolean("show-thanks-message", true);
      this.frustumCulling = this.config.getBoolean("frustum-culling", true);
      this.visibilityBuffer = this.config.getInt("visibility-buffer", 3);
      this.scanInterval = (long)Math.max(1, this.config.getInt("scan-interval", 1));
      this.workerThreads = this.config.getInt("worker-threads", -1);
      this.fakeBlockMode = this.config.getString("fake-block-mode", "AUTO");
      this.enabledWorlds.clear();
      this.enabledWorlds.addAll(this.config.getStringList("enabled-worlds"));
      this.worldRadiusOverrides.clear();
      if (this.config.isConfigurationSection("world-radius-overrides")) {
         for(String worldName : this.config.getConfigurationSection("world-radius-overrides").getKeys(false)) {
            this.worldRadiusOverrides.put(worldName, this.config.getDouble("world-radius-overrides." + worldName, this.revealRadius));
         }
      }

      this.loadMaterialSet(this.config.getStringList("protected-blocks"), this.protectedBlocks);
      this.antiEspEnabled = this.config.getBoolean("anti-esp.enabled", true);
      this.antiEspRevealRadius = this.config.getDouble("anti-esp.reveal-radius", (double)32.0F);
      this.antiEspObfuscatePlayers = this.config.getBoolean("anti-esp.obfuscate-players", true);
      this.antiEspObfuscateMobs = this.config.getBoolean("anti-esp.obfuscate-mobs", true);
      this.antiEspHideNameplates = this.config.getBoolean("anti-esp.hide-nameplates", true);
      this.antiEspHideInvisibleEntities = this.config.getBoolean("anti-esp.hide-invisible-entities", false);
      this.loadEntityTypeSet(this.config.getStringList("anti-esp.extra-entity-hide"), this.antiEspExtraEntityHide);
      this.plugin.getLogger().info("Config reloaded! Active in: " + String.join(", ", this.enabledWorlds));
   }

   public boolean isEnabledWorld(World world) {
      return this.enabledWorlds.contains(world.getName());
   }

   private void loadMaterialSet(List<String> source, EnumSet<Material> target) {
      target.clear();

      for(String name : source) {
         try {
            target.add(Material.valueOf(name.toUpperCase()));
         } catch (IllegalArgumentException var6) {
            this.plugin.getLogger().warning("Invalid Material in config: " + name);
         }
      }

   }

   private void loadEntityTypeSet(List<String> source, EnumSet<EntityType> target) {
      target.clear();

      for(String name : source) {
         try {
            target.add(EntityType.valueOf(name.toUpperCase()));
         } catch (IllegalArgumentException var6) {
            this.plugin.getLogger().warning("Invalid EntityType in config: " + name);
         }
      }

   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public double getRevealRadius(World world) {
      return world != null && this.worldRadiusOverrides.containsKey(world.getName()) ? (Double)this.worldRadiusOverrides.get(world.getName()) : this.revealRadius;
   }

   public double getRevealRadius() {
      return this.revealRadius;
   }

   public boolean isShowThanksMessage() {
      return this.showThanksMessage;
   }

   public boolean isFrustumCulling() {
      return this.frustumCulling;
   }

   public int getVisibilityBuffer() {
      return this.visibilityBuffer;
   }

   public long getScanInterval() {
      return this.scanInterval;
   }

   public int getWorkerThreads() {
      return this.workerThreads;
   }

   public String getFakeBlockMode() {
      return this.fakeBlockMode;
   }

   public EnumSet<Material> getProtectedBlocks() {
      return this.protectedBlocks;
   }

   public boolean isAntiEspEnabled() {
      return this.antiEspEnabled;
   }

   public double getAntiEspRevealRadius() {
      return this.antiEspRevealRadius;
   }

   public boolean isAntiEspObfuscatePlayers() {
      return this.antiEspObfuscatePlayers;
   }

   public boolean isAntiEspObfuscateMobs() {
      return this.antiEspObfuscateMobs;
   }

   public boolean isAntiEspHideNameplates() {
      return this.antiEspHideNameplates;
   }

   public boolean isAntiEspHideInvisibleEntities() {
      return this.antiEspHideInvisibleEntities;
   }

   public EnumSet<EntityType> getAntiEspExtraEntityHide() {
      return this.antiEspExtraEntityHide;
   }
}
