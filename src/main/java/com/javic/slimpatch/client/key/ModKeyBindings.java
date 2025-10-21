package com.javic.slimpatch.client.key;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.dialogue.DialogueManager;
import com.javic.slimpatch.client.gui.VillagerDialogueScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SlimPatch.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class ModKeyBindings {

    public static KeyMapping OPEN_DIALOGUE;

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        OPEN_DIALOGUE = new KeyMapping(
                "key." + SlimPatch.MODID + ".open_dialogue", // nombre interno
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R, // Tecla R por defecto
                "key.categories." + SlimPatch.MODID // categoría en controles
        );
        event.register(OPEN_DIALOGUE);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (OPEN_DIALOGUE.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && mc.hitResult != null) {
                if (mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                    if (((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity() instanceof Villager villager) {
                        // 🔹 Marca inicio de diálogo en el manager
                        DialogueManager.startDialogue(villager, player);

                        // 🔹 Abre la pantalla de diálogo
                        mc.setScreen(new VillagerDialogueScreen(villager));
                    }
                }
            }
        }
    }
}