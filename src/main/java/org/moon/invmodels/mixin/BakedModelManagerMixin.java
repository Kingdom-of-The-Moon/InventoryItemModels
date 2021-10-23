package org.moon.invmodels.mixin;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;
import org.moon.invmodels.access.BakedModelManagerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin implements BakedModelManagerAccess {

    @Shadow private Map<Identifier, BakedModel> models;

    @Override @Unique
    public BakedModel reallyGetModel(Identifier model) {
        return this.models.get(model);
    }
}
