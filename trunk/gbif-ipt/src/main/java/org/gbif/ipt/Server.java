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

package org.gbif.ipt;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebInfConfiguration;
import org.mortbay.jetty.webapp.WebXmlConfiguration;

/**
 * @author markus
 * 
 */
public class Server {
  private static final org.mortbay.jetty.Server SERVER = new org.mortbay.jetty.Server();

  public static void main(String[] args) throws Exception {
    String webapp = "webapp";
    Integer port = 7001;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.err.println("Argument is not a valid port number: " + args[0] + " - using default port " + port);
      }
    }
    WebAppContext app = new WebAppContext();
    app.setContextPath("/");
    app.setWar(webapp);
    // Avoid the taglib configuration because its a PITA if you don't have a net connection
    app.setConfigurationClasses(new String[]{WebInfConfiguration.class.getName(), WebXmlConfiguration.class.getName()});
    app.setParentLoaderPriority(true);
    // We explicitly use the SocketConnector because the SelectChannelConnector locks files
    Connector connector = new SocketConnector();
    connector.setPort(Integer.parseInt(System.getProperty("jetty.port", port.toString())));
    connector.setMaxIdleTime(60000);
    Server.SERVER.setConnectors(new Connector[]{connector});
    Server.SERVER.setHandlers(new Handler[]{app});
    Server.SERVER.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
    Server.SERVER.setStopAtShutdown(true);
    try {
      Server.SERVER.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
