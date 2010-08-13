/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.action.manage;

/**
 * Class to indicate that no session holding a resource has been established yet.
 * This can happen if a manage link is shared with someone else who doesnt have the same session or when a jetty
 * server
 * is restarted which doesnt persist sessions (tomcat does)
 * 
 * @author markus
 * 
 */
public class MissingResourceSession extends RuntimeException {

}
