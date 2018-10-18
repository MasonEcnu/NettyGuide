package com.mason.netty.guide.basic.ch2.aio

import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.CountDownLatch


/**
 * Created by mwu on 2018/10/4
 * NIO2.0 异步IO Client
 */
object TimeClient {
  @JvmStatic
  fun main(args: Array<String>) {
    val timeClient = AsyncTimeClientHandler()
    Thread(timeClient, "AIO-AsyncTimeClientHandler-001").start()
  }
}

//首先通过AsynchronousSocketChannel的open方法创建一个新的AsynchronousSocketChannel对象。
class AsyncTimeClientHandler : CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {

  private lateinit var client: AsynchronousSocketChannel
  private lateinit var latch: CountDownLatch

  init {
    try {
      client = AsynchronousSocketChannel.open()
    } catch (ioe: IOException) {
      printError(ioe)
    }

  }

  override fun run() {
    //创建CountDownLatch进行等待，防止异步操作没有执行完成线程就退出。
    latch = CountDownLatch(1)
    //通过connect方法发起异步操作，它有两个参数，
    //A attachment：AsynchronousSocketChannel的附件，用于回调通知时作为入参被传递，调用者可以自定义；
    //CompletionHandler＜Void,? super A＞ handler：异步操作回调通知接口，由调用者实现。
    client.connect(InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT), this, this)
    try {
      latch.await()
    } catch (e1: InterruptedException) {
      e1.printStackTrace()
    }

    try {
      client.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  //异步连接成功之后的方法回调——completed方法
  override fun completed(result: Void?, attachment: AsyncTimeClientHandler) {
    //创建请求消息体，对其进行编码，然后复制到发送缓冲区writeBuffer中，
    //调用Asynchronous SocketChannel的write方法进行异步写。
    //与服务端类似，我们可以实现CompletionHandler ＜Integer, ByteBuffer＞接口用于写操作完成后的回调。
    val req = "QUERY TIME ORDER".toByteArray()
    val writeBuffer = ByteBuffer.allocate(req.size)
    writeBuffer.put(req)
    writeBuffer.flip()
    client.write(writeBuffer, writeBuffer,
        object : CompletionHandler<Int, ByteBuffer> {
          override fun completed(result: Int, buffer: ByteBuffer) {
            //如果发送缓冲区中仍有尚未发送的字节，将继续异步发送，如果已经发送完成，则执行异步读取操作。
            if (buffer.hasRemaining()) {
              client.write(buffer, buffer, this)
            } else {
              //客户端异步读取时间服务器服务端应答消息的处理逻辑
              val readBuffer = ByteBuffer.allocate(1024)
              //调用AsynchronousSocketChannel的read方法异步读取服务端的响应消息。
              //由于read操作是异步的，所以我们通过内部匿名类实现CompletionHandler＜Integer，ByteBuffer＞接口,
              //当读取完成被JDK回调时，构造应答消息。
              client.read(readBuffer, readBuffer, object : CompletionHandler<Int, ByteBuffer> {
                override fun completed(result: Int, buffer: ByteBuffer) {
                  //从CompletionHandler的ByteBuffer中读取应答消息，然后打印结果。
                  buffer.flip()
                  val bytes = ByteArray(buffer.remaining())
                  buffer.get(bytes)
                  val body: String
                  try {
                    body = String(bytes, Charsets.UTF_8)
                    println("Now is : $body")
                    latch.countDown()
                  } catch (uee: UnsupportedEncodingException) {
                    printError(uee)
                  }
                }

                override fun failed(exc: Throwable, attachment: ByteBuffer) {
                  //当读取发生异常时，关闭链路，
                  //同时调用CountDownLatch的countDown方法让AsyncTimeClientHandler线程执行完毕，客户端退出执行。
                  try {
                    client.close()
                    latch.countDown()
                  } catch (ioe: IOException) {
                    printError(ioe)
                  }
                }
              })
            }
          }

          override fun failed(exc: Throwable, attachment: ByteBuffer) {
            try {
              client.close()
              latch.countDown()
            } catch (ioe: IOException) {
              printError(ioe)
            }
          }
        })
  }

  override fun failed(exc: Throwable, attachment: AsyncTimeClientHandler) {
    exc.printStackTrace()
    try {
      client.close()
      latch.countDown()
    } catch (ioe: IOException) {
      printError(ioe)
    }
  }
}