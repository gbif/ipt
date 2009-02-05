package org.geoserver.console;

import org.geoserver.console.GeoServerConsole.DebugHandler;
import org.geoserver.console.GeoServerConsole.Handler;
import org.geoserver.console.GeoServerConsole.ProductionHandler;

/**
 * Entrance point to {@link GeoServerConsole} application.
 * 
 * @author Justin Deoliveira, OpenGEO
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Handler h = null;
        for ( int i = 0; args != null && i < args.length; i++) {
            if ( "--debug".equalsIgnoreCase( args[i] ) ) {
                h = new DebugHandler();
            }
        }
        if ( h == null ) {
            h = new ProductionHandler();
        }
        GeoServerConsole console = new GeoServerConsole( h );
    }
}
