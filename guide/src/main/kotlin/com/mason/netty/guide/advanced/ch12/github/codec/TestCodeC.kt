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
import io.netty.buffer.Unpooled

import java.io.IOException
import java.util.HashMap

/**
 * @author Administrator
 * @version 1.0
 * @date 2014年3月15日
 */
class TestCodeC @Throws(IOException::class) constructor() {

  private var marshallingEncoder: MarshallingEncoder = MarshallingEncoder()
  private var marshallingDecoder: MarshallingDecoder = MarshallingDecoder()

  val message: NettyMessage
    get() {
      val nettyMessage = NettyMessage()
      val header = Header()
      header.length = 123
      header.sessionID = 99999L
      header.type = 1
      header.priority = 7
      val attachment = HashMap<String, Any>()
      for (i in 0..9) {
        attachment["ciyt --> $i"] = "lilinfeng $i"
      }
      header.attachment = attachment
      nettyMessage.header = header
      nettyMessage.body = "abcdefg-----------------------AAAAAA"
      return nettyMessage
    }

  @Throws(Exception::class)
  fun encode(msg: NettyMessage): ByteBuf {
    val sendBuf = Unpooled.buffer()
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
    sendBuf.setInt(4, sendBuf.readableBytes())
    return sendBuf
  }

  @Throws(Exception::class)
  fun decode(`in`: ByteBuf): NettyMessage {
    val message = NettyMessage()
    val header = Header()
    header.crcCode = `in`.readInt()
    header.length = `in`.readInt()
    header.sessionID = `in`.readLong()
    header.type = `in`.readInt()
    header.priority = `in`.readInt()

    val size = `in`.readInt()
    if (size > 0) {
      val attach = HashMap<String, Any>(size)
      var keySize: Int
      var keyArray: ByteArray?
      var key: String?
      for (i in 0 until size) {
        keySize = `in`.readInt()
        keyArray = ByteArray(keySize)
        `in`.readBytes(keyArray)
        key = String(keyArray, Charsets.UTF_8)
        attach[key] = marshallingDecoder.decode(`in`)
      }
      header.attachment = attach
    }
    if (`in`.readableBytes() > 4) {
      message.body = marshallingDecoder.decode(`in`)
    }
    message.header = header
    return message
  }

  companion object {

    /**
     * @param args
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
      val testC = TestCodeC()
      val message = testC.message
      println("$message [body ] " + message.body)

      for (i in 0..4) {
        val buf = testC.encode(message)
        val decodeMsg = testC.decode(buf)
        println("$decodeMsg [body ] " + decodeMsg.body)
        println("-------------------------------------------------")

      }

    }
  }

}
