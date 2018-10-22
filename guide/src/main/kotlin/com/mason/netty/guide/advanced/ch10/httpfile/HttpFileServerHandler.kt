package com.mason.netty.guide.advanced.ch10.httpfile

import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaders.isKeepAlive
import io.netty.handler.codec.http.HttpHeaders.setContentLength
import io.netty.handler.stream.ChunkedFile
import io.netty.util.CharsetUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.regex.Pattern
import javax.activation.MimetypesFileTypeMap

/**
 * Created by mwu on 2018/10/19
 */
class HttpFileServerHandler(private val url: String) : SimpleChannelInboundHandler<FullHttpRequest>() {

  override fun messageReceived(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    if (!request.decoderResult.isSuccess) {
      sendError(ctx, HttpResponseStatus.BAD_REQUEST)
      return
    }
    if (request.method !== HttpMethod.GET) {
      sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED)
      return
    }

    val uri = request.uri
    val path = sanitizeUri(uri)
    if (path == null) {
      sendError(ctx, HttpResponseStatus.FORBIDDEN)
      return
    }

    val file = File(path)
    if (file.isHidden || !file.exists()) {
      sendError(ctx, HttpResponseStatus.NOT_FOUND)
      return
    }
    if (file.isDirectory) {
      if (uri.endsWith("/")) {
        sendListing(ctx, file)
      } else {
        sendRedirect(ctx, "$uri/")
      }
      return
    }
    if (!file.isFile) {
      sendError(ctx, HttpResponseStatus.FORBIDDEN)
      return
    }

    val randomAccessFile: RandomAccessFile
    try {
      randomAccessFile = RandomAccessFile(file, "r")
    } catch (fnfe: FileNotFoundException) {
      sendError(ctx, HttpResponseStatus.NOT_FOUND)
      return
    }

    val fileLength = randomAccessFile.length()
    val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    setContentLength(response, fileLength)
    setContentTypeHeader(response, file)

    if (isKeepAlive(request)) {
      response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    }

    ctx.write(response)
    val sendFileFuture: ChannelFuture
    sendFileFuture = ctx.write(ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise())
    sendFileFuture.addListener(object : ChannelProgressiveFutureListener {

      override fun operationComplete(future: ChannelProgressiveFuture) {
        println("Transfer complete.")

      }

      override fun operationProgressed(future: ChannelProgressiveFuture, progress: Long, total: Long) {
        if (total < 0)
          System.err.println("Transfer progress: $progress")
        else
          System.err.println("Transfer progress: $progress/$total")
      }
    })

    val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    if (!isKeepAlive(request))
      lastContentFuture.addListener(ChannelFutureListener.CLOSE)

  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    if (ctx.channel().isActive)
      sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
  }

  private fun sanitizeUri(uri: String): String? {
    var tempUri: String
    tempUri = try {
      URLDecoder.decode(uri, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
      try {
        URLDecoder.decode(uri, "ISO-8859-1")
      } catch (e1: UnsupportedEncodingException) {
        throw Error()
      }

    }

    if (!tempUri.startsWith(url))
      return null
    if (!tempUri.startsWith("/"))
      return null

    tempUri = tempUri.replace('/', File.separatorChar)
    return if (tempUri.contains(File.separator + '.') || tempUri.contains('.' + File.separator) || tempUri.startsWith(".") || tempUri.endsWith(".")
        || INSECURE_URI.matcher(tempUri).matches()) {
      null
    } else System.getProperty("user.dir") + File.separator + tempUri
  }

  companion object {

    private val INSECURE_URI = Pattern.compile(".*[<>&\"].*")

    private val ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9.]*")

    private fun sendListing(ctx: ChannelHandlerContext, dir: File) {
      val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8")

      val dirPath = dir.path
      val buf = StringBuilder()

      buf.append("<!DOCTYPE html>\r\n")
      buf.append("<html><head><title>")
      buf.append(dirPath)
      buf.append("目录:")
      buf.append("</title></head><body>\r\n")

      buf.append("<h3>")
      buf.append(dirPath).append(" 目录：")
      buf.append("</h3>\r\n")
      buf.append("<ul>")
      buf.append("<li>链接：<a href=\" ../\">..</a></li>\r\n")
      for (f in dir.listFiles()) {
        if (f.isHidden || !f.canRead()) {
          continue
        }
        val name = f.name
        if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
          continue
        }

        buf.append("<li>链接: ")
        buf.append("<a href=\"")
        buf.append(name)
        buf.append("\">")
        buf.append(name)
        buf.append("</a></li>\r\n")
      }

      buf.append("</ul></body></html>\r\n")

      val buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8)
      response.content().writeBytes(buffer)
      buffer.release()
      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }


    private fun sendRedirect(ctx: ChannelHandlerContext, newUri: String) {
      val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND)
      response.headers().set(HttpHeaders.Names.LOCATION, newUri)
      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private fun sendError(ctx: ChannelHandlerContext, status: HttpResponseStatus) {
      val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
          Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8))
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8")
      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private fun setContentTypeHeader(response: HttpResponse, file: File) {
      val fileTypeMap = MimetypesFileTypeMap()
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, fileTypeMap.getContentType(file.path))
    }
  }
}
