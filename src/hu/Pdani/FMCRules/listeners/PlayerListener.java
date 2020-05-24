package hu.Pdani.FMCRules.listeners;

import hu.Pdani.FMCRules.FMCRulesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @since 1.0
 */
public class PlayerListener implements Listener {
    private FMCRulesPlugin plugin;
    public PlayerListener(FMCRulesPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        if(plugin.getConfig().getBoolean("disallowchat",true) && !plugin.getStatus(event.getPlayer())){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("messages.error.disallowed")));
        }
    }

    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent event){
        String msg = event.getMessage().substring(1);
        String cmd = msg;
        String[] args = new String[0];
        StringBuilder sb = new StringBuilder();
        if(msg.contains(" ")){
            args = msg.split(" ");
            cmd = args[0];
            args = msg.replace(args[0]+" ","").split(" ");
            for (String arg : args) {
                sb.append(" ").append(arg);
            }
        }
        if(cmd.equalsIgnoreCase(plugin.getConfig().getString("command.rules"))){
            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (event.getPlayer().hasPermission("fmcrules.admin")) {
                        plugin.reloadConfig();
                        plugin.reloadIdlist();
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.reload")));
                        return;
                    }
                }
            }
            plugin.sendRules(event.getPlayer());
            event.setCancelled(true);
        } else if(cmd.equalsIgnoreCase(plugin.getConfig().getString("command.acceptrules"))){
            plugin.startAccept(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        plugin.getQuizManager().disconnect(event.getPlayer());
    }
}
