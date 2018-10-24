package com.mason.netty.guide.advanced.ch12.self.codec

import io.netty.buffer.ByteBuf

/**
 * Created by mwu on 2018/10/23
 * Netty消息编码工具类
 */
class MarshallingEncoder {

  private val LENGTH_PLACEHOLDER = ByteArray(4)

  private val marshaller = MarshallerCodecFactory.buildMarshalling()

  fun encode(msg: Any, out: ByteBuf) {
    marshaller.use { marshaller ->
      val lengthPos = out.writerIndex()
      out.writeBytes(LENGTH_PLACEHOLDER)
      val output = ChannelBufferByteOutput(out)
      marshaller.start(output)
      marshaller.writeObject(msg)
      marshaller.finish()
      out.setInt(lengthPos, out.writerIndex() - lengthPos - 4)
    }
  }
}