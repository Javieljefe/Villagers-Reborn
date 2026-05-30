package com.javic.slimpatch.events;

import com.javic.slimpatch.dialogue.DialogueManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "slimpatch")
public class DialogueSessionHandler {

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        DialogueManager.endDialoguesForPlayer(event.getEntity().getUUID());
    }
}
