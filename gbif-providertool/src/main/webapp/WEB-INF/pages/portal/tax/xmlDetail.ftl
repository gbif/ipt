<?xml version='1.0' encoding='utf-8'?>
<record guid="${record.guid}" xmlns="http://ipt.gbif.org" xmlns:dwc="http://rs.tdwg.org/dwc/terms/" ${nsr.xmlnsDef()}>
<#escape x as x?xml>
	<dwc:TaxonID>${taxon.guid}</dwc:TaxonID>
	<dwc:ScientificName>${taxon.scientificName!}</dwc:ScientificName>
	<dwc:Rank>${taxon.rank!}</dwc:Rank>
	<dwc:TaxonomicStatus>${taxon.taxonomicStatus!}</dwc:TaxonomicStatus>
	<dwc:NomenclaturalStatus>${taxon.nomenclaturalStatus!}</dwc:NomenclaturalStatus>
	<dwc:NomenclaturalCode>${taxon.code!}</dwc:NomenclaturalCode>
	<#if taxon.getParent()??>
	<dwc:HigherTaxon>${(taxon.getParent().guid)!}</dwc:HigherTaxon>
	<#else>
	<dwc:AcceptedTaxon>
		<dwc:TaxonID>${taxon.getAcceptedTaxon().guid}</dwc:TaxonID>
		<dwc:ScientificName>${taxon.getAcceptedTaxon().scientificName!}</dwc:ScientificName>
	</dwc:AcceptedTaxon>
	</#if>
	<#if taxon.getBasionym()??>
	<dwc:Basionym>
		<dwc:TaxonID>${taxon.getBasionym().guid}</dwc:TaxonID>
		<dwc:ScientificName>${taxon.getBasionym().scientificName!}</dwc:ScientificName>
	</dwc:Basionym>
	</#if>
	<dwc:Remarks>${taxon.notes!}</dwc:Remarks>

  <synonyms>
	<#list synonyms as s>
	<synonym>
		<dwc:TaxonID>${s.guid}</dwc:TaxonID>
		<dwc:ScientificName>${s.scientificName}</dwc:ScientificName>
		<dwc:TaxonomicStatus>${s.taxonomicStatus!}</dwc:TaxonomicStatus>
		<dwc:NomenclaturalStatus>${s.nomenclaturalStatus!}</dwc:NomenclaturalStatus>
		<dwc:Remarks>${s.notes!}</dwc:Remarks>
	</synonym>
	</#list>
  </synonyms>
  
  <#include "/WEB-INF/pages/tapir/model/extensionRecords.ftl">

</#escape>
</record>
