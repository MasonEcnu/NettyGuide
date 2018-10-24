package com.mason.netty.guide

/**
 * Created by mwu on 2018/10/3
 */

const val DEFAULT_HOST = "localhost"

const val DEFAULT_PORT = 8008

const val QUERY_ORDER = "QUERY TIME ORDER"

const val BAD_ORDER = "BAD ORDER"

const val BUFFER_CACHE_SIZE = 1024

const val SEPARATOR = "\$_"

fun printError(err: Throwable) {
  println("Something is wrong: [${err.javaClass.name}, ${err.message}]")
  System.exit(1)
}
