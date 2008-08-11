
--
-- Temporary table structure for view `providertool`.`view_ogc_dwc`
--
DROP TABLE IF EXISTS `providertool`.`view_ogc_dwc`;
DROP VIEW IF EXISTS `providertool`.`view_ogc_dwc`;
CREATE TABLE `providertool`.`view_ogc_dwc` (
  `resourceId` bigint(20),
  `kingdom` varchar(128),
  `phylum` varchar(128),
  `classs` varchar(128),
  `orderrr` varchar(128),
  `family` varchar(128),
  `genus` varchar(128),
  `scientificName` varchar(255),
  `basisOfRecord` varchar(255),
  `latitude` float,
  `longitude` float
);



--
-- Definition of view `providertool`.`view_ogc_dwc`
--

DROP TABLE IF EXISTS `providertool`.`view_ogc_dwc`;
DROP VIEW IF EXISTS `providertool`.`view_ogc_dwc`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `providertool`.`view_ogc_dwc` AS select `dwc`.`resource_id` AS `resourceId`,`t`.`kingdom` AS `kingdom`,`t`.`phylum` AS `phylum`,`t`.`classs` AS `classs`,`t`.`orderrr` AS `orderrr`,`t`.`family` AS `family`,`t`.`genus` AS `genus`,`t`.`scientificName` AS `scientificName`,`dwc`.`basisOfRecord` AS `basisOfRecord`,`dwc`.`latitudeAsFloat` AS `latitude`,`dwc`.`longitudeAsFloat` AS `longitude` from (`providertool`.`DarwinCore` `dwc` join `providertool`.`DarwinCoreTaxonomy` `t` on((`t`.`id` = `dwc`.`id`)));

