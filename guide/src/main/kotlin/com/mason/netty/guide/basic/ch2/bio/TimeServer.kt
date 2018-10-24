package com.mason.netty.guide.basic.ch2.bio

import com.mason.netty.guide.DEFAULT_PORT
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * Created by mwu on 2018/10/2
 * 同步阻塞IO--Server
 */
object TimeServer {
  @JvmStatic
  fun main(args: Array<String>) {
    val server = ServerSocket(DEFAULT_PORT)
    println("The time netty.guide.ch12.server is started at DEFAULT_PORT: $DEFAULT_PORT")
    var socket: Socket
    try {
      while (true) {
        socket = server.accept()
        Thread(TimeServerHandler(socket)).start()
      }
    } catch (ioe: IOException) {
      println("Something is wrong: ${ioe.message}")
      System.exit(0)
    } finally {
      server.close()
      System.exit(0)
    }
  }
}

class TimeServerHandler(private val socket: Socket) : Runnable {

  override fun run() {
    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val writer = PrintWriter(socket.getOutputStream(), true)
    val currentTime = System.currentTimeMillis()
    var body: String?
    try {
      while (true) {
        body = reader.readLine()
        if (body == null) {
          break
        }
        println("The time netty.guide.ch12.server receive order: $body")
        val msg = if ("QUERY TIME ORDER".equals(body, true)) Date(currentTime).toString() else "Bad Order"
        writer.println(msg)
      }
    } catch (ioe: IOException) {
      println("Something is wrong: ${ioe.message}")
      System.exit(0)
    } catch (ise: IllegalStateException) {
      println("Something is wrong: ${ise.message}")
      System.exit(0)
    } finally {
      reader.close()
      writer.close()
      socket.close()
    }
  }
}
