/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.webapp.action;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsonplugin.annotations.JSON;
import com.opensymphony.xwork2.Action;

public class JSONExample {
    private String field1 = "str";
    private int[] ints = {10, 20};
    private Map map = new HashMap();
    private String customName = "custom";

    //'transient' fields are not serialized
    private transient String field2;

    //fields without getter method are not serialized
    private String field3;

    public String execute() {
        map.put("John", "Galt");
        return Action.SUCCESS;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    @JSON(name="newName")
    public String getCustomName() {
        return this.customName;
    }
}
