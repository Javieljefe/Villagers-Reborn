package com.javic.slimpatch.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HumanIllagerSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "slimpatch");

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_AMBIENT =
            SOUND_EVENTS.register("illager.male.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.male.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_HURT =
            SOUND_EVENTS.register("illager.male.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.male.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_DEATH =
            SOUND_EVENTS.register("illager.male.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.male.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_AMBIENT =
            SOUND_EVENTS.register("illager.female.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.female.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT =
            SOUND_EVENTS.register("illager.female.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.female.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_DEATH =
            SOUND_EVENTS.register("illager.female.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "illager.female.death")));

    public static SoundEvent maleAmbient() {
        return MALE_AMBIENT.get();
    }

    public static SoundEvent femaleAmbient() {
        return FEMALE_AMBIENT.get();
    }

    public static SoundEvent maleHurt() {
        return MALE_HURT.get();
    }

    public static SoundEvent femaleHurt() {
        return FEMALE_HURT.get();
    }

    public static SoundEvent maleDeath() {
        return MALE_DEATH.get();
    }

    public static SoundEvent femaleDeath() {
        return FEMALE_DEATH.get();
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}