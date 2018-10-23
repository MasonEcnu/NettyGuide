package com.mason.netty.guide.codec

import com.mason.netty.guide.printError
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder
import org.msgpack.MessagePack

/**
 * Created by mwu on 2018/10/5
 * Msgpack Encoder & Decoder
 */
class MsgpackDecoder : MessageToMessageDecoder<ByteBuf>() {
  override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
    val size = msg.readableBytes()
    val array = ByteArray(size)
    msg.getBytes(msg.readerIndex(), array, 0, size)
    val msgpack = MessagePack()
    out.add(msgpack.read(array))
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.close()
    printError(cause)
  }
}

class MsgpackEncoder : MessageToByteEncoder<Any>() {
  override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
    val msgpack = MessagePack()
    val raw = msgpack.write(msg)
    out.writeBytes(raw)
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.close()
    printError(cause)
  }
}
