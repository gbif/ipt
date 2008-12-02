package org.gbif.provider.model.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

public class DwcRegion implements Comparable<DwcRegion>{
	private Region region = new Region();
	private String continent;
	private String waterBody;
	private String islandGroup;
	private String island;
	private String country;
	private String stateProvince;
	private String county;
	private String locality;
	
	public static DwcRegion newDwcRegion(DarwinCore dwc){
		DwcRegion reg = new DwcRegion();
		reg.region.setResource(dwc.getResource());
		// 
		reg.continent = normalizeString(dwc.getContinent());
		reg.waterBody = normalizeString(dwc.getWaterBody());
		reg.islandGroup = normalizeString(dwc.getIslandGroup());
		reg.island = normalizeString(dwc.getIsland());
		reg.country = normalizeString(dwc.getCountry());
		reg.stateProvince = normalizeString(dwc.getStateProvince());
		reg.county = normalizeString(dwc.getCounty());
		reg.locality = normalizeString(dwc.getLocality());
		reg.setRankAndLabel();
		return reg;
	}
	
	private void setRankAndLabel() {
		if (locality != null){
			this.region.setType(RegionType.Locality);
			this.region.setLabel(locality);
		}
		else if (county != null){
			this.region.setType(RegionType.County);
			this.region.setLabel(county);
		}
		else if (stateProvince != null){
			this.region.setType(RegionType.State);
			this.region.setLabel(stateProvince);
		}
		else if (country != null){
			this.region.setType(RegionType.Country);
			this.region.setLabel(country);
		}
		else if (island != null){
			this.region.setType(RegionType.Island);
			this.region.setLabel(island);
		}
		else if (islandGroup != null){
			this.region.setType(RegionType.IslandGroup);
			this.region.setLabel(islandGroup);
		}
		else if (waterBody != null){
			this.region.setType(RegionType.Waterbody);
			this.region.setLabel(waterBody);
		}
		else if (continent != null){
			this.region.setType(RegionType.Continent);
			this.region.setLabel(continent);
		}
	}

	/**
	 * creates a new higher DwcRegion instance based on an existing one.
	 * It will copy the properties relevant for the new desired, higher rank
	 * @param orig
	 * @param rank of the new region. Determines which properties get copied
	 * @return
	 */
	public static DwcRegion newDwcRegion(DwcRegion orig, RegionType rank){
		if (orig.getType() != null && rank.compareTo(orig.getType())>0){
			throw new IllegalArgumentException("Can only clone regions of higher or same rank than the original region");
		}
		DwcRegion newRegion = new DwcRegion();
		newRegion.region.setResource(orig.region.getResource());
		newRegion.setRank(rank);

		// fill target taxon depending on the requested new rank
		if (rank.compareTo(RegionType.Continent)>=0){
			newRegion.setContinent(orig.getContinent());
			newRegion.region.setLabel(newRegion.continent);
		}
		if (rank.compareTo(RegionType.Waterbody)>=0){
			newRegion.setWaterBody(orig.getWaterBody());
			newRegion.region.setLabel(newRegion.waterBody);
		}
		if (rank.compareTo(RegionType.IslandGroup)>=0){
			newRegion.setIslandGroup(orig.getIslandGroup());
			newRegion.region.setLabel(newRegion.islandGroup);
		}
		if (rank.compareTo(RegionType.Island)>=0){
			newRegion.setIsland(orig.getIsland());
			newRegion.region.setLabel(newRegion.island);
		}
		if (rank.compareTo(RegionType.Country)>=0){
			newRegion.setCountry(orig.getCountry());
			newRegion.region.setLabel(newRegion.country);
		}
		if (rank.compareTo(RegionType.State)>=0){
			newRegion.setStateProvince(orig.getStateProvince());
			newRegion.region.setLabel(newRegion.stateProvince);
		}
		if (rank.compareTo(RegionType.County)>=0){
			newRegion.setCounty(orig.getCounty());
			newRegion.region.setLabel(newRegion.county);
		}
		if (rank.compareTo(RegionType.Locality)>=0){
			newRegion.setLocality(orig.getLocality());
			newRegion.region.setLabel(newRegion.locality);
		}

		return newRegion;
	}
	
	
	/**
	 * Takes one taxon with the dwc hierarchy included and creates a list of taxa, one for each rank
	 * starting with the highest taxon kingdom if it exists. As the final list element the original taxon is included.
	 * This method creates taxa only for existing ranks, not empty strings!
	 * @param orig
	 * @return
	 */
	public static List<DwcRegion> explodeRegions(DwcRegion orig){
		List<DwcRegion> regions = new ArrayList<DwcRegion>();
		if (orig.getContinent() != null){
			DwcRegion k = DwcRegion.newDwcRegion(orig, RegionType.Continent);
			regions.add(k);
		}
		if (orig.getWaterBody() != null){
			DwcRegion p = DwcRegion.newDwcRegion(orig, RegionType.Waterbody);
			regions.add(p);
		}
		if (orig.getIslandGroup() != null){
			DwcRegion c = DwcRegion.newDwcRegion(orig, RegionType.IslandGroup);
			regions.add(c);
		}
		if (orig.getIsland() != null){
			DwcRegion o = DwcRegion.newDwcRegion(orig, RegionType.Island);
			regions.add(o);
		}
		if (orig.getCountry() != null){
			DwcRegion f = DwcRegion.newDwcRegion(orig, RegionType.Country);
			regions.add(f);
		}
		if (orig.getStateProvince() != null){
			DwcRegion g = DwcRegion.newDwcRegion(orig, RegionType.State);
			regions.add(g);
		}
		if (orig.getCounty() != null){
			DwcRegion s = DwcRegion.newDwcRegion(orig, RegionType.County);
			regions.add(s);
		}
		if (orig.getLocality() != null){
			DwcRegion s = DwcRegion.newDwcRegion(orig, RegionType.Locality);
			regions.add(s);
		}
		return regions;
	}
	
	/**
	 * Removes all redundant spaces to a single one, trims the string and return null in case of an empty string
	 * @param name
	 * @return
	 */
	private static String normalizeString(String name){
		if (name == null){
			return null;
		}
		name = name.trim().replaceAll(" +", " ");
		if (name.equals("")){
			name = null;
		}
		return name;
	}
	
		

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getContinent() {
		return continent;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}

	public String getWaterBody() {
		return waterBody;
	}

	public void setWaterBody(String waterBody) {
		this.waterBody = waterBody;
	}

	public String getIslandGroup() {
		return islandGroup;
	}

	public void setIslandGroup(String islandGroup) {
		this.islandGroup = islandGroup;
	}

	public String getIsland() {
		return island;
	}

	public void setIsland(String island) {
		this.island = island;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStateProvince() {
		return stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public Long getId() {
		return region.getId();
	}

	public String getLabel() {
		return region.getLabel();
	}

	public Long getLft() {
		return region.getLft();
	}

	public Region getParent() {
		return region.getParent();
	}

	public RegionType getType() {
		return region.getType();
	}

	public Resource getResource() {
		return region.getResource();
	}

	public Long getRgt() {
		return region.getRgt();
	}

	public void setId(Long id) {
		region.setId(id);
	}

	public void setLabel(String label) {
		region.setLabel(label);
	}

	public void setLft(Long lft) {
		region.setLft(lft);
	}

	public void setParent(Region parent) {
		region.setParent(parent);
	}

	public void setRank(RegionType rank) {
		region.setType(rank);
	}

	public void setResource(Resource resource) {
		region.setResource(resource);
	}

	public void setRgt(Long rgt) {
		region.setRgt(rgt);
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof DwcRegion)) {
			return false;
		}
		DwcRegion dr = (DwcRegion) object;
        return this.hashCode() == dr.hashCode();		
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        result = 31 * result + (continent != null ? continent.hashCode() : 0);
        result = 31 * result + (waterBody != null ? waterBody.hashCode() : 0);
        result = 31 * result + (islandGroup != null ? islandGroup.hashCode() : 0);
        result = 31 * result + (island != null ? island.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (stateProvince != null ? stateProvince.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        if (region.getResource() != null){
            result = 31 * result + (region.getResource().getId() != null ? region.getResource().getId().hashCode() : 0);
        }
        return result;
	}

		
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(DwcRegion myClass) {
		return new CompareToBuilder()
				.append(this.continent, myClass.continent)
				.append(this.waterBody, myClass.waterBody)
				.append(this.islandGroup, myClass.islandGroup)
				.append(this.island, myClass.island)
				.append(this.country,myClass.country)
				.append(this.stateProvince,	myClass.stateProvince)
				.append(this.county,	myClass.county)
				.append(this.locality, myClass.locality)
				.append(this.region.getResource(), myClass.region.getResource())
				.toComparison();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s", this.region.getLabel());
	}

}
