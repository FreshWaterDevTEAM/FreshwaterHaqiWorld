package com.freshwater.haqiworld.registry;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import com.freshwater.haqiworld.haqi.HaqiItem;
import com.freshwater.haqiworld.haqi.HaqiTier;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All items added by FreshwaterHaqiWorld: the four haqi tiers plus the Warden-dropped
 * crafting material used to make the top-tier haqi.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FreshwaterHaqiWorld.MODID);

    public static final RegistryObject<Item> HAQI_BASIC = registerHaqi("haqi_basic", HaqiTier.BASIC);
    public static final RegistryObject<Item> HAQI_UPGRADED = registerHaqi("haqi_upgraded", HaqiTier.UPGRADED);
    public static final RegistryObject<Item> HAQI_ENHANCED = registerHaqi("haqi_enhanced", HaqiTier.ENHANCED);
    public static final RegistryObject<Item> HAQI_WARDEN = registerHaqi("haqi_warden", HaqiTier.WARDEN);

    /** Material dropped by the Warden, used to craft the top-tier haqi. */
    public static final RegistryObject<Item> WARDEN_ECHO = ITEMS.register("warden_echo",
            () -> new Item(new Item.Properties().setId(ITEMS.key("warden_echo"))));

    private ModItems() {
    }

    private static RegistryObject<Item> registerHaqi(String name, HaqiTier tier) {
        return ITEMS.register(name,
                () -> new HaqiItem(new Item.Properties().stacksTo(1).setId(ITEMS.key(name)), tier));
    }

    public static void register(BusGroup modBusGroup) {
        ITEMS.register(modBusGroup);
    }
}
