package com.dog.morevaulting.config;

import com.dog.morevaulting.MoreVaulting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.util.VHAPIProcesserUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config/the_vault";

    /**
     * Loads a config or creates default if it doesn't exist
     *
     * @param resourcePath Path to the default config in resources
     * @param configFileName Filename to use in the config directory
     * @param vhapiRegistryId Registry ID for VHAPI
     */
    public static void loadOrCreateConfig(String resourcePath, String configFileName, ResourceLocation vhapiRegistryId) {
        Path configFile = Paths.get(CONFIG_DIR, configFileName);

        if (!Files.exists(configFile)) {
            createDefaultConfig(resourcePath, configFile.toString());
        }

        try {
            File file = configFile.toFile();
            try (FileReader reader = new FileReader(file)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                VHAPIProcesserUtils.addManualConfigFile(file, vhapiRegistryId);
                MoreVaulting.LOGGER.info("Loaded config: " + configFileName);
            }
        } catch (IOException e) {
            MoreVaulting.LOGGER.error("Error loading config " + configFileName + ": " + e.getMessage());
            loadFromResource(resourcePath, vhapiRegistryId);
        }
    }

    /**
     * Creates a default config file by copying from resources
     *
     * @param resourcePath Source path in resources
     * @param targetPath Target file path
     */
    private static void createDefaultConfig(String resourcePath, String targetPath) {
        try (InputStream input = MoreVaulting.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                MoreVaulting.LOGGER.error("Default config not found in resources: " + resourcePath);
                return;
            }

            JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(input));

            // Write the JSON to the config file
            try (FileWriter writer = new FileWriter(targetPath)) {
                GSON.toJson(jsonElement, writer);
                MoreVaulting.LOGGER.info("Created default config: " + targetPath);
            }
        } catch (IOException e) {
            MoreVaulting.LOGGER.error("Failed to create default config: " + e.getMessage());
        }
    }

    /**
     * Fallback method to load directly from resource
     */
    private static void loadFromResource(String resourcePath, ResourceLocation vhapiRegistryId) {
        try (InputStream stream = MoreVaulting.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            VHAPIProcesserUtils.addManualConfigFile(stream, vhapiRegistryId);
            MoreVaulting.LOGGER.info("Loaded config from resource: " + resourcePath);
        } catch (IOException e) {
            MoreVaulting.LOGGER.error("Failed to load from resource: " + e.getMessage());
        }
    }
}