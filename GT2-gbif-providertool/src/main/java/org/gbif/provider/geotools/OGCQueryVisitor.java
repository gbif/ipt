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
	protected Log logger = LogFactory.getLog(this.getClass());
	
	// the features of interest
	protected Integer resourceId;
	protected String kingdom;
	protected String phylum;
	protected String klass;
	protected String order;
	protected String family;
	protected String genus;
	protected String scientificName;
	protected String basisOfRecord;
	protected Coordinate[] coords;
	
	// sax style capturing toggle
	// 0 = no capture
	// 1 = capture resource
	// 2 = capture kingdom
	// 3 = capture phylum
	// 4 = capture class
	// 5 = capture order
	// 6 = capture family
	// 7 = capture genus
	// 8 = capture scientific name
	// 9 = capture basis of record
	// 10 = capture coords
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
				} catch (NumberFormatException e) {
					logger.warn("Ignoring invalid resource id from request: " + resourceIdAsString);
				}
			}
		} else if (capture==2) 
			kingdom = lit.getValue().toString();
		else if (capture==3)  
			phylum = lit.getValue().toString();
		else if (capture==4)  
			klass = lit.getValue().toString();
		else if (capture==5)  
			order = lit.getValue().toString();
		else if (capture==6)  
			family = lit.getValue().toString();
		else if (capture==7)  
			genus = lit.getValue().toString();
		else if (capture==8)  
			scientificName = lit.getValue().toString();
		else if (capture==9)  
			basisOfRecord = lit.getValue().toString();
		else if (capture==10) {
			String geomString = lit.getValue().toString();
			try {
				Geometry geom =  new WKTReader().read(geomString);
				if (geom instanceof Polygon) {
					coords =  ((Polygon)geom).getCoordinates();
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
	// 1 = capture resource
	// 2 = capture kingdom
	// 3 = capture phylum
	// 4 = capture class
	// 5 = capture order
	// 6 = capture family
	// 7 = capture genus
	// 8 = capture scientific name
	// 9 = capture basis of record
	// 10 = capture coords
	 * 
	 */
	public void visit(AttributeExpression exp) {
		if (exp.getAttributePath().equals("ResourceId")) {
			capture=1;
		} else if (exp.getAttributePath().equals("Kingdom")) {
			capture=2;
		} else if (exp.getAttributePath().equals("Phylum")) {
			capture=3;
		} else if (exp.getAttributePath().equals("Class")) {
			capture=4;
		} else if (exp.getAttributePath().equals("Order")) {
			capture=5;
		} else if (exp.getAttributePath().equals("Family")) {
			capture=6;
		} else if (exp.getAttributePath().equals("Genus")) {
			capture=7;
		} else if (exp.getAttributePath().equals("ScientificName")) {
			capture=8;
		} else if (exp.getAttributePath().equals("BasisOfRecord")) {
			capture=9;
		} else if (exp.getAttributePath().equals("Geom")) {
			capture=10;
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

	public String getKingdom() {
		return kingdom;
	}

	public String getPhylum() {
		return phylum;
	}

	public String getKlass() {
		return klass;
	}

	public String getOrder() {
		return order;
	}

	public String getFamily() {
		return family;
	}

	public String getGenus() {
		return genus;
	}

	public String getScientificName() {
		return scientificName;
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
}
