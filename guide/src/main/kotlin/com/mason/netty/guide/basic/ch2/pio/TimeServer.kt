package com.mason.netty.guide.basic.ch2.pio

import com.mason.netty.guide.basic.ch2.bio.TimeServerHandler
import com.mason.netty.guide.DEFAULT_PORT
import com.mason.netty.guide.printError
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by mwu on 2018/10/3
 * 伪异步IO Server
 */

object TimeServer {
  @JvmStatic
  fun main(args: Array<String>) {
    val server = ServerSocket(DEFAULT_PORT)
    println("The time netty.guide.ch12.server is started at DEFAULT_PORT: $DEFAULT_PORT")
    var socket: Socket
    try {
      val singleExecutor = TimeServerHandlerExecutePool(50, 10000)  // 创建IO任务线程池
      while (true) {
        socket = server.accept()
        singleExecutor.execute(TimeServerHandler(socket))
      }
    } catch (ioe: IOException) {
      printError(ioe)
    } finally {
      server.close()
      System.exit(1)
    }
  }
}

class TimeServerHandlerExecutePool(maxPoolSize: Int, queueSize: Int) {
  private val executor: ExecutorService

  init {
    executor = ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS, ArrayBlockingQueue<Runnable>(queueSize))
  }

  fun execute(task: Runnable) {
    executor.execute(task)
  }

}
