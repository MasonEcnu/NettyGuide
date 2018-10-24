package com.mason.netty.guide.advanced.ch12.self.codec

import io.netty.buffer.ByteBuf
import org.jboss.marshalling.ByteInput

/**
 * Created by mwu on 2018/10/23
 */
class ChannelBufferByteInput(val buffer: ByteBuf) : ByteInput {
  override fun skip(n: Long): Long {
    val readable = buffer.readableBytes()
    var result = n
    if (readable < n) {
      result = readable.toLong()
    }
    buffer.readerIndex((buffer.readerIndex() + result).toInt())
    return result
  }

  override fun available(): Int {
    return buffer.readableBytes()
  }

  override fun close() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun read(): Int {
    if (buffer.isReadable) {
      return buffer.readByte().toInt().and(0xff)
    }

    return -1
  }

  override fun read(b: ByteArray): Int {
    return read(b, 0, b.size)
  }

  override fun read(b: ByteArray, off: Int, len: Int): Int {
    val available = available()
    if (available == 0) {
      return -1
    }

    val length = Math.min(available, len)
    buffer.readBytes(b, off, length)
    return length
  }
}