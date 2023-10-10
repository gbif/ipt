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

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

public class InferredCamtrapMetadata implements InferredMetadata {

    private InferredCamtrapGeographicScope inferredGeographicScope;
    private InferredCamtrapTaxonomicScope inferredTaxonomicScope;
    private InferredCamtrapTemporalScope inferredTemporalScope;
    private Date lastModified;

    public InferredCamtrapGeographicScope getInferredGeographicScope() {
        return inferredGeographicScope;
    }

    public void setInferredGeographicScope(InferredCamtrapGeographicScope inferredGeographicScope) {
        this.inferredGeographicScope = inferredGeographicScope;
    }

    public InferredCamtrapTaxonomicScope getInferredTaxonomicScope() {
        return inferredTaxonomicScope;
    }

    public void setInferredTaxonomicScope(InferredCamtrapTaxonomicScope inferredTaxonomicScope) {
        this.inferredTaxonomicScope = inferredTaxonomicScope;
    }

    public InferredCamtrapTemporalScope getInferredTemporalScope() {
        return inferredTemporalScope;
    }

    public void setInferredTemporalScope(InferredCamtrapTemporalScope inferredTemporalScope) {
        this.inferredTemporalScope = inferredTemporalScope;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InferredCamtrapMetadata that = (InferredCamtrapMetadata) o;
        return Objects.equals(inferredGeographicScope, that.inferredGeographicScope)
                && Objects.equals(inferredTaxonomicScope, that.inferredTaxonomicScope)
                && Objects.equals(inferredTemporalScope, that.inferredTemporalScope)
                && Objects.equals(lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inferredGeographicScope, inferredTaxonomicScope, inferredTemporalScope, lastModified);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InferredCamtrapMetadata.class.getSimpleName() + "[", "]")
                .add("inferredGeographicScope=" + inferredGeographicScope)
                .add("inferredTaxonomicScope=" + inferredTaxonomicScope)
                .add("inferredTemporalScope=" + inferredTemporalScope)
                .add("lastModified=" + lastModified)
                .toString();
    }
}
