package middle.ch8

import com.google.protobuf.InvalidProtocolBufferException
import com.mason.proto.ProtoSubscribeReq

/**
 * Created by mwu on 2018/10/10
 */
object TestSubscribeReqProto {

  private fun encode(req: ProtoSubscribeReq.SubscribeReq): ByteArray {
    return req.toByteArray()
  }

  private fun decode(body: ByteArray): ProtoSubscribeReq.SubscribeReq {
    return ProtoSubscribeReq.SubscribeReq.parseFrom(body)
  }

  private fun createSubscribeReq(): ProtoSubscribeReq.SubscribeReq {
    val builder = ProtoSubscribeReq.SubscribeReq.newBuilder()
    builder.subReqId = 1
    builder.userName = "Mason"
    builder.productName = "Netty Guide Book"
    val address = arrayListOf<String>()
    address.add("Beijing")
    address.add("Shanghai")
    address.add("Guangzhou")
    address.add("Shenzhen")
    builder.addAllAddress(address)
    return builder.build()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    try {
      val req = createSubscribeReq()
      println("Before encode: $req")
      val decodedReq = decode(encode(req))
      println("After decode: $decodedReq")
      println("Assert equal: ${decodedReq == req}")
    } catch (ipbe: InvalidProtocolBufferException) {
      println("[${ipbe.javaClass}, ${ipbe.message}]")
    }
  }
}