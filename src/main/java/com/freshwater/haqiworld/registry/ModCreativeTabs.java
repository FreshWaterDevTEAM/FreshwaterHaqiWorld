package com.freshwater.haqiworld.registry;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab holding every haqi item and material.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FreshwaterHaqiWorld.MODID);

    public static final RegistryObject<CreativeModeTab> HAQI_TAB = TABS.register("haqi", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fhw.haqi"))
            .icon(() -> ModItems.HAQI_BASIC.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.HAQI_BASIC.get());
                output.accept(ModItems.HAQI_UPGRADED.get());
                output.accept(ModItems.HAQI_ENHANCED.get());
                output.accept(ModItems.HAQI_WARDEN.get());
                output.accept(ModItems.WARDEN_ECHO.get());
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(BusGroup modBusGroup) {
        TABS.register(modBusGroup);
    }
}
