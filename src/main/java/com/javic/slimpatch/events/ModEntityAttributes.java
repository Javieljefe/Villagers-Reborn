package com.javic.slimpatch.events;

import com.javic.slimpatch.ModEntities;
import com.javic.slimpatch.SlimPatch;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 🧠 Registro de atributos para las entidades personalizadas.
 * Compatible con NeoForge 1.21.1.
 */
@EventBusSubscriber(modid = SlimPatch.MODID)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Obtener los atributos del tipo vanilla Wandering Trader
        AttributeSupplier traderAttrs = DefaultAttributes.getSupplier(EntityType.WANDERING_TRADER);

        if (traderAttrs != null) {
            // ✅ Solo registramos atributos para el nuevo tipo (spawn natural)
            try {
                event.put(ModEntities.HUMAN_TRADER_NATURAL.get(), traderAttrs);
                SlimPatch.LOGGER.info("[SlimPatch] ✅ Atributos asignados correctamente a HumanTraderNatural.");
            } catch (IllegalStateException e) {
                SlimPatch.LOGGER.warn("[SlimPatch] ⚠️ Atributos ya estaban registrados para HumanTraderNatural, se omite duplicado.");
            }
        } else {
            SlimPatch.LOGGER.error("[SlimPatch] ⚠️ No se pudieron obtener los atributos del WanderingTrader vanilla.");
        }
    }
}