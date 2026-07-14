package com.freshwater.haqiworld.plugin.interaction;

import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Custom haqi upgrades are matched by PDC in the crafting grid.
 * Paper 1.21 rejects arbitrary {@code RecipeChoice} implementations, so we do not
 * register Bukkit {@code ShapedRecipe}s for these items.
 */
public final class HaqiCraftListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepare(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        ItemStack result = HaqiItems.matchUpgradeResult(matrix);
        if (result != null) {
            inv.setResult(result);
        }
    }
}
