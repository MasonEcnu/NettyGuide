package com.mason.netty.guide.advanced.ch11

import com.mason.netty.guide.DEFAULT_PORT
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * Created by mwu on 2018/10/22
 */
object WebSocketServer {
  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }

  private fun run() {
    // 配置服务端NIO线程组
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
      val serverBoot = ServerBootstrap()
      serverBoot.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel::class.java)
          .childHandler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
              val pipeline = ch.pipeline()
              pipeline.addLast("http-codec", HttpServerCodec())
                  .addLast("aggregator", HttpObjectAggregator(65536))
                  // ChunkedWriteHandler：用于支持客户端和服务端的WebSocket通信
                  .addLast("http-chunked", ChunkedWriteHandler())
                  .addLast(WebSocketServerHandler())
            }
          })

      // 绑定端口
      val future = serverBoot.bind(DEFAULT_PORT).sync()

      println("Web socket server started at port: $DEFAULT_PORT")

      println("Open your browser and navigate to http://localhost:$DEFAULT_PORT/")

      // 等待服务端断开连接
      future.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}