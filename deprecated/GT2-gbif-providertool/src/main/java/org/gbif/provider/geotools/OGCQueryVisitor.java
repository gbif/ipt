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
	protected Long taxonLft;
	protected Long taxonRgt;
	protected Long regionId;
	protected Long regionLft;
	protected Long regionRgt;
	protected String scientificName;
	protected String family;
	protected String typeStatus;
	protected String locality;
	protected String institutionCode;
	protected String collectionCode;
	protected String catalogNumber;
	protected String collector;
	protected String earliestDateCollected;
	protected String basisOfRecord;
	protected Coordinate[] coords;
	
	protected int capture = 0;

	/**
	 * Does the magic - nothing of interest except that the geom is parsed
	 * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
	 */
	public void visit(LiteralExpression lit) {
		if (capture==1){ 
			guid = lit.getValue().toString();
		}else if (capture==2){
			String taxonIdAsString = lit.getValue().toString();
			if (taxonIdAsString!=null && taxonIdAsString.length()>0) {
				try {
					taxonId = Long.parseLong(taxonIdAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid taxon id from request: " + taxonIdAsString);
				}
			}
		}else if (capture==3){
			String taxonLftAsString = lit.getValue().toString();
			if (taxonLftAsString!=null && taxonLftAsString.length()>0) {
				try {
					taxonLft = Long.parseLong(taxonLftAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid taxon lft from request: " + taxonLftAsString);
				}
			}
		}else if (capture==4){
			String taxonRgtAsString = lit.getValue().toString();
			if (taxonRgtAsString!=null && taxonRgtAsString.length()>0) {
				try {
					taxonRgt = Long.parseLong(taxonRgtAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid taxon rgt from request: " + taxonRgtAsString);
				}
			}
		}else if (capture==5){
			String regionIdAsString = lit.getValue().toString();
			if (regionIdAsString!=null && regionIdAsString.length()>0) {
				try {
					regionId = Long.parseLong(regionIdAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid region id from request: " + regionIdAsString);
				}
			}
		}else if (capture==6){
			String regionLftAsString = lit.getValue().toString();
			if (regionLftAsString!=null && regionLftAsString.length()>0) {
				try {
					regionLft = Long.parseLong(regionLftAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid region lft from request: " + regionLftAsString);
				}
			}
		}else if (capture==7){
			String regionRgtAsString = lit.getValue().toString();
			if (regionRgtAsString!=null && regionRgtAsString.length()>0) {
				try {
					regionRgt = Long.parseLong(regionRgtAsString);
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid region rgt from request: " + regionRgtAsString);
				}
			}
		}
		else if (capture==8)  
			scientificName = lit.getValue().toString();
		else if (capture==9)  
			family = lit.getValue().toString();
		else if (capture==10)  
			typeStatus = lit.getValue().toString();
		else if (capture==11)  
			locality = lit.getValue().toString();
		else if (capture==12)  
			institutionCode = lit.getValue().toString();
		else if (capture==13)  
			collectionCode = lit.getValue().toString();
		else if (capture==14)  
			catalogNumber = lit.getValue().toString();
		else if (capture==15)  
			collector = lit.getValue().toString();
		else if (capture==16)  
			earliestDateCollected = lit.getValue().toString();
		else if (capture==17)  
			basisOfRecord = lit.getValue().toString();
		else if (capture==18) {
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
		if (exp.getAttributePath().equals("SampleID")) {
			capture=1;
		} else if (exp.getAttributePath().equals("TaxonId")) {
			capture=2;
		} else if (exp.getAttributePath().equals("TaxonLft")) {
			capture=3;
		} else if (exp.getAttributePath().equals("TaxonRgt")) {
			capture=4;
		} else if (exp.getAttributePath().equals("SamplingLocationID")) {
			capture=5;
		} else if (exp.getAttributePath().equals("SamplingLocationLft")) {
			capture=6;
		} else if (exp.getAttributePath().equals("SamplingLocationRgt")) {
			capture=7;
		} else if (exp.getAttributePath().equals("ScientificName")) {
			capture=8;
		} else if (exp.getAttributePath().equals("Family")) {
			capture=9;
		} else if (exp.getAttributePath().equals("TypeStatus")) {
			capture=10;
		} else if (exp.getAttributePath().equals("Locality")) {
			capture=11;
		} else if (exp.getAttributePath().equals("InstitutionCode")) {
			capture=12;
		} else if (exp.getAttributePath().equals("CollectionCode")) {
			capture=13;
		} else if (exp.getAttributePath().equals("CatalogNumber")) {
			capture=14;
		} else if (exp.getAttributePath().equals("Collector")) {
			capture=15;
		} else if (exp.getAttributePath().equals("EarliestDateCollected")) {
			capture=16;
		} else if (exp.getAttributePath().equals("BasisOfRecord")) {
			capture=17;
		} else if (exp.getAttributePath().equals("Geom")) {
			capture=18;
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

	public Long getTaxonLft() {
		return taxonLft;
	}


	public Long getTaxonRgt() {
		return taxonRgt;
	}


	public Long getRegionLft() {
		return regionLft;
	}


	public Long getRegionRgt() {
		return regionRgt;
	}


	public String getFamily() {
		return family;
	}


	public String getTypeStatus() {
		return typeStatus;
	}


	public String getEarliestDateCollected() {
		return earliestDateCollected;
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
