package com.javic.slimpatch.client;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.client.gui.SkinSelectionHandler;
import com.javic.slimpatch.client.model.*;
import com.javic.slimpatch.client.renderer.*;
import com.javic.slimpatch.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class ClientSetup {

    public static void init(IEventBus modEventBus) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(SkinSelectionHandler.class);
        modEventBus.addListener(ClientSetup::onClientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerLayerDefinitions);
        modEventBus.addListener(ClientSetup::registerItemColors);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MALE_VILLAGER.get(), MaleVillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.FEMALE_VILLAGER.get(), FemaleVillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_WANDERING_TRADER.get(), HumanWanderingTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_PILLAGER.get(), HumanPillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_VINDICATOR.get(), HumanVindicatorRenderer::new);
        event.registerEntityRenderer(ModEntities.HUMAN_EVOKER.get(), HumanEvokerRenderer::new);
        event.registerEntityRenderer(net.minecraft.world.entity.EntityType.WITCH, HumanWitchRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FemaleVillagerModel.LAYER_LOCATION, FemaleVillagerModel::createBodyLayer);
        event.registerLayerDefinition(CustomVillagerModelMale.LAYER_LOCATION, CustomVillagerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanPillagerModelMale.LAYER_LOCATION, HumanPillagerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanPillagerModelFemale.LAYER_LOCATION, HumanPillagerModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanVindicatorModelMale.LAYER_LOCATION, HumanVindicatorModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanVindicatorModelFemale.LAYER_LOCATION, HumanVindicatorModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanEvokerModelMale.LAYER_LOCATION, HumanEvokerModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanEvokerModelFemale.LAYER_LOCATION, HumanEvokerModelFemale::createBodyLayer);
        event.registerLayerDefinition(HumanWanderingTraderModelMale.LAYER_LOCATION, HumanWanderingTraderModelMale::createBodyLayer);
        event.registerLayerDefinition(HumanWanderingTraderModelFemale.LAYER_LOCATION, HumanWanderingTraderModelFemale::createBodyLayer);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> -1, ModItems.MALE_VILLAGER_SPAWN_EGG.get());
        event.register((stack, layer) -> -1, ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
    }
}