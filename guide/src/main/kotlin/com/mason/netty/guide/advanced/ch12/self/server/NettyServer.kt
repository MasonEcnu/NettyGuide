package com.mason.netty.guide.advanced.ch12.self.server

import com.mason.netty.guide.advanced.ch12.self.NettyConstant
import com.mason.netty.guide.advanced.ch12.self.codec.NettyMessageDecoder
import com.mason.netty.guide.advanced.ch12.self.codec.NettyMessageEncoder
import com.mason.netty.guide.advanced.ch12.self.handler.HeartBeatRespHandler
import com.mason.netty.guide.advanced.ch12.self.handler.LoginAuthRespHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler

/**
 * Created by mwu on 2018/10/24
 */
object NettyServer {

  @JvmStatic
  fun main(args: Array<String>) {
    bind()
  }

  private fun bind() {
    // 配置服务端线程组
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val serverBoot = ServerBootstrap()
    serverBoot.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(LoggingHandler(LogLevel.INFO))
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          override fun initChannel(ch: SocketChannel) {
            ch.pipeline()
                .addLast("MessageDecoder", NettyMessageDecoder(1024 * 1024, 4, 4))
                .addLast("MessageEncoder", NettyMessageEncoder())
                .addLast("ReadTimeOutHandler", ReadTimeoutHandler(30))
                .addLast("LoginAuthHandler", LoginAuthRespHandler())
                .addLast("HeartBeatHandler", HeartBeatRespHandler())
          }
        })
    // 绑定端口，同步等待成功
    serverBoot.bind(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT).sync()

    println("Netty netty.guide.ch12.server start ok: [${NettyConstant.REMOTE_IP}:${NettyConstant.REMOTE_PORT}]")

//    future.channel().closeFuture().sync()
  }
}