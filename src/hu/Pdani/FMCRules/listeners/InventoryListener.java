package hu.Pdani.FMCRules.listeners;

import hu.Pdani.FMCRules.FMCRulesPlugin;
import hu.Pdani.FMCRules.util.FRHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @since 1.0
 */
public class InventoryListener implements Listener {
    private FMCRulesPlugin plugin;
    public InventoryListener(FMCRulesPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked(); // The player that clicked the item
        ItemStack clicked = event.getCurrentItem(); // The item that was clicked
        int slot = event.getRawSlot();
        Inventory inventory = event.getInventory(); // The inventory that was clicked in
        if(inventory.getHolder() instanceof FRHolder){
            if(slot > inventory.getSize()-1){
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if(clicked == null || clicked.getType() == Material.AIR)
                return;
            ItemMeta meta = clicked.getItemMeta();
            if(meta == null)
                return;
            if(meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                plugin.getQuizManager().nextQuiz(player, true);
            } else {
                if(!meta.hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS))
                    return;
                if(plugin.getConfig().getBoolean("quiz.failonwrong",false)){
                    plugin.getQuizManager().endQuiz(player,false);
                } else {
                    plugin.getQuizManager().nextQuiz(player,false);
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        Inventory closed = event.getInventory();
        Player player = (Player) event.getPlayer();
        if(!(closed.getHolder() instanceof FRHolder)){
            return;
        }
        if(plugin.getQuizManager().hasStopClose(player))
            return;
        plugin.getQuizManager().endQuiz(player,false);
    }
}
