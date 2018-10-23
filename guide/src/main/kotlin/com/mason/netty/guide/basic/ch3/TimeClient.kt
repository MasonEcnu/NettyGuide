package com.mason.netty.guide.basic.ch3

import com.mason.netty.guide.QUERY_ORDER
import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Created by mwu on 2018/10/4
 * Netty入门 -- Client
 */
object TimeClient {
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
          .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
              ch.pipeline().addLast(TimeClientHandler())
            }
          })

      // 发起异步连接操作
      val future = boot.connect(DEFAULT_HOST, DEFAULT_PORT).sync()

      // 等待客户端链路关闭
      future.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }

  private class TimeClientHandler : ChannelInboundHandlerAdapter() {

    private var firstMessage: ByteBuf

    init {
      val req = QUERY_ORDER.toByteArray()
      firstMessage = Unpooled.buffer(req.size)
      firstMessage.writeBytes(req)
    }

    /**
     * 当客户端与服务端建立连接成功后
     * NIO线程会调用该方法
     * 发送查询时间的指令给服务端
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
      ctx.writeAndFlush(firstMessage)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val buf = msg as ByteBuf
      val req = ByteArray(buf.readableBytes())
      buf.readBytes(req)
      val body = String(req, Charsets.UTF_8)
      println("Now is: $body")
    }

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      ctx.close()
      printError(cause)
    }

  }
}