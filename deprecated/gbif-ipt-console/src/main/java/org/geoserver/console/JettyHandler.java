package org.geoserver.console;

/**
 * Strategy for controlling a Jetty instance.
 */
public interface JettyHandler {
    void start() throws Exception;
    boolean isStarted() throws Exception;
    
    void stop() throws Exception;
    boolean isStopped() throws Exception;
}