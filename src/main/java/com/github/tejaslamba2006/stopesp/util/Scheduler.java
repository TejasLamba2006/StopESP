package com.github.tejaslamba2006.stopesp.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Scheduler {
   private static final boolean IS_FOLIA = isClassPresent("io.papermc.paper.threadedregions.RegionConfiguration") || isClassPresent("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");

   private static boolean isClassPresent(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static boolean isFolia() {
      return IS_FOLIA;
   }

   public static Task run(Plugin plugin, Runnable runnable) {
      if (IS_FOLIA) {
         Bukkit.getGlobalRegionScheduler().execute(plugin, runnable);
         return () -> {
         };
      } else {
         BukkitTask task = Bukkit.getScheduler().runTask(plugin, runnable);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runAsync(Plugin plugin, Runnable runnable) {
      if (IS_FOLIA) {
         ScheduledTask task = Bukkit.getAsyncScheduler().runNow(plugin, (t) -> runnable.run());
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runLater(Plugin plugin, Runnable runnable, long ticks) {
      if (IS_FOLIA) {
         ScheduledTask task = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (t) -> runnable.run(), ticks);
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runLaterAsync(Plugin plugin, Runnable runnable, long ticks) {
      if (IS_FOLIA) {
         ScheduledTask task = Bukkit.getAsyncScheduler().runDelayed(plugin, (t) -> runnable.run(), ticks * 50L, TimeUnit.MILLISECONDS);
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runTimer(Plugin plugin, Runnable runnable, long delay, long period) {
      if (IS_FOLIA) {
         ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (t) -> runnable.run(), delay, period);
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runEntity(Plugin plugin, Entity entity, Runnable runnable) {
      if (IS_FOLIA) {
         ScheduledTask task = entity.getScheduler().run(plugin, (t) -> runnable.run(), (Runnable)null);
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTask(plugin, runnable);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public static Task runRegion(Plugin plugin, Location location, Runnable runnable) {
      if (IS_FOLIA) {
         ScheduledTask task = Bukkit.getRegionScheduler().run(plugin, location, (t) -> runnable.run());
         Objects.requireNonNull(task);
         return task::cancel;
      } else {
         BukkitTask task = Bukkit.getScheduler().runTask(plugin, runnable);
         Objects.requireNonNull(task);
         return task::cancel;
      }
   }

   public interface Task {
      void cancel();
   }
}
