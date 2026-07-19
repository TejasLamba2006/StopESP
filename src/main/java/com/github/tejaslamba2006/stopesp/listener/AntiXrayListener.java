package com.github.tejaslamba2006.stopesp.listener;

import com.github.tejaslamba2006.stopesp.StopESP;
import com.github.tejaslamba2006.stopesp.manager.AntiXrayManager;
import com.github.tejaslamba2006.stopesp.util.Scheduler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class AntiXrayListener implements Listener {
   private final StopESP plugin;
   private final AntiXrayManager manager;

   public AntiXrayListener(StopESP plugin, AntiXrayManager manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (player.isOp() && this.manager.getDsConfig().isShowThanksMessage()) {
         player.sendMessage("");
         String var10001 = String.valueOf(ChatColor.AQUA);
         player.sendMessage(var10001 + String.valueOf(ChatColor.BOLD) + "StopESP " + String.valueOf(ChatColor.DARK_GRAY)
               + "» " + String.valueOf(ChatColor.GRAY) + "Thanks for your download!");
         player.sendMessage(String.valueOf(ChatColor.GRAY) + "If you find bugs, please report them on Discord.");
         String var10002 = String.valueOf(ChatColor.AQUA);
         TextComponent discordBtn = new TextComponent(var10002 + String.valueOf(ChatColor.BOLD) + "[JOIN DISCORD] ");
         discordBtn.setClickEvent(new ClickEvent(Action.OPEN_URL, "https://discord.gg/msEkYDWpXM"));
         discordBtn.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
               new Content[] { new Text(String.valueOf(ChatColor.YELLOW) + "Click to join our support server") }));
         var10002 = String.valueOf(ChatColor.YELLOW);
         TextComponent reviewBtn = new TextComponent(var10002 + String.valueOf(ChatColor.BOLD) + "[LEAVE REVIEW]");
         reviewBtn.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
               new Content[] { new Text(String.valueOf(ChatColor.YELLOW) + "We appreciate your feedback!") }));
         player.spigot().sendMessage((new ComponentBuilder("")).append(discordBtn).append(reviewBtn).create());
         player.sendMessage("");
      }

   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onQuit(PlayerQuitEvent event) {
      Scheduler.runEntity(this.plugin, event.getPlayer(), () -> this.manager.cleanupPlayer(event.getPlayer()));
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onChunkLoad(ChunkLoadEvent event) {
      this.manager.indexChunk(event.getChunk());
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onChunkUnload(ChunkUnloadEvent event) {
      this.manager.removeChunk(event.getChunk());
      this.manager.cleanupChunk(event.getChunk());
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockPlace(BlockPlaceEvent event) {
      this.manager.addBlock(event.getPlayer(), event.getBlock());
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockBreak(BlockBreakEvent event) {
      this.manager.removeBlock(event.getBlock());
      this.manager.revealInstant(event.getBlock().getLocation());
   }
}
