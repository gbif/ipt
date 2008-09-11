--
-- Temporary table structure for view `view_ogc_dwc`
--
DROP TABLE IF EXISTS `view_ogc_dwc`;
DROP VIEW IF EXISTS `view_ogc_dwc`;


--
-- Temporary table structure for view `view_ogc_dwc`
--
DROP TABLE IF EXISTS `view_ogc_dwc`;
DROP VIEW IF EXISTS `view_ogc_dwc`;
CREATE TABLE `view_ogc_dwc` (
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
-- Definition of view `view_ogc_dwc`
--

DROP TABLE IF EXISTS `view_ogc_dwc`;
DROP VIEW IF EXISTS `view_ogc_dwc`;
CREATE ALGORITHM=UNDEFINED DEFINER=`providertool`@`localhost` SQL SECURITY DEFINER VIEW `view_ogc_dwc` AS select `dwc`.`resource_fk` AS `resourceId`,`t`.`kingdom` AS `kingdom`,`t`.`phylum` AS `phylum`,`t`.`classs` AS `classs`,`t`.`orderrr` AS `orderrr`,`t`.`family` AS `family`,`t`.`genus` AS `genus`,`t`.`scientific_name` AS `scientificName`,`dwc`.`basis_of_record` AS `basisOfRecord`,`dwc`.`lat` AS `latitude`,`dwc`.`lon` AS `longitude` from (`dwcore` `dwc` join `dwcore_tax` `t` on((`t`.`id` = `dwc`.`id`)));
