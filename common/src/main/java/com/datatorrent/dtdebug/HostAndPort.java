package com.datatorrent.dtdebug;

import java.io.Serializable;

public class HostAndPort implements Serializable
{
  private static final long serialVersionUID = 3169637685063854851L;

  public String host;
  public int port;

  public HostAndPort(String host, int port)
  {
    this.host = host;
    this.port = port;
  }

  @Override
  public String toString()
  {
    return host + ":" + port;
  }
}
