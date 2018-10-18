package com.mason.netty.guide.middle.ch7.stick

import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.codec.MsgpackDecoder
import com.mason.netty.guide.codec.MsgpackEncoder
import com.mason.netty.guide.printError
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.msgpack.annotation.Message
import java.net.ConnectException

/**
 * Created by mwu on 2018/10/5
 * Msgpack
 */
object EchoClient {

  @JvmStatic
  fun main(args: Array<String>) {
    try {
      start(10)
    } catch (be: ConnectException) {
      printError(be)
    }
  }

  private fun start(sendNumber: Int) {
    // 配置客户端NIO线程组
    val group = NioEventLoopGroup()

    try {
      val boot = Bootstrap()
      boot.group(group)
          .channel(NioSocketChannel::class.java)
          .option(ChannelOption.TCP_NODELAY, true)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
          .handler(EchoClientInitializer(sendNumber))

      // 绑定端口，同步等待成功
      val future = boot.connect(DEFAULT_HOST, DEFAULT_PORT).sync()
      // 等待服务端监听端口关闭
      future.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }

  private class EchoClientInitializer(private val sendNumber: Int) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      pipeline.addLast("msgpack decoder", MsgpackDecoder())
          .addLast("msgpack encoder", MsgpackEncoder())
          .addLast(EchoClientHandler(sendNumber))
    }
  }

  private class EchoClientHandler(private val sendNumber: Int) : ChannelHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
      val infos = getUserInfos()
      // 有丢包。。好神奇！
      for (user in infos) {
        ctx.writeAndFlush(user)
      }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      println("Client receives the msgpack message: $msg")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
      ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      ctx.close()
      printError(cause)
    }

    private fun getUserInfos(): Array<User> {
      val userInfos = Array(size = sendNumber, init = { User() })
      for (i in (0 until sendNumber)) {
        userInfos[i] = User("ABC$i", i)
      }
      return userInfos
    }
  }

  @Message
  private data class User(var name: String = "", var age: Int = 0)
}