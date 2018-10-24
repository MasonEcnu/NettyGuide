package com.mason.netty.guide.advanced.ch12.self.codec

import org.jboss.marshalling.Marshaller
import org.jboss.marshalling.Unmarshaller
import org.jboss.marshalling.MarshallingConfiguration
import org.jboss.marshalling.Marshalling


/**
 * Created by mwu on 2018/10/23
 */
class MarshallerCodecFactory {

  companion object {
    fun buildMarshalling(): Marshaller {
      val marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial")
      val configuration = MarshallingConfiguration()
      configuration.version = 5
      return marshallerFactory.createMarshaller(configuration)
    }

    fun buildUnMarshalling(): Unmarshaller {
      val marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial")
      val configuration = MarshallingConfiguration()
      configuration.version = 5
      return marshallerFactory.createUnmarshaller(configuration)
    }
  }
}