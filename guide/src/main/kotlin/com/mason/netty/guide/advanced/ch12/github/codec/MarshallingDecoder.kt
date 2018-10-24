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

import io.netty.buffer.ByteBuf
import org.jboss.marshalling.ByteInput
import org.jboss.marshalling.Unmarshaller

import java.io.IOException
import java.io.StreamCorruptedException

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
class MarshallingDecoder
/**
 * Creates a new decoder whose maximum object size is `1048576` bytes.
 * If the size of the received object is greater than `1048576` bytes,
 * a [StreamCorruptedException] will be raised.
 *
 * @throws IOException
 */
@Throws(IOException::class)
constructor() {

  private val unmarshaller: Unmarshaller

  init {
    unmarshaller = MarshallingCodecFactory.buildUnMarshalling()
  }

  @Throws(Exception::class)
  fun decode(`in`: ByteBuf): Any {
    val objectSize = `in`.readInt()
    val buf = `in`.slice(`in`.readerIndex(), objectSize)
    val input = ChannelBufferByteInput(buf)
    try {
      unmarshaller.start(input)
      val obj = unmarshaller.readObject()
      unmarshaller.finish()
      `in`.readerIndex(`in`.readerIndex() + objectSize)
      return obj
    } finally {
      unmarshaller.close()
    }
  }
}
