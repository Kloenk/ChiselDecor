package com.knowyourknot.chiseldecor.config;

import java.io.File;
import java.util.Arrays;

import com.knowyourknot.chiseldecor.ChiselDecorEntryPoint;
import com.oroarmor.config.ArrayConfigItem;
import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;

import net.fabricmc.loader.api.FabricLoader;

public class ChiselDecorConfig extends Config {
    private static final ConfigItemGroup MAIN_GROUP = new ConfigRoot();
    private static final File CONFIG_DIRECTORY = new File(FabricLoader.getInstance().getConfigDir().toFile(), "chiseldecor");
    private static final File CONFIG_FILE = new File(CONFIG_DIRECTORY, "config.json");
    
    public ChiselDecorConfig() {
		super(Arrays.asList(MAIN_GROUP), CONFIG_FILE, "chiseldecor");
        if (!CONFIG_DIRECTORY.exists()) { 
            CONFIG_DIRECTORY.mkdir();
        }
        readConfigFromFile();
	}
    
    public static class ConfigRoot extends ConfigItemGroup {
        public static final ArrayConfigItem<String> BLOCK_PACKS = new ArrayConfigItem<String>("block_packs", new String[]{""}, "config.chiseldecor.block_packs");

        public ConfigRoot() {
            super(Arrays.asList(BLOCK_PACKS), "chiseldecor_config");
        }
    }

    public String[] getBlockPackDirs() {
        ConfigItem<?> dirStringConfigItem = this.getConfigs().get(0).getConfigs().get(0);
        Object result = dirStringConfigItem.getValue();
        if (!(result instanceof String[])) {
            ChiselDecorEntryPoint.LOGGER.warn("Blockpacks config invalid. No Blockpacks could be loaded");
            return new String[]{};
        }

        String[] stringResult = (String[])result;
        ChiselDecorEntryPoint.LOGGER.info(
                String.format("Found %d blockpack(s): %s.", stringResult.length, Arrays.toString(stringResult))
        );
        return stringResult;
    }
}
