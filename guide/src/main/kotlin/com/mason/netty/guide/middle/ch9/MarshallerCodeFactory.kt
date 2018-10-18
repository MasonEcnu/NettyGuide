package com.mason.netty.guide.middle.ch9

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider
import io.netty.handler.codec.marshalling.MarshallingDecoder
import io.netty.handler.codec.marshalling.MarshallingEncoder
import org.jboss.marshalling.Marshalling
import org.jboss.marshalling.MarshallingConfiguration

/**
 * Created by mwu on 2018/10/15
 * MarshallingDecoder和MarshallingEncoder
 * 支持半包和粘包的解决
 */
object MarshallerCodeFactory {

  /**
   * 创建JBoss Marshalling解码器
   * MarshallingDecoder
   */
  fun buildMarshallingDecoder(): MarshallingDecoder {
    // serial表示创建的是Java序列化工厂对象
    val marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial")
    val config = MarshallingConfiguration()
    config.version = 5
    val provider = DefaultUnmarshallerProvider(marshallerFactory, config)
    return MarshallingDecoder(provider)
  }

  /**
   * 创建JBoss Marshalling编码器
   * MarshallingEncoder
   */
  fun buildMarshallingEncoder(): MarshallingEncoder {
    val marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial")
    val config = MarshallingConfiguration()
    config.version = 5
    val provider = DefaultMarshallerProvider(marshallerFactory, config)
    return MarshallingEncoder(provider)
  }
}