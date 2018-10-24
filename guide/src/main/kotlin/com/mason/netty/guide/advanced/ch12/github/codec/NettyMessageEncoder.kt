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

import com.mason.netty.guide.advanced.ch12.github.struct.NettyMessage
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

import java.io.IOException

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
class NettyMessageEncoder @Throws(IOException::class)
constructor() : MessageToByteEncoder<NettyMessage>() {

  internal var marshallingEncoder: MarshallingEncoder

  init {
    this.marshallingEncoder = MarshallingEncoder()
  }

  @Throws(Exception::class)
  override fun encode(ctx: ChannelHandlerContext, msg: NettyMessage?, sendBuf: ByteBuf) {
    if (msg?.header == null)
      throw Exception("The encode message is null")
    sendBuf.writeInt(msg.header?.crcCode ?: 0)
    sendBuf.writeInt(msg.header?.length ?: 0)
    sendBuf.writeLong(msg.header?.sessionID ?: 0L)
    sendBuf.writeByte(msg.header?.type ?: 0)
    sendBuf.writeByte(msg.header?.priority ?: 0)
    sendBuf.writeInt(msg.header?.attachment?.size ?: 0)
    var key: String?
    var keyArray: ByteArray?
    var value: Any?
    for (param in msg.header?.attachment?.entries ?: emptySet()) {
      key = param.key
      keyArray = key.toByteArray(charset("UTF-8"))
      sendBuf.writeInt(keyArray.size)
      sendBuf.writeBytes(keyArray)
      value = param.value
      marshallingEncoder.encode(value, sendBuf)
    }
    if (msg.body != null) {
      marshallingEncoder.encode(msg.body ?: Any(), sendBuf)
    } else
      sendBuf.writeInt(0)
    sendBuf.setInt(4, sendBuf.readableBytes() - 8)
  }
}
