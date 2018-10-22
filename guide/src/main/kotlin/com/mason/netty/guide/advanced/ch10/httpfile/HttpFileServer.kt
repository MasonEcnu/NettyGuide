package com.mason.netty.guide.advanced.ch10.httpfile

import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * Created by mwu on 2018/10/19
 */
object HttpFileServer {

  private const val DEFAULT_URL = "/guide/src/"

  @JvmStatic
  fun main(args: Array<String>) {
    run(DEFAULT_URL)
  }

  private fun run(url: String) {
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
      val serverBoot = ServerBootstrap()
      serverBoot.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel::class.java)
          .childHandler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
              /**
               * HttpObjectAggregator：作用是将多个消息转换为单一的FullHttpRequest或FullHttpResponse
               * 因为HTTP解码器在每个HTTP消息中会生成多个消息对象
               * 1.HttpRequest/HttpResponse
               * 2.HttpContent
               * 3.LastHttpContent
               *
               * ChunkedWriteHandler：支持异步发送大码流
               * 占用较少内存，防止Java内存溢出
               */
              ch.pipeline().addLast("http-decoder", HttpRequestDecoder())
                  .addLast("http-aggregator", HttpObjectAggregator(65536))
                  .addLast("http-encoder", HttpResponseEncoder())
                  .addLast("http-chunked", ChunkedWriteHandler())
                  .addLast("fileServerHandler", HttpFileServerHandler(url))
            }
          })

      val future = serverBoot.bind(DEFAULT_HOST, DEFAULT_PORT).sync()

      println("HTTP文件目录服务器启动，网址是：http://$DEFAULT_HOST:$DEFAULT_PORT$url")
      future.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}