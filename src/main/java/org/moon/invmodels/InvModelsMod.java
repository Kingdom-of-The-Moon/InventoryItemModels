package org.moon.invmodels;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InvModelsMod implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("invmodels");
	public static final Predicate<String> filePredicate = str -> str.endsWith(".json");
	public static final HashMap<Identifier, HashMap<ModelTransformation.Mode, ExtraRenderData>> INVENTORY_MODELS = new HashMap<>();
	public static boolean bakedModelManagerReload;

	public record ExtraRenderData(Identifier id, boolean disableLighting) {}

	@Override
	public void onInitializeClient() {
		ModelLoadingRegistryImpl.INSTANCE.registerModelProvider(onModelLoad());
	}

	private ExtraModelProvider onModelLoad() {
		JsonParser parser = new JsonParser();
		INVENTORY_MODELS.clear();
		bakedModelManagerReload = true;
		return (manager, out) -> {
			Collection<Identifier> extraModels = manager.findResources("invmodels", filePredicate);
			extraModels.forEach(identifier -> {
				String path = identifier.getPath();
				path = path.substring("invmodels/".length(), path.length()-".json".length());
				Identifier originalModel = new Identifier(identifier.getNamespace(), path);
				try {
					Resource metadataResource = manager.getResource(identifier);
					String rawJson = new String(metadataResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
					loadMetadata(originalModel, parser.parse(rawJson).getAsJsonObject(), out);

				} catch (IOException e) {
					LOGGER.error("Failed to load model metadata for " + identifier);
					e.printStackTrace();
				}
			});
		};
	}

	private void loadMetadata(Identifier originalModel, JsonObject metadata, Consumer<Identifier> extraModels) {
		Arrays.stream(ModelTransformation.Mode.values()).forEach(mode -> {
			String memberName = mode.name().toLowerCase();

			if (metadata.has(memberName)) {
				JsonElement modeData = metadata.get(memberName);
				Identifier modelId;
				boolean disableLighting = false;
				if (modeData.isJsonObject()) {
					JsonObject entry = modeData.getAsJsonObject();
					modelId = new Identifier(entry.get("id").getAsString());
					if (entry.has("disable_lighting")) {
						disableLighting = entry.get("disable_lighting").getAsBoolean();
					}
				} else {
					modelId = new Identifier(modeData.getAsString());
				}

				makeCustomization(originalModel, disableLighting, mode, modelId);
				extraModels.accept(modelId);
			}

		});
	}

	public static boolean hasCustomization(Identifier originalModel) {
		return INVENTORY_MODELS.containsKey(originalModel);
	}

	public static Optional<ExtraRenderData> getCustomization(Identifier originalModel, ModelTransformation.Mode mode) {
		if (hasCustomization(originalModel)) {
			HashMap<ModelTransformation.Mode, ExtraRenderData> modeMap = INVENTORY_MODELS.get(originalModel);
			if (modeMap.containsKey(mode)) {
				return Optional.of(modeMap.get(mode));
			}
		}
		return Optional.empty();
	}

	public static void makeCustomization(Identifier originalModel, boolean disableLighting, ModelTransformation.Mode mode, Identifier newModel) {
		INVENTORY_MODELS.putIfAbsent(originalModel, new HashMap<>());
		HashMap<ModelTransformation.Mode, ExtraRenderData> modelMap = INVENTORY_MODELS.get(originalModel);
		modelMap.put(mode, new ExtraRenderData(newModel, disableLighting));
	}
}
