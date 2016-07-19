package mods.belgabor.bitdrawers.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by Belgabor on 19.07.2016.
 */
public class ConfigManager {
    private static final String LANG_PREFIX = "bitDrawers.config.";
    
    public Configuration config;
    
    public boolean debugTrace = false;
    public int bitdrawerStorage = 16;
    
    public ConfigManager(File config) {
        this.config = new Configuration(config);
        sync();
    }
    
    public void sync() {
        debugTrace = config.get(Configuration.CATEGORY_GENERAL, "enableDebugLogging", false,
                "Writes additional log messages while using the mod.  Mainly for debug purposes.  Should be kept disabled unless instructed otherwise.")
                .setLanguageKey(LANG_PREFIX + "prop.enableDebugLogging").getBoolean();
        
        bitdrawerStorage = config.get(Configuration.CATEGORY_GENERAL, "bitdrawerBaseStorage", 16,
                "Base storage of a bit drawer (stacks).").setRequiresWorldRestart(true)
                .setLanguageKey(LANG_PREFIX + "prop.bitdrawerBaseStorage").getInt();
        
        if (config.hasChanged())
            config.save();
    }
}
