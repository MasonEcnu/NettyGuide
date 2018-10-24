package com.mason.netty.guide.advanced.ch12.self.client

import com.mason.netty.guide.advanced.ch12.self.NettyConstant
import com.mason.netty.guide.advanced.ch12.self.codec.NettyMessageDecoder
import com.mason.netty.guide.advanced.ch12.self.codec.NettyMessageEncoder
import com.mason.netty.guide.advanced.ch12.self.handler.HeartBeatReqHandler
import com.mason.netty.guide.advanced.ch12.self.handler.LoginAuthReqHandler
import com.mason.netty.guide.printError
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by mwu on 2018/10/24
 * 客户端代码
 */
object NettyClient {

  private val executor = Executors.newScheduledThreadPool(1)

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
              ch.pipeline()
                  .addLast("MessageDecoder", NettyMessageDecoder(1024 * 1024, 4, 4))
                  .addLast("MessageEncoder", NettyMessageEncoder())
                  .addLast("ReadTimeOutHandler", ReadTimeoutHandler(30))
                  .addLast("LoginAuthHandler", LoginAuthReqHandler())
                  .addLast("HeartBeatHandler", HeartBeatReqHandler())
            }
          })
      // 发起异步连接操作
      val future = boot.connect(
          InetSocketAddress(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT),
          InetSocketAddress(NettyConstant.LOCAL_IP, NettyConstant.LOCAL_PORT)
      ).sync()

      future.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
      // 所有资源释放完成后，清空资源，再次发起重练操作
      executor.execute {
        try {
          TimeUnit.SECONDS.sleep(5)
          try {
            connect()
          } catch (e: Exception) {
            printError(e)
          }
        } catch (ie: InterruptedException) {
          printError(ie)
        }
      }
    }
  }
}
