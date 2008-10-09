--
-- Please make sure you run this script first before creating the full IPT SCHEMA. 
--      see IPT-DDL.SQL
-- here non-hibernate tables are created.
--      data is inserted via file IPT-DATA.SQL
--


ALTER DATABASE ipt_test DEFAULT CHARACTER SET utf8;


DROP TABLE IF EXISTS `ext_dwc_dwcgeospatial`;
CREATE TABLE `ext_dwc_dwcgeospatial` (
  `coreid` bigint(20) NOT NULL,
  `resource_fk` bigint(20) NOT NULL,
  `decimal_latitude` varchar(128) default NULL,
  `decimal_longitude` varchar(128) default NULL,
  `geodetic_datum` varchar(128) default NULL,
  `coordinate_uncertainty_in_meters` varchar(128) default NULL,
  `footprint_wkt` varchar(128) default NULL,
  `footprint_spatial_fit` varchar(128) default NULL,
  `verbatim_coordinates` varchar(128) default NULL,
  `verbatim_latitude` varchar(128) default NULL,
  `verbatim_longitude` varchar(128) default NULL,
  `verbatim_coordinate_system` varchar(128) default NULL,
  `georeference_protocol` varchar(128) default NULL,
  `georeference_sources` varchar(128) default NULL,
  `georeference_verification_status` varchar(128) default NULL,
  `georeference_remarks` varchar(128) default NULL,
  KEY `coreid` (`coreid`),
  KEY `resource_fk` (`resource_fk`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
