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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jNetworkAppender extends AppenderSkeleton
{
  private HostAndPort masterHostPort;
  private HostAndPort logServerHostPort;

  private String host;
  private String service;
  private String user = null;
  private String application = null;
  private String applicationId = null;
  private String containerId = null;
//  private String operators = null;

  private boolean ignoreLogEntry = false;

  private Socket socket = null;
  private ObjectInputStream objectInputStream = null;
  private ObjectOutputStream objectOutputStream = null;

  public Log4jNetworkAppender(String host, int port, String service)
  {
    super();
    this.service = service;
    init(new HostAndPort(host, port));
  }

  private void init(HostAndPort masterHostPort)
  {
    this.masterHostPort = masterHostPort;

    try {
      InetAddress addr;
      addr = InetAddress.getLocalHost();
      host = addr.getHostName();
    } catch (UnknownHostException ex) {
      host = "unknown";
    }

    if (service.startsWith("apex")) {

      user = System.getenv("HADOOP_USER_NAME");;
      application = System.getProperties().getProperty("application.name");

      String envContainerId = System.getenv("CONTAINER_ID");

      String[] splits = envContainerId.split("_");

      try {
        applicationId = splits[2] + "_" + splits[3];

        if (service.equals("apexcontainer")) {
          containerId = splits[4] + "_" + splits[5];
        }
      } catch (Exception ex) {

        applicationId = ex.getMessage();
        containerId = ex.getStackTrace().toString();
        application = envContainerId;
      }
    }

    findNextHost();
  }

/*  private void findServerHost()
  {
    while (!findNextHost()) {
      ;
    }
  } */

  private boolean findNextHost()
  {
    try {
      sendRequestToMaster();
      return connectToLogServer();
    } catch (Exception e) {

      ignoreLogEntry = true;
      e.printStackTrace();
      return false;
    }
  }

  private void sendRequestToMaster() throws IOException, ClassNotFoundException
  {
    Socket masterServerSocket = new Socket(masterHostPort.host, masterHostPort.port);

    ObjectOutputStream objOutputStream = new ObjectOutputStream(masterServerSocket.getOutputStream());
    objOutputStream.writeObject("client");
    objOutputStream.flush();

    ObjectInputStream objInputStream = new ObjectInputStream(masterServerSocket.getInputStream());
    logServerHostPort = (HostAndPort)objInputStream.readObject();
    masterServerSocket.close();
  }

  private boolean connectToLogServer()
  {
    try {

      socket = new Socket(logServerHostPort.host, logServerHostPort.port);
      objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

      objectOutputStream.writeObject(service);
      objectOutputStream.writeObject(host);
      if (user != null) {
        objectOutputStream.writeObject(user);
      }
      if (application != null) {
        objectOutputStream.writeObject(application);
      }
      if (applicationId != null) {
        objectOutputStream.writeObject(applicationId);
      }
      if (containerId != null) {
        objectOutputStream.writeObject(containerId);
      }
      objectOutputStream.flush();

    } catch (Exception e) {

      e.printStackTrace();
      closeSocket();
      ignoreLogEntry = true;
      return false;
    }

    return true;
  }

  private void closeSocket()
  {

    try {
      if (objectInputStream != null) {

        objectInputStream.close();
        objectInputStream = null;
      }

      if (objectOutputStream != null) {

        objectOutputStream.close();
        objectOutputStream = null;
      }

      if (socket != null) {

        socket = null;
        socket.close();
      }
    } catch (Exception e) {
      ;
    }
  }

  @Override
  public void append(LoggingEvent loggingEvent)
  {
    if (ignoreLogEntry) {
      return;
    }

    try {

      objectOutputStream.writeObject(new DTLogEvent(loggingEvent));
      objectOutputStream.flush();

    } catch (Exception ex) {
      ignoreLogEntry = true;
    }
  }

  public void close()
  {
    closeSocket();
  }

  public boolean requiresLayout()
  {
    return false;
  }

  public void setApplication(String application)
  {
    this.application = application;
  }

  public void setUser(String user)
  {
    this.user = user;
  }

  private static final Logger LOG = LoggerFactory.getLogger(Log4jNetworkAppender.class);

}
