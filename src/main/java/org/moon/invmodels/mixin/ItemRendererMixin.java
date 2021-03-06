package org.moon.invmodels.mixin;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.moon.invmodels.access.BakedModelManagerAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static org.moon.invmodels.InvModelsMod.*;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
	@Shadow @Final private ItemModels models;

	private ItemStack stack;
	@Unique private ModelTransformation.Mode renderMode;
	@Unique	private boolean disableLighting;

	@Inject(
			at = @At(
					value = "INVOKE",
					shift = At.Shift.BEFORE,
					target = "Lnet/minecraft/client/render/model/BakedModel;getTransformation()Lnet/minecraft/client/render/model/json/ModelTransformation;"
			),
			method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"
	)
	private void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		this.stack = stack;
		this.renderMode = renderMode;
	}

	@ModifyVariable(
			at = @At(
					value = "INVOKE",
					shift = At.Shift.BEFORE,
					target = "Lnet/minecraft/client/render/model/BakedModel;getTransformation()Lnet/minecraft/client/render/model/json/ModelTransformation;"
			),
			method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"
	)
	private BakedModel modifyModel(BakedModel model) {
		BakedModelManagerAccess modelManager = BakedModelManagerAccess.from(this.models.getModelManager());
		try {
			Identifier modelID = Registry.ITEM.getId(stack.getItem());
			if (hasCustomization(modelID)) {
				Optional<ExtraRenderData> renderData = getCustomization(modelID, renderMode);
				if (renderData.isPresent()) {
					BakedModel newModel = modelManager.reallyGetModel(renderData.get().id());
					if (newModel != null) {
						if (renderData.get().disableLighting()) disableLighting = true;
						return newModel;
					}
				}
			}
		} catch (RuntimeException ignored) {}

		return model;
	}

	@Inject(
			at = @At("HEAD"),
			method = "renderBakedItemModel"
	)
	private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
		if (disableLighting) {
			DiffuseLighting.disableGuiDepthLighting();
		}
	}

	@Inject(
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", shift = At.Shift.BEFORE),
			method = "renderGuiItemModel"
	)
	private void renderGuiItemModel(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
		if (disableLighting) {
			DiffuseLighting.enableGuiDepthLighting();
			disableLighting = false;
		}
	}

}