package com.mason.netty.guide.middle.ch9

import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import com.mason.proto.ProtoSubscribeReq
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Created by mwu on 2018/10/15
 * JBoss Marshalling
 * todo 服务端收不到信息
 */
object SubReqClient {
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
          .handler(SubReqClientInitializer())

      // 发起异步连接操作
      val future = boot.connect(DEFAULT_HOST, DEFAULT_PORT).sync()

      // 等待客户端链路关闭
      future.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }

  private class SubReqClientInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      pipeline.addLast(MarshallerCodeFactory.buildMarshallingDecoder())
          .addLast(MarshallerCodeFactory.buildMarshallingEncoder())
          .addLast(SubReqClientHandler())
    }
  }

  private class SubReqClientHandler : ChannelHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
      (0 until 10).forEach { it ->
        ctx.write(subReq(it))
      }
      ctx.flush()
    }

    private fun subReq(it: Int): ProtoSubscribeReq.SubscribeReq {
      val builder = ProtoSubscribeReq.SubscribeReq.newBuilder()
      builder.subReqId = it
      builder.userName = "Mason"
      builder.productName = "Netty Book For Marshalling"
      val addressList = arrayListOf<String>()
      addressList.add("Shanghai")
      addressList.add("Beijing")
      addressList.add("Xi\'an")
      builder.addAllAddress(addressList)
      return builder.build()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      println("Receive server response: \n[\n$msg]")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
      ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      printError(cause)
      ctx.close()
    }
  }
}