package hu.Pdani.FMCRules.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * @since 1.0
 */
public class FRHolder implements InventoryHolder {
    private Inventory inventory = null;

    public FRHolder(){
    }

    public FRHolder setInventory(Inventory inventory){
        this.inventory = inventory;
        return this;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
