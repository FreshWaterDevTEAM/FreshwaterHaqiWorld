package com.freshwater.haqiworld.registry;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom sound events, backed by the real ogg clips in {@code assets/fhw/sounds/}.
 *
 * <ul>
 *   <li>{@code HAQI_PLAYER} - the player's haqi attack voice (吴我朝 / 大声老吴).</li>
 *   <li>{@code HAQI_MOB} - hostile / strong-mob haqi voice (耄耋哈气 1-3).</li>
 *   <li>{@code HAQI_MOB_PEACEFUL} - peaceful-mob haqi voice (温柔老吴).</li>
 * </ul>
 *
 * <p>The sonic boom blast itself still reuses the vanilla Warden boom sound; these events
 * are the layered "haqi" voices for the player and mobs.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FreshwaterHaqiWorld.MODID);

    public static final RegistryObject<SoundEvent> HAQI_PLAYER = register("haqi_player");
    public static final RegistryObject<SoundEvent> HAQI_MOB = register("haqi_mob");
    public static final RegistryObject<SoundEvent> HAQI_MOB_PEACEFUL = register("haqi_mob_peaceful");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(FreshwaterHaqiWorld.MODID, name)));
    }

    public static void register(BusGroup modBusGroup) {
        SOUND_EVENTS.register(modBusGroup);
    }
}
