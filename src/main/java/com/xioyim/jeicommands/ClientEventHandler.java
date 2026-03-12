package com.xioyim.jeicommands;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

/**
 * Registers the three JEI commands on the SERVER dispatcher so they work with:
 * <ul>
 *   <li>Direct player input: {@code /jeiitemr minecraft:diamond}</li>
 *   <li>Execute: {@code /execute at @a run jeiitemr minecraft:diamond}</li>
 *   <li>Tellraw click: {@code "clickEvent":{"action":"run_command","value":"/jeiitemr minecraft:diamond"}}</li>
 *   <li>Command blocks targeting a player</li>
 * </ul>
 *
 * No {@code Dist.CLIENT} restriction — {@link RegisterCommandsEvent} fires on
 * both the integrated server and dedicated server, so commands are registered
 * in all environments.  Permission level 0 allows any player (no OP required).
 */
@Mod.EventBusSubscriber(modid = JeiCommandsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        // /jeiitemr <item>  — open recipe/source view (equivalent to R key in JEI)
        dispatcher.register(Commands.literal("jeiitemr")
                .requires(src -> src.hasPermission(0))
                .then(Commands.argument("item", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> suggestItems(builder))
                        .executes(CommandHelper::executeItemRecipe)));

        // /jeiitemu <item>  — open usage view (equivalent to U key in JEI)
        dispatcher.register(Commands.literal("jeiitemu")
                .requires(src -> src.hasPermission(0))
                .then(Commands.argument("item", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> suggestItems(builder))
                        .executes(CommandHelper::executeItemUsage)));

        // /jeiiteme <effect>  — open JEED effect description page
        dispatcher.register(Commands.literal("jeiiteme")
                .requires(src -> src.hasPermission(0))
                .then(Commands.argument("effect", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> suggestEffects(builder))
                        .executes(CommandHelper::executeEffect)));
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestItems(
            SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        ForgeRegistries.ITEMS.getKeys().stream()
                .map(net.minecraft.resources.ResourceLocation::toString)
                .filter(s -> s.contains(remaining))
                .sorted()
                .limit(100)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestEffects(
            SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        ForgeRegistries.MOB_EFFECTS.getKeys().stream()
                .map(net.minecraft.resources.ResourceLocation::toString)
                .filter(s -> s.contains(remaining))
                .sorted()
                .limit(100)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
