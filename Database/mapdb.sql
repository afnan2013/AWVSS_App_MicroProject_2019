-- phpMyAdmin SQL Dump
-- version 4.7.9
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Jun 13, 2019 at 06:53 PM
-- Server version: 5.7.21
-- PHP Version: 5.6.35

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `mapdb`
--

-- --------------------------------------------------------

--
-- Table structure for table `tbl_current_location`
--

DROP TABLE IF EXISTS `tbl_current_location`;
CREATE TABLE IF NOT EXISTS `tbl_current_location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lat` varchar(45) NOT NULL,
  `lon` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tbl_current_location`
--

INSERT INTO `tbl_current_location` (`id`, `lat`, `lon`) VALUES
(1, '180', '180');

-- --------------------------------------------------------

--
-- Table structure for table `tbl_destintion_location`
--

DROP TABLE IF EXISTS `tbl_destintion_location`;
CREATE TABLE IF NOT EXISTS `tbl_destintion_location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lat` varchar(45) NOT NULL,
  `lon` varchar(45) NOT NULL,
  `status` varchar(5) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tbl_destintion_location`
--

INSERT INTO `tbl_destintion_location` (`id`, `lat`, `lon`, `status`) VALUES
(1, '22.989898900', '89.090988800', '0'),
(2, '22.877788800', '89.090988800', '0'),
(3, '22.0007888', '89.1119888', '1');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
