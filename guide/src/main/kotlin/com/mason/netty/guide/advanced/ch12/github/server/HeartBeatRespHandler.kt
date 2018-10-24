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
package com.mason.netty.guide.advanced.ch12.github.server

import com.mason.netty.guide.advanced.ch12.github.struct.NettyMessage
import com.mason.netty.guide.advanced.ch12.github.MessageType
import com.mason.netty.guide.advanced.ch12.github.struct.Header
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class HeartBeatRespHandler : ChannelHandlerAdapter() {
  @Throws(Exception::class)
  fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val message = msg as NettyMessage
    // 返回心跳应答消息
    if (message.header != null && message.header?.type == MessageType.HEARTBEAT_REQ.value) {
      println("Receive netty.guide.ch12.client heart beat message : ---> $message")
      val heartBeat = buildHeatBeat()
      println("Send heart beat response message to netty.guide.ch12.client : ---> $heartBeat")
      ctx.writeAndFlush(heartBeat)
    } else {
      ctx.fireChannelRead(msg)
    }

  }

  private fun buildHeatBeat(): NettyMessage {
    val message = NettyMessage()
    val header = Header()
    header.type = MessageType.HEARTBEAT_RESP.value
    message.header = header
    return message
  }

}
