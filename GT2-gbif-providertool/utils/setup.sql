-- from what Markus gave me to work with I did the following

alter table darwincore add column(latitudeAsFloat float default null);
alter table darwincore add column(longitudeAsFloat float default null);

update darwincore set resource_id=mod(id,2)+1;
update darwincore set latitudeAsFloat=mod(id,180)-90;
update darwincore set longitudeAsFloat=mod(id,360)-180;

create view VIEW_OGC_DWC as 
select 
	dwc.resource_id as resourceId, 
	kingdom, 
	phylum, 
	classs, 
	orderrr, 
	family, 
	genus, 
	scientificName, 
	basisOfRecord, 
	latitudeAsFloat as latitude, 
	longitudeAsFloat as longitude
from 
	darwincore dwc
		inner join darwincoretaxonomy t on t.id=dwc.id;
