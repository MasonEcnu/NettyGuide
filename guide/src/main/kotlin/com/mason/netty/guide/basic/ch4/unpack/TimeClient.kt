package com.mason.netty.guide.basic.ch4.unpack

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
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder

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
              val pipeline = ch.pipeline()
              pipeline.addLast(LineBasedFrameDecoder(1024))
                  .addLast(StringDecoder())
                  .addLast(TimeClientHandler())
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

  private class TimeClientHandler : ChannelHandlerAdapter() {

    private var counter = 0

    private var req: ByteArray = (QUERY_ORDER + System.getProperty("line.separator")).toByteArray()

    /**
     * 当客户端与服务端建立连接成功后
     * NIO线程会调用该方法
     * 发送查询时间的指令给服务端
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
      var message: ByteBuf
      /**
       * client循环发送100次消息
       * 理论上server应该受到100条消息
       * 并回复100次时间
       * 实际上，client和server都发生粘包现象1
       */
      (0 until 10).forEach { _ ->
        message = Unpooled.buffer(req.size)
        message.writeBytes(req)
        ctx.writeAndFlush(message)
      }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val body = msg as String
      println("Now is: $body; the counter is: ${++counter}")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      ctx.close()
      printError(cause)
    }

  }
}