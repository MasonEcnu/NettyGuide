/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mason.netty.guide.advanced.ch12.github.client

import com.mason.netty.guide.advanced.ch12.github.NettyConstant
import com.mason.netty.guide.advanced.ch12.github.codec.NettyMessageDecoder
import com.mason.netty.guide.advanced.ch12.github.codec.NettyMessageEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class NettyClient {

  private val executor = Executors
      .newScheduledThreadPool(1)
  internal var group: EventLoopGroup = NioEventLoopGroup()

  @Throws(Exception::class)
  fun connect(port: Int, host: String) {

    // 配置客户端NIO线程组

    try {
      val b = Bootstrap()
      b.group(group).channel(NioSocketChannel::class.java)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            public override fun initChannel(ch: SocketChannel) {
              ch.pipeline().addLast(NettyMessageDecoder(1024 * 1024, 4, 4))
              ch.pipeline().addLast("MessageEncoder", NettyMessageEncoder())
              ch.pipeline().addLast("readTimeoutHandler", ReadTimeoutHandler(50))
              ch.pipeline().addLast("LoginAuthHandler", LoginAuthReqHandler())
              ch.pipeline().addLast("HeartBeatHandler", HeartBeatReqHandler())
            }
          })
      // 发起异步连接操作
      val future = b.connect(
          InetSocketAddress(host, port),
          InetSocketAddress(NettyConstant.LOCAL_IP,
              NettyConstant.LOCAL_PORT)).sync()
      future.channel().closeFuture().sync()
    } finally {
      // 所有资源释放完成之后，清空资源，再次发起重连操作
      executor.execute {
        try {
          TimeUnit.SECONDS.sleep(1)
          try {
            connect(NettyConstant.REMOTE_PORT, NettyConstant.REMOTE_IP)// 发起重连操作
          } catch (e: Exception) {
            e.printStackTrace()
          }

        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
  }

  companion object {

    /**
     * @param args
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
      NettyClient().connect(NettyConstant.REMOTE_PORT, NettyConstant.REMOTE_IP)
    }
  }

}
