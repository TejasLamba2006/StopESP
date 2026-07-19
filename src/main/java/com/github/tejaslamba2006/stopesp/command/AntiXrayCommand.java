package com.github.tejaslamba2006.stopesp.command;

import com.github.tejaslamba2006.stopesp.StopESP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiXrayCommand implements CommandExecutor {
   private final StopESP plugin;

   public AntiXrayCommand(StopESP plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("stopesp.admin")) {
         sender.sendMessage(String.valueOf(ChatColor.RED) + "No permission!");
         return true;
      } else if (args.length == 0) {
         this.sendUsage(sender);
         return true;
      } else {
         switch (args[0].toLowerCase()) {
            case "reload":
               this.plugin.getAntiXrayModule().getManager().getDsConfig().reload();
               sender.sendMessage(String.valueOf(ChatColor.GREEN) + "StopESP configuration reloaded!");
               return true;
            case "on":
               this.plugin.getAntiXrayModule().getManager().startScan();
               String var9 = String.valueOf(ChatColor.GREEN);
               sender.sendMessage(var9 + "StopESP has been " + String.valueOf(ChatColor.BOLD) + "ENABLED" + String.valueOf(ChatColor.RESET) + String.valueOf(ChatColor.GREEN) + "!");
               return true;
            case "off":
               this.plugin.getAntiXrayModule().getManager().stopTask();
               String var8 = String.valueOf(ChatColor.RED);
               sender.sendMessage(var8 + "StopESP has been " + String.valueOf(ChatColor.BOLD) + "DISABLED" + String.valueOf(ChatColor.RESET) + String.valueOf(ChatColor.RED) + " and all blocks revealed!");
               return true;
            case "debug":
               if (args.length < 2) {
                  sender.sendMessage(String.valueOf(ChatColor.RED) + "Usage: /stopesp debug <player>");
                  return true;
               } else {
                  Player target = Bukkit.getPlayer(args[1]);
                  if (target == null) {
                     String var10001 = String.valueOf(ChatColor.RED);
                     sender.sendMessage(var10001 + "Player \"" + args[1] + "\" not found or offline.");
                     return true;
                  }

                  this.sendDebug(sender, target);
                  return true;
               }
            default:
               this.sendUsage(sender);
               return true;
         }
      }
   }

   private void sendDebug(CommandSender sender, Player target) {
      String[] lines = this.plugin.getAntiXrayModule().getManager().getDebugInfo(target);
      String var10001 = String.valueOf(ChatColor.AQUA);
      sender.sendMessage(var10001 + "--- StopESP Debug: " + String.valueOf(ChatColor.WHITE) + target.getName() + String.valueOf(ChatColor.AQUA) + " ---");

      for(String line : lines) {
         var10001 = String.valueOf(ChatColor.GRAY);
         sender.sendMessage(var10001 + "  " + line);
      }

      sender.sendMessage(String.valueOf(ChatColor.AQUA) + "-------------------------------------------");
   }

   private void sendUsage(CommandSender sender) {
      sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "StopESP Commands:");
      String var10001 = String.valueOf(ChatColor.GRAY);
      sender.sendMessage(var10001 + "  /stopesp reload         " + String.valueOf(ChatColor.WHITE) + "- Reload config");
      var10001 = String.valueOf(ChatColor.GRAY);
      sender.sendMessage(var10001 + "  /stopesp on             " + String.valueOf(ChatColor.WHITE) + "- Enable the plugin");
      var10001 = String.valueOf(ChatColor.GRAY);
      sender.sendMessage(var10001 + "  /stopesp off            " + String.valueOf(ChatColor.WHITE) + "- Disable and reveal all");
      var10001 = String.valueOf(ChatColor.GRAY);
      sender.sendMessage(var10001 + "  /stopesp debug <player> " + String.valueOf(ChatColor.WHITE) + "- Show debug info for a player");
   }
}
