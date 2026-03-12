package com.xioyim.jeicommands;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * JEI plugin that captures the runtime so commands can open JEI views.
 * Discovered automatically by JEI via the @JeiPlugin annotation.
 */
@JeiPlugin
public class JeiCommandsPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation(JeiCommandsMod.MOD_ID, "plugin");

    /** Volatile so client-thread writes are visible immediately. */
    @Nullable
    public static volatile IJeiRuntime runtime;

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
