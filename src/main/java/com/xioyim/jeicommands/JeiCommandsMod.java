package com.xioyim.jeicommands;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(JeiCommandsMod.MOD_ID)
public class JeiCommandsMod {

    public static final String MOD_ID = "jei_commands";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JeiCommandsMod() {
        // Register the server→client network channel for JEI open-GUI packets.
        Network.register();
    }
}
