package com.javic.slimpatch.client.model;

import com.javic.slimpatch.entity.FamilyVillager;
import com.javic.slimpatch.entity.VillagerAgeStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class CustomVillagerModelMale<T extends LivingEntity> extends HumanoidModel<T> {

    private VillagerAgeStage ageStage = VillagerAgeStage.ADULT;

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

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F,
                                8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 32)
                        .addBox(-4.0F, 0.0F, -2.0F,
                                8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

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

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32)
                        .addBox(-2.0F, 0.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .mirror(true)
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

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.ageStage = entity instanceof FamilyVillager familyVillager ? familyVillager.getAgeStage() : VillagerAgeStage.ADULT;

        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof CrossbowItem && entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.rightArm.xRot = (float) Math.toRadians(-65);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-35);
            this.leftArm.yRot = (float) Math.toRadians(-15);
            this.leftArm.zRot = (float) Math.toRadians(10);
        } else if (entity.getMainHandItem().getItem() instanceof CrossbowItem && entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() != null) {
            this.rightArm.xRot = (float) Math.toRadians(-90);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-30);
            this.leftArm.yRot = (float) Math.toRadians(15);
            this.leftArm.zRot = 0.0F;
        } else if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof BowItem && entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.rightArm.xRot = (float) Math.toRadians(-90) + this.head.xRot;
            this.rightArm.yRot = this.head.yRot - 0.1F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (float) Math.toRadians(-70) + this.head.xRot;
            this.leftArm.yRot = this.head.yRot + 0.45F;
            this.leftArm.zRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (this.ageStage == VillagerAgeStage.TODDLER) {
            poseStack.pushPose();
            poseStack.scale(1.2F, 1.2F, 1.2F);
            poseStack.translate(0.0F, 0.12F, 0.0F);
            this.headParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, color));
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(0.85F, 0.85F, 0.85F);
            poseStack.translate(0.0F, 0.16F, 0.0F);
            this.bodyParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, color));
            poseStack.popPose();
            return;
        }
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
