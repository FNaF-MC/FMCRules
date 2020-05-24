package hu.Pdani.FMCRules.util;

import hu.Pdani.FMCRules.FMCRulesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * @since 1.0
 */
public class QuizManager {
    private FMCRulesPlugin plugin;
    private HashMap<Player,FRPlayer> quizlist = new HashMap<>();
    private List<Player> stopClose = new ArrayList<>();
    private Integer[] intlist = {19,21,23,25};

    public QuizManager(FMCRulesPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * Start a rule accept quiz for the Player
     * @param player the requested Player
     * @since 1.0
     */
    public void startQuiz(Player player){
        if(plugin.getStatus(player))
            return;
        FRPlayer frp = new FRPlayer(player);
        if(getQuiz(frp))
            quizlist.put(player,frp);
    }

    /**
     * Starts the next quiz, or ends the quiz if the minimum good answer count was reached
     * @param player the requested Player
     * @param success true if the answer was correct
     * @since 1.0
     */
    public void nextQuiz(Player player, boolean success){
        FRPlayer frp = quizlist.get(player);
        frp.addFinished();
        if(success)
            frp.addSuccess();
        if(frp.getSuccess() >= plugin.getConfig().getInt("quiz.minimum",plugin.getIdlist().size())){
            endQuiz(player,true);
            return;
        }
        if(frp.getFinished().size()-1 >= plugin.getConfig().getInt("quiz.minimum",plugin.getIdlist().size())){
            endQuiz(player,false);
            return;
        }
        stopClose.add(player);
        if(getQuiz(frp))
            quizlist.replace(player,frp);
    }

    /**
     * Open a quiz for the Player
     * @param frp the Player
     * @since 1.3.0
     */
    private boolean getQuiz(FRPlayer frp){
        Player player = frp.getPlayer();
        RandomCollection<String> rcidlist = new RandomCollection<>();
        double weight = 100.00 / (double) plugin.getIdlist().size();
        for(String id : plugin.getIdlist()){
            rcidlist.add(weight,id);
        }
        String next = rcidlist.next();
        if(frp.getFinished().contains(next)){
            while (frp.getFinished().contains(next)){
                next = rcidlist.next();
            }
        }
        try {
            frp.newId(next);
        } catch (FRException e) {
            plugin.getLogger().warning(e.toString());
            try {
                next = rcidlist.next();
                if(frp.getFinished().contains(next)){
                    while (frp.getFinished().contains(next)){
                        next = rcidlist.next();
                    }
                }
                frp.newId(next);
            } catch (FRException ex) {
                plugin.getLogger().severe(e.toString());
                player.sendMessage(c(plugin.getConfig().getString("messages.error.config")));
                return false;
            }
        }
        List<String> goodlist = new ArrayList<>();
        List<String> wronglist = new ArrayList<>();
        int size = (plugin.getConfig().getBoolean("quiz.answers.random",true)) ? 54 : 27;
        Inventory myInv = plugin.getServer().createInventory(new FRHolder(), size, c(frp.getTitle()));
        Random rand = new Random();
        int maxgood = plugin.getConfig().getInt("quiz.answers.good");
        if(maxgood > plugin.getConfig().getStringList("questions."+frp.getId()+".good").size())
            maxgood = plugin.getConfig().getStringList("questions."+frp.getId()+".good").size();
        int maxwrong = plugin.getConfig().getInt("quiz.answers.wrong");
        if(maxwrong > plugin.getConfig().getStringList("questions."+frp.getId()+".wrong").size())
            maxwrong = plugin.getConfig().getStringList("questions."+frp.getId()+".wrong").size();
        String item = frp.newItem();
        if(plugin.getConfig().getBoolean("quiz.answers.random",true)) {
            for (int i = 0; i < maxgood; i++) {
                int r = rand.nextInt(53);
                if (myInv.getItem(r) != null && myInv.getItem(r).getType() != Material.AIR) {
                    while (myInv.getItem(r) != null && myInv.getItem(r).getType() != Material.AIR) {
                        r = rand.nextInt(53);
                    }
                }
                Material ai;
                try {
                    ai = Material.valueOf(item);
                } catch (IllegalArgumentException e) {
                    ai = Material.STONE;
                }
                ItemStack stack = new ItemStack(ai, 1);
                ItemMeta meta = stack.getItemMeta();
                String name = frp.newGood();
                if (goodlist.contains(name)) {
                    while (goodlist.contains(name)) {
                        name = frp.newGood();
                    }
                }
                meta.setDisplayName(c(name));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.setItemMeta(meta);
                myInv.setItem(r, stack);
                goodlist.add(name);
            }
            for (int i = 0; i < maxwrong; i++) {
                int r = rand.nextInt(53);
                if (myInv.getItem(r) != null && myInv.getItem(r).getType() != Material.AIR) {
                    while (myInv.getItem(r) != null && myInv.getItem(r).getType() != Material.AIR) {
                        r = rand.nextInt(53);
                    }
                }
                Material ai;
                try {
                    ai = Material.valueOf(item);
                } catch (IllegalArgumentException e) {
                    ai = Material.STONE;
                }
                ItemStack stack = new ItemStack(ai, 1);
                ItemMeta meta = stack.getItemMeta();
                String name = frp.newWrong();
                if (wronglist.contains(name)) {
                    while (wronglist.contains(name)) {
                        name = frp.newWrong();
                    }
                }
                meta.setDisplayName(c(name));
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                stack.setItemMeta(meta);
                myInv.setItem(r, stack);
                wronglist.add(name);
            }
        } else {
            String questItem = frp.newItem();
            while (questItem.equalsIgnoreCase(item)){
                questItem = frp.newItem();
            }
            Material ai;
            try {
                ai = Material.valueOf(item);
            } catch (IllegalArgumentException e) {
                ai = Material.STONE;
            }
            List<Integer> taken = new ArrayList<>();
            for(int i = 0; i < 4; i++) {
                int pos = rand.nextInt(4);
                while (taken.contains(pos)){
                    pos = rand.nextInt(4);
                }
                if(rand.nextInt(2) == 1 && goodlist.size() < 2 || wronglist.size() == 2) {
                    String name = frp.newGood();
                    if (goodlist.contains(name)) {
                        while (goodlist.contains(name)) {
                            name = frp.newGood();
                        }
                    }
                    ItemStack stack = new ItemStack(ai, 1);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(c(name));
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    stack.setItemMeta(meta);
                    myInv.setItem(intlist[pos], stack);
                    goodlist.add(name);
                } else {
                    String name = frp.newWrong();
                    if (wronglist.contains(name)) {
                        while (wronglist.contains(name)) {
                            name = frp.newWrong();
                        }
                    }
                    ItemStack stack = new ItemStack(ai, 1);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(c(name));
                    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    stack.setItemMeta(meta);
                    myInv.setItem(intlist[pos], stack);
                    wronglist.add(name);
                }
                taken.add(pos);
            }
            try {
                ai = Material.valueOf(questItem);
            } catch (IllegalArgumentException e) {
                ai = Material.STONE;
            }
            ItemStack stack = new ItemStack(ai, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(c(frp.getQuestion()));
            stack.setItemMeta(meta);
            myInv.setItem(4, stack);
        }
        player.openInventory(myInv);
        return true;
    }

    /**
     * This is only called if the Player finishes the given quiz
     * @param player the Player
     * @param success true if the player successfully completed the quiz
     * @since 1.0
     */
    public void endQuiz(Player player, boolean success){
        stopClose.add(player);
        player.closeInventory();
        if(success) {
            File cfile = new File(plugin.getDataFolder(), "players/" + player.getUniqueId().toString() + ".yml");
            FileConfiguration fc = YamlConfiguration.loadConfiguration(cfile);
            fc.set("username", player.getName());
            fc.set("accepted", true);
            try {
                fc.save(cfile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Unable to save player file: " + cfile.toString());
            }
            player.sendMessage(c(plugin.getConfig().getString("messages.finished")));
        } else {
            if(plugin.getConfig().getBoolean("quiz.restartonfail",true)){
                player.chat("/rules");
            } else {
                player.sendMessage(c(plugin.getConfig().getString("messages.failed")));
            }
        }
        quizlist.remove(player);
    }

    /**
     *
     * @param player the Player
     * @return true if the player had an InventoryCloseEvent preventor, false otherwise
     * @since 1.0.7
     */
    public boolean hasStopClose(Player player){
        if(stopClose.contains(player)){
            stopClose.remove(player);
            return true;
        }
        return false;
    }

    /**
     *
     * @param player the Player
     * @return true if the player is currently running a quiz
     * @deprecated unused
     */
    public boolean hasQuiz(Player player){
        return quizlist.containsKey(player);
    }

    /**
     * Remove the player from the running quiz list
     * @param player the Player
     * @since 1.0.8
     */
    public void disconnect(Player player){
        quizlist.remove(player);
    }


    public String c(String msg){
        return ChatColor.translateAlternateColorCodes('&',msg);
    }
}
