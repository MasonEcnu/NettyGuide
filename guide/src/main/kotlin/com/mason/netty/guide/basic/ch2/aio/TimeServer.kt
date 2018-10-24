package com.mason.netty.guide.basic.ch2.aio

import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * Created by mwu on 2018/10/4
 * NIO2.0 异步IO Server
 */
object TimeServer {

  @JvmStatic
  fun main(args: Array<String>) {
    val timeServer = AsyncTimeServerHandler()
    Thread(timeServer, "AIO-AsyncTimeServerHandler-001").start()
  }
}

class AsyncTimeServerHandler : Runnable {

  lateinit var latch: CountDownLatch

  lateinit var asyncServerSocketChannel: AsynchronousServerSocketChannel

  init {
    try {
      asyncServerSocketChannel = AsynchronousServerSocketChannel.open()
      asyncServerSocketChannel.bind(InetSocketAddress(DEFAULT_PORT))
      println("The time netty.guide.ch12.server is started at DEFAULT_PORT: $DEFAULT_PORT")
    } catch (ioe: IOException) {
      printError(ioe)
    }
  }

  override fun run() {
    latch = CountDownLatch(1)
    doAccept()
    try {
      latch.await()
    } catch (ie: InterruptedException) {
      printError(ie)
    }
  }

  private fun doAccept() {
    asyncServerSocketChannel.accept(this, AcceptCompletionHandler())
  }
}

class AcceptCompletionHandler : CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {
  override fun completed(result: AsynchronousSocketChannel, attachment: AsyncTimeServerHandler) {
    attachment.asyncServerSocketChannel.accept(attachment, this)
    val buffer = ByteBuffer.allocate(1024)
    result.read(buffer, buffer, ReadCompletionHandler(result))
  }

  override fun failed(exc: Throwable, attachment: AsyncTimeServerHandler) {
    attachment.latch.countDown()
  }
}

class ReadCompletionHandler(val channel: AsynchronousSocketChannel) : CompletionHandler<Int, ByteBuffer> {
  override fun completed(result: Int, attachment: ByteBuffer) {
    attachment.flip()
    val body = ByteArray(attachment.remaining())
    attachment.get(body)
    try {
      val req = String(body, Charsets.UTF_8)
      println("The time netty.guide.ch12.server receives order: $req")
      val currentTime = if ("QUERY TIME ORDER".equals(req, true)) Date().toString() else "Bad Order"
      doWrite(currentTime)
    } catch (uee: UnsupportedEncodingException) {
      printError(uee)
    }
  }

  private fun doWrite(currentTime: String) {
    if (currentTime.trim().isNotEmpty()) {
      val bytes = currentTime.toByteArray()
      val writeBuffer = ByteBuffer.allocate(1024)
      writeBuffer.put(bytes)
      writeBuffer.flip()
      channel.write(writeBuffer, writeBuffer, object : CompletionHandler<Int, ByteBuffer> {
        override fun completed(result: Int, buffer: ByteBuffer) {
          if (buffer.hasRemaining()) {
            channel.write(buffer, buffer, this)
          }
        }

        override fun failed(exc: Throwable, buffer: ByteBuffer) {
          channel.close()
          printError(exc)
        }
      })
    }
  }

  override fun failed(exc: Throwable, attachment: ByteBuffer) {
    channel.close()
    printError(exc)
  }
}