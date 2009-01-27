package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
public class BaseManagerJDBC {
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	protected IptNamingStrategy namingStrategy;

	protected Connection getConnection() {
		Session s = getSession();
		Connection cn = s.connection();
		return cn;
	}

	private Session getSession() {
		return SessionFactoryUtils.getSession(sessionFactory, false);
	}

	protected int executeCount(String sql) {
		Connection cn = getConnection();
		int count = 0;
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (result.next()){
				count = result.getInt(1);
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql), e);
		}
		return count;
	}

	protected List<Object> executeList(String sql) {
		Connection cn = getConnection();
		List<Object> result = new ArrayList<Object>();
		try {
			Statement st = cn.createStatement();			
			ResultSet resultset = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (resultset.next()){
				result.add(resultset.getObject(1));
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql), e);
		}
		return result;
	}
	protected List<String> executeListAsString(String sql) {
		Connection cn = getConnection();
		List<String> result = new ArrayList<String>();
		try {
			Statement st = cn.createStatement();			
			ResultSet resultset = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (resultset.next()){
				result.add(resultset.getString(1));
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql), e);
		}
		return result;
	}
	protected Map<String, Integer> executeMap(String sql) {
		Connection cn = getConnection();
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (result.next()){
				String key = result.getString(1);
				if (key!=null){
					map.put(key, result.getInt(2));
				}
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql), e);
		}
		return map;
	}
}
