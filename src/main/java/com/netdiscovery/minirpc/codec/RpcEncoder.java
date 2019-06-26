package com.netdiscovery.minirpc.codec;

import com.netdiscovery.minirpc.utils.SerializableUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by tony on 2019-06-22.
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        if (genericClass.isInstance(msg)) {
            String json = SerializableUtils.toJson(msg);
            byte[] data = json.getBytes();
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
