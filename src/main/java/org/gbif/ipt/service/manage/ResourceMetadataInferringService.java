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
package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;

import java.util.OptionalLong;

public interface ResourceMetadataInferringService {

    /**
     * Method for inferring metadata from sources (geographic, taxonomic, temporal coverages).
     *
     * @param resource resource
     *
     * @return inferred metadata
     */
    InferredMetadata inferMetadata(Resource resource);

    /**
     * Counts the rows inferring the metadata would read, from the row counts of the analysed source files.
     *
     * @param resource resource
     *
     * @return number of rows, or empty if a source is read remotely (SQL, URL): its size is unknown, and its data can
     * change without the IPT knowing
     */
    OptionalLong countRowsToRead(Resource resource);
}
