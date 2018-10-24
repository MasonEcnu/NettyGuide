package com.mason.netty.guide.advanced.ch12.self.codec

import io.netty.buffer.ByteBuf

/**
 * Created by mwu on 2018/10/23
 * Netty消息编码工具类
 */
class MarshallingDecoder {

  private val unmarshaller = MarshallerCodecFactory.buildUnMarshalling()

  fun decode(bin: ByteBuf): Any {
    unmarshaller.use { unmarshaller ->
      val objectSize = bin.readInt()
      val buf = bin.slice(bin.readerIndex(), objectSize)
      val input = ChannelBufferByteInput(buf)
      unmarshaller.start(input)
      val obj = unmarshaller.readObject()
      unmarshaller.finish()
      bin.readerIndex(bin.readerIndex() + objectSize)
      return obj
    }
  }
}