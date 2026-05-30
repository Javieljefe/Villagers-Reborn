package com.javic.slimpatch.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HumanVillagerSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, "slimpatch");

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_CLICK =
            SOUND_EVENTS.register("male_click",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_click")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_CLICK =
            SOUND_EVENTS.register("female_click",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_click")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_REACTION_POSITIVE =
            SOUND_EVENTS.register("villager.male.reaction.positive",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.male.reaction.positive")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_REACTION_NEGATIVE =
            SOUND_EVENTS.register("villager.male.reaction.negative",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.male.reaction.negative")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_REACTION_POSITIVE =
            SOUND_EVENTS.register("villager.female.reaction.positive",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.female.reaction.positive")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_REACTION_NEGATIVE =
            SOUND_EVENTS.register("villager.female.reaction.negative",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "villager.female.reaction.negative")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_HURT =
            SOUND_EVENTS.register("male_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT =
            SOUND_EVENTS.register("female_hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MALE_DEATH =
            SOUND_EVENTS.register("male_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "male_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_DEATH =
            SOUND_EVENTS.register("female_death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "female_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WEDDING_CUTSCENE_1 =
            SOUND_EVENTS.register("wedding_cutscene_1",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "wedding_cutscene_1")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WEDDING_CUTSCENE_2 =
            SOUND_EVENTS.register("wedding_cutscene_2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "wedding_cutscene_2")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WEDDING_CUTSCENE_3 =
            SOUND_EVENTS.register("wedding_cutscene_3",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "wedding_cutscene_3")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WEDDING_CUTSCENE_4 =
            SOUND_EVENTS.register("wedding_cutscene_4",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "wedding_cutscene_4")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FAMILY_CUTSCENE_1 =
            SOUND_EVENTS.register("family_cutscene_1",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "family_cutscene_1")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FAMILY_CUTSCENE_2 =
            SOUND_EVENTS.register("family_cutscene_2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "family_cutscene_2")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FAMILY_CUTSCENE_3 =
            SOUND_EVENTS.register("family_cutscene_3",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "family_cutscene_3")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FAMILY_CUTSCENE_4 =
            SOUND_EVENTS.register("family_cutscene_4",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("slimpatch", "family_cutscene_4")));

    public static SoundEvent maleClick() {
        return MALE_CLICK.get();
    }

    public static SoundEvent femaleClick() {
        return FEMALE_CLICK.get();
    }

    public static SoundEvent maleReactionPositive() {
        return MALE_REACTION_POSITIVE.get();
    }

    public static SoundEvent maleReactionNegative() {
        return MALE_REACTION_NEGATIVE.get();
    }

    public static SoundEvent femaleReactionPositive() {
        return FEMALE_REACTION_POSITIVE.get();
    }

    public static SoundEvent femaleReactionNegative() {
        return FEMALE_REACTION_NEGATIVE.get();
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

    public static SoundEvent weddingCutscene1() {
        return WEDDING_CUTSCENE_1.get();
    }

    public static SoundEvent weddingCutscene2() {
        return WEDDING_CUTSCENE_2.get();
    }

    public static SoundEvent weddingCutscene3() {
        return WEDDING_CUTSCENE_3.get();
    }

    public static SoundEvent weddingCutscene4() {
        return WEDDING_CUTSCENE_4.get();
    }

    public static SoundEvent familyCutscene1() {
        return FAMILY_CUTSCENE_1.get();
    }

    public static SoundEvent familyCutscene2() {
        return FAMILY_CUTSCENE_2.get();
    }

    public static SoundEvent familyCutscene3() {
        return FAMILY_CUTSCENE_3.get();
    }

    public static SoundEvent familyCutscene4() {
        return FAMILY_CUTSCENE_4.get();
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
