package com.github.tejaslamba2006.stopesp.util;

import java.util.EnumSet;
import org.bukkit.Material;

public class TransparentBlocks {
   private static final EnumSet<Material> EXPLICIT_TRANSPARENT = EnumSet.noneOf(Material.class);

   private static void safeAdd(String name) {
      try {
         EXPLICIT_TRANSPARENT.add(Material.valueOf(name));
      } catch (IllegalArgumentException var2) {
      }

   }

   public static boolean isTransparent(Material material) {
      if (material != null && !material.isAir()) {
         if (EXPLICIT_TRANSPARENT.contains(material)) {
            return true;
         } else {
            String name = material.name();
            if (name.endsWith("_GLASS")) {
               return true;
            } else if (name.endsWith("_GLASS_PANE")) {
               return true;
            } else if (name.endsWith("_DOOR")) {
               return true;
            } else if (name.endsWith("_FENCE")) {
               return true;
            } else if (name.endsWith("_FENCE_GATE")) {
               return true;
            } else if (name.endsWith("_GATE")) {
               return true;
            } else if (name.endsWith("_TRAPDOOR")) {
               return true;
            } else if (name.endsWith("_LEAVES")) {
               return true;
            } else if (name.endsWith("_SIGN")) {
               return true;
            } else if (name.endsWith("_WALL_SIGN")) {
               return true;
            } else if (name.endsWith("_HANGING_SIGN")) {
               return true;
            } else if (name.endsWith("_BANNER")) {
               return true;
            } else if (name.endsWith("_WALL_BANNER")) {
               return true;
            } else if (name.endsWith("_CARPET")) {
               return true;
            } else if (name.endsWith("_SLAB")) {
               return true;
            } else if (name.endsWith("_STAIRS")) {
               return true;
            } else if (name.endsWith("_PRESSURE_PLATE")) {
               return true;
            } else if (name.endsWith("_BUTTON")) {
               return true;
            } else if (name.endsWith("_CANDLE")) {
               return true;
            } else if (name.endsWith("_CANDLE_CAKE")) {
               return true;
            } else if (name.endsWith("_GRATE")) {
               return true;
            } else if (name.endsWith("_WALL_HEAD")) {
               return true;
            } else if (name.endsWith("_WALL_SKULL")) {
               return true;
            } else if (name.endsWith("_HEAD")) {
               return true;
            } else if (name.endsWith("_SKULL")) {
               return true;
            } else if (name.endsWith("_SAPLING")) {
               return true;
            } else if (name.endsWith("_ROOTS")) {
               return true;
            } else if (name.endsWith("_FUNGUS")) {
               return true;
            } else if (name.endsWith("_VINES")) {
               return true;
            } else if (name.endsWith("_VINES_PLANT")) {
               return true;
            } else if (name.endsWith("_FROGLIGHT")) {
               return true;
            } else if (name.startsWith("POTTED_")) {
               return true;
            } else if (name.startsWith("WAXED_")) {
               return isTransparent(unwaxed(name));
            } else if (name.contains("_FLOWER")) {
               return true;
            } else if (name.contains("CORAL")) {
               return true;
            } else if (name.contains("CAMPFIRE")) {
               return true;
            } else if (name.contains("CAULDRON")) {
               return true;
            } else {
               return name.contains("ANVIL");
            }
         }
      } else {
         return true;
      }
   }

   private static Material unwaxed(String name) {
      String stripped = name.substring("WAXED_".length());

      try {
         return Material.valueOf(stripped);
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }

   static {
      safeAdd("GLASS");
      safeAdd("GLASS_PANE");
      safeAdd("TINTED_GLASS");
      safeAdd("IRON_BARS");
      safeAdd("CHAIN");
      safeAdd("TORCH");
      safeAdd("WALL_TORCH");
      safeAdd("SOUL_TORCH");
      safeAdd("SOUL_WALL_TORCH");
      safeAdd("REDSTONE_TORCH");
      safeAdd("REDSTONE_WALL_TORCH");
      safeAdd("LANTERN");
      safeAdd("SOUL_LANTERN");
      safeAdd("SHORT_GRASS");
      safeAdd("TALL_GRASS");
      safeAdd("FERN");
      safeAdd("LARGE_FERN");
      safeAdd("DEAD_BUSH");
      safeAdd("SEAGRASS");
      safeAdd("TALL_SEAGRASS");
      safeAdd("KELP");
      safeAdd("KELP_PLANT");
      safeAdd("BAMBOO");
      safeAdd("BAMBOO_SAPLING");
      safeAdd("SUGAR_CANE");
      safeAdd("CACTUS");
      safeAdd("SWEET_BERRY_BUSH");
      safeAdd("CAVE_VINES");
      safeAdd("CAVE_VINES_PLANT");
      safeAdd("SPORE_BLOSSOM");
      safeAdd("HANGING_ROOTS");
      safeAdd("SMALL_DRIPLEAF");
      safeAdd("BIG_DRIPLEAF");
      safeAdd("BIG_DRIPLEAF_STEM");
      safeAdd("GLOW_LICHEN");
      safeAdd("MOSS_CARPET");
      safeAdd("PINK_PETALS");
      safeAdd("PITCHER_PLANT");
      safeAdd("PITCHER_CROP");
      safeAdd("TORCHFLOWER");
      safeAdd("TORCHFLOWER_CROP");
      safeAdd("OAK_SAPLING");
      safeAdd("SPRUCE_SAPLING");
      safeAdd("BIRCH_SAPLING");
      safeAdd("JUNGLE_SAPLING");
      safeAdd("ACACIA_SAPLING");
      safeAdd("DARK_OAK_SAPLING");
      safeAdd("CHERRY_SAPLING");
      safeAdd("MANGROVE_PROPAGULE");
      safeAdd("AZALEA");
      safeAdd("FLOWERING_AZALEA");
      safeAdd("PALE_OAK_SAPLING");
      safeAdd("DANDELION");
      safeAdd("POPPY");
      safeAdd("BLUE_ORCHID");
      safeAdd("ALLIUM");
      safeAdd("AZURE_BLUET");
      safeAdd("RED_TULIP");
      safeAdd("ORANGE_TULIP");
      safeAdd("WHITE_TULIP");
      safeAdd("PINK_TULIP");
      safeAdd("OXEYE_DAISY");
      safeAdd("CORNFLOWER");
      safeAdd("LILY_OF_THE_VALLEY");
      safeAdd("WITHER_ROSE");
      safeAdd("SUNFLOWER");
      safeAdd("LILAC");
      safeAdd("ROSE_BUSH");
      safeAdd("PEONY");
      safeAdd("LILY_PAD");
      safeAdd("OPEN_EYEBLOSSOM");
      safeAdd("CLOSED_EYEBLOSSOM");
      safeAdd("WILDFLOWERS");
      safeAdd("CACTUS_FLOWER");
      safeAdd("RED_MUSHROOM");
      safeAdd("BROWN_MUSHROOM");
      safeAdd("CRIMSON_FUNGUS");
      safeAdd("WARPED_FUNGUS");
      safeAdd("CRIMSON_ROOTS");
      safeAdd("WARPED_ROOTS");
      safeAdd("NETHER_SPROUTS");
      safeAdd("WEEPING_VINES");
      safeAdd("WEEPING_VINES_PLANT");
      safeAdd("TWISTING_VINES");
      safeAdd("TWISTING_VINES_PLANT");
      safeAdd("CHORUS_PLANT");
      safeAdd("CHORUS_FLOWER");
      safeAdd("SHORT_DRY_GRASS");
      safeAdd("TALL_DRY_GRASS");
      safeAdd("BUSH");
      safeAdd("FIREFLY_BUSH");
      safeAdd("LEAF_LITTER");
      safeAdd("COBWEB");
      safeAdd("LADDER");
      safeAdd("VINE");
      safeAdd("SCAFFOLDING");
      safeAdd("SNOW");
      safeAdd("POWDER_SNOW");
      safeAdd("WATER");
      safeAdd("LAVA");
      safeAdd("LEVER");
      safeAdd("TRIPWIRE");
      safeAdd("TRIPWIRE_HOOK");
      safeAdd("STRING");
      safeAdd("LIGHTNING_ROD");
      safeAdd("END_ROD");
      safeAdd("CANDLE");
      safeAdd("FLOWER_POT");
      safeAdd("BREWING_STAND");
      safeAdd("BELL");
      safeAdd("POINTED_DRIPSTONE");
      safeAdd("AMETHYST_CLUSTER");
      safeAdd("SMALL_AMETHYST_BUD");
      safeAdd("MEDIUM_AMETHYST_BUD");
      safeAdd("LARGE_AMETHYST_BUD");
      safeAdd("MANGROVE_ROOTS");
      safeAdd("SCULK_VEIN");
      safeAdd("SCULK_SENSOR");
      safeAdd("CALIBRATED_SCULK_SENSOR");
      safeAdd("CONDUIT");
      safeAdd("NETHER_PORTAL");
      safeAdd("END_PORTAL");
      safeAdd("END_GATEWAY");
      safeAdd("BARRIER");
      safeAdd("COPPER_GRATE");
      safeAdd("EXPOSED_COPPER_GRATE");
      safeAdd("WEATHERED_COPPER_GRATE");
      safeAdd("OXIDIZED_COPPER_GRATE");
      safeAdd("WAXED_COPPER_GRATE");
      safeAdd("WAXED_EXPOSED_COPPER_GRATE");
      safeAdd("WAXED_WEATHERED_COPPER_GRATE");
      safeAdd("WAXED_OXIDIZED_COPPER_GRATE");
      safeAdd("DECORATED_POT");
      safeAdd("TRIAL_SPAWNER");
      safeAdd("VAULT");
      safeAdd("PALE_HANGING_MOSS");
      safeAdd("RESIN_CLUMP");
      safeAdd("ICE");
      safeAdd("FROSTED_ICE");
      safeAdd("HONEY_BLOCK");
      safeAdd("SLIME_BLOCK");
      safeAdd("TURTLE_EGG");
      safeAdd("SNIFFER_EGG");
      safeAdd("SEA_PICKLE");
      safeAdd("CAMPFIRE");
      safeAdd("SOUL_CAMPFIRE");
      safeAdd("DAYLIGHT_DETECTOR");
      safeAdd("ENCHANTING_TABLE");
      safeAdd("STONECUTTER");
      safeAdd("GRINDSTONE");
      safeAdd("LECTERN");
      safeAdd("COMPOSTER");
      safeAdd("HOPPER");
      safeAdd("CAULDRON");
      safeAdd("WATER_CAULDRON");
      safeAdd("LAVA_CAULDRON");
      safeAdd("POWDER_SNOW_CAULDRON");
      safeAdd("ANVIL");
      safeAdd("CHIPPED_ANVIL");
      safeAdd("DAMAGED_ANVIL");
      safeAdd("CHEST");
      safeAdd("TRAPPED_CHEST");
      safeAdd("ENDER_CHEST");
      safeAdd("DRAGON_EGG");
      safeAdd("OCHRE_FROGLIGHT");
      safeAdd("VERDANT_FROGLIGHT");
      safeAdd("PEARLESCENT_FROGLIGHT");
      safeAdd("WHEAT");
      safeAdd("CARROTS");
      safeAdd("POTATOES");
      safeAdd("BEETROOTS");
      safeAdd("MELON_STEM");
      safeAdd("PUMPKIN_STEM");
      safeAdd("ATTACHED_MELON_STEM");
      safeAdd("ATTACHED_PUMPKIN_STEM");
      safeAdd("NETHER_WART");
      safeAdd("RAIL");
      safeAdd("POWERED_RAIL");
      safeAdd("DETECTOR_RAIL");
      safeAdd("ACTIVATOR_RAIL");
      safeAdd("REDSTONE_WIRE");
      safeAdd("REPEATER");
      safeAdd("COMPARATOR");
      safeAdd("SKELETON_SKULL");
      safeAdd("SKELETON_WALL_SKULL");
      safeAdd("WITHER_SKELETON_SKULL");
      safeAdd("WITHER_SKELETON_WALL_SKULL");
      safeAdd("ZOMBIE_HEAD");
      safeAdd("ZOMBIE_WALL_HEAD");
      safeAdd("PLAYER_HEAD");
      safeAdd("PLAYER_WALL_HEAD");
      safeAdd("CREEPER_HEAD");
      safeAdd("CREEPER_WALL_HEAD");
      safeAdd("DRAGON_HEAD");
      safeAdd("DRAGON_WALL_HEAD");
      safeAdd("PIGLIN_HEAD");
      safeAdd("PIGLIN_WALL_HEAD");
   }
}
