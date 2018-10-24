package com.mason.netty.guide.advanced.ch12.self.codec

import io.netty.buffer.ByteBuf
import org.jboss.marshalling.ByteOutput

/**
 * Created by mwu on 2018/10/23
 */
class ChannelBufferByteOutput(val buffer: ByteBuf) : ByteOutput {
  override fun write(b: ByteArray) {
    buffer.writeBytes(b)
  }

  override fun write(b: ByteArray, off: Int, len: Int) {
    buffer.writeBytes(b, off, len)
  }

  override fun flush() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun close() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun write(b: Int) {
    buffer.writeByte(b)
  }
}