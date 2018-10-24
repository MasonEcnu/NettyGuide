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
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class LoginAuthReqHandler : ChannelHandlerAdapter() {

  /**
   * Calls [ChannelHandlerContext.fireChannelActive] to forward to the
   * next [ChannelHandler] in the [ChannelPipeline].
   *
   *
   * Sub-classes may override this method to change behavior.
   */
  @Throws(Exception::class)
  fun channelActive(ctx: ChannelHandlerContext) {
    ctx.writeAndFlush(buildLoginReq())
  }

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

    // 如果是握手应答消息，需要判断是否认证成功
    if (message.header != null && message.header?.type == MessageType.LOGIN_RESP.value) {
      val loginResult = message.body as Int
      if (loginResult != 0) {
        // 握手失败，关闭连接
        ctx.close()
      } else {
        println("Login is ok : $message")
        ctx.fireChannelRead(msg)
      }
    } else
      ctx.fireChannelRead(msg)
  }

  private fun buildLoginReq(): NettyMessage {
    val header = Header()
    header.type = MessageType.LOGIN_REQ.value
    return NettyMessage().apply {
      this.header = header
    }
  }

  @Suppress("OverridingDeprecatedMember")
  @Throws(Exception::class)
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.fireExceptionCaught(cause)
  }
}
