package com.mason.netty.guide.basic.ch3

import com.mason.netty.guide.BAD_ORDER
import com.mason.netty.guide.QUERY_ORDER
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.*

/**
 * Created by mwu on 2018/10/4
 * Netty入门 -- Server
 */
object TimeServer {
  @JvmStatic
  fun main(args: Array<String>) {
    bind()
  }

  private fun bind() {
    /**
     * 配置服务端NIO线程组
     * 包含一组专门用于网络事件处理的线程
     * Reactor线程组
     * 其中，boosGroup用于接收客户端连接
     * workerGroup用于SocketChannel的网路读写
     */
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
      /**
       * ServerBootstrap是启动netty服务的启动类
       */
      val serverBoot = ServerBootstrap()
      serverBoot.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel::class.java)
          .option(ChannelOption.SO_BACKLOG, 1024)
          .childHandler(ChildChannelHandler())

      /**
       * 绑定端口，同步等待成功
       * ChannelFuture用于异步操作的通知回调
       */

      val future = serverBoot.bind(DEFAULT_PORT).sync()

      if (future.isSuccess) {
        println("The time netty.guide.ch12.server is started at DEFAULT_PORT: $DEFAULT_PORT")
      }

      /**
       * 等待服务器监听端口关闭
       * sync()方法用于阻塞main函数
       */
      future.channel().closeFuture().sync()
    } finally {
      // 优雅的退出并释放相关资源
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  private class ChildChannelHandler : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      ch.pipeline().addLast(TimeServerHandler())
    }
  }

  private class TimeServerHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val buf = msg as ByteBuf
      val req = ByteArray(buf.readableBytes())
      // 将缓冲区中的字节数组复制到req中
      buf.readBytes(req)
      val body = String(req, Charsets.UTF_8)
      println("The time netty.guide.ch12.server receives order: $body")
      val currentTime = if (QUERY_ORDER.equals(body, true)) Date().toString() else BAD_ORDER
      val resp = Unpooled.copiedBuffer(currentTime.toByteArray())
      ctx.write(resp)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
      /**
       * ctx.write()方法将待发送的消息发送到缓冲数组中
       * ctx.flush()方法将发送缓冲区中的消息全部写到SocketChannel中
       */
      ctx.flush()
    }

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      ctx.close()
      printError(cause)
    }
  }
}