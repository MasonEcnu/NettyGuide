/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mason.netty.guide.advanced.ch12.github.codec

import com.mason.netty.guide.advanced.ch12.github.struct.Header
import com.mason.netty.guide.advanced.ch12.github.struct.NettyMessage
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

import java.io.IOException
import java.util.HashMap

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class NettyMessageDecoder @Throws(IOException::class)
constructor(maxFrameLength: Int, lengthFieldOffset: Int,
            lengthFieldLength: Int) : LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength) {

  private var marshallingDecoder: MarshallingDecoder = MarshallingDecoder()

  @Throws(Exception::class)
  override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf): Any? {
    val frame = super.decode(ctx, `in`) as ByteBuf ?: return null

    val message = NettyMessage()
    val header = Header()
    header.crcCode = frame.readInt()
    header.length = frame.readInt()
    header.sessionID = frame.readLong()
    header.type = frame.readInt()
    header.priority = frame.readInt()

    val size = frame.readInt()
    if (size > 0) {
      val attach = HashMap<String, Any>(size)
      var keySize: Int
      var keyArray: ByteArray?
      var key: String?
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
    message.header = header
    return message
  }
}
