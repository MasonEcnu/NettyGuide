package com.mason.netty.guide.advanced.ch12.self.handler

import com.mason.netty.guide.advanced.ch12.self.Header
import com.mason.netty.guide.advanced.ch12.self.MsgType
import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import com.mason.netty.guide.printError
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Created by mwu on 2018/10/23
 */
class LoginAuthReqHandler : ChannelInboundHandlerAdapter() {

  override fun channelActive(ctx: ChannelHandlerContext) {
    ctx.writeAndFlush(buildLoginReq())
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    msg as NettyMessage
    // 如果是握手应答消息，需要判断是否认证成功
    if (msg.header.type == MsgType.LOGIN_RESP.id) {
      val loginResult = msg.body as Int
      if (loginResult != 0) {
        // 握手失败，关闭连接
        println("Hand shake failed")
        ctx.close()
      } else {
        println("Login is Ok: $msg")
        ctx.fireChannelRead(msg)
      }
    }
  }

  private fun buildLoginReq(): NettyMessage {
    val header = Header(type = MsgType.LOGIN_REQ.id)
    return NettyMessage(header = header)
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    printError(cause)
    ctx.fireExceptionCaught(cause)
  }
}