package com.mason.netty.guide.advanced.ch12.self.handler

import com.mason.netty.guide.advanced.ch12.self.Header
import com.mason.netty.guide.advanced.ch12.self.MsgType
import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import com.mason.netty.guide.printError
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Created by mwu on 2018/10/23
 * 心跳请求处理
 */
class HeartBeatRespHandler : ChannelInboundHandlerAdapter() {

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    msg as NettyMessage
    // 返回心跳应答消息
    when {
      msg.header.type == MsgType.PING.id -> {
        println("Receive netty.guide.ch12.client heart beat message --> $msg")
        val heartBeat = buildHeartBeat()
        println("Sent heart beat response message to netty.guide.ch12.client: --> $heartBeat")
        ctx.writeAndFlush(heartBeat)
      }
      else -> {
        ctx.fireChannelRead(msg)
      }
    }
  }

  private fun buildHeartBeat(): NettyMessage {
    val header = Header(type = MsgType.PING.id)
    return NettyMessage(header = header)
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    printError(cause)
    ctx.fireExceptionCaught(cause)
  }
}