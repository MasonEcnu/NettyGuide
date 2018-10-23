package com.mason.netty.guide.middle.ch9

import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import com.mason.proto.ProtoSubscribeReq
import com.mason.proto.ProtoSubscribeResp
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.marshalling.DefaultMarshallerProvider
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider
import io.netty.handler.codec.marshalling.MarshallingDecoder
import io.netty.handler.codec.marshalling.MarshallingEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.jboss.marshalling.*

/**
 * Created by mwu on 2018/10/15
 * JBoss Marshalling
 */
object SebReqServer {

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
          .childHandler(SebReqServerInitializer())

      // 绑定端口，同步等待成功
      val future = serverBoot.bind(DEFAULT_PORT).sync()
      // 等待服务端监听端口关闭
      future.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  private class SebReqServerInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val pipeline = ch.pipeline()
      pipeline.addLast(MarshallerCodeFactory.buildMarshallingDecoder())
          .addLast((MarshallerCodeFactory.buildMarshallingEncoder()))
          .addLast(SubReqServerHandler())
    }
  }

  private class SubReqServerHandler : ChannelInboundHandlerAdapter() {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
      println("handlerAdded: ${ctx.name()}")
    }

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

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      printError(cause)
      ctx.close()
    }
  }
}