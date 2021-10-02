/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserTest {

  @Test
  public void testHash() {
    User u1 = new User();
    u1.setEmail("Peter@gbif.org");

    User u2 = new User();
    u2.setEmail("peter@gbif.org");

    User u3 = new User();
    u3.setEmail("pia@gbif.org");

    assertEquals(u1, u2);
    assertEquals(u1, u2);
    assertFalse(u1.equals(u3));

    Set<User> users = new HashSet<User>();
    users.add(u1);
    assertEquals(1, users.size());
    users.add(u2);
    assertEquals(1, users.size());
    users.add(u3);
    assertEquals(2, users.size());
    users.remove(u1);
    assertEquals(1, users.size());
    users.add(u1);
    assertEquals(2, users.size());
    users.remove(u2);
    assertEquals(1, users.size());
    users.remove(u3);
    assertEquals(0, users.size());

  }
}
