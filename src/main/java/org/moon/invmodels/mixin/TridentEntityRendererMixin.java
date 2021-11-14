package org.moon.invmodels.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.TridentEntityRenderer;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntityRenderer.class)
public class TridentEntityRendererMixin {

    @Shadow @Final private TridentEntityModel model;
    @Unique private ItemStack stack;
    @Unique private VertexConsumerProvider vcp;
    @Unique private float g;
    @Unique private TridentEntity entity;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/projectile/TridentEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    private void render(TridentEntity tridentEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        this.stack = ((TridentEntityAccessMixin) tridentEntity).getTridentStack();
        this.vcp = vertexConsumerProvider;
        this.g = g;
        this.entity = tridentEntity;
    }


    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/TridentEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
            method = "render(Lnet/minecraft/entity/projectile/TridentEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    private void render(TridentEntityModel instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();

        matrices.push();
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
        matrices.translate(0,-1,0);

        itemRenderer.renderItem(stack, ModelTransformation.Mode.NONE, light, overlay, matrices, vcp, 42);

        matrices.pop();

    }

}
