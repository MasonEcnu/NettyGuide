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

import java.util.HashMap

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
class Header {

  /**
   * @return the crcCode
   */
  /**
   * @param crcCode the crcCode to set
   */
  var crcCode = -0x5410feff

  /**
   * @return the length
   */
  /**
   * @param length the length to set
   */
  var length: Int = 0// 消息长度

  /**
   * @return the sessionID
   */
  /**
   * @param sessionID the sessionID to set
   */
  var sessionID: Long = 0// 会话ID

  /**
   * @return the type
   */
  /**
   * @param type the type to set
   */
  var type: Int = 0// 消息类型

  /**
   * @return the priority
   */
  /**
   * @param priority the priority to set
   */
  var priority: Int = 0// 消息优先级

  /**
   * @return the attachment
   */
  /**
   * @param attachment the attachment to set
   */
  var attachment: Map<String, Any> = HashMap() // 附件

  /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
  override fun toString(): String {
    return ("Header [crcCode=" + crcCode + ", length=" + length
        + ", sessionID=" + sessionID + ", type=" + type + ", priority="
        + priority + ", attachment=" + attachment + "]")
  }

}
