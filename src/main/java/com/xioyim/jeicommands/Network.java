package com.xioyim.jeicommands;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Registers the server→client network channel used to forward JEI open-GUI
 * requests to the executing player's client.
 *
 * Commands are registered server-side (so tellraw / execute at @a run work),
 * but JEI lives on the client. This channel bridges the gap: the server handler
 * sends a {@link PacketOpenJei} to the target player; the client receives it and
 * calls the JEI API.
 */
public class Network {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JeiCommandsMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    /** Call once during mod init to register all packet types. */
    public static void register() {
        CHANNEL.registerMessage(
                0,
                PacketOpenJei.class,
                PacketOpenJei::encode,
                PacketOpenJei::decode,
                PacketOpenJei::handle);
    }

    /** Send a packet to a single player's client. */
    public static void sendToPlayer(ServerPlayer player, PacketOpenJei pkt) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }
}
