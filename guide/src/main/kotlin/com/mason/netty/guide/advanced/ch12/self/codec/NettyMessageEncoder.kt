package com.mason.netty.guide.advanced.ch12.self.codec

import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import com.mason.netty.guide.printError
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

/**
 * Created by mwu on 2018/10/23
 * Netty消息编码类
 */
class NettyMessageEncoder : MessageToMessageEncoder<NettyMessage>() {

  private val marshallingEncoder = MarshallingEncoder()

  override fun encode(ctx: ChannelHandlerContext, msg: NettyMessage, out: MutableList<Any>) {
    val sendBuf = Unpooled.buffer()
    sendBuf.writeInt(msg.header.crcCode)
    sendBuf.writeInt(msg.header.length)
    sendBuf.writeLong(msg.header.sessionId)
    sendBuf.writeInt(msg.header.type)
    sendBuf.writeInt(msg.header.priority)
    sendBuf.writeInt(msg.header.attachment.size)

    var key: String
    var keyArray: ByteArray
    var value: Any

    for ((attKey, attValue) in msg.header.attachment) {
      key = attKey
      keyArray = key.toByteArray(Charsets.UTF_8)
      sendBuf.writeInt(keyArray.size)
      sendBuf.writeBytes(keyArray)
      value = attValue
      marshallingEncoder.encode(value, sendBuf)
    }
    if (msg.body.toString() == "") {
      sendBuf.writeInt(0)
    } else {
      marshallingEncoder.encode(msg.body, sendBuf)
    }
    sendBuf.setInt(4, sendBuf.readableBytes())
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    printError(cause)
    ctx.close()
  }
}