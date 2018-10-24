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
package com.mason.netty.guide.advanced.ch12.github.client

import com.mason.netty.guide.advanced.ch12.github.MessageType
import com.mason.netty.guide.advanced.ch12.github.struct.Header
import com.mason.netty.guide.advanced.ch12.github.struct.NettyMessage
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class HeartBeatReqHandler : ChannelHandlerAdapter() {

  @Volatile
  private var heartBeat: ScheduledFuture<*>? = null

  @Throws(Exception::class)
  fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val message = msg as NettyMessage
    // 握手成功，主动发送心跳消息
    if (message.header != null && message.header?.type == MessageType.LOGIN_RESP.value) {
      heartBeat = ctx.executor().scheduleAtFixedRate(
          HeartBeatTask(ctx), 0, 5000,
          TimeUnit.MILLISECONDS)
    } else if (message.header != null && message.header?.type == MessageType.HEARTBEAT_RESP.value) {
      println("Client receive netty.guide.ch12.server heart beat message : ---> $message")
    } else
      ctx.fireChannelRead(msg)
  }

  private inner class HeartBeatTask(private val ctx: ChannelHandlerContext) : Runnable {

    override fun run() {
      val heatBeat = buildHeatBeat()
      println("Client send heart beat messsage to netty.guide.ch12.server : ---> $heatBeat")
      ctx.writeAndFlush(heatBeat)
    }

    private fun buildHeatBeat(): NettyMessage {
      val message = NettyMessage()
      val header = Header()
      header.type = MessageType.HEARTBEAT_REQ.value
      message.header = header
      return message
    }
  }

  @Throws(Exception::class)
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    if (heartBeat != null) {
      heartBeat!!.cancel(true)
      heartBeat = null
    }
    ctx.fireExceptionCaught(cause)
  }
}
