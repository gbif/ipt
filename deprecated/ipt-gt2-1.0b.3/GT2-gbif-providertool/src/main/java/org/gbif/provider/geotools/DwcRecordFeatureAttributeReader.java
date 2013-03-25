package org.gbif.provider.geotools;


import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.AttributeReader;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author tim
 *
 */
@SuppressWarnings("unchecked")
public class DwcRecordFeatureAttributeReader implements AttributeReader {
	protected Log log = LogFactory.getLog(this.getClass());
	
	// for iteration
	protected int cursor=0;

	// the data
	protected List<DwcRecord> data;
	
	/**
	 * Constructor
	 */
	public DwcRecordFeatureAttributeReader(List<DwcRecord> data) {
		this.data = data;	
	}


	/**
	 * Reads the data
	 * @see org.geotools.data.AttributeReader#read(int)
	 */	
	public Object read(int index) throws ArrayIndexOutOfBoundsException {
		DwcRecord row = data.get(cursor);		
		if (index==0) 
			return row.getGuid();
		else if (index==1) 
			return row.getTaxonId();
		else if (index==2) 
			return row.getRegionId();
		else if (index==3) 
			return row.getScientificName();
		else if (index==4) 
			return row.getLocality();
		else if (index==5) 
			return row.getInstitutionCode();
		else if (index==6) 
			return row.getCollectionCode();
		else if (index==7) 
			return row.getCatalogNumber();
		else if (index==8) 
			return row.getCollector();
		else if (index==9) 
			return row.getDateCollected();
		else if (index==10) 
			return row.getBasisOfRecord();
		else if (index==11) {
			Coordinate c= new Coordinate(row.getLongitude(), row.getLatitude());
			// TODO - 4326 ???
			return new Point(c, new PrecisionModel(), 4326);
		} else {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	
	/**
	 * @see org.geotools.data.AttributeReader#close()
	 */
	public void close() throws IOException {
		data=null;
	}

	/**
	 * @see org.geotools.data.AttributeReader#getAttributeCount()
	 */
	public int getAttributeCount() {
		return JDBCDwCDatastore.type.getAttributeCount();
	}

	/**
	 * @see org.geotools.data.AttributeReader#getAttributeType(int)
	 */
	public AttributeDescriptor getAttributeType(int index) throws ArrayIndexOutOfBoundsException {
		return JDBCDwCDatastore.type.getDescriptor(index);
	}

	/**
	 * @see org.geotools.data.AttributeReader#hasNext()
	 */
	public boolean hasNext() {
		 return cursor<(data.size()-1);
	}

	/**
	 * @see org.geotools.data.AttributeReader#next()
	 */
	public void next() throws IOException {
		cursor++;
	}
}
