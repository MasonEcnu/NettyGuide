package com.mason.netty.guide.basic.ch2.nio

import com.mason.netty.guide.DEFAULT_PORT
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * Created by mwu on 2018/10/3
 * NIO1.0 异步IO Server
 */
object TimeServer {
  @JvmStatic
  fun main(args: Array<String>) {
    val timeServer = MultiplexerTimeServer()
    Thread(timeServer, "NIO-MultiplexerTimeServer-001").start()
  }
}

class MultiplexerTimeServer : Runnable {

  private lateinit var selector: Selector

  private lateinit var serverChannel: ServerSocketChannel

  @Volatile
  private var stop: Boolean = false

  init {
    try {
      selector = Selector.open()
      serverChannel = ServerSocketChannel.open()
      serverChannel.configureBlocking(false)
      serverChannel.socket().bind(InetSocketAddress(DEFAULT_PORT), 1024)
      serverChannel.register(selector, SelectionKey.OP_ACCEPT)
      println("The time netty.guide.ch12.server is started at DEFAULT_PORT: $DEFAULT_PORT")
    } catch (ioe: IOException) {
      println("Something is wrong: ${ioe.message}")
      System.exit(1)
    }
  }

  fun stop() {
    stop = true
  }

  override fun run() {
    while (!stop) {
      try {
        selector.select(1000)
        val selectedKeys = selector.selectedKeys()
        val it = selectedKeys.iterator()
        var key: SelectionKey
        while (it.hasNext()) {
          key = it.next()
          it.remove()
          try {
            handleInput(key)
          } catch (ioe: IOException) {
            println("Something is wrong: ${ioe.message}")
            System.exit(1)
          }
        }
      } catch (t: Throwable) {
        println("Something is wrong: ${t.message}")
        System.exit(1)
      }
    }
    selector.close()
  }

  @Throws(IOException::class)
  private fun handleInput(key: SelectionKey) {
    if (key.isValid) {
      // 处理新接入的请求
      if (key.isAcceptable) {
        // 接收新请求
        val ssc = key.channel() as ServerSocketChannel
        val sc = ssc.accept()
        sc.configureBlocking(false)
        // 向selector中添加新连接
        sc.register(selector, SelectionKey.OP_READ)
      }
      if (key.isReadable) {
        // 读数据
        val sc = key.channel() as SocketChannel
        val readBuffer = ByteBuffer.allocate(1024)
        val readBytes = sc.read(readBuffer)
        if (readBytes > 0) {
          readBuffer.flip()
          val bytes = ByteArray(readBuffer.remaining())
          readBuffer.get(bytes)
          val body = String(bytes, Charsets.UTF_8)
          println("The time netty.guide.ch12.server receives order: $body")
          val currentTime = if ("QUERY TIME ORDER".equals(body.trim(), false)) System.currentTimeMillis().toString() else "Bad Order"
          doWrite(sc, currentTime)
        } else if (readBytes < 0) {
          // 对端链路关闭
          key.cancel()
          sc.close()
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun doWrite(channel: SocketChannel, response: String) {
    if (response.trim().isNotEmpty()) {
      val bytes = response.toByteArray()
      val writerBuffer = ByteBuffer.allocate(bytes.size)
      writerBuffer.put(bytes)
      writerBuffer.flip()
      channel.write(writerBuffer)
    }
  }
}