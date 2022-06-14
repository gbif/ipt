package org.gbif.ipt.task;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.gbif.ipt.model.PropertyMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicPropertiesGenerator {

  private List<DynamicPropertiesMapping> unmappedProperties = Lists.newArrayList();

  public void init(List<String> sourceColumns, Set<PropertyMapping> mappedProperties) {
    unmappedProperties.clear();
    if ((sourceColumns != null) && !sourceColumns.isEmpty()) {
      for (int index=0; index<sourceColumns.size(); index++) {
        String column = sourceColumns.get(index);
        if (!isMapped(index, mappedProperties)) {
          DynamicPropertiesMapping unmappedProperty = new DynamicPropertiesMapping();
          unmappedProperty.name = column;
          unmappedProperty.index = index;
          unmappedProperties.add(unmappedProperty);
        }
      }
    }
  }

  public String generateJson(String[] values) {
    Map<String, String> map = new HashMap<String, String>();
    for (DynamicPropertiesMapping prop : unmappedProperties) {
      if ((prop.name != null) && (prop.index < values.length)) {
        map.put(prop.name, values[prop.index]);
      }
    }
    return new Gson().toJson(map);
  }

  private boolean isMapped(Integer index, Set<PropertyMapping> mappedProperties) {
    for (PropertyMapping p : mappedProperties) {
      if (index.equals(p.getIndex())) {
        return true;
      }
    }
    return  false;
  }

  private class DynamicPropertiesMapping {
    String name;
    Integer index;
  }
}
