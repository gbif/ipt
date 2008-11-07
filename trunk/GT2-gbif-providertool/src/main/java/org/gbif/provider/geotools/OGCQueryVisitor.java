package org.gbif.provider.geotools;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.feature.type.TextualAttributeType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.visitor.AbstractFilterVisitor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
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
	protected Integer resourceId;
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
	// 1 = capture resourceId;
	// 2 = capture guid;
	// 3 = capture taxonId;
	// 4 = capture regionId;
	// 5 = capture scientificName;
	// 6 = capture locality;
	// 7 = capture institutionCode;
	// 8 = capture collectionCode;
	// 9 = capture catalogNumber;
	// 10 = capture collector;
	// 11 = capture dateCollected;
	// 12 = capture basisOfRecord;
	// 13 = capture coords

	protected int capture = 0;

	/**
	 * Does the magic - nothing of interest except that the geom is parsed
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
	 */
	public void visit(LiteralExpression lit) {
		if (capture==1) {
			String resourceIdAsString = lit.getValue().toString();
			if (resourceIdAsString!=null && resourceIdAsString.length()>0) {
				try {
					resourceId = Integer.parseInt(lit.getValue().toString());
					logger.debug("Resource id from request: " + resourceId);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid resource id from request: " + resourceIdAsString);
				}
			}
		} else if (capture==2){ 
			guid = lit.getValue().toString();
			logger.debug("GUID from request: " + guid);
		}else if (capture==3){
			String taxonIdAsString = lit.getValue().toString();
			if (taxonIdAsString!=null && taxonIdAsString.length()>0) {
				try {
					taxonId = Long.parseLong(taxonIdAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid taxon id from request: " + taxonIdAsString);
				}
			}
		}
		else if (capture==4){
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
		else if (capture==5)  
			scientificName = lit.getValue().toString();
		else if (capture==6)  
			locality = lit.getValue().toString();
		else if (capture==7)  
			institutionCode = lit.getValue().toString();
		else if (capture==8)  
			collectionCode = lit.getValue().toString();
		else if (capture==9)  
			catalogNumber = lit.getValue().toString();
		else if (capture==10)  
			collector = lit.getValue().toString();
		else if (capture==11)  
			dateCollected = lit.getValue().toString();
		else if (capture==12)  
			basisOfRecord = lit.getValue().toString();
		else if (capture==13) {
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
	
	/**
	 * Sets the capturing toggle
	// 0 = no capture
	// 1 = capture resourceId;
	// 2 = capture guid;
	// 3 = capture taxonId;
	// 4 = capture regionId;
	// 5 = capture scientificName;
	// 6 = capture locality;
	// 7 = capture institutionCode;
	// 8 = capture collectionCode;
	// 9 = capture catalogNumber;
	// 10 = capture collector;
	// 11 = capture dateCollected;
	// 12 = capture basisOfRecord;
	// 13 = capture coords
	 * 
	 */
	public void visit(AttributeExpression exp) {
		if (exp.getAttributePath().equals("ResourceId")) {
			capture=1;
		} else if (exp.getAttributePath().equals("GUID")) {
			capture=2;
		} else if (exp.getAttributePath().equals("TaxonId")) {
			capture=3;
		} else if (exp.getAttributePath().equals("RegionId")) {
			capture=4;
		} else if (exp.getAttributePath().equals("ScientificName")) {
			capture=5;
		} else if (exp.getAttributePath().equals("Locality")) {
			capture=6;
		} else if (exp.getAttributePath().equals("InstitutionCode")) {
			capture=7;
		} else if (exp.getAttributePath().equals("CollectionCode")) {
			capture=8;
		} else if (exp.getAttributePath().equals("CatalogNumber")) {
			capture=9;
		} else if (exp.getAttributePath().equals("Collector")) {
			capture=10;
		} else if (exp.getAttributePath().equals("DateCollected")) {
			capture=11;
		} else if (exp.getAttributePath().equals("BasisOfRecord")) {
			capture=12;
		} else if (exp.getAttributePath().equals("Geom")) {
			capture=13;
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

	public Integer getResourceId() {
		return resourceId;
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
