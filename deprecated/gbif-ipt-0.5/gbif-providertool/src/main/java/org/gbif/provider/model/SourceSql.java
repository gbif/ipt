package org.gbif.provider.model;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.util.AppConfig;
import org.hibernate.validator.NotNull;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Entity
public class SourceSql extends SourceBase {
	private static Log log = LogFactory.getLog(SourceSql.class);
	private String sql;

	public SourceSql() {
		super();
	}
	public SourceSql(String name, String sql) {
		super();
		this.sql = sql;
	}
	
	@Lob
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}	
		
	@Override
	@Transient
	public boolean isValid() {
		if(resource != null && StringUtils.isNotBlank(sql) && sql.trim().length() > 10){
			if (resource.hasDbConnection()){
				return true;
			}
		}
		return false;
	}	
	
}
