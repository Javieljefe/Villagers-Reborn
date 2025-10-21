package com.javic.slimpatch.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CustomVillagerModelMale<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("slimpatch", "male_villager"),
                    "main"
            );

    public CustomVillagerModelMale(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        // ============================================================
        // 🔹 Cabeza
        // ============================================================
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Cuerpo
        // ============================================================
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 32)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Brazos
        // ============================================================
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 32)
                        .addBox(-2.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-1.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 48)
                        .addBox(-1.0F, -2.0F, -2.0F,
                                3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Pierna derecha — normal
        // ============================================================
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);

        // ============================================================
        // 🔹 Pierna izquierda — corregida UV y espejo
        // ============================================================
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .mirror(true) // 🔸 usa la misma zona UV pero invertida
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32)
                        .mirror(true)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}