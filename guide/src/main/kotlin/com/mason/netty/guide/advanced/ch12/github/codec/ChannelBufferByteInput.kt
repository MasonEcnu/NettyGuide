/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.mason.netty.guide.advanced.ch12.github.codec

import io.netty.buffer.ByteBuf
import org.jboss.marshalling.ByteInput

import java.io.IOException
import kotlin.experimental.and

/**
 * [ByteInput] implementation which reads its data from a [ByteBuf]
 */
internal class ChannelBufferByteInput(private val buffer: ByteBuf) : ByteInput {

  @Throws(IOException::class)
  override fun close() {
    // nothing to do
  }

  @Throws(IOException::class)
  override fun available(): Int {
    return buffer.readableBytes()
  }

  @Throws(IOException::class)
  override fun read(): Int {
    return if (buffer.isReadable) {
      buffer.readByte().toInt().and(0xff)
    } else -1
  }

  @Throws(IOException::class)
  override fun read(array: ByteArray): Int {
    return read(array, 0, array.size)
  }

  @Throws(IOException::class)
  override fun read(dst: ByteArray, dstIndex: Int, length: Int): Int {
    var length = length
    val available = available()
    if (available == 0) {
      return -1
    }

    length = Math.min(available, length)
    buffer.readBytes(dst, dstIndex, length)
    return length
  }

  @Throws(IOException::class)
  override fun skip(bytes: Long): Long {
    var bytes = bytes
    val readable = buffer.readableBytes()
    if (readable < bytes) {
      bytes = readable.toLong()
    }
    buffer.readerIndex((buffer.readerIndex() + bytes).toInt())
    return bytes
  }

}
