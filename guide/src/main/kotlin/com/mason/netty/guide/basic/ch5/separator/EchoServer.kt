package com.mason.netty.guide.basic.ch5.separator

import com.mason.netty.guide.BUFFER_CACHE_SIZE
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.SEPARATOR
import com.mason.netty.guide.printError
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.net.BindException

/**
 * Created by mwu on 2018/10/5
 */
object EchoServer {

  @JvmStatic
  fun main(args: Array<String>) {
    try {
      bind()
    } catch (be: BindException) {
      printError(be)
    }
  }

  private fun bind() {
    // 配置服务端NIO线程组
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
      val serverBoot = ServerBootstrap()
      serverBoot.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel::class.java)
          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(LoggingHandler(LogLevel.INFO))
          .childHandler(EchoServerInitializer())

      // 绑定端口，同步等待成功
      val future = serverBoot.bind(DEFAULT_PORT).sync()
      // 等待服务端监听端口关闭
      future.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  /**
   * DelimiterBasedFrameDecoder：单条消息最大长度BUFFER_CACHE_SIZE
   * 超出则报TooLongFrameException
   */
  private class EchoServerInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val delimiter = Unpooled.copiedBuffer(SEPARATOR.toByteArray())
      val pipeline = ch.pipeline()
      pipeline.addLast(DelimiterBasedFrameDecoder(BUFFER_CACHE_SIZE, delimiter))
          .addLast(StringDecoder())
          .addLast(EchoServerHandler())
    }

  }

  private class EchoServerHandler : ChannelInboundHandlerAdapter() {
    private var counter = 0

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      var body = msg as String
      println("This is the ${++counter} times when the netty.guide.ch12.server receives netty.guide.ch12.client: [$body]")
      body += SEPARATOR
      val echo = Unpooled.copiedBuffer(body.toByteArray())
      ctx.writeAndFlush(echo)
    }

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      printError(cause)
      ctx.close()
    }
  }
}