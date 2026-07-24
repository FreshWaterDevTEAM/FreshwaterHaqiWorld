package com.freshwater.haqiworld.plugin.haqi;

import com.freshwater.haqiworld.plugin.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

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
        ItemMeta meta = requireMeta(stack);
        meta.getPersistentDataContainer().set(KEY_HAQI_ITEM, PersistentDataType.STRING, tier.id());
        applyCustomModelData(meta, tier.customModelData());
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
        ItemMeta meta = requireMeta(stack);
        meta.getPersistentDataContainer().set(KEY_WARDEN_ECHO, PersistentDataType.BYTE, (byte) 1);
        applyCustomModelData(meta, HaqiTier.WARDEN_ECHO_CMD);
        meta.displayName(Component.translatable("item.fhw.warden_echo")
                .color(NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * 1.21.4+ reads {@code custom_model_data.floats[index]} for item models.
     * Setting only the legacy integer is unreliable across Paper → Forge clients.
     */
    private static void applyCustomModelData(ItemMeta meta, int value) {
        meta.setCustomModelData(value);
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setFloats(List.of((float) value));
        meta.setCustomModelDataComponent(cmd);
    }

    public static HaqiTier getHaqiTier(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String id = meta.getPersistentDataContainer().get(KEY_HAQI_ITEM, PersistentDataType.STRING);
        return id == null ? null : HaqiTier.byId(id);
    }

    public static boolean isWardenEcho(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte v = meta.getPersistentDataContainer().get(KEY_WARDEN_ECHO, PersistentDataType.BYTE);
        return v != null && v == 1;
    }

    public static boolean isHaqiRelated(ItemStack stack) {
        return getHaqiTier(stack) != null || isWardenEcho(stack);
    }

    /** Removes all haqi items and warden echoes from inventory. Returns amount removed. */
    public static int clearHaqiItems(org.bukkit.entity.Player player) {
        int removed = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (isHaqiRelated(stack)) {
                removed += stack.getAmount();
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
        ItemStack cursor = player.getItemOnCursor();
        if (isHaqiRelated(cursor)) {
            removed += cursor.getAmount();
            player.setItemOnCursor(null);
        }
        return removed;
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

    /**
     * Resolves a workbench 3x3 matrix into an upgrade result (PDC-based, no Bukkit RecipeChoice).
     * Patterns:
     * - iron around basic → upgraded
     * - diamond around upgraded → enhanced
     * - echo shards around enhanced, with warden echo at bottom-center → warden
     */
    public static ItemStack matchUpgradeResult(ItemStack[] matrix) {
        if (matrix == null || matrix.length < 9) {
            return null;
        }
        ItemStack center = matrix[4];
        HaqiTier centerTier = getHaqiTier(center);
        if (centerTier == null) {
            return null;
        }

        int[] surround = {0, 1, 2, 3, 5, 6, 7, 8};
        if (centerTier == HaqiTier.BASIC && allMatchMaterial(matrix, surround, Material.IRON_INGOT)) {
            return createHaqi(HaqiTier.UPGRADED);
        }
        if (centerTier == HaqiTier.UPGRADED && allMatchMaterial(matrix, surround, Material.DIAMOND)) {
            return createHaqi(HaqiTier.ENHANCED);
        }
        // Warden: enhanced in center, warden echo bottom-middle, echo shards in the other 7 slots
        if (centerTier == HaqiTier.ENHANCED
                && isWardenEcho(matrix[7])
                && allMatchMaterial(matrix, new int[]{0, 1, 2, 3, 5, 6, 8}, Material.ECHO_SHARD)) {
            return createHaqi(HaqiTier.WARDEN);
        }
        return null;
    }

    private static boolean allMatchMaterial(ItemStack[] matrix, int[] slots, Material material) {
        for (int slot : slots) {
            ItemStack stack = matrix[slot];
            if (stack == null || stack.getType() != material) {
                return false;
            }
        }
        return true;
    }

    private static ItemMeta requireMeta(ItemStack stack) {
        return Objects.requireNonNull(stack.getItemMeta(),
                "ItemMeta unavailable for " + stack.getType());
    }
}
