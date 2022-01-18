package org.gbif.ipt.model;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

public class KeyNamePair {

  private String key;
  private String name;

  @NotNull
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KeyNamePair that = (KeyNamePair) o;
    return Objects.equals(key, that.key) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, name);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", KeyNamePair.class.getSimpleName() + "[", "]")
        .add("key='" + key + "'")
        .add("name='" + name + "'")
        .toString();
  }
}
