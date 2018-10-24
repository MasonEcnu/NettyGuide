package com.mason.netty.guide.basic.ch4.unpack

import com.mason.netty.guide.BAD_ORDER
import com.mason.netty.guide.QUERY_ORDER
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
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

  /**
   * LineBasedFrameDecoder：遍历ByteBuf中的可读字节
   * 判断是否有"\n"或者"\r\n"，如果有，就以此位置为结束位置
   * 传入参数为最大单行长度，若读取字节超过该长度
   * 则抛出异常，并抛弃之前读到的字节
   *
   * LineBasedFrameDecoder + StringDecoder：按行切换的文本解码器
   */
  private class ChildChannelHandler : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      pipeline.addLast(LineBasedFrameDecoder(1024))
          .addLast(StringDecoder())
          .addLast(TimeServerHandler())

    }
  }

  private class TimeServerHandler : ChannelInboundHandlerAdapter() {

    private var counter = 0

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val body = msg as String
      println("The time netty.guide.ch12.server receives order: $body; the counter is: ${++counter}")
      val currentTime = (if (QUERY_ORDER.equals(body, true)) Date().toString() else BAD_ORDER) + System.getProperty("line.separator")
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