package org.sparrow.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.sparrow.utils.NettyChannelLRUMap;

public class RpcEncoder extends MessageToByteEncoder {

	private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = NettyChannelLRUMap.SerializationUtil.serialize(in);
            out.writeInt(data.length);
            System.out.println("data.length"+(data.length));
            out.writeBytes(data);
        }
    }
}
