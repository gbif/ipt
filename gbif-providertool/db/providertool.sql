-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.51b


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema providertool
--

CREATE DATABASE IF NOT EXISTS providertool;
USE providertool;

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
-- Definition of table `providertool`.`DarwinCore`
--

DROP TABLE IF EXISTS `providertool`.`DarwinCore`;
CREATE TABLE  `providertool`.`DarwinCore` (
  `id` bigint(20) NOT NULL auto_increment,
  `deleted` bit(1) NOT NULL,
  `guid` varchar(128) default NULL,
  `link` varchar(128) default NULL,
  `localId` varchar(128) default NULL,
  `modified` datetime default NULL,
  `attributes` varchar(255) default NULL,
  `basisOfRecord` varchar(255) default NULL,
  `catalogNumber` varchar(255) default NULL,
  `collectionCode` varchar(255) default NULL,
  `globalUniqueIdentifier` varchar(255) default NULL,
  `imageURL` varchar(255) default NULL,
  `informationWithheld` varchar(255) default NULL,
  `institutionCode` varchar(255) default NULL,
  `latitudeAsFloat` float NOT NULL,
  `lifeStage` varchar(255) default NULL,
  `longitudeAsFloat` float NOT NULL,
  `relatedInformation` varchar(255) default NULL,
  `remarks` varchar(255) default NULL,
  `sex` varchar(255) default NULL,
  `taxon_id` bigint(20) default NULL,
  `resource_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKA416B886F4A32044` (`taxon_id`),
  KEY `FKA416B8866A2EF961` (`resource_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`DarwinCore`
--

/*!40000 ALTER TABLE `DarwinCore` DISABLE KEYS */;
LOCK TABLES `DarwinCore` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `DarwinCore` ENABLE KEYS */;


--
-- Definition of table `providertool`.`DarwinCoreLocation`
--

DROP TABLE IF EXISTS `providertool`.`DarwinCoreLocation`;
CREATE TABLE  `providertool`.`DarwinCoreLocation` (
  `id` bigint(20) NOT NULL,
  `collectingMethod` varchar(255) default NULL,
  `collector` varchar(128) default NULL,
  `continent` varchar(128) default NULL,
  `country` varchar(128) default NULL,
  `county` varchar(255) default NULL,
  `dayOfYear` varchar(16) default NULL,
  `earliestDateCollected` varchar(64) default NULL,
  `higherGeography` varchar(255) default NULL,
  `island` varchar(255) default NULL,
  `islandGroup` varchar(255) default NULL,
  `latestDateCollected` varchar(64) default NULL,
  `locality` text,
  `maximumDepthInMeters` varchar(32) default NULL,
  `maximumDepthInMetersAsInteger` int(11) default NULL,
  `maximumElevationInMeters` varchar(32) default NULL,
  `maximumElevationInMetersAsInteger` int(11) default NULL,
  `minimumDepthInMeters` varchar(32) default NULL,
  `minimumDepthInMetersAsInteger` int(11) default NULL,
  `minimumElevationInMeters` varchar(32) default NULL,
  `minimumElevationInMetersAsInteger` int(11) default NULL,
  `stateProvince` varchar(128) default NULL,
  `validDistributionFlag` varchar(16) default NULL,
  `waterBody` varchar(255) default NULL,
  `dwc_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK6EDAD65BD6EF6D66` (`dwc_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`DarwinCoreLocation`
--

/*!40000 ALTER TABLE `DarwinCoreLocation` DISABLE KEYS */;
LOCK TABLES `DarwinCoreLocation` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `DarwinCoreLocation` ENABLE KEYS */;


--
-- Definition of table `providertool`.`DarwinCoreTaxonomy`
--

DROP TABLE IF EXISTS `providertool`.`DarwinCoreTaxonomy`;
CREATE TABLE  `providertool`.`DarwinCoreTaxonomy` (
  `id` bigint(20) NOT NULL,
  `authorYearOfScientificName` varchar(255) default NULL,
  `classs` varchar(128) default NULL,
  `family` varchar(128) default NULL,
  `genus` varchar(128) default NULL,
  `higherTaxon` varchar(255) default NULL,
  `identificationQualifer` varchar(64) default NULL,
  `infraspecificEpithet` varchar(128) default NULL,
  `infraspecificRank` varchar(128) default NULL,
  `kingdom` varchar(128) default NULL,
  `nomenclaturalCode` varchar(64) default NULL,
  `orderrr` varchar(128) default NULL,
  `phylum` varchar(128) default NULL,
  `scientificName` varchar(255) default NULL,
  `specificEpithet` varchar(128) default NULL,
  `dwc_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKEDE569F7D6EF6D66` (`dwc_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`DarwinCoreTaxonomy`
--

/*!40000 ALTER TABLE `DarwinCoreTaxonomy` DISABLE KEYS */;
LOCK TABLES `DarwinCoreTaxonomy` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `DarwinCoreTaxonomy` ENABLE KEYS */;


--
-- Definition of table `providertool`.`Extension`
--

DROP TABLE IF EXISTS `providertool`.`Extension`;
CREATE TABLE  `providertool`.`Extension` (
  `id` bigint(20) NOT NULL auto_increment,
  `link` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `namespace` varchar(255) default NULL,
  `tablename` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`Extension`
--

/*!40000 ALTER TABLE `Extension` DISABLE KEYS */;
LOCK TABLES `Extension` WRITE;
INSERT INTO `providertool`.`Extension` VALUES  (1,'http://wiki.tdwg.org/twiki/bin/view/DarwinCore/DarwinCoreDraftStandard','Darwin Core','http://rs.tdwg.org/dwc/dwcore/','core/'),
 (2,'http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CuratorialExtension','DwC Curatorial','http://rs.tdwg.org/dwc/curatorial/','curatorial'),
 (3,'http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeospatialExtension','DwC Geospatial','http://rs.tdwg.org/dwc/geospatial/','geospatial'),
 (4,'http://wiki.tdwg.org/twiki/bin/view/DarwinCore/PaleontologyElement','DwC Paleontology','http://rs.tdwg.org/dwc/paleontology/','paleontology');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Extension` ENABLE KEYS */;


--
-- Definition of table `providertool`.`ExtensionProperty`
--

DROP TABLE IF EXISTS `providertool`.`ExtensionProperty`;
CREATE TABLE  `providertool`.`ExtensionProperty` (
  `id` bigint(20) NOT NULL auto_increment,
  `columnLength` int(11) NOT NULL,
  `columnName` varchar(32) default NULL,
  `link` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `qualName` varchar(255) default NULL,
  `required` bit(1) NOT NULL,
  `extension_id` bigint(20) NOT NULL,
  `property_order` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKBAE4EF14E6B2BB24` (`extension_id`)
) ENGINE=MyISAM AUTO_INCREMENT=401 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`ExtensionProperty`
--

/*!40000 ALTER TABLE `ExtensionProperty` DISABLE KEYS */;
LOCK TABLES `ExtensionProperty` WRITE;
INSERT INTO `providertool`.`ExtensionProperty` VALUES  (1,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GlobalUniqueIdentifier','GlobalUniqueIdentifier','http://rs.tdwg.org/dwc/dwcore/GlobalUniqueIdentifier',0x00,1,0),
 (2,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/DateLastModified','DateLastModified','http://rs.tdwg.org/dwc/dwcore/DateLastModified',0x00,1,1),
 (3,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/BasisOfRecord','BasisOfRecord','http://rs.tdwg.org/dwc/dwcore/BasisOfRecord',0x00,1,2),
 (4,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/InstitutionCode','InstitutionCode','http://rs.tdwg.org/dwc/dwcore/InstitutionCode',0x00,1,3),
 (5,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CollectionCode','CollectionCode','http://rs.tdwg.org/dwc/dwcore/CollectionCode',0x00,1,4),
 (6,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CatalogNumber','CatalogNumber','http://rs.tdwg.org/dwc/dwcore/CatalogNumber',0x00,1,5),
 (7,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/InformationWithheld','InformationWithheld','http://rs.tdwg.org/dwc/dwcore/InformationWithheld',0x00,1,6),
 (8,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Remarks','Remarks','http://rs.tdwg.org/dwc/dwcore/Remarks',0x00,1,7),
 (9,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/ScientificName','ScientificName','http://rs.tdwg.org/dwc/dwcore/ScientificName',0x00,1,8),
 (10,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/HigherTaxon','HigherTaxon','http://rs.tdwg.org/dwc/dwcore/HigherTaxon',0x00,1,9),
 (11,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Kingdom','Kingdom','http://rs.tdwg.org/dwc/dwcore/Kingdom',0x00,1,10),
 (12,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Phylum','Phylum','http://rs.tdwg.org/dwc/dwcore/Phylum',0x00,1,11),
 (13,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Class','Class','http://rs.tdwg.org/dwc/dwcore/Class',0x00,1,12),
 (14,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Order','Order','http://rs.tdwg.org/dwc/dwcore/Order',0x00,1,13),
 (15,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Family','Family','http://rs.tdwg.org/dwc/dwcore/Family',0x00,1,14),
 (16,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Genus','Genus','http://rs.tdwg.org/dwc/dwcore/Genus',0x00,1,15),
 (17,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/SpecificEpithet','SpecificEpithet','http://rs.tdwg.org/dwc/dwcore/SpecificEpithet',0x00,1,16),
 (18,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/InfraspecificRank','InfraspecificRank','http://rs.tdwg.org/dwc/dwcore/InfraspecificRank',0x00,1,17),
 (19,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/InfraspecificEpithet','InfraspecificEpithet','http://rs.tdwg.org/dwc/dwcore/InfraspecificEpithet',0x00,1,18),
 (20,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/AuthorYearOfScientificName','AuthorYearOfScientificName','http://rs.tdwg.org/dwc/dwcore/AuthorYearOfScientificName',0x00,1,19),
 (21,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/NomenclaturalCode','NomenclaturalCode','http://rs.tdwg.org/dwc/dwcore/NomenclaturalCode',0x00,1,20),
 (22,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/IdentificationQualifier','IdentificationQualifier','http://rs.tdwg.org/dwc/dwcore/IdentificationQualifier',0x00,1,21),
 (23,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/HigherGeography','HigherGeography','http://rs.tdwg.org/dwc/dwcore/HigherGeography',0x00,1,22),
 (24,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Continent','Continent','http://rs.tdwg.org/dwc/dwcore/Continent',0x00,1,23),
 (25,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/WaterBody','WaterBody','http://rs.tdwg.org/dwc/dwcore/WaterBody',0x00,1,24),
 (26,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/IslandGroup','IslandGroup','http://rs.tdwg.org/dwc/dwcore/IslandGroup',0x00,1,25),
 (27,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Island','Island','http://rs.tdwg.org/dwc/dwcore/Island',0x00,1,26),
 (28,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Country','Country','http://rs.tdwg.org/dwc/dwcore/Country',0x00,1,27),
 (29,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/StateProvince','StateProvince','http://rs.tdwg.org/dwc/dwcore/StateProvince',0x00,1,28),
 (30,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/County','County','http://rs.tdwg.org/dwc/dwcore/County',0x00,1,29),
 (31,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Locality','Locality','http://rs.tdwg.org/dwc/dwcore/Locality',0x00,1,30),
 (32,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/MinimumElevationInMeters','MinimumElevationInMeters','http://rs.tdwg.org/dwc/dwcore/MinimumElevationInMeters',0x00,1,31),
 (33,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/MaximumElevationInMeters','MaximumElevationInMeters','http://rs.tdwg.org/dwc/dwcore/MaximumElevationInMeters',0x00,1,32),
 (34,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/MinimumDepthInMeters','MinimumDepthInMeters','http://rs.tdwg.org/dwc/dwcore/MinimumDepthInMeters',0x00,1,33),
 (35,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/MaximumDepthInMeters','MaximumDepthInMeters','http://rs.tdwg.org/dwc/dwcore/MaximumDepthInMeters',0x00,1,34),
 (36,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CollectingMethod','CollectingMethod','http://rs.tdwg.org/dwc/dwcore/CollectingMethod',0x00,1,35),
 (37,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/ValidDistributionFlag','ValidDistributionFlag','http://rs.tdwg.org/dwc/dwcore/ValidDistributionFlag',0x00,1,36),
 (38,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/EarliestDateCollected','EarliestDateCollected','http://rs.tdwg.org/dwc/dwcore/EarliestDateCollected',0x00,1,37),
 (39,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/LatestDateCollected','LatestDateCollected','http://rs.tdwg.org/dwc/dwcore/LatestDateCollected',0x00,1,38),
 (40,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Collector','Collector','http://rs.tdwg.org/dwc/dwcore/Collector',0x00,1,39),
 (41,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Sex','Sex','http://rs.tdwg.org/dwc/dwcore/Sex',0x00,1,40),
 (42,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/LifeStage','LifeStage','http://rs.tdwg.org/dwc/dwcore/LifeStage',0x00,1,41),
 (43,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Attributes','Attributes','http://rs.tdwg.org/dwc/dwcore/Attributes',0x00,1,42),
 (44,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/ImageURL','ImageURL','http://rs.tdwg.org/dwc/dwcore/ImageURL',0x00,1,43),
 (45,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/RelatedInformation','RelatedInformation','http://rs.tdwg.org/dwc/dwcore/RelatedInformation',0x00,1,44),
 (100,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/DecimalLatitude','DecimalLatitude','http://rs.tdwg.org/dwc/geospatial/DecimalLatitude',0x00,3,0),
 (101,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/DecimalLongitude','DecimalLongitude','http://rs.tdwg.org/dwc/geospatial/DecimalLongitude',0x00,3,1),
 (102,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeodeticDatum','GeodeticDatum','http://rs.tdwg.org/dwc/geospatial/GeodeticDatum',0x00,3,2),
 (103,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CoordinateUncertaintyInMeters','CoordinateUncertaintyInMeters','http://rs.tdwg.org/dwc/geospatial/CoordinateUncertaintyInMeters',0x00,3,3),
 (104,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/FootprintWKT','FootprintWKT','http://rs.tdwg.org/dwc/geospatial/FootprintWKT',0x00,3,4),
 (105,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/FootprintSpatialFit','FootprintSpatialFit','http://rs.tdwg.org/dwc/geospatial/FootprintSpatialFit',0x00,3,5),
 (106,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimCoordinates','VerbatimCoordinates','http://rs.tdwg.org/dwc/geospatial/VerbatimCoordinates',0x00,3,6),
 (107,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimLatitude','VerbatimLatitude','http://rs.tdwg.org/dwc/geospatial/VerbatimLatitude',0x00,3,7),
 (108,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimLongitude','VerbatimLongitude','http://rs.tdwg.org/dwc/geospatial/VerbatimLongitude',0x00,3,8),
 (109,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimCoordinateSystem','VerbatimCoordinateSystem','http://rs.tdwg.org/dwc/geospatial/VerbatimCoordinateSystem',0x00,3,9),
 (110,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeoreferenceProtocol','GeoreferenceProtocol','http://rs.tdwg.org/dwc/geospatial/GeoreferenceProtocol',0x00,3,10),
 (111,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeoreferenceSources','GeoreferenceSources','http://rs.tdwg.org/dwc/geospatial/GeoreferenceSources',0x00,3,11),
 (112,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeoreferenceVerificationStatus','GeoreferenceVerificationStatus','http://rs.tdwg.org/dwc/geospatial/GeoreferenceVerificationStatus',0x00,3,12),
 (113,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GeoreferenceRemarks','GeoreferenceRemarks','http://rs.tdwg.org/dwc/geospatial/GeoreferenceRemarks',0x00,3,13),
 (200,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CatalogNumberNumeric','CatalogNumberNumeric','http://rs.tdwg.org/dwc/curatorial/CatalogNumberNumeric',0x00,2,0),
 (201,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/IdentifiedBy','IdentifiedBy','http://rs.tdwg.org/dwc/curatorial/IdentifiedBy',0x00,2,1),
 (202,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/DateIdentified','DateIdentified','http://rs.tdwg.org/dwc/curatorial/DateIdentified',0x00,2,2),
 (203,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/CollectorNumber','CollectorNumber','http://rs.tdwg.org/dwc/curatorial/CollectorNumber',0x00,2,3),
 (204,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/FieldNumber','FieldNumber','http://rs.tdwg.org/dwc/curatorial/FieldNumber',0x00,2,4),
 (205,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/FieldNotes','FieldNotes','http://rs.tdwg.org/dwc/curatorial/FieldNotes',0x00,2,5),
 (206,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimCollectingDate','VerbatimCollectingDate','http://rs.tdwg.org/dwc/curatorial/VerbatimCollectingDate',0x00,2,6),
 (207,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimElevation','VerbatimElevation','http://rs.tdwg.org/dwc/curatorial/VerbatimElevation',0x00,2,7),
 (208,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/VerbatimDepth','VerbatimDepth','http://rs.tdwg.org/dwc/curatorial/VerbatimDepth',0x00,2,8),
 (209,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Preparations','Preparations','http://rs.tdwg.org/dwc/curatorial/Preparations',0x00,2,9),
 (210,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/TypeStatus','TypeStatus','http://rs.tdwg.org/dwc/curatorial/TypeStatus',0x00,2,10),
 (211,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/GenBankNumber','GenBankNumber','http://rs.tdwg.org/dwc/curatorial/GenBankNumber',0x00,2,11),
 (212,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/OtherCatalogNumbers','OtherCatalogNumbers','http://rs.tdwg.org/dwc/curatorial/OtherCatalogNumbers',0x00,2,12),
 (213,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/RelatedCatalogedItems','RelatedCatalogedItems','http://rs.tdwg.org/dwc/curatorial/RelatedCatalogedItems',0x00,2,13),
 (214,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/Disposition','Disposition','http://rs.tdwg.org/dwc/curatorial/Disposition',0x00,2,14),
 (215,128,'colname','http://wiki.tdwg.org/twiki/bin/view/DarwinCore/IndividualCount','IndividualCount','http://rs.tdwg.org/dwc/curatorial/IndividualCount',0x00,2,15),
 (400,128,'colname',NULL,'Formation','http://rs.tdwg.org/dwc/paleontology/Formation',0x00,4,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `ExtensionProperty` ENABLE KEYS */;


--
-- Definition of table `providertool`.`ExtensionProperty_terms`
--

DROP TABLE IF EXISTS `providertool`.`ExtensionProperty_terms`;
CREATE TABLE  `providertool`.`ExtensionProperty_terms` (
  `ExtensionProperty_id` bigint(20) NOT NULL,
  `element` varchar(255) default NULL,
  `term_order` int(11) NOT NULL,
  PRIMARY KEY  (`ExtensionProperty_id`,`term_order`),
  KEY `FKBBE0FC9CA741C924` (`ExtensionProperty_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`ExtensionProperty_terms`
--

/*!40000 ALTER TABLE `ExtensionProperty_terms` DISABLE KEYS */;
LOCK TABLES `ExtensionProperty_terms` WRITE;
INSERT INTO `providertool`.`ExtensionProperty_terms` VALUES  (3,'PreservedSpecimen',0),
 (3,'FossilSpecimen',1),
 (3,'LivingSpecimen',2),
 (3,'HumanObservation',3),
 (21,'ICBN',0),
 (21,'ICZN',1),
 (21,'BC',2),
 (3,'MachineObservation',4),
 (3,'StillImage',5),
 (3,'MovingImage',6),
 (3,'SoundRecording',7),
 (3,'OtherSpecimen',8),
 (21,'ICNCP',3),
 (21,'BioCode',4);
UNLOCK TABLES;
/*!40000 ALTER TABLE `ExtensionProperty_terms` ENABLE KEYS */;


--
-- Definition of table `providertool`.`Job`
--

DROP TABLE IF EXISTS `providertool`.`Job`;
CREATE TABLE  `providertool`.`Job` (
  `id` bigint(20) NOT NULL auto_increment,
  `created` datetime default NULL,
  `dataAsJSON` text,
  `description` varchar(255) default NULL,
  `instanceId` varchar(255) default NULL,
  `jobClassName` varchar(255) default NULL,
  `jobGroup` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `nextFireTime` datetime default NULL,
  `runningGroup` varchar(255) default NULL,
  `started` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`Job`
--

/*!40000 ALTER TABLE `Job` DISABLE KEYS */;
LOCK TABLES `Job` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Job` ENABLE KEYS */;


--
-- Definition of table `providertool`.`LogEvent`
--

DROP TABLE IF EXISTS `providertool`.`LogEvent`;
CREATE TABLE  `providertool`.`LogEvent` (
  `id` bigint(20) NOT NULL auto_increment,
  `groupId` bigint(20) default NULL,
  `infoAsJSON` text,
  `instanceId` varchar(255) default NULL,
  `level` int(11) NOT NULL,
  `message` varchar(255) default NULL,
  `messageParamsAsJSON` text,
  `sourceId` int(11) NOT NULL,
  `sourceType` int(11) NOT NULL,
  `timestamp` datetime default NULL,
  `user_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK7A73ADD6F503D155` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`LogEvent`
--

/*!40000 ALTER TABLE `LogEvent` DISABLE KEYS */;
LOCK TABLES `LogEvent` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `LogEvent` ENABLE KEYS */;


--
-- Definition of table `providertool`.`PropertyMapping`
--

DROP TABLE IF EXISTS `providertool`.`PropertyMapping`;
CREATE TABLE  `providertool`.`PropertyMapping` (
  `id` bigint(20) NOT NULL auto_increment,
  `column_index` int(11) default NULL,
  `value` varchar(255) default NULL,
  `property_id` bigint(20) default NULL,
  `viewMapping_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKD66141195010B2C3` (`property_id`),
  KEY `FKD6614119F39BEC44` (`viewMapping_id`)
) ENGINE=MyISAM AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`PropertyMapping`
--

/*!40000 ALTER TABLE `PropertyMapping` DISABLE KEYS */;
LOCK TABLES `PropertyMapping` WRITE;
INSERT INTO `providertool`.`PropertyMapping` VALUES  (1,8,NULL,9,1),
 (2,2,NULL,6,1),
 (3,25,'',15,2),
 (4,2,'',4,2),
 (5,15,'',40,2),
 (6,21,'',11,2),
 (7,19,'',16,2),
 (8,10,'',31,2),
 (9,1002,'Observation',3,2),
 (10,22,'',12,2),
 (11,16,'',32,2),
 (12,23,'',13,2),
 (13,18,'',9,2),
 (14,9,'',28,2),
 (15,4,'',6,2),
 (16,24,'',14,2),
 (17,3,'',5,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `PropertyMapping` ENABLE KEYS */;


--
-- Definition of table `providertool`.`Resource`
--

DROP TABLE IF EXISTS `providertool`.`Resource`;
CREATE TABLE  `providertool`.`Resource` (
  `DTYPE` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL auto_increment,
  `created` datetime default NULL,
  `description` text,
  `guid` varchar(128) default NULL,
  `link` varchar(128) default NULL,
  `modified` datetime default NULL,
  `title` varchar(128) default NULL,
  `jdbcDriverClass` varchar(64) default NULL,
  `jdbcPassword` varchar(64) default NULL,
  `jdbcUrl` varchar(128) default NULL,
  `jdbcUser` varchar(64) default NULL,
  `lastImport` datetime default NULL,
  `recordCount` int(11) default NULL,
  `serviceName` varchar(32) default NULL,
  `coreMapping_id` bigint(20) default NULL,
  `modifier_id` bigint(20) default NULL,
  `creator_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKEF86282EA1C4CEC9` (`modifier_id`),
  KEY `FKEF86282E4FFFD554` (`creator_id`),
  KEY `FKEF86282E3BB38A9F` (`coreMapping_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`Resource`
--

/*!40000 ALTER TABLE `Resource` DISABLE KEYS */;
LOCK TABLES `Resource` WRITE;
INSERT INTO `providertool`.`Resource` VALUES  ('OccurrenceResource',1,'2008-06-23 17:10:51','Plant specimens gathered in the Toroslar mountain range of southern Turkey and the Pontic mountain range in north eastern torkey in 1999. The collection mainly covers grass vegetation plots of the subalpine level. It was collected together with many more observation records for vegetational studies applying phytosociological analysis. The resulting thesis was released in the public domain and is available at http://www.archive.org/details/VegetationskundlicheUntersuchungenInDerHochgebirgsregionDerBolkar',NULL,NULL,'2008-06-23 17:11:44','Pontaurus DB','com.mysql.jdbc.Driver','w32wfun','jdbc:mysql://localhost/pontaurus','providertool','2008-07-02 00:00:00',18439,'pontaurus',1,4,4),
 ('OccurrenceResource',2,'2008-06-25 17:10:51','The Great Backyard Bird Count is an annual four-day event that engages bird watchers of all ages in counting birds to create a real-time snapshot of where the birds are across the continent. Anyone can participate, from beginning bird watchers to experts. It takes as little as 15 minutes. It?s free, fun, and easy?and it helps the birds.\n\n        Participants count birds anywhere for as little or as long as they wish during the four-day period. They tally the highest number of birds of each species seen together at any one time. To report their counts, they fill out an online checklist at the Great Backyard Bird Count web site.\n        \n        As the count progresses, anyone with Internet access can explore what is being reported from their own towns or anywhere in the United States and Canada. They can also see how this year\'s numbers compare with those from previous years. Participants may also send in photographs of the birds they see. A selection of images is posted in the online photo gallery.\n        \n        In 2007, participants reported a record-breaking 11 million birds of 616 species. They submitted more than 80,000 checklists, an all-time record for the ten years of the count.',NULL,NULL,'2008-06-25 17:11:44','Cornell Ornithology','com.mysql.jdbc.Driver','w32wfun','jdbc:mysql://localhost/cornellornithology','providertool','2008-07-02 00:00:00',0,'cornith',2,4,4);
UNLOCK TABLES;
/*!40000 ALTER TABLE `Resource` ENABLE KEYS */;


--
-- Definition of table `providertool`.`Taxon`
--

DROP TABLE IF EXISTS `providertool`.`Taxon`;
CREATE TABLE  `providertool`.`Taxon` (
  `id` bigint(20) NOT NULL auto_increment,
  `authorship` varchar(255) default NULL,
  `fullname` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `rank` varchar(255) default NULL,
  `parent_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK4CD9EAA7FAFDA64` (`parent_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`Taxon`
--

/*!40000 ALTER TABLE `Taxon` DISABLE KEYS */;
LOCK TABLES `Taxon` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Taxon` ENABLE KEYS */;


--
-- Definition of table `providertool`.`UploadEvent`
--

DROP TABLE IF EXISTS `providertool`.`UploadEvent`;
CREATE TABLE  `providertool`.`UploadEvent` (
  `id` bigint(20) NOT NULL auto_increment,
  `executionDate` datetime default NULL,
  `recordsAdded` int(11) NOT NULL,
  `recordsChanged` int(11) NOT NULL,
  `recordsDeleted` int(11) NOT NULL,
  `recordsUploaded` int(11) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKA176B4F9D00577D2` (`resource_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`UploadEvent`
--

/*!40000 ALTER TABLE `UploadEvent` DISABLE KEYS */;
LOCK TABLES `UploadEvent` WRITE;
INSERT INTO `providertool`.`UploadEvent` VALUES  (1,'2008-01-01 00:00:00',12789,0,0,12789,1),
 (2,'2008-02-12 00:00:00',312,612,5,13096,1),
 (3,'2008-03-07 00:00:00',210,207,1,13305,1),
 (4,'2008-04-28 00:00:00',1415,2010,12,14708,1),
 (5,'2008-05-21 00:00:00',917,80,36,15589,1),
 (6,'2008-07-02 00:00:00',2891,778,41,18439,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `UploadEvent` ENABLE KEYS */;


--
-- Definition of table `providertool`.`ViewMapping`
--

DROP TABLE IF EXISTS `providertool`.`ViewMapping`;
CREATE TABLE  `providertool`.`ViewMapping` (
  `DTYPE` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL auto_increment,
  `coreIdColumnIndex` int(11) default NULL,
  `viewSql` varchar(255) default NULL,
  `guidColumnIndex` int(11) default NULL,
  `linkColumnIndex` int(11) default NULL,
  `extension_id` bigint(20) default NULL,
  `resource_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK94C3CA49E6B2BB24` (`extension_id`),
  KEY `FK94C3CA49D00577D2` (`resource_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`ViewMapping`
--

/*!40000 ALTER TABLE `ViewMapping` DISABLE KEYS */;
LOCK TABLES `ViewMapping` WRITE;
INSERT INTO `providertool`.`ViewMapping` VALUES  ('CoreViewMapping',1,1,'select * from specimen join taxon on taxon_fk=taxon_id limit 100',NULL,NULL,1,NULL),
 ('CoreViewMapping',2,1,'select * from specimen_small join taxon on taxon_fk=taxon_id',NULL,NULL,1,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `ViewMapping` ENABLE KEYS */;


--
-- Definition of table `providertool`.`app_user`
--

DROP TABLE IF EXISTS `providertool`.`app_user`;
CREATE TABLE  `providertool`.`app_user` (
  `id` bigint(20) NOT NULL auto_increment,
  `account_expired` bit(1) NOT NULL,
  `account_locked` bit(1) NOT NULL,
  `address` varchar(150) default NULL,
  `city` varchar(50) NOT NULL,
  `country` varchar(100) default NULL,
  `postal_code` varchar(15) NOT NULL,
  `province` varchar(100) default NULL,
  `credentials_expired` bit(1) NOT NULL,
  `email` varchar(255) NOT NULL,
  `account_enabled` bit(1) default NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `password_hint` varchar(255) default NULL,
  `phone_number` varchar(255) default NULL,
  `username` varchar(50) NOT NULL,
  `version` int(11) default NULL,
  `website` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`app_user`
--

/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
LOCK TABLES `app_user` WRITE;
INSERT INTO `providertool`.`app_user` VALUES  (1,0x01,0x01,'','Grannisson','US','5678','UT',0x01,'larissa@grange.com',0x01,'Larissa','La Grange','12dea96fec20593566ab75692c9949596833adc9','\"user\"','','admin',0,'http://www.lagrange.com'),
 (2,0x01,0x01,'','Aarlborg','DK','1234','Aal',0x01,'bernd@schneider.org',0x01,'Bernd','Schneider','12dea96fec20593566ab75692c9949596833adc9','\"user\"','','manager',0,'http://www.gbif.org'),
 (3,0x01,0x01,'','Quenchingen','DE','9999','nuthing',0x01,'hendrik@mailinator.com',0x01,'Hendrik','Osterfeld','12dea96fec20593566ab75692c9949596833adc9','\"user\"','','user',0,'http://www.osterfeld.com'),
 (4,0x00,0x00,'jf89e89','Krakow','AL','12345','kula',0x00,'mdoering@gbif.org',0x01,'Maria','Magdalena','e21fc56c1a272b630e0d1439079d0598cf8b8329','maria','','maria',0,'http://www.gbif.org');
UNLOCK TABLES;
/*!40000 ALTER TABLE `app_user` ENABLE KEYS */;


--
-- Definition of table `providertool`.`role`
--

DROP TABLE IF EXISTS `providertool`.`role`;
CREATE TABLE  `providertool`.`role` (
  `id` bigint(20) NOT NULL auto_increment,
  `description` varchar(64) default NULL,
  `name` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`role`
--

/*!40000 ALTER TABLE `role` DISABLE KEYS */;
LOCK TABLES `role` WRITE;
INSERT INTO `providertool`.`role` VALUES  (1,'Administrator role (can edit Users and Datasources)','ROLE_ADMIN'),
 (2,'Default role for all Users','ROLE_USER'),
 (3,'Manager role (can edit Datasources)','ROLE_MANAGER');
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;


--
-- Definition of table `providertool`.`user_role`
--

DROP TABLE IF EXISTS `providertool`.`user_role`;
CREATE TABLE  `providertool`.`user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`user_id`,`role_id`),
  KEY `FK143BF46AF503D155` (`user_id`),
  KEY `FK143BF46A4FD90D75` (`role_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `providertool`.`user_role`
--

/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
LOCK TABLES `user_role` WRITE;
INSERT INTO `providertool`.`user_role` VALUES  (1,1),
 (2,3),
 (3,2),
 (4,1),
 (4,3);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;


--
-- Definition of view `providertool`.`view_ogc_dwc`
--

DROP TABLE IF EXISTS `providertool`.`view_ogc_dwc`;
DROP VIEW IF EXISTS `providertool`.`view_ogc_dwc`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `providertool`.`view_ogc_dwc` AS select `dwc`.`resource_id` AS `resourceId`,`t`.`kingdom` AS `kingdom`,`t`.`phylum` AS `phylum`,`t`.`classs` AS `classs`,`t`.`orderrr` AS `orderrr`,`t`.`family` AS `family`,`t`.`genus` AS `genus`,`t`.`scientificName` AS `scientificName`,`dwc`.`basisOfRecord` AS `basisOfRecord`,`dwc`.`latitudeAsFloat` AS `latitude`,`dwc`.`longitudeAsFloat` AS `longitude` from (`providertool`.`DarwinCore` `dwc` join `providertool`.`DarwinCoreTaxonomy` `t` on((`t`.`id` = `dwc`.`id`)));



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
