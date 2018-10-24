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
package com.mason.netty.guide.advanced.ch12.github

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
enum class MessageType(val value: Int) {

  SERVICE_REQ(0), SERVICE_RESP(1), ONE_WAY(2), LOGIN_REQ(3),
  LOGIN_RESP(4), HEARTBEAT_REQ(5), HEARTBEAT_RESP(6);
}
