package org.gbif.provider.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.UploadEvent;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class DarwinCoreDaoImpl extends GenericDaoHibernate<DarwinCore, Long> implements DarwinCoreDao  {
	public DarwinCoreDaoImpl() {
		super(DarwinCore.class);
	}

	public DarwinCore findByLocalId(final String localId, final Long resourceId) {
		HibernateTemplate template = getHibernateTemplate();

		List<DarwinCore> records =  (List) template.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createQuery("select dwc FROM DarwinCore dwc JOIN dwc.resource res WHERE res.id = :resourceId and dwc.localId = :localId");
				//Query query = session.createQuery("from UploadEvent as event where UploadEvent.resource.id = :resourceId");
				query.setParameter("resourceId", resourceId);
				query.setParameter("localId", localId);
				query.setCacheable(true);				
				return query.list();
			}
		});
		if (records.size()>1){
			log.warn("findByLocalId returned more than 1 unique record for localId="+localId+" and resourceId="+resourceId);
			return records.get(0);
		}else if (records.size()==1){
			return records.get(0);
		}else{
			return null;
		}
	}

	public void flagAsDeleted(Long resourceId) {
		//FIXME: not sure if it is a good idea to mix JDBC and Hibernate...
		Connection con = this.getSession().connection();		
		String sql = "UPDATE DarwinCore SET deleted=false WHERE resource_id = "+resourceId;
		try {
			PreparedStatement ps = con.prepareStatement(sql);
			int rowsupdated = ps.executeUpdate(sql);
			log.info(rowsupdated+" DarwinCore records of resource "+resourceId+" were flagged as deleted.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateIsDeleted(Long id, boolean isDeleted) {
		//FIXME: not sure if it is a good idea to mix JDBC and Hibernate...
		Connection con = this.getSession().connection();		
		String sql = "UPDATE DarwinCore SET deleted="+isDeleted+" WHERE id = "+id;
		try {
			PreparedStatement ps = con.prepareStatement(sql);
			ps.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
