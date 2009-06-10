package org.geoserver.console;

import org.geoserver.console.IptServerConsole.DebugHandler;
import org.geoserver.console.IptServerConsole.ProductionHandler;

/**
 * Entrance point to {@link IptServerConsole} application.
 * 
 * @author Justin Deoliveira, OpenGEO
 */
public class Main {

    public static void main(String[] args) throws Exception {
        JettyHandler h = null;
        for ( int i = 0; args != null && i < args.length; i++) {
            if ( "--debug".equalsIgnoreCase( args[i] ) ) {
                h = new DebugHandler();
            }
        }
        if ( h == null ) {
            h = new ProductionHandler();
        }
        IptLiteServerConsole console = new IptLiteServerConsole( h );
    }
}
