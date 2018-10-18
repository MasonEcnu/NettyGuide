package com.mason.netty.guide.basic.ch2.bio

import com.mason.netty.guide.DEFAULT_HOST
import com.mason.netty.guide.DEFAULT_PORT
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket

/**
 * Created by mwu on 2018/10/2
 * 同步阻塞IO--Client
 */
object TimeClient {
  @JvmStatic
  fun main(args: Array<String>) {
    var socket: Socket? = null
    var reader: BufferedReader? = null
    var writer: PrintWriter? = null
    try {
      socket = Socket(DEFAULT_HOST, DEFAULT_PORT)
      reader = BufferedReader(InputStreamReader(socket.getInputStream()))
      writer = PrintWriter(socket.getOutputStream(), true)
      writer.println("QUERY TIME ORDER")
      println("Send order to server successfully")
      val result = reader.readLine()
      println("Now is: $result")
    } catch (ce: ConnectException) {
      println("Connect failed: ${ce.message}")
      System.exit(0)
    } finally {
      reader?.close()
      writer?.close()
      socket?.close()
      System.exit(0)
    }
  }
}
