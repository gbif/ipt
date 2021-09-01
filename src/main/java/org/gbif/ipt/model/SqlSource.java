package org.gbif.ipt.model;

import org.gbif.ipt.config.JdbcSupport;

/**
 * A SQL view based data source.
 * The view is configured via a fully custom and raw select statement that can use any db specific feature needed.
 */
public class SqlSource extends SourceBase {

  private String sql;
  private JdbcSupport.JdbcInfo rdbms;
  private String host;
  private String database;
  private String username;
  private Password password = new Password();

  public String getDatabase() {
    return database;
  }

  public String getHost() {
    return host;
  }

  public String getJdbcDriver() {
    return rdbms.getDriver();
  }

  public String getJdbcUrl() {
    return rdbms.getJdbcUrl(this);

  }

  public String getPassword() {
    return password.password;
  }

  public JdbcSupport.JdbcInfo getRdbms() {
    return rdbms;
  }

  public String getSql() {
    return sql;
  }

  /**
   * The configured sql with an additional limit clause.
   * The exact format of this clause depends on the database and is kept in the JdbcSupport.
   * Select TOP ?, Where ROWNUM<? and LIMIT ? are readily supported.
   *
   * @param limit the number of rowIterator to limit this query by
   *
   * @return the final sql string
   */
  public String getSqlLimited(int limit) {
    return rdbms.addLimit(sql, limit);
  }

  public String getUsername() {
    return username;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPassword(String password) {
    this.password.password = password;
  }

  public void setRdbms(JdbcSupport.JdbcInfo rdbms) {
    this.rdbms = rdbms;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public SourceType getSourceType() {
    return SourceType.SQL;
  }
}
