package com.xioyim.jeicommands;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server→Client packet that tells the client which JEI view to open.
 *
 * The server validates the item/effect id and sends this packet to the
 * target player. {@link ClientCommandHelper} (client-only) then opens the
 * appropriate JEI screen. Using {@link DistExecutor#unsafeRunWhenOn} prevents
 * {@link ClientCommandHelper} from being class-loaded on a dedicated server.
 */
public class PacketOpenJei {

    public enum Action { RECIPE, USAGE, EFFECT }

    public final Action action;
    public final ResourceLocation id;

    public PacketOpenJei(Action action, ResourceLocation id) {
        this.action = action;
        this.id = id;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeResourceLocation(id);
    }

    public static PacketOpenJei decode(FriendlyByteBuf buf) {
        return new PacketOpenJei(buf.readEnum(Action.class), buf.readResourceLocation());
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // enqueueWork runs on the client game thread — safe to call JEI here.
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientCommandHelper.execute(action, id)));
        ctx.setPacketHandled(true);
    }
}
