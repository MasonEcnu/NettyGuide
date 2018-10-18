package com.mason.netty.guide.middle.ch8

import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import com.mason.proto.ProtoSubscribeReq
import com.mason.proto.ProtoSubscribeResp
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

/**
 * Created by mwu on 2018/10/10
 */
object SubReqServer {

  @JvmStatic
  fun main(args: Array<String>) {
    bind()
  }

  private fun bind() {
    // 配置服务端NIO线程组
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
      val serverBoot = ServerBootstrap()
      serverBoot.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel::class.java)
          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(LoggingHandler(LogLevel.INFO))
          .childHandler(SubReqServerInitializer())

      // 绑定端口，同步等待成功
      val future = serverBoot.bind(DEFAULT_PORT).sync()
      // 等待服务端监听端口关闭
      future.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  private class SubReqServerInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      /**
       * ProtobufVarint32FrameDecoder：处理半包
       * 如果不处理半包：InvalidProtocolBufferException
       * LengthFieldBasedFrameDecoder
       * ByteToMessageDecoder
       * 继承这两个类，自己处理半包问题
       */
      pipeline.addLast(ProtobufVarint32FrameDecoder())
          // ProtobufDecoder：解码，但不能处理半包问题
          .addLast(ProtobufDecoder(ProtoSubscribeReq.SubscribeReq.getDefaultInstance()))
          .addLast(ProtobufVarint32LengthFieldPrepender())
          .addLast(ProtobufEncoder())
          .addLast(SubReqServerHandler())
    }
  }

  private class SubReqServerHandler : ChannelHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val req = msg as ProtoSubscribeReq.SubscribeReq
      if ("Mason".equals(req.userName, true)) {
        println("Service accept client subscribe req: \n[\n$req]")
        ctx.writeAndFlush(resp(req.subReqId))
      }
    }

    private fun resp(subReqId: Int): ProtoSubscribeResp.SubscribeResp {
      val builder = ProtoSubscribeResp.SubscribeResp.newBuilder()
      builder.subReqId = subReqId
      builder.respCode = 0
      builder.desc = "Netty book order succeed, 3 days later, sent to the designated address"
      return builder.build()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      printError(cause)
      ctx.close()
    }
  }
}