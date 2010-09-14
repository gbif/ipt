/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.server;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Start {

  private static class MonitorThread extends Thread {

    private ServerSocket socket;

    public MonitorThread() {
      setDaemon(true);
      setName("StopMonitor");
      try {
        socket = new ServerSocket(8079, 1, InetAddress.getByName("127.0.0.1"));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      System.out.println("*** running jetty 'stop' thread");
      Socket accept;
      try {
        accept = socket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
        reader.readLine();
        System.out.println("*** stopping jetty embedded server");
        server.stop();
        accept.close();
        socket.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Server server;

  public static void main(String[] args) throws Exception {
    server = new Server();
    SocketConnector connector = new SocketConnector();
    connector.setPort(7001);
    server.setConnectors(new Connector[]{connector});
    WebAppContext context = new WebAppContext();
    context.setServer(server);
    context.setContextPath("/ipt");
    context.setWar("src/main/webapp");
    server.addHandler(context);

    System.out.println(context.getExtraClasspath());
    Thread monitor = new MonitorThread();
    monitor.start();
    server.start();
    server.join();
  }

}