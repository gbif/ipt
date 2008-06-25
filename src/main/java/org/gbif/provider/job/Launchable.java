package org.gbif.provider.job;

import java.util.Map;

/**
* Anything that is scheduled, must be launchable
* @author timrobertson
*/
public interface Launchable {
       public void launch(Map<String, Object> seed) throws Exception;
}