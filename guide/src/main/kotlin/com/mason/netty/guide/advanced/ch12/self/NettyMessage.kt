package com.mason.netty.guide.advanced.ch12.self

/**
 * Created by mwu on 2018/10/23
 * Netty消息声明类
 */
data class NettyMessage(var header: Header = Header(), var body: Any = Any()) {
  override fun toString(): String {
    return "NettyMessage [header = $header, body = $body]"
  }
}

data class Header(
    var crcCode: Int = 0xabef0101.toInt(),
    var length: Int = -1,  // 消息长度
    var sessionId: Long = -1L,  // 会话ID
    var type: Int = -1, // 类型
    var priority: Int = -1, // 优先级
    var attachment: Map<String, Any> = hashMapOf()
) {
  override fun toString(): String {
    return "Header [crcCode = $crcCode, length = $length, sessionId = $sessionId, type = $type, priority = $priority, attachment = $attachment]"
  }
}

enum class MsgType(val id: Int) {
  SERVICE_REQ(0),
  SERVICE_RESP(1),
  ONE_WAY(2),
  LOGIN_REQ(3),
  LOGIN_RESP(4),
  PING(5),
  PONG(6),
  UNKNOWN(999);

  companion object {

    fun valueOf(id: Int): MsgType {
      return values().find { it.id == id } ?: UNKNOWN
    }
  }
}