package com.daren.chen.iot.mqtt.protocol.ws;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * WebSocketFrameToByteBufDecoder
 *
 * @author chendaren
 * @create 2019-11-20 13:46
 **/
public class WebSocketFrameToByteBufDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame wsFrame, List<Object> out)
        throws Exception {
        ByteBuf buf = wsFrame.content();
        buf.retain();
        out.add(buf);
    }
}
