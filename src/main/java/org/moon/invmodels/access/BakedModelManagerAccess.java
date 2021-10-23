package org.moon.invmodels.access;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;

public interface BakedModelManagerAccess {
    BakedModel reallyGetModel(Identifier model);

    static BakedModelManagerAccess from(BakedModelManager manager) {
        return (BakedModelManagerAccess) manager;
    }
}
