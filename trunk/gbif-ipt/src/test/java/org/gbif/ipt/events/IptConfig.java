/*
 * Copyright 2010 GBIF.
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
 */
package org.gbif.ipt.events;

/**
 * Default implementation of {@link Config}.
 * 
 */
public class IptConfig implements Config {

  public static class Builder {
    private String baseUrl;

    private Builder() {
    }

    public Builder baseUrl(String val) {
      baseUrl = val;
      return this;
    }

    Config build() {
      return new IptConfig(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final String baseUrl;

  private IptConfig(Builder builder) {
    baseUrl = builder.baseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
