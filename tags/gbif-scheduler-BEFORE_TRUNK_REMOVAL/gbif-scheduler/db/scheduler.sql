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
-- Create schema scheduler
--

CREATE DATABASE IF NOT EXISTS scheduler;
USE scheduler;

--
-- Definition of table `scheduler`.`Job`
--

DROP TABLE IF EXISTS `scheduler`.`Job`;
CREATE TABLE  `scheduler`.`Job` (
  `id` bigint(20) NOT NULL auto_increment,
  `created` datetime default NULL,
  `dataAsJSON` text,
  `description` varchar(255) default NULL,
  `instanceId` varchar(255) default NULL,
  `jobClassName` varchar(255) default NULL,
  `jobGroup` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  `nextFireTime` datetime default NULL,
  `repeatInDays` int(11) NOT NULL,
  `runningGroup` varchar(255) default NULL,
  `started` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `scheduler`.`Job`
--

/*!40000 ALTER TABLE `Job` DISABLE KEYS */;
LOCK TABLES `Job` WRITE;
INSERT INTO `scheduler`.`Job` VALUES  (-3,'2008-04-15 00:00:00','','Harvest','localhost','none','Datasoure 1','Harvest','2008-04-15 00:00:00',0,'','2008-04-15 00:00:00'),
 (-2,'2008-04-15 00:00:00','','Harvest','localhost','none','Datasoure 1','Harvest','2008-04-15 00:00:00',0,'','2008-04-15 00:00:00'),
 (-1,'2008-04-15 00:00:00','','Harvest','localhost','none','Datasoure 1','Harvest','2008-04-15 00:00:00',0,'','2008-04-15 00:00:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Job` ENABLE KEYS */;


--
-- Definition of table `scheduler`.`app_user`
--

DROP TABLE IF EXISTS `scheduler`.`app_user`;
CREATE TABLE  `scheduler`.`app_user` (
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `scheduler`.`app_user`
--

/*!40000 ALTER TABLE `app_user` DISABLE KEYS */;
LOCK TABLES `app_user` WRITE;
INSERT INTO `scheduler`.`app_user` VALUES  (-2,0x00,0x00,'','Denver','US','80210','CO',0x00,'tim@ibiodiversity.org',0x01,'Tim','Robertson','d033e22ae348aeb5660fc2140aec35850c4da997','Not a female kitty.','','admin',1,'http://www.ibiodiversity.com'),
 (-1,0x00,0x00,'','Denver','US','80210','CO',0x00,'tim@ibiodiversity.com',0x01,'Tomcat','User','12dea96fec20593566ab75692c9949596833adc9','A male kitty.','','user',1,'http://tomcat.apache.org');
UNLOCK TABLES;
/*!40000 ALTER TABLE `app_user` ENABLE KEYS */;


--
-- Definition of table `scheduler`.`role`
--

DROP TABLE IF EXISTS `scheduler`.`role`;
CREATE TABLE  `scheduler`.`role` (
  `id` bigint(20) NOT NULL auto_increment,
  `description` varchar(64) default NULL,
  `name` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `scheduler`.`role`
--

/*!40000 ALTER TABLE `role` DISABLE KEYS */;
LOCK TABLES `role` WRITE;
INSERT INTO `scheduler`.`role` VALUES  (-2,'Default role for all Users','ROLE_USER'),
 (-1,'Administrator role (can edit Users)','ROLE_ADMIN');
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;


--
-- Definition of table `scheduler`.`user_role`
--

DROP TABLE IF EXISTS `scheduler`.`user_role`;
CREATE TABLE  `scheduler`.`user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`user_id`,`role_id`),
  KEY `FK143BF46AF503D155` (`user_id`),
  KEY `FK143BF46A4FD90D75` (`role_id`),
  CONSTRAINT `FK143BF46A4FD90D75` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK143BF46AF503D155` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `scheduler`.`user_role`
--

/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
LOCK TABLES `user_role` WRITE;
INSERT INTO `scheduler`.`user_role` VALUES  (-2,-1),
 (-1,-2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
