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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.file.FileStoreManager;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Connections to the embedded DuckDB driver must not give the user-supplied SQL or database path access to anything
 * outside the database file (#3283).
 */
public class SourceManagerImplDuckDbTest {

  @TempDir
  Path dir;

  private SourceManagerImpl manager;
  private JdbcSupport.JdbcInfo duckDb;
  private File dbFile;

  @BeforeEach
  public void setup() throws Exception {
    manager = new SourceManagerImpl(mock(AppConfig.class), mock(DataDir.class), mock(FileStoreManager.class));

    duckDb = mock(JdbcSupport.JdbcInfo.class);
    when(duckDb.getDriver()).thenReturn("org.duckdb.DuckDBDriver");
    when(duckDb.getJdbcUrl(any(SqlSource.class)))
        .thenAnswer(inv -> "jdbc:duckdb:" + ((SqlSource) inv.getArgument(0)).getDatabase());

    dbFile = dir.resolve("occurrences.duckdb").toFile();
    Class.forName("org.duckdb.DuckDBDriver");
    try (Connection c = DriverManager.getConnection("jdbc:duckdb:" + dbFile.getAbsolutePath());
         Statement s = c.createStatement()) {
      s.execute("CREATE TABLE occurrence AS SELECT 'occ1' AS id UNION ALL SELECT 'occ2'");
    }
  }

  private SqlSource source(String database) {
    SqlSource source = new SqlSource();
    source.setName("occurrence");
    source.setRdbms(duckDb);
    source.setDatabase(database);
    return source;
  }

  private static long count(Connection c) throws SQLException {
    try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT count(*) FROM occurrence")) {
      rs.next();
      return rs.getLong(1);
    }
  }

  @Test
  public void readsTablesWithConcurrentConnections() throws Exception {
    SqlSource source = source(dbFile.getAbsolutePath());
    // two sources on the same file published at once share one DuckDB instance
    try (Connection c1 = manager.getDbConnection(source); Connection c2 = manager.getDbConnection(source)) {
      assertEquals(2, count(c1));
      assertEquals(2, count(c2));
    }
    // and the file can be opened again afterwards
    try (Connection c = manager.getDbConnection(source)) {
      assertEquals(2, count(c));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "SELECT content FROM read_text('%s')",
      "COPY (SELECT 'x') TO '%s.copy.csv'",
      "ATTACH '%s.attached.duckdb' AS other",
      "CREATE TABLE written AS SELECT 1",
      "INSTALL httpfs",
      "LOAD httpfs",
      "SET enable_external_access=true",
      "SET lock_configuration=false",
      "RESET enable_external_access"
  })
  public void refusesSqlReachingOutsideTheDatabase(String sql) throws Exception {
    File other = dir.resolve("secret.txt").toFile();
    Files.writeString(other.toPath(), "secret", StandardCharsets.UTF_8);
    try (Connection c = manager.getDbConnection(source(dbFile.getAbsolutePath()));
         Statement s = c.createStatement()) {
      assertThrows(SQLException.class, () -> s.execute(String.format(sql, other.getAbsolutePath())));
    }
    assertFalse(new File(other.getAbsolutePath() + ".copy.csv").exists());
    assertFalse(new File(other.getAbsolutePath() + ".attached.duckdb").exists());
  }

  @Test
  public void refusesUrlOptionsInTheDatabase() throws Exception {
    File created = dir.resolve("created.duckdb").toFile();
    assertThrows(SQLException.class,
        () -> manager.getDbConnection(source(created.getAbsolutePath() + ";duckdb.read_only=false")));
    assertFalse(created.exists());

    File written = dir.resolve("written.csv").toFile();
    File init = dir.resolve("init.sql").toFile();
    Files.writeString(init.toPath(), "COPY (SELECT 'x') TO '" + written.getAbsolutePath() + "';",
        StandardCharsets.UTF_8);
    assertThrows(SQLException.class, () -> manager.getDbConnection(
        source(dbFile.getAbsolutePath() + ";session_init_sql_file=" + init.getAbsolutePath())));
    assertThrows(SQLException.class,
        () -> manager.getDbConnection(source(dbFile.getAbsolutePath() + "?enable_external_access=true")));
    assertFalse(written.exists());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", ":memory:", ":memory:x"})
  public void refusesNonFileDatabases(String database) {
    assertThrows(SQLException.class, () -> manager.getDbConnection(source(database)));
  }

  /**
   * MotherDuck databases are only checked by the validation, never by connecting. With the connection properties,
   * connecting fails too, as the extension can't be loaded, but it's the validation that refuses them with a clear
   * message.
   */
  @ParameterizedTest
  @ValueSource(strings = {"md:my_db", "MD:my_db", "motherduck:my_db", "MotherDuck:my_db"})
  public void refusesRemoteDatabases(String database) {
    assertThrows(SQLException.class, () -> SourceManagerImpl.validateDuckDbDatabase(database));
  }

  @Test
  public void doesNotCreateAMissingDatabaseFile() {
    File missing = dir.resolve("missing.duckdb").toFile();
    assertThrows(SQLException.class, () -> manager.getDbConnection(source(missing.getAbsolutePath())));
    assertFalse(missing.exists());
  }

  @Test
  public void validatesTheDatabasePath() throws Exception {
    SourceManagerImpl.validateDuckDbDatabase("/data/occurrences.duckdb");
    SourceManagerImpl.validateDuckDbDatabase("C:\\data\\occurrences.duckdb");
    assertEquals("true", SourceManagerImpl.duckDbConnectionProperties().getProperty("lock_configuration"));
  }

  /**
   * @return a manager that records the connections it opens
   */
  private SourceManagerImpl recordingConnections(List<Connection> connections) throws SQLException {
    SourceManagerImpl recording = spy(manager);
    doAnswer(inv -> {
      Connection c = (Connection) inv.callRealMethod();
      connections.add(c);
      return c;
    }).when(recording).getDbConnection(any(SqlSource.class));
    return recording;
  }

  /**
   * SQL that the restrictions refuse fails when the iterators are created. They must close the connection, as the
   * caller gets no iterator to close: an open connection keeps the shared DuckDB instance in use.
   */
  @Test
  public void iteratorsCloseTheConnectionWhenTheQueryFails() throws Exception {
    List<Connection> connections = new ArrayList<>();
    SourceManagerImpl recording = recordingConnections(connections);
    File other = dir.resolve("secret.txt").toFile();
    Files.writeString(other.toPath(), "secret", StandardCharsets.UTF_8);
    SqlSource source = source(dbFile.getAbsolutePath());
    source.setSql("SELECT content FROM read_text('" + other.getAbsolutePath() + "')");

    assertThrows(SourceException.class, () -> recording.rowIterator(source));
    assertThrows(SourceException.class, () -> recording.inspectColumn(source, 0, 10, 0));

    assertEquals(2, connections.size());
    for (Connection c : connections) {
      assertTrue(c.isClosed());
    }
  }

  @Test
  public void iteratorsCloseTheConnection() throws Exception {
    List<Connection> connections = new ArrayList<>();
    SourceManagerImpl recording = recordingConnections(connections);
    SqlSource source = source(dbFile.getAbsolutePath());
    source.setSql("SELECT id FROM occurrence ORDER BY id");

    try (ClosableReportingIterator<String[]> rows = recording.rowIterator(source)) {
      assertEquals("occ1", rows.next()[0]);
    }
    // including the value of the first row
    assertEquals(Set.of("occ1", "occ2"), recording.inspectColumn(source, 0, 10, 0));

    assertEquals(2, connections.size());
    for (Connection c : connections) {
      assertTrue(c.isClosed());
    }
  }
}
