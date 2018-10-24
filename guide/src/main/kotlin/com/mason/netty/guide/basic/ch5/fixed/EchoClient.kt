package com.mason.netty.guide.basic.ch5.fixed

import com.mason.netty.guide.*
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.FixedLengthFrameDecoder
import io.netty.handler.codec.string.StringDecoder

/**
 * Created by mwu on 2018/10/5
 */
object EchoClient {
  @JvmStatic
  fun main(args: Array<String>) {
    connect()
  }

  private fun connect() {
    // 配置客户端NIO线程组
    val group = NioEventLoopGroup()
    try {
      val boot = Bootstrap()
      boot.group(group)
          .channel(NioSocketChannel::class.java)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(EchoClientInitializer())

      // 发起异步连接操作
      val future = boot.connect(DEFAULT_HOST, DEFAULT_PORT).sync()

      // 等待客户端链路关闭
      future.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }

  private class EchoClientInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      pipeline.addLast(FixedLengthFrameDecoder(20))
          .addLast(StringDecoder())
          .addLast(EchoClientHandler())
    }
  }

  private class EchoClientHandler : ChannelInboundHandlerAdapter() {

    private var counter = 0

    private val ECHO_REQ = "Hi, Mason. Welcome to Netty.$SEPARATOR"

    override fun channelActive(ctx: ChannelHandlerContext) {
      (0 until 10).forEach { _ ->
        val buffer = Unpooled.copiedBuffer(ECHO_REQ.toByteArray())
        ctx.writeAndFlush(buffer)
      }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      println("This is the ${++counter} times when netty.guide.ch12.client receives netty.guide.ch12.server: $msg")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
      ctx.flush()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      ctx.close()
      printError(cause)
    }
  }
}