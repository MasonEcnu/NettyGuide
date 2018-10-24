package com.mason.netty.guide.advanced.ch12.self.codec

import com.mason.netty.guide.advanced.ch12.self.Header
import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import com.mason.netty.guide.printError
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

/**
 * Created by mwu on 2018/10/23
 * Netty消息解码类
 */
class NettyMessageDecoder(maxFrameLength: Int, lengthFieldOffset: Int, lengthFieldLength: Int) : LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength) {
  private val marshallingDecoder = MarshallingDecoder()
  override fun decode(ctx: ChannelHandlerContext, bin: ByteBuf): Any {
    val frame = super.decode(ctx, bin) ?: return Any()
    frame as ByteBuf
    val header = Header(
        crcCode = frame.readInt(),
        length = frame.readInt(),
        sessionId = frame.readLong(),
        type = frame.readInt(),
        priority = frame.readInt(),
        attachment = hashMapOf()
    )
    val message = NettyMessage(header, Any())
    val size = frame.readInt()
    if (size > 0) {
      val attach = HashMap<String, Any>(size)
      var keySize: Int
      var keyArray: ByteArray
      var key: String
      for (i in 0 until size) {
        keySize = frame.readInt()
        keyArray = ByteArray(keySize)
        frame.readBytes(keyArray)
        key = String(keyArray, Charsets.UTF_8)
        attach[key] = marshallingDecoder.decode(frame)
      }
      header.attachment = attach
    }
    if (frame.readableBytes() > 4) {
      message.body = marshallingDecoder.decode(frame)
    }
    return message
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    printError(cause)
    ctx.close()
  }
}