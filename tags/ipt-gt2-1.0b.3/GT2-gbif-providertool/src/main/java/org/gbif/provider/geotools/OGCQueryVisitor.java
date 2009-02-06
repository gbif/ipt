package org.gbif.provider.geotools;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.visitor.AbstractFilterVisitor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * This captures all the information from the query that the service is interested in
 * DwC values
 * 
 * Note: The GT2 framework calls for use of deprecated APIs - honest!
 * 
 * @author trobertson
 */
@SuppressWarnings("deprecation")
public class OGCQueryVisitor extends AbstractFilterVisitor {
	protected Log logger = LogFactory.getLog(OGCQueryVisitor.class);
	
	// the features of interest
	protected String guid;
	protected Long taxonId;
	protected Long regionId;
	protected String scientificName;
	protected String locality;
	protected String institutionCode;
	protected String collectionCode;
	protected String catalogNumber;
	protected String collector;
	protected String dateCollected;
	protected String basisOfRecord;
	protected Coordinate[] coords;
	
	// sax style capturing toggle
	// 0 = no capture
	// 1 = capture guid;
	// 2 = capture taxonId;
	// 3 = capture regionId;
	// 4 = capture scientificName;
	// 5 = capture locality;
	// 6 = capture institutionCode;
	// 7 = capture collectionCode;
	// 8 = capture catalogNumber;
	// 9 = capture collector;
	// 10 = capture dateCollected;
	// 11 = capture basisOfRecord;
	// 12 = capture coords

	protected int capture = 0;

	/**
	 * Does the magic - nothing of interest except that the geom is parsed
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
	 */
	public void visit(LiteralExpression lit) {
		if (capture==1){ 
			guid = lit.getValue().toString();
			logger.debug("GUID from request: " + guid);
		}else if (capture==2){
			String taxonIdAsString = lit.getValue().toString();
			if (taxonIdAsString!=null && taxonIdAsString.length()>0) {
				try {
					taxonId = Long.parseLong(taxonIdAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid taxon id from request: " + taxonIdAsString);
				}
			}
		}
		else if (capture==3){
			String regionIdAsString = lit.getValue().toString();
			if (regionIdAsString!=null && regionIdAsString.length()>0) {
				try {
					regionId = Long.parseLong(regionIdAsString);
					logger.debug("regionId from request: " + regionId);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid region id from request: " + regionIdAsString);
				}
			}
		}
		else if (capture==4)  
			scientificName = lit.getValue().toString();
		else if (capture==5)  
			locality = lit.getValue().toString();
		else if (capture==6)  
			institutionCode = lit.getValue().toString();
		else if (capture==7)  
			collectionCode = lit.getValue().toString();
		else if (capture==8)  
			catalogNumber = lit.getValue().toString();
		else if (capture==9)  
			collector = lit.getValue().toString();
		else if (capture==10)  
			dateCollected = lit.getValue().toString();
		else if (capture==11)  
			basisOfRecord = lit.getValue().toString();
		else if (capture==12) {
			String geomString = lit.getValue().toString();
			try {
				Geometry geom =  new WKTReader().read(geomString);
				if (geom instanceof Polygon) {
					coords =  ((Polygon)geom).getCoordinates();
					logger.debug("coords from request: " + coords.toString());
					logger.debug("coords length from request: " + coords.length);
				}
			} catch (ParseException e) {
				// logger.severe("Error parsing geom: " + e);
				// when the SLD comes into play, this gets thrown - ignore it
			}
			
		}
		super.visit(lit);
	}
	

	public void visit(AttributeExpression exp) {
		if (exp.getAttributePath().equals("GUID")) {
			capture=1;
		} else if (exp.getAttributePath().equals("TaxonId")) {
			capture=2;
		} else if (exp.getAttributePath().equals("RegionId")) {
			capture=3;
		} else if (exp.getAttributePath().equals("ScientificName")) {
			capture=4;
		} else if (exp.getAttributePath().equals("Locality")) {
			capture=5;
		} else if (exp.getAttributePath().equals("InstitutionCode")) {
			capture=6;
		} else if (exp.getAttributePath().equals("CollectionCode")) {
			capture=7;
		} else if (exp.getAttributePath().equals("CatalogNumber")) {
			capture=8;
		} else if (exp.getAttributePath().equals("Collector")) {
			capture=9;
		} else if (exp.getAttributePath().equals("DateCollected")) {
			capture=10;
		} else if (exp.getAttributePath().equals("BasisOfRecord")) {
			capture=11;
		} else if (exp.getAttributePath().equals("Geom")) {
			capture=12;
		} else {
			capture=0;
		}
		super.visit(exp);
	}
	
	// getters follow
	public double getMinX() {
		if (coords != null && coords.length==5)
			return coords[0].x;
		return -180;
	}
	public double getMinY() {
		if (coords != null && coords.length==5)
			return coords[0].y;
		return -90;
	}
	public double getMaxX() {
		if (coords != null && coords.length==5)
			return coords[2].x;
		return 180;
	}
	public double getMaxY() {
		if (coords != null && coords.length==5)
			return coords[2].y;
		return 90;
	}

	public Log getLogger() {
		return logger;
	}

	public String getGuid() {
		return guid;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getDateCollected() {
		return dateCollected;
	}

	public String getInstitutionCode() {
		return institutionCode;
	}

	public String getCollectionCode() {
		return collectionCode;
	}

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public String getCollector() {
		return collector;
	}

	public String getBasisOfRecord() {
		return basisOfRecord;
	}

	public Coordinate[] getCoords() {
		return coords;
	}

	public int getCapture() {
		return capture;
	}

	public Long getTaxonId() {
		return taxonId;
	}

	public Long getRegionId() {
		return regionId;
	}

	public String getLocality() {
		return locality;
	}
	
}
