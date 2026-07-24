package com.freshwater.haqiworld.client;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FreshwaterHaqiWorld.MODID);

    public static final RegistryObject<SimpleParticleType> HAQI_SONIC_BOOM =
            PARTICLES.register("haqi_sonic_boom", () -> new SimpleParticleType(true));

    private ModParticles() {
    }
}
