package org.gbif.provider.model.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.Rank;

public class DwcTaxon implements Comparable<DwcTaxon>{
	private Taxon taxon = new Taxon();
	private String kingdom;
	private String phylum;
	private String classs;
	private String order;
	private String family;
	private String genus;
	private String speciesEpi;
	private String infraSpeciesEpi;
	private boolean terminal;
	
	public static DwcTaxon newDwcTaxon(DarwinCore dwc){
		if (dwc == null){
			throw new NullPointerException();
		}
		DwcTaxon tax = new DwcTaxon();
		tax.taxon.setResource(dwc.getResource());
		tax.taxon.setCode(dwc.getNomenclaturalCode());
		tax.taxon.setFullname(dwc.getScientificName());
		tax.taxon.setAuthorship(dwc.getAuthorYearOfScientificName());
		tax.taxon.setRank(dwc.getInfraspecificRank());
		// 
		tax.terminal=true;
		tax.kingdom = normalizeTaxonName(dwc.getKingdom());
		tax.phylum = normalizeTaxonName(dwc.getPhylum());
		tax.classs = normalizeTaxonName(dwc.getClasss());
		tax.order = normalizeTaxonName(dwc.getOrder());
		tax.family = normalizeTaxonName(dwc.getFamily());
		tax.genus = normalizeTaxonName(dwc.getGenus());
		tax.speciesEpi = normalizeTaxonName(dwc.getSpecificEpithet());
		tax.infraSpeciesEpi = normalizeTaxonName(dwc.getInfraspecificEpithet());
		return tax;
	}
	
	/**
	 * creates a new higher dwcTaxon instance based on an existing one.
	 * It will copy the properties relevant for the new desired, higher rank
	 * @param dt
	 * @param rank of the new taxon. Determines which properties get copied
	 * @return
	 */
	public static DwcTaxon newDwcTaxon(DwcTaxon dt, Rank rank){
		if (rank.compareTo(Rank.Species)>0){
			throw new IllegalArgumentException("No ranks below species accepted");
		}
		if (dt.getDwcRank() != null && rank.compareTo(dt.getDwcRank())>0){
			throw new IllegalArgumentException("Can only clone taxa of higher or same rank than the original taxon");
		}
		DwcTaxon tax = new DwcTaxon();
		tax.taxon.setResource(dt.taxon.getResource());
		tax.taxon.setCode(dt.taxon.getCode());
		tax.taxon.setDwcRank(rank);
		tax.taxon.setRank(rank.name());
		tax.terminal=false;
		// fill target taxon depending on the requested new rank
		if (rank.compareTo(Rank.Kingdom)>=0){
			tax.setKingdom(dt.getKingdom());
			tax.taxon.setFullname(dt.getKingdom());
		}
		if (rank.compareTo(Rank.Phylum)>=0){
			tax.setPhylum(dt.getPhylum());
			tax.taxon.setFullname(dt.getPhylum());
		}
		if (rank.compareTo(Rank.Class)>=0){
			tax.setClasss(dt.getClasss());
			tax.taxon.setFullname(dt.getClasss());
		}
		if (rank.compareTo(Rank.Order)>=0){
			tax.setOrder(dt.getOrder());
			tax.taxon.setFullname(dt.getOrder());
		}
		if (rank.compareTo(Rank.Family)>=0){
			tax.setFamily(dt.getFamily());
			tax.taxon.setFullname(dt.getFamily());
		}
		if (rank.compareTo(Rank.Genus)>=0){
			tax.setGenus(dt.getGenus());
			tax.taxon.setFullname(dt.getGenus());
		}
		if (rank.compareTo(Rank.Species)>=0){
			tax.setSpeciesEpi(dt.getSpeciesEpi());
			tax.taxon.setFullname(String.format("%s %s", dt.getGenus(), dt.getSpeciesEpi()));
		}

		return tax;
	}
	
	
	/**
	 * Takes one taxon with the dwc hierarchy included and creates a list of taxa, one for each rank
	 * starting with the highest taxon kingdom if it exists. As the final list element the original taxon is included.
	 * This method creates taxa only for existing ranks, not empty strings!
	 * @param dt
	 * @return
	 */
	public static List<DwcTaxon> explodeTaxon(DwcTaxon dt){
		List<DwcTaxon> taxa = new ArrayList<DwcTaxon>();
		if (dt.getKingdom() != null){
			DwcTaxon k = DwcTaxon.newDwcTaxon(dt, Rank.Kingdom);
			taxa.add(k);
		}
		if (dt.getPhylum() != null){
			DwcTaxon p = DwcTaxon.newDwcTaxon(dt, Rank.Phylum);
			taxa.add(p);
		}
		if (dt.getClasss() != null){
			DwcTaxon c = DwcTaxon.newDwcTaxon(dt, Rank.Class);
			taxa.add(c);
		}
		if (dt.getOrder() != null){
			DwcTaxon o = DwcTaxon.newDwcTaxon(dt, Rank.Order);
			taxa.add(o);
		}
		if (dt.getFamily() != null){
			DwcTaxon f = DwcTaxon.newDwcTaxon(dt, Rank.Family);
			taxa.add(f);
		}
		if (dt.getGenus() != null){
			DwcTaxon g = DwcTaxon.newDwcTaxon(dt, Rank.Genus);
			taxa.add(g);
		}
		if (dt.getSpeciesEpi() != null && dt.getInfraSpeciesEpi() != null){
			DwcTaxon s = DwcTaxon.newDwcTaxon(dt, Rank.Species);
			taxa.add(s);
		}
		// finally add terminal taxon too
		taxa.add(dt);
		return taxa;
	}
	
	/**
	 * Removes all redundant spaces to a single one, trims the string and return null in case of an empty string
	 * @param name
	 * @return
	 */
	private static String normalizeTaxonName(String name){
		if (name == null){
			return null;
		}
		name = name.trim().replaceAll(" +", " ");
		if (name.equals("")){
			name = null;
		}
		return name;
	}
	
		
	public Taxon getTaxon() {
		return taxon;
	}

	public String getKingdom() {
		return kingdom;
	}

	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	public String getPhylum() {
		return phylum;
	}

	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	public String getClasss() {
		return classs;
	}

	public void setClasss(String classs) {
		this.classs = classs;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpeciesEpi() {
		return speciesEpi;
	}

	public void setSpeciesEpi(String speciesEpi) {
		this.speciesEpi = speciesEpi;
	}

	public String getInfraSpeciesEpi() {
		return infraSpeciesEpi;
	}

	public void setInfraSpeciesEpi(String infraSpeciesEpi) {
		this.infraSpeciesEpi = infraSpeciesEpi;
	}

	public String getAuthorship() {
		return taxon.getAuthorship();
	}

	public String getCode() {
		return taxon.getCode();
	}

	public Rank getDwcRank() {
		return taxon.getDwcRank();
	}

	public String getFullname() {
		return taxon.getFullname();
	}

	public Long getId() {
		return taxon.getId();
	}

	public Long getLft() {
		return taxon.getLft();
	}

	public String getName() {
		return taxon.getName();
	}

	public Taxon getParent() {
		return taxon.getParent();
	}

	public String getRank() {
		return taxon.getRank();
	}

	public Long getRgt() {
		return taxon.getRgt();
	}

	public void setAuthorship(String authorship) {
		taxon.setAuthorship(authorship);
	}

	public void setCode(String code) {
		taxon.setCode(code);
	}

	public void setDwcRank(Rank dwcRank) {
		taxon.setDwcRank(dwcRank);
	}

	public void setFullname(String fullname) {
		taxon.setFullname(fullname);
	}

	public void setLft(Long lft) {
		taxon.setLft(lft);
	}

	public void setName(String name) {
		taxon.setName(name);
	}

	public void setParent(Taxon parent) {
		taxon.setParent(parent);
	}

	public void setRank(String rank) {
		taxon.setRank(rank);
	}

	public void setRgt(Long rgt) {
		taxon.setRgt(rgt);
	}

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof DwcTaxon)) {
			return false;
		}
		DwcTaxon dt = (DwcTaxon) object;
        return this.hashCode() == dt.hashCode();		
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        result = 31 * result + (kingdom != null ? kingdom.hashCode() : 0);
        result = 31 * result + (phylum != null ? phylum.hashCode() : 0);
        result = 31 * result + (classs != null ? classs.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        result = 31 * result + (family != null ? family.hashCode() : 0);
        result = 31 * result + (genus != null ? genus.hashCode() : 0);
        result = 31 * result + (speciesEpi != null ? speciesEpi.hashCode() : 0);
        result = 31 * result + (infraSpeciesEpi != null ? infraSpeciesEpi.hashCode() : 0);
        
        result = 31 * result + (taxon.getCode() != null ? taxon.getCode().hashCode() : 0);
        result = 31 * result + (taxon.getFullname() != null ? taxon.getFullname().hashCode() : 0);
        result = 31 * result + (taxon.getAuthorship() != null ? taxon.getAuthorship().hashCode() : 0);
        return result;
	}

		
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(DwcTaxon object) {
		DwcTaxon myClass = (DwcTaxon) object;
		return new CompareToBuilder()
				.append(this.kingdom, myClass.kingdom)
				.append(this.phylum, myClass.phylum)
				.append(this.classs, myClass.classs)
				.append(this.order, myClass.order)
				.append(this.family,myClass.family)
				.append(this.genus,	myClass.genus)
				.append(this.genus,	myClass.genus)
				.append(this.speciesEpi, myClass.speciesEpi)
				.append(this.infraSpeciesEpi, myClass.infraSpeciesEpi)
				.append(this.taxon.getFullname(),	myClass.taxon.getFullname())
				.toComparison();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s", this.taxon.getFullname());
	}

}
