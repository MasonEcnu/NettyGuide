package com.mason.netty.guide.middle.ch7.unpack

import com.mason.netty.guide.BUFFER_CACHE_SIZE
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.codec.MsgpackDecoder
import com.mason.netty.guide.codec.MsgpackEncoder
import com.mason.netty.guide.printError
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.io.IOException
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
          .option(ChannelOption.SO_BACKLOG, BUFFER_CACHE_SIZE)
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
      val pipeline = ch.pipeline()
      pipeline
          .addLast("frame decoder", LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))
          .addLast("msgpack decoder", MsgpackDecoder())
          .addLast("frame encoder", LengthFieldPrepender(2))
          .addLast("msgpack encoder", MsgpackEncoder())
          .addLast(EchoServerHandler())
    }

  }

  private class EchoServerHandler : ChannelHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      println("Server receives the msgpack message: [$msg]")
      val str = "Hello World"
      val echo = Unpooled.copiedBuffer(str.toByteArray())
      ctx.writeAndFlush(echo)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      printError(cause)
      ctx.close()
    }
  }
}