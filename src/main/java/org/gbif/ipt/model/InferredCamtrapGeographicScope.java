/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class InferredCamtrapGeographicScope {

    private Double minLatitude;
    private Double minLongitude;
    private Double maxLatitude;
    private Double maxLongitude;

    private boolean inferred = false;
    private Set<String> errors = new HashSet<>();

    public Double getMinLatitude() {
        return minLatitude;
    }

    public void setMinLatitude(Double minLatitude) {
        this.minLatitude = minLatitude;
    }

    public Double getMinLongitude() {
        return minLongitude;
    }

    public void setMinLongitude(Double minLongitude) {
        this.minLongitude = minLongitude;
    }

    public Double getMaxLatitude() {
        return maxLatitude;
    }

    public void setMaxLatitude(Double maxLatitude) {
        this.maxLatitude = maxLatitude;
    }

    public Double getMaxLongitude() {
        return maxLongitude;
    }

    public void setMaxLongitude(Double maxLongitude) {
        this.maxLongitude = maxLongitude;
    }

    public boolean isInferred() {
        return inferred;
    }

    public void setInferred(boolean inferred) {
        this.inferred = inferred;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InferredCamtrapGeographicScope that = (InferredCamtrapGeographicScope) o;
        return inferred == that.inferred && Objects.equals(minLatitude, that.minLatitude)
                && Objects.equals(minLongitude, that.minLongitude)
                && Objects.equals(maxLatitude, that.maxLatitude)
                && Objects.equals(maxLongitude, that.maxLongitude)
                && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minLatitude, minLongitude, maxLatitude, maxLongitude, inferred, errors);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InferredCamtrapGeographicScope.class.getSimpleName() + "[", "]")
                .add("minLatitude=" + minLatitude)
                .add("minLongitude=" + minLongitude)
                .add("maxLatitude=" + maxLatitude)
                .add("maxLongitude=" + maxLongitude)
                .add("inferred=" + inferred)
                .add("errors=" + errors)
                .toString();
    }
}
