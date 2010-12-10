package org.gbif.ipt.config;

import org.gbif.ipt.config.JdbcSupport.JdbcInfo;
import org.gbif.ipt.config.JdbcSupport.LIMIT_TYPE;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JdbcInfoTest {
  @Test
  public void testSql() {
    JdbcSupport support = new JdbcSupport();

    JdbcInfo info = support.new JdbcInfo("mysql", "MySQL", "com.mysql.jdbc.Driver", "jdbc:mysql://{host}/{database}", LIMIT_TYPE.LIMIT);
    assertEquals("Select * from specimen LIMIT 10", info.addLimit("Select * from specimen", 10));
    assertEquals(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true LIMIT 10",
        info.addLimit(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true limit 100000", 10));
    assertEquals(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true group by fake LIMIT 10",
        info.addLimit(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true group by fake", 10));
    assertEquals("select * from specimen union select * from observation LIMIT 10",
        info.addLimit("select * from specimen union select * from observation", 10));
    assertEquals("select * from specimen where country in (select country from countries where public=true) LIMIT 10",
        info.addLimit("select * from specimen where country in (select country from countries where public=true)", 10));

    info = support.new JdbcInfo("mysql", "MySQL", "com.mysql.jdbc.Driver", "jdbc:mysql://{host}/{database}", LIMIT_TYPE.TOP);
    assertEquals(" SELECT TOP 10 * from specimen", info.addLimit("Select * from specimen", 10));
    assertEquals(" SELECT TOP 10 * from specimen union SELECT TOP 10 * from observation",
        info.addLimit("select * from specimen union select * from observation", 10));
    assertEquals(" SELECT TOP 10 s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true",
        info.addLimit(" SELECT top 23415 s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true", 10));
    assertEquals(" SELECT TOP 10 s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true group by fake",
        info.addLimit(" SELECT top 100000 s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE s.public=true group by fake",
            10));

    info = support.new JdbcInfo("mysql", "MySQL", "com.mysql.jdbc.Driver", "jdbc:mysql://{host}/{database}", LIMIT_TYPE.ROWNUM);
    assertEquals("Select * from specimen WHERE rownum <= 10", info.addLimit("Select * from specimen", 10));
    assertEquals("Select * from specimen WHERE rownum <= 10 AND s.public=true", info.addLimit("Select * from specimen where s.public=true", 10));
    assertEquals("Select * from specimen Where   rownum <= 10  and s.public=true group by fake",
        info.addLimit("Select * from specimen Where   ROWNUM<=99 and s.public=true group by fake", 10));
    assertEquals("Select * from specimen where   rownum <= 10  and s.public=true group by fake",
        info.addLimit("Select * from specimen where   ROWNUM <= 99 and s.public=true group by fake", 10));
    assertEquals("Select * from specimen   WHERE rownum <= 10  and s.public=true group by fake",
        info.addLimit("Select * from specimen   WHERE ROWNUM <=  99 and s.public=true group by fake", 10));
    assertEquals(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id WHERE rownum <= 10 AND s.public=true",
        info.addLimit(" SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id where s.public=true", 10));
    assertEquals(
        " SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id Where rownum <= 10  and s.public=true group by fake",
        info.addLimit(
            " SELECT s.id,sciname,genus,lat,lon from specimen s join location l on s.location_fk=l.id Where ROWNUM <=  10 and s.public=true group by fake", 10));
  }

}
