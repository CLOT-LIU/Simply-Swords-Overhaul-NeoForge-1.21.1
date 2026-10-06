package net.jrdemiurge.simplyswordsoverhaul;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(SimplySwordsOverhaul.MOD_ID)
public class SimplySwordsOverhaul {
    public static final String MOD_ID = "simply_swords_overhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimplySwordsOverhaul(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modBus.addListener(Config::onLoad);
    }
}
