package com.mason.netty.guide.basic.ch2.nio

import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

/**
 * Created by mwu on 2018/10/3
 */
object TimeClient {
  @JvmStatic
  fun main(args: Array<String>) {
    Thread(TimeClientHandler()).start()
  }
}

class TimeClientHandler : Runnable {

  private lateinit var selector: Selector

  private lateinit var socketChannel: SocketChannel

  @Volatile
  private var stop: Boolean = false

  init {
    try {
      selector = Selector.open()
      socketChannel = SocketChannel.open()
      socketChannel.configureBlocking(false)
    } catch (ioe: IOException) {
      println("Something is wrong: ${ioe.message}")
      System.exit(1)
    }
  }

  private fun stop() {
    stop = true
  }

  override fun run() {
    try {
      doConnect()
    } catch (ioe: IOException) {
      println("Something is wrong: ${ioe.message}")
      System.exit(1)
    }

    while (!stop) {
      try {
        selector.select(1000)
        val selectedKey = selector.selectedKeys()
        val it = selectedKey.iterator()
        var key: SelectionKey
        while (it.hasNext()) {
          key = it.next()
          it.remove()
          try {
            handleInput(key)
          } catch (ioe: IOException) {
            key.cancel()
            socketChannel.close()
            println("Something is wrong: ${ioe.message}")
            System.exit(1)
          }
        }
      } catch (ioe: IOException) {
        println("Something is wrong: ${ioe.message}")
        System.exit(1)
      }
    }
    selector.close()
  }

  private fun handleInput(key: SelectionKey) {
    if (key.isValid) {
      val sc = key.channel() as SocketChannel
      if (key.isConnectable) {
        if (sc.finishConnect()) {
          sc.register(selector, SelectionKey.OP_READ)
          doWrite()
        } else {
          System.exit(1)
        }
        if (key.isReadable) {
          // 读数据
          val readBuffer = ByteBuffer.allocate(1024)
          val readBytes = sc.read(readBuffer)
          if (readBytes > 0) {
            readBuffer.flip()
            val bytes = ByteArray(readBuffer.remaining())
            readBuffer.get(bytes)
            val body = String(bytes, Charsets.UTF_8)
            println("Now is: $body")
            stop()
          } else if (readBytes < 0) {
            // 对端链路关闭
            key.cancel()
            sc.close()
          }
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun doConnect() {
    if (socketChannel.connect(InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT))) {
      socketChannel.register(selector, SelectionKey.OP_READ)
      doWrite()
    } else {
      socketChannel.register(selector, SelectionKey.OP_CONNECT)
    }
  }

  private fun doWrite() {
    val req = "QUERY TIME ORDER".toByteArray()
    val writeBuffer = ByteBuffer.allocate(req.size)
    writeBuffer.put(req)
    writeBuffer.flip()
    socketChannel.write(writeBuffer)
    if (!writeBuffer.hasRemaining()) {
      println("Send order to netty.guide.ch12.server successfully")
    }
  }
}