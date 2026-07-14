package com.freshwater.haqiworld.plugin.haqi;

import com.freshwater.haqiworld.plugin.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class HaqiItems {
    public static NamespacedKey KEY_HAQI_ITEM;
    public static NamespacedKey KEY_WARDEN_ECHO;

    private HaqiItems() {
    }

    public static void init(JavaPlugin plugin) {
        KEY_HAQI_ITEM = new NamespacedKey(plugin, "haqi_item");
        KEY_WARDEN_ECHO = new NamespacedKey(plugin, "warden_echo");
    }

    public static ItemStack createHaqi(HaqiTier tier) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_HAQI_ITEM, PersistentDataType.STRING, tier.id());
        meta.setCustomModelData(tier.customModelData());
        meta.displayName(Component.translatable("item.fhw.haqi_" + tier.id())
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("蹲下右键永久解锁此阶哈气", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createWardenEcho() {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_WARDEN_ECHO, PersistentDataType.BYTE, (byte) 1);
        meta.setCustomModelData(HaqiTier.WARDEN_ECHO_CMD);
        meta.displayName(Component.translatable("item.fhw.warden_echo")
                .color(NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    public static HaqiTier getHaqiTier(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }
        String id = stack.getItemMeta().getPersistentDataContainer().get(KEY_HAQI_ITEM, PersistentDataType.STRING);
        return id == null ? null : HaqiTier.byId(id);
    }

    public static boolean isWardenEcho(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        Byte v = stack.getItemMeta().getPersistentDataContainer().get(KEY_WARDEN_ECHO, PersistentDataType.BYTE);
        return v != null && v == 1;
    }

    public static HaqiTier heldHaqiTier(org.bukkit.entity.Player player) {
        HaqiTier main = getHaqiTier(player.getInventory().getItemInMainHand());
        if (main != null) {
            return main;
        }
        return getHaqiTier(player.getInventory().getItemInOffHand());
    }

    public static HaqiTier resolveCombatTier(org.bukkit.entity.Player player, boolean requireItem) {
        HaqiTier unlocked = PlayerData.getUnlockedTier(player);
        if (!requireItem) {
            return unlocked;
        }
        HaqiTier held = heldHaqiTier(player);
        if (held == null) {
            return null;
        }
        return held.level() <= unlocked.level() ? held : unlocked;
    }

    public static void registerRecipes(JavaPlugin plugin) {
        registerUpgrade(plugin, "haqi_upgraded", HaqiTier.BASIC, HaqiTier.UPGRADED, Material.IRON_INGOT);
        registerUpgrade(plugin, "haqi_enhanced", HaqiTier.UPGRADED, HaqiTier.ENHANCED, Material.DIAMOND);
        // Warden: center enhanced, surround warden echo — use recipe choice matching PDC via exact item
        ItemStack center = createHaqi(HaqiTier.ENHANCED);
        ItemStack result = createHaqi(HaqiTier.WARDEN);
        ItemStack echo = createWardenEcho();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "haqi_warden"), result);
        recipe.shape("EEE", "EHE", "EEE");
        recipe.setIngredient('H', center);
        recipe.setIngredient('E', echo);
        plugin.getServer().addRecipe(recipe);
    }

    private static void registerUpgrade(JavaPlugin plugin, String key, HaqiTier from, HaqiTier to, Material surround) {
        ItemStack center = createHaqi(from);
        ItemStack result = createHaqi(to);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, key), result);
        recipe.shape("SSS", "SHS", "SSS");
        recipe.setIngredient('H', center);
        recipe.setIngredient('S', surround);
        plugin.getServer().addRecipe(recipe);
    }
}
