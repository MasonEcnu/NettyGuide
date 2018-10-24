/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mason.netty.guide.advanced.ch12.github.codec

import org.jboss.marshalling.*

import java.io.IOException

/**
 * @author Administrator
 * @version 1.0
 * @date 2014年3月15日
 */
object MarshallingCodecFactory {

  /**
   * 创建Jboss Marshaller
   *
   * @return
   * @throws IOException
   */
  @Throws(IOException::class)
  internal fun buildMarshalling(): Marshaller {
    val marshallerFactory = Marshalling
        .getProvidedMarshallerFactory("serial")
    val configuration = MarshallingConfiguration()
    configuration.version = 5
    return marshallerFactory
        .createMarshaller(configuration)
  }

  /**
   * 创建Jboss Unmarshaller
   *
   * @return
   * @throws IOException
   */
  @Throws(IOException::class)
  internal fun buildUnMarshalling(): Unmarshaller {
    val marshallerFactory = Marshalling
        .getProvidedMarshallerFactory("serial")
    val configuration = MarshallingConfiguration()
    configuration.version = 5
    return marshallerFactory
        .createUnmarshaller(configuration)
  }
}
