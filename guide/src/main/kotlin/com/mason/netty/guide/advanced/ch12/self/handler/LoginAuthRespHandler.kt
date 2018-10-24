package com.mason.netty.guide.advanced.ch12.self.handler

import com.mason.netty.guide.advanced.ch12.self.Header
import com.mason.netty.guide.advanced.ch12.self.MsgType
import com.mason.netty.guide.advanced.ch12.self.NettyMessage
import com.mason.netty.guide.printError
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by mwu on 2018/10/23
 */
class LoginAuthRespHandler : ChannelInboundHandlerAdapter() {

  private val nodeCheck = ConcurrentHashMap<String, Boolean>()

  private val whiteList = arrayOf("127.0.0.1", "172.25.49.71")

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    msg as NettyMessage
    // 如果是握手请求消息，处理，其他消息透传
    if (msg.header.type == MsgType.LOGIN_REQ.id) {
      val nodeIndex = ctx.channel().remoteAddress().toString()
      val loginResp: NettyMessage
      // 重复登录，拒绝
      if (nodeCheck.containsKey(nodeIndex)) {
        loginResp = buildResponse(-1)
      } else {
        val address = ctx.channel().remoteAddress() as InetSocketAddress
        val ip = address.address.hostAddress
        var isOk = false
        for (wip in whiteList) {
          if (wip == ip) {
            isOk = true
            break
          }
        }
        loginResp = if (isOk) buildResponse(0) else buildResponse(-1)
        if (isOk) nodeCheck[nodeIndex] = true
      }
      println("The login response is: $loginResp, body [${loginResp.body}]")
    } else {
      ctx.fireChannelRead(msg)
    }
  }

  private fun buildResponse(code: Int): NettyMessage {
    val message = NettyMessage()
    message.header = Header(type = MsgType.LOGIN_RESP.id)
    message.body = code
    return message
  }

  @Suppress("OverridingDeprecatedMember")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    nodeCheck.remove(ctx.channel().remoteAddress().toString())
    printError(cause)
    ctx.close()
    ctx.fireExceptionCaught(cause)
  }
}