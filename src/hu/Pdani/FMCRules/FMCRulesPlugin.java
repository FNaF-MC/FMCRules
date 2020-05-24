package hu.Pdani.FMCRules;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import hu.Pdani.FMCRules.listeners.InventoryListener;
import hu.Pdani.FMCRules.listeners.PlayerListener;
import hu.Pdani.FMCRules.util.QuizManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Rule manager plugin written for FNaF MC
 * @version 1.0
 * @author Pdani001
 */
public class FMCRulesPlugin extends JavaPlugin {
    private static JavaPlugin plugin;
    private List<String> idlist = new ArrayList<>();
    private QuizManager quizManager;

    @Override
    public void onEnable() {
        File cfile = new File(getDataFolder(),"config.yml");
        if(!cfile.exists()) {
            saveDefaultConfig();
        } else {
            if(getConfig().getInt("version",0) != getConfig().getDefaults().getInt("version")){
                getConfig().options().copyDefaults(true);
                saveConfig();
            }
        }
        StringBuilder authors = new StringBuilder();
        Iterator<String> listauthors = getDescription().getAuthors().iterator();
        while(listauthors.hasNext()){
            if(authors.length() == 0)
                authors = new StringBuilder(listauthors.next());
            else
                authors.append(", ").append(listauthors.next());
        }
        plugin = this;
        if(hasSkript() && getConfig().getBoolean("useskript",true)){
            getLogger().log(Level.INFO,"Skript found, registering classes...");
            SkriptAddon addon = Skript.registerAddon(this);
            try {
                addon.loadClasses("hu.Pdani.FMCRules", "skript");
                getLogger().log(Level.INFO,"Classes registered successfully!");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"Class registration failed!");
                getLogger().log(Level.SEVERE,e.toString());
            }
        }
        idlist.addAll(getConfig().getConfigurationSection("questions").getKeys(false));
        quizManager = new QuizManager(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this),this);
        getLogger().log(Level.INFO,"The plugin is now enabled.");
        getLogger().log(Level.INFO,"Created by: "+authors.toString());
        getLogger().log(Level.INFO,"Version "+getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO,"The plugin is now disabled.");
    }

    private boolean hasSkript() {
        Plugin pl = getServer().getPluginManager().getPlugin("Skript");
        if (pl == null) {
            return false;
        }
        return (pl instanceof Skript);
    }

    /**
     *
     * @return the QuizManager
     */
    public QuizManager getQuizManager() {
        return quizManager;
    }

    /**
     * Send rules to a Player
     * @param player the receiver Player
     */
    public void sendRules(Player player){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',getConfig().getString("rules")));
        if(!getStatus(player))
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.accept")));
        if(player.hasPermission("fmcrules.admin"))
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.admin")));
    }

    /**
     * Begins the rules accept protocol
     * @param player the requested Player
     */
    public void startAccept(Player player){
        if(getStatus(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.error.accepted")));
            return;
        }
        if(getConfig().getBoolean("quiz.enabled",true)) {
            quizManager.startQuiz(player);
        } else {
            File cfile = new File(plugin.getDataFolder(),"players/"+player.getUniqueId().toString()+".yml");
            FileConfiguration fc = YamlConfiguration.loadConfiguration(cfile);
            fc.set("username",player.getName());
            fc.set("accepted",true);
            try {
                fc.save(cfile);
            } catch (IOException e) {
                getLogger().log(Level.WARNING,"Unable to save player file: "+cfile.toString());
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("messages.done")));
        }
    }

    /**
     * Get the status of the Player
     * @param player the Player
     * @return true if the user file exists and the user accepted the rules, false otherwise
     */
    public boolean getStatus(Player player){
        return getUserStatus(player);
    }

    /**
     * Get the status of the Player
     * @param player the Player
     * @return true if the user file exists and the user accepted the rules, false otherwise
     */
    public static boolean getUserStatus(Player player){
        if(player.hasPermission("fmcrules.admin"))
            return true;
        File cfile = new File(plugin.getDataFolder(),"players/"+player.getUniqueId().toString()+".yml");
        if(!cfile.exists())
            return false;
        FileConfiguration fc = YamlConfiguration.loadConfiguration(cfile);
        return fc.getBoolean("accepted",false);
    }

    public JavaPlugin getJavaPlugin(){
        return getPlugin();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get the available question ids
     * @return List of available question ids
     */
    public List<String> getIdlist() {
        return idlist;
    }

    /**
     * Reload the list of question ids
     */
    public void reloadIdlist(){
        idlist.clear();
        idlist.addAll(getConfig().getConfigurationSection("questions").getKeys(false));
    }
}
