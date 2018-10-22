package com.mason.netty.guide.advanced.ch11

import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaders.isKeepAlive
import io.netty.handler.codec.http.HttpHeaders.setContentLength
import io.netty.handler.codec.http.websocketx.*
import java.lang.UnsupportedOperationException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by mwu on 2018/10/22
 */
class WebSocketServerHandler : SimpleChannelInboundHandler<Any>() {

  private val logger = Logger.getLogger(WebSocketServerHandler::class.java.simpleName)

  private var handshaker: WebSocketServerHandshaker? = null

  override fun messageReceived(ctx: ChannelHandlerContext, msg: Any) {
    // 传统HTTP接入
    if (msg is FullHttpRequest) {
      handleHttpRequest(ctx, msg)
    }
    // WebSocket接入
    if (msg is WebSocketFrame) {
      handleWebSocketFrame(ctx, msg)
    }
  }

  private fun handleWebSocketFrame(ctx: ChannelHandlerContext, socketFrame: WebSocketFrame) {
    // 判断是否关闭链路指令
    if (socketFrame is CloseWebSocketFrame) {
      handshaker?.close(ctx.channel(), socketFrame.retain())
      return
    }
    // 判断是否Ping消息
    if (socketFrame is PingWebSocketFrame) {
      ctx.channel().write(PongWebSocketFrame(socketFrame.content().retain()))
      return
    }
    // 本例程仅支持文本消息，不支持二进制消息
    if (socketFrame !is TextWebSocketFrame) {
      throw UnsupportedOperationException("${socketFrame.javaClass.simpleName} frame type is not supported")
    }
    // 返回应答消息
    val request = socketFrame.text()
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("${ctx.channel()} received $request")
    }
    ctx.channel().write(
        TextWebSocketFrame("$request, 欢迎使用Netty Websocket服务， 现在时刻： ${Date()}")
    )
  }

  private fun handleHttpRequest(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    // 如果HTTP解析失败，返回HTTP异常
    if (!request.decoderResult.isSuccess || "websocket" != request.headers().get("Upgrade")) {
      sendHttpResponse(ctx, request, DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
      return
    }
    // 构造握手响应放回，本机测试
    val wsFactory = WebSocketServerHandshakerFactory("ws://localhost:$DEFAULT_PORT/websocket", null, false)
    handshaker = wsFactory.newHandshaker(request)
    if (handshaker == null) {
      WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel())
    } else {
      val future = handshaker?.handshake(ctx.channel(), request)
      // 握手成功，则动态添加WebSocket的decoder和encoder
      if (future != null && future.isSuccess) {
        val pipeline = ctx.pipeline()
        if (pipeline["wsdecoder"] == null && pipeline["wsencoder"] == null) {
          pipeline.addBefore(ctx.name(), "wsdecoder", WebSocket00FrameDecoder())
          pipeline.addBefore(ctx.name(), "wsencoder", WebSocket00FrameEncoder())
        }
      } else {
        sendHttpResponse(ctx, request, DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN))
      }
    }
  }

  private fun sendHttpResponse(ctx: ChannelHandlerContext, request: FullHttpRequest, response: FullHttpResponse) {
    // 返回应答给客户端
    if (response.status.code() != 200) {
      val buf = Unpooled.copiedBuffer(response.status.toString(), Charsets.UTF_8)
      response.content().writeBytes(buf)
      buf.release()
      setContentLength(response, response.content().readableBytes().toLong())
    }

    // 如果是非Keep-Alive，关闭连接
    val future = ctx.channel().writeAndFlush(response)
    if (!isKeepAlive(request) || response.status.code() != 200) {
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  override fun channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush()
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    printError(cause)
    ctx.close()
  }
}