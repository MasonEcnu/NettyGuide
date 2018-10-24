package com.mason.netty.guide.advanced.ch12.self.handler

import com.mason.netty.guide.advanced.ch12.self.Header
import com.mason.netty.guide.advanced.ch12.self.MsgType
import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Created by mwu on 2018/10/23
 * 心跳请求处理
 */
class HeartBeatReqHandler : ChannelInboundHandlerAdapter() {

  @Volatile
  private var heartBeat: ScheduledFuture<*>? = null

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    msg as NettyMessage
    // 握手成功，主动发送心跳信息
    when {
      msg.header.type == MsgType.LOGIN_RESP.id -> {
        heartBeat = ctx.executor().scheduleAtFixedRate(HeartBeatTask(ctx), 0, 5, TimeUnit.SECONDS)
      }
      msg.header.type == MsgType.PONG.id -> {
        println("Client receives netty.guide.ch12.server heart beat message: --> $msg")
      }
      else -> {
        ctx.fireChannelRead(msg)
      }
    }
  }

  private class HeartBeatTask(val ctx: ChannelHandlerContext) : Runnable {
    override fun run() {
      val heartBeat = buildHeartBeat()
      println("Client send heart beat message to netty.guide.ch12.server: --> $heartBeat")
      ctx.writeAndFlush(heartBeat)
    }

    private fun buildHeartBeat(): NettyMessage {
      val header = Header(type = MsgType.PING.id)
      return NettyMessage(header = header)
    }
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    if (heartBeat != null) {
      heartBeat?.cancel(true)
      heartBeat = null
    }
    ctx.fireExceptionCaught(cause)
  }
}