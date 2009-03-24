package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.util.ExtensionFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ExtensionManagerHibernate extends GenericManagerHibernate<Extension> implements ExtensionManager {
    @Autowired
	private IptNamingStrategy namingStrategy;
    @Autowired
	private RegistryManager registryManager;
    @Autowired
	private ExtensionFactory extensionFactory;    

	public ExtensionManagerHibernate() {
	        super(Extension.class);
    }
	
	@Override
	public Extension get(Long id) {
		Extension e = super.get(id);
		e.getProperties();
		return e;
	}

	@Transactional(readOnly=false)
	public void installExtension(Extension extension){
		if (extension==null || extension.getName()==null){
			throw new IllegalArgumentException("Extension needs to have a name");
		}
		String table = namingStrategy.extensionTableName(extension);
		if (extension.getProperties().size()==0){
			throw new IllegalArgumentException("Extension needs to define properties");
		}
		if (extension.isCore()){
			throw new IllegalArgumentException("Extension cannot define be the core itself");
		}

		Connection cn = null;
		try {
			cn = getConnection();
			// create table basics
			String ddl = String.format("CREATE TABLE IF NOT EXISTS %s (coreid bigint NOT NULL, resource_fk bigint NOT NULL)", table);
			Statement st = cn.createStatement();
			try {
				st.execute(ddl);
			}finally{
				st.close();
			}
			// create indices
			String[] indexedColumns = {"coreid","resource_fk"};
			for (String col : indexedColumns){
				ddl = String.format("CREATE INDEX IDX%s_%s ON %s(%s)", table, col, table, col);
				st = cn.createStatement();
				try {
					st.execute(ddl);
				}finally{
					st.close();
				}
			}
			// add columns
			for (ExtensionProperty prop : extension.getProperties()){
				if (prop!=null && prop.getName()!=null && prop.getColumnLength()>0){
					if (prop.getColumnLength()>256 || prop.getColumnLength()<0){
						// use LOB instead of varchar
						ddl = String.format("ALTER TABLE %s ADD %s clob",table, namingStrategy.propertyToColumnName(prop.getName()));
					}else{
						// use varchar
						ddl = String.format("ALTER TABLE %s ADD %s VARCHAR(%s)",table, namingStrategy.propertyToColumnName(prop.getName()), prop.getColumnLength());
					}
					st = cn.createStatement();
					try {
						st.execute(ddl);
					}finally{
						st.close();
					}
				}else{
					log.warn("Extension property doesnt contain valid column description");
				}
			}
			// persist extension in case it isnt yet
			extension.setInstalled(true);
			save(extension);
		} catch (SQLException e) {
			extension.setInstalled(false);
			save(extension);
			e.printStackTrace();
		} finally {
			if (cn!=null){
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Transactional(readOnly=false)
	public void removeExtension(Extension extension){
		if (extension==null || extension.getName()==null){
			throw new IllegalArgumentException("Extension needs to have a name");
		}
		String table = namingStrategy.extensionTableName(extension);

		Connection cn = null;
		Statement st = null;
		try {
			cn = getConnection();
			st = cn.createStatement();
			String ddl = String.format("DROP TABLE IF EXISTS %s", table);
			st.execute(ddl);
			extension.setInstalled(false);
			save(extension);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (st!=null){
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (cn!=null){
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	@Transactional(readOnly=false)
	private void removeExtensionCompletely(Extension extension) throws SQLException{
		removeExtension(extension);
		// remove extension, its properties and potentially existing property mappings
		Session session = getSession();
		for (ExtensionProperty prop : extension.getProperties()){
			// delete all existing property mappings
			String hqlUpdate = "delete PropertyMapping WHERE property = :property";
			int count = session.createQuery( hqlUpdate ).setEntity("property", prop).executeUpdate();
			log.info(String.format("Removed %s property mappings bound to extension property %s", count, prop.getQualName()));
			// remove property itself
			universalRemove(prop);
		}
		// finally remove extension
		remove(extension);
	}

	@SuppressWarnings("unchecked")
	public List<Extension> getInstalledExtensions() {
        return getSession().createQuery(String.format("from Extension where installed=true and core=false"))
        	.list();
	}
	
	@SuppressWarnings("unchecked")
	public Extension getCore() {
        return (Extension) getSession().createQuery(String.format("from Extension where core=true"))
        	.uniqueResult();
	}

	public ExtensionProperty getProperty(String qualname) {
		Session session = getSession();
        Object obj = session.createQuery(String.format("select p from ExtensionProperty p where p.qualName=:qualname"))
    	.setParameter("qualname", qualname)
    	.uniqueResult();
        
        return (ExtensionProperty) obj;
	}

	public void synchroniseExtensionsWithRepository() {
		Collection<String> urls = registryManager.listAllExtensions();
		Collection<Extension> extensions = extensionFactory.build(urls);
		for (Extension e: extensions) {
			// see if it exists - we don't support any versioning so they don't get updated
			List existing = getSession().createQuery("from Extension where namespace=:namespace and name=:name")
			.setParameter("namespace", e.getNamespace())
			.setParameter("name", e.getName())
			.list();		
			if (existing != null && existing.size()==0) {
				this.save(e);
			} else {
				log.info("Not updating Extension since it already exists: " + e.getNamespace() + " - " + e.getName());
			}
		}
	}
}
