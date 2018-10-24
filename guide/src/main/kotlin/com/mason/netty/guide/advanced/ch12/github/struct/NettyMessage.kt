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
package com.mason.netty.guide.advanced.ch12.github.struct

/**
 * @author lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
class NettyMessage {

  /**
   * @return the header
   */
  /**
   * @param header the header to set
   */
  var header: Header? = null

  /**
   * @return the body
   */
  /**
   * @param body the body to set
   */
  var body: Any? = null

  /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
  override fun toString(): String {
    return "NettyMessage [header=$header]"
  }
}
