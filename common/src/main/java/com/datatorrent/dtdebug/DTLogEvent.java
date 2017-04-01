/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.dtdebug;

import java.io.Serializable;

import org.apache.log4j.spi.LoggingEvent;

public class DTLogEvent implements Serializable
{
  private static final long serialVersionUID = -868428216207456145L;

  public DTLogEvent(LoggingEvent loggingEvent)
  {
    renderedMessage = loggingEvent.getRenderedMessage();
    threadName = loggingEvent.getThreadName();
    timeStamp = loggingEvent.timeStamp;
    level = loggingEvent.getLevel().toString();
    fullInfo = loggingEvent.getLocationInformation().fullInfo;
  }

  public String renderedMessage;
  public String threadName;
  public long timeStamp;
  public String level;
  public String fullInfo;
}
