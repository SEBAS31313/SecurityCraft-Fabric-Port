package net.geforcemods.securitycraft.platform;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class StreamCodecs {
    private StreamCodecs() {}
    public static <B extends ByteBuf, E extends Enum<E>> StreamCodec<B,E> enumCodec(Class<E> type) {
        E[] values = type.getEnumConstants();
        return new StreamCodec<>() {
            public E decode(B buffer) {
                int ordinal = ByteBufCodecs.VAR_INT.decode(buffer);
                if (ordinal < 0 || ordinal >= values.length) throw new DecoderException("Unknown " + type.getSimpleName() + " value " + ordinal);
                return values[ordinal];
            }
            public void encode(B buffer, E value) { ByteBufCodecs.VAR_INT.encode(buffer, value.ordinal()); }
        };
    }
}
