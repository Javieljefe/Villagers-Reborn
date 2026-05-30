package com.javic.slimpatch.network;

import com.javic.slimpatch.SlimPatch;
import com.javic.slimpatch.entity.CommandableVillager;
import com.javic.slimpatch.entity.FemaleVillagerEntity;
import com.javic.slimpatch.entity.MaleVillagerEntity;
import com.javic.slimpatch.entity.VillagerCommandHandler;
import com.javic.slimpatch.familytree.FamilyTreeTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;

import java.util.UUID;

public class VillagerEditDataPacket implements CustomPacketPayload {

    private static final double MAX_EDIT_DISTANCE_SQR = 64.0D;

    public static final Type<VillagerEditDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SlimPatch.MODID, "villager_edit_data_packet"));
    public static final StreamCodec<FriendlyByteBuf, VillagerEditDataPacket> CODEC = CustomPacketPayload.codec(VillagerEditDataPacket::encode, VillagerEditDataPacket::new);

    private final UUID villagerId;
    private final String savedSkinInput;
    private final String customSkinPath;
    private final int height;
    private final int width;

    public VillagerEditDataPacket(UUID villagerId, String savedSkinInput, String customSkinPath, int height, int width) {
        this.villagerId = villagerId;
        this.savedSkinInput = savedSkinInput;
        this.customSkinPath = customSkinPath;
        this.height = height;
        this.width = width;
    }

    public VillagerEditDataPacket(FriendlyByteBuf buf) {
        this.villagerId = buf.readUUID();
        this.savedSkinInput = buf.readUtf();
        this.customSkinPath = buf.readUtf();
        this.height = buf.readInt();
        this.width = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(villagerId);
        buf.writeUtf(savedSkinInput);
        buf.writeUtf(customSkinPath);
        buf.writeInt(height);
        buf.writeInt(width);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VillagerEditDataPacket msg, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
                Entity entity = level.getEntity(msg.villagerId);
                if (!(entity instanceof Villager baseVillager)) return;
                if (player.distanceToSqr(baseVillager) > MAX_EDIT_DISTANCE_SQR) return;
                if (entity instanceof CommandableVillager commandableVillager
                        && !VillagerCommandHandler.canUseProtectedAction(baseVillager, commandableVillager, player)) {
                    return;
                }
                boolean allowCustomSkin = !level.getServer().isDedicatedServer() && !level.getServer().isPublished();
                String savedSkinInput = allowCustomSkin ? msg.savedSkinInput : null;
                String customSkinPath = allowCustomSkin ? msg.customSkinPath : null;
                if (entity instanceof MaleVillagerEntity maleVillager) {
                    if (savedSkinInput != null) {
                        maleVillager.setSavedSkinInput(savedSkinInput);
                    }
                    if (customSkinPath != null) {
                        maleVillager.setCustomSkinPath(customSkinPath);
                    }
                    maleVillager.setVisualHeight(msg.height);
                    maleVillager.setVisualWidth(msg.width);
                } else if (entity instanceof FemaleVillagerEntity femaleVillager) {
                    if (savedSkinInput != null) {
                        femaleVillager.setSavedSkinInput(savedSkinInput);
                    }
                    if (customSkinPath != null) {
                        femaleVillager.setCustomSkinPath(customSkinPath);
                    }
                    femaleVillager.setVisualHeight(msg.height);
                    femaleVillager.setVisualWidth(msg.width);
                } else {
                    if (savedSkinInput == null) {
                    } else if (savedSkinInput.isEmpty()) {
                        baseVillager.getPersistentData().remove("SavedSkinInput");
                    } else {
                        baseVillager.getPersistentData().putString("SavedSkinInput", savedSkinInput);
                    }
                    if (customSkinPath == null) {
                    } else if (customSkinPath.isEmpty()) {
                        baseVillager.getPersistentData().remove("CustomSkinPath");
                    } else {
                        baseVillager.getPersistentData().putString("CustomSkinPath", customSkinPath);
                    }
                    baseVillager.getPersistentData().putInt("Height", msg.height);
                    baseVillager.getPersistentData().putInt("Width", msg.width);
                }
                FamilyTreeTracker.onVillagerSkinChanged(level.getServer(), baseVillager);
            }
        });
    }
}
