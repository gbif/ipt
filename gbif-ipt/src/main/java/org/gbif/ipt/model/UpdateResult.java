package org.gbif.ipt.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UpdateResult {

    // key=uri
    public Set<String> updated = new HashSet<String>();
    // key=uri
    public Set<String> unchanged = new HashSet<String>();
    // key=uri, value=error text
    public Map<String, String> errors = new HashMap<String, String>();

}
