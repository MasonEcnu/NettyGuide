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
package com.mason.netty.guide.advanced.ch12.github.server

import com.mason.netty.guide.advanced.ch12.github.NettyConstant
import com.mason.netty.guide.advanced.ch12.github.codec.NettyMessageDecoder
import com.mason.netty.guide.advanced.ch12.github.codec.NettyMessageEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler

import java.io.IOException

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
class NettyServer {

  @Throws(Exception::class)
  fun bind() {
    // 配置服务端的NIO线程组
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val b = ServerBootstrap()
    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel::class.java)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(LoggingHandler(LogLevel.INFO))
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          @Throws(IOException::class)
          public override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(NettyMessageDecoder(1024 * 1024, 4, 4))
            ch.pipeline().addLast(NettyMessageEncoder())
            ch.pipeline().addLast("readTimeoutHandler", ReadTimeoutHandler(50))
            ch.pipeline().addLast(LoginAuthRespHandler())
            ch.pipeline().addLast("HeartBeatHandler", HeartBeatRespHandler())
          }
        })

    // 绑定端口，同步等待成功
    b.bind(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT).sync()
    println("Netty netty.guide.ch12.server start ok : " + (NettyConstant.REMOTE_IP + " : " + NettyConstant.REMOTE_PORT))
  }

  companion object {

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
      NettyServer().bind()
    }
  }
}
