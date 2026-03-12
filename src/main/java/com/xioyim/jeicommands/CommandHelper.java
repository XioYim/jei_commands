package com.xioyim.jeicommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Server-side command executors.
 *
 * Each method validates the requested id, then sends a {@link PacketOpenJei}
 * to the executing player's client.  Because commands are registered via
 * {@link net.minecraftforge.event.RegisterCommandsEvent} with permission level 0,
 * they work for all players regardless of OP status, including:
 * <ul>
 *   <li>{@code /execute at @a run jeiitemr minecraft:diamond}</li>
 *   <li>tellraw {@code run_command} click events</li>
 *   <li>Command blocks targeting a player</li>
 * </ul>
 * No JEI imports here — all JEI work happens client-side in
 * {@link ClientCommandHelper}.
 */
public class CommandHelper {

    // /jeiitemr <item>
    public static int executeItemRecipe(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Item not found: " + id));
            return 0;
        }
        return sendToPlayer(ctx.getSource(), new PacketOpenJei(PacketOpenJei.Action.RECIPE, id));
    }

    // /jeiitemu <item>
    public static int executeItemUsage(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Item not found: " + id));
            return 0;
        }
        return sendToPlayer(ctx.getSource(), new PacketOpenJei(PacketOpenJei.Action.USAGE, id));
    }

    // /jeiiteme <effect>
    public static int executeEffect(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "effect");
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            ctx.getSource().sendFailure(Component.literal("Effect not found: " + id));
            return 0;
        }
        return sendToPlayer(ctx.getSource(), new PacketOpenJei(PacketOpenJei.Action.EFFECT, id));
    }

    // -------------------------------------------------------------------------

    private static int sendToPlayer(CommandSourceStack source, PacketOpenJei pkt) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Network.sendToPlayer(player, pkt);
            return 1;
        } catch (CommandSyntaxException e) {
            // Non-player source (command block without target, etc.) — silently skip.
            return 0;
        }
    }
}
