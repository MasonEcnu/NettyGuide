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
import org.jboss.marshalling.ByteOutput

import java.io.IOException

/**
 * [ByteOutput] implementation which writes the data to a [ByteBuf]
 */
internal class ChannelBufferByteOutput
/**
 * Create a new instance which use the given [ByteBuf]
 */
(
    /**
     * Return the [ByteBuf] which contains the written content
     */
    val buffer: ByteBuf) : ByteOutput {

  @Throws(IOException::class)
  override fun close() {
    // Nothing to do
  }

  @Throws(IOException::class)
  override fun flush() {
    // nothing to do
  }

  @Throws(IOException::class)
  override fun write(b: Int) {
    buffer.writeByte(b)
  }

  @Throws(IOException::class)
  override fun write(bytes: ByteArray) {
    buffer.writeBytes(bytes)
  }

  @Throws(IOException::class)
  override fun write(bytes: ByteArray, srcIndex: Int, length: Int) {
    buffer.writeBytes(bytes, srcIndex, length)
  }
}
