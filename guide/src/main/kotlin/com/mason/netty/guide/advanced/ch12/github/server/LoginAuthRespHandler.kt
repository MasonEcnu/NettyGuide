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

import com.mason.netty.guide.advanced.ch12.github.MessageType
import com.mason.netty.guide.advanced.ch12.github.struct.Header
import com.mason.netty.guide.advanced.ch12.github.struct.NettyMessage
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class LoginAuthRespHandler : ChannelHandlerAdapter() {

  private val nodeCheck = ConcurrentHashMap<String, Boolean>()
  private val whitekList = arrayOf("127.0.0.1", "192.168.1.104")

  /**
   * Calls [ChannelHandlerContext.fireChannelRead] to forward to
   * the next [ChannelHandler] in the [ChannelPipeline].
   *
   *
   * Sub-classes may override this method to change behavior.
   */
  @Throws(Exception::class)
  fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val message = msg as NettyMessage

    // 如果是握手请求消息，处理，其它消息透传
    if (message.header != null && message.header?.type == MessageType.LOGIN_REQ.value) {
      val nodeIndex = ctx.channel().remoteAddress().toString()
      var loginResp: NettyMessage? = null
      // 重复登陆，拒绝
      if (nodeCheck.containsKey(nodeIndex)) {
        loginResp = buildResponse((-1).toByte())
      } else {
        val address = ctx.channel()
            .remoteAddress() as InetSocketAddress
        val ip = address.address.hostAddress
        var isOK = false
        for (WIP in whitekList) {
          if (WIP == ip) {
            isOK = true
            break
          }
        }
        loginResp = if (isOK) buildResponse(0.toByte()) else buildResponse((-1).toByte())
        if (isOK)
          nodeCheck[nodeIndex] = true
      }
      println("The login response is : " + loginResp
          + " body [" + loginResp.body + "]")
      ctx.writeAndFlush(loginResp)
    } else {
      ctx.fireChannelRead(msg)
    }
  }

  private fun buildResponse(result: Byte): NettyMessage {
    val message = NettyMessage()
    val header = Header()
    header.type = MessageType.LOGIN_RESP.value
    message.header = header
    message.body = result
    return message
  }

  @Suppress("OverridingDeprecatedMember")
  @Throws(Exception::class)
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    nodeCheck.remove(ctx.channel().remoteAddress().toString())// 删除缓存
    ctx.close()
    ctx.fireExceptionCaught(cause)
  }
}
