
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `hostel_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `hostel_db`;
DROP TABLE IF EXISTS `complaints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaints` (
  `id` varchar(255) NOT NULL,
  `category` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `priority` enum('HIGH','LOW','MEDIUM') NOT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  `status` enum('IN_PROGRESS','PENDING','RESOLVED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `student_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh1cahj0r1cwkhoc2evv4y83el` (`student_id`),
  CONSTRAINT `FKh1cahj0r1cwkhoc2evv4y83el` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `complaints` WRITE;
/*!40000 ALTER TABLE `complaints` DISABLE KEYS */;
INSERT INTO `complaints` (`id`, `category`, `created_at`, `description`, `priority`, `remarks`, `status`, `updated_at`, `student_id`) VALUES ('CMP-501','Electrical','2026-11-02 13:11:21.890294','Room corridor light is flickering and needs replacement','MEDIUM','Maintenance technician assigned to replace corridor light.','IN_PROGRESS','2026-11-02 13:18:33.138190','STU-1009'),('CMP-502','Electrical','2026-11-02 13:28:35.681564','Fan making buzzing noise and light flickering in study area.','HIGH',NULL,'PENDING','2026-11-02 13:28:35.681564','STU-1010'),('CMP-503','Furniture / Woodwork','2026-11-02 13:34:21.764062','Study table chair is broken','MEDIUM','Carpentry team notified.','IN_PROGRESS','2026-11-02 13:35:09.639833','STU-1010');
/*!40000 ALTER TABLE `complaints` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `gate_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_date` date NOT NULL,
  `parent_contact` varchar(255) DEFAULT NULL,
  `room_number` varchar(255) DEFAULT NULL,
  `scan_time` datetime(6) NOT NULL,
  `student_id` varchar(255) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `entry_type` enum('IN','OUT') NOT NULL,
  `warden_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `gate_logs` WRITE;
/*!40000 ALTER TABLE `gate_logs` DISABLE KEYS */;
INSERT INTO `gate_logs` (`id`, `log_date`, `parent_contact`, `room_number`, `scan_time`, `student_id`, `student_name`, `entry_type`, `warden_id`) VALUES (1,'2026-11-02','Mother: 9876543217','Unassigned','2026-11-02 13:11:21.929552','STU-1009','Kavitha Raman','IN','WRD-GENERAL'),(2,'2026-11-02','Mother: 9876543217','Unassigned','2026-11-02 13:11:52.286497','STU-1009','Kavitha Raman','OUT','WRD-GENERAL');
/*!40000 ALTER TABLE `gate_logs` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `gate_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(1000) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `recipient_id` varchar(255) NOT NULL,
  `recipient_role` varchar(255) NOT NULL,
  `student_id` varchar(255) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `entry_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `gate_notifications` WRITE;
/*!40000 ALTER TABLE `gate_notifications` DISABLE KEYS */;
INSERT INTO `gate_notifications` (`id`, `message`, `is_read`, `recipient_id`, `recipient_role`, `student_id`, `student_name`, `timestamp`, `entry_type`) VALUES (1,'Student Kavitha Raman (STU-1009) has entered the hostel at 06:41 pm on 2026-11-02. Room: Unassigned',_binary '\0','WRD-GENERAL','WARDEN','STU-1009','Kavitha Raman','2026-11-02 13:11:21.948563','IN'),(2,'Student Kavitha Raman (STU-1009) has entered the hostel at 06:41 pm on 2026-11-02.',_binary '\0','Mother: 9876543217','PARENT','STU-1009','Kavitha Raman','2026-11-02 13:11:21.953733','IN'),(3,'Student Kavitha Raman (STU-1009) has left the hostel at 06:41 pm on 2026-11-02. Room: Unassigned',_binary '\0','WRD-GENERAL','WARDEN','STU-1009','Kavitha Raman','2026-11-02 13:11:52.289833','OUT'),(4,'Student Kavitha Raman (STU-1009) has left the hostel at 06:41 pm on 2026-11-02.',_binary '\0','Mother: 9876543217','PARENT','STU-1009','Kavitha Raman','2026-11-02 13:11:52.292457','OUT');
/*!40000 ALTER TABLE `gate_notifications` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `hostel_qr_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hostel_qr_codes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `qr_token` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKn8rm1f8v20o0f5mpksse7iuy4` (`qr_token`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `hostel_qr_codes` WRITE;
/*!40000 ALTER TABLE `hostel_qr_codes` DISABLE KEYS */;
INSERT INTO `hostel_qr_codes` (`id`, `created_at`, `qr_token`, `status`, `updated_at`) VALUES (1,'2026-11-02 13:05:01.797526','HOSTEL-HUB-COMMON-GATE-QR-2026','ACTIVE','2026-11-02 13:05:01.797526');
/*!40000 ALTER TABLE `hostel_qr_codes` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `hostel_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hostel_rules` (
  `id` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL,
  `category` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `hostel_rules` WRITE;
/*!40000 ALTER TABLE `hostel_rules` DISABLE KEYS */;
INSERT INTO `hostel_rules` (`id`, `active`, `category`, `created_at`, `description`, `title`, `updated_at`) VALUES ('RUL-1',_binary '','Timings','2026-09-02 06:47:17.257076','Hostel gates close strictly at 10:00 PM. Late entry requires prior permission letter approved by Block Warden.','Night Curfew & Entry Timings','2026-09-02 06:47:17.257076'),('RUL-2',_binary '','Security','2026-09-02 06:47:17.257076','Visitors permitted in the hostel reception lounge only between 4:00 PM and 7:00 PM. Overnight guests in rooms strictly prohibited.','Guest & Visitor Policy','2026-09-02 06:47:17.257076'),('RUL-3',_binary '','Maintenance','2026-09-02 06:47:17.257076','High-wattage appliances (heaters, electric kettles, hot plates) are banned to avoid electrical overloads.','Prohibited Electrical Items','2026-09-02 06:47:17.257076'),('RUL-4',_binary '','Dining','2026-09-02 06:47:17.257076','Breakfast: 7:30 - 9:30 AM | Lunch: 12:30 - 2:30 PM | Dinner: 7:30 - 9:30 PM. Food cannot be taken outside mess without Warden sick pass.','Mess Schedule & Dining Discipline','2026-09-02 06:47:17.257076'),('RUL-5',_binary '','General Discipline','2026-09-02 06:47:17.257076','Strict quiet hours enforced in all corridors and rooms from 11:00 PM to 6:00 AM daily.','Quiet Study Hours','2026-09-02 06:47:17.257076');
/*!40000 ALTER TABLE `hostel_rules` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `leave_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_requests` (
  `id` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_date` varchar(255) NOT NULL,
  `leave_type` varchar(255) NOT NULL,
  `parent_contact` varchar(255) NOT NULL,
  `reason` varchar(1000) NOT NULL,
  `start_date` varchar(255) NOT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `warden_remarks` varchar(1000) DEFAULT NULL,
  `student_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5m8d2wwxbf245dqhbntl7hnn1` (`student_id`),
  CONSTRAINT `FK5m8d2wwxbf245dqhbntl7hnn1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `leave_requests` WRITE;
/*!40000 ALTER TABLE `leave_requests` DISABLE KEYS */;
INSERT INTO `leave_requests` (`id`, `created_at`, `end_date`, `leave_type`, `parent_contact`, `reason`, `start_date`, `status`, `updated_at`, `warden_remarks`, `student_id`) VALUES ('REQ-801','2026-11-02 13:11:21.850951','2026-11-08','Home Leave','9876543217','Visiting hometown for festival celebration','2026-11-05','APPROVED','2026-11-02 13:18:33.092519','Approved by Block A Warden Dr. Anitha. Family emergency leave authorized.','STU-1009'),('REQ-802','2026-11-02 13:28:40.906884','2026-11-12','Home Leave','9876543211','Family function and personal work.','2026-11-10','PENDING','2026-11-02 13:28:40.907908',NULL,'STU-1010'),('REQ-803','2026-11-02 13:33:57.324655','2026-11-12','Home Leave','9876543211','Attending family wedding','2026-11-10','APPROVED','2026-11-02 13:34:50.695022','Approved. Return by Monday.','STU-1010');
/*!40000 ALTER TABLE `leave_requests` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `id` varchar(255) NOT NULL,
  `block` varchar(255) NOT NULL,
  `capacity` int NOT NULL,
  `current_occupancy` int NOT NULL,
  `floor` int NOT NULL,
  `room_number` varchar(255) NOT NULL,
  `room_type` varchar(255) NOT NULL,
  `status` enum('AVAILABLE','BOOKED','FULL','PARTIALLY_OCCUPIED','REMAINING','VACANT') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` (`id`, `block`, `capacity`, `current_occupancy`, `floor`, `room_number`, `room_type`, `status`) VALUES ('A-101','Block A',2,1,1,'101','Double AC','PARTIALLY_OCCUPIED'),('A-102','Block A',2,0,1,'102','Double Non-AC','VACANT'),('A-103','Block A',1,1,1,'103','Single AC','FULL'),('A-201','Block A',2,0,2,'201','Double AC','VACANT'),('A-202','Block A',1,1,2,'202','Single Deluxe','FULL'),('A-203','Block A',2,0,2,'203','Double Non-AC','VACANT'),('B-101','Block B',2,0,1,'101','Double AC','VACANT'),('B-102','Block B',1,0,1,'102','Single AC','VACANT'),('B-201','Block B',2,0,2,'201','Double Non-AC','VACANT'),('B-202','Block B',2,0,2,'202','Double AC','VACANT'),('B-301','Block B',1,0,3,'301','Single Deluxe','VACANT'),('C-101','Block C',2,0,1,'101','Double AC','VACANT'),('C-102','Block C',2,0,1,'102','Double Non-AC','VACANT'),('C-201','Block C',1,0,2,'201','Single AC','VACANT');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` varchar(255) NOT NULL,
  `allocation_status` varchar(255) NOT NULL,
  `department` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `emergency_contact` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `gender` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `room_needs` varchar(1000) DEFAULT NULL,
  `student_year` varchar(255) NOT NULL,
  `assigned_room_id` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe2rndfrsx22acpq2ty1caeuyw` (`email`),
  UNIQUE KEY `UKg4fwvutq09fjdlb4bb0byp7t` (`user_id`),
  KEY `FK1dmnth20otxc6vug67jgl1md4` (`assigned_room_id`),
  CONSTRAINT `FK1dmnth20otxc6vug67jgl1md4` FOREIGN KEY (`assigned_room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `FKdt1cjx5ve5bdabmuuf3ibrwaq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` (`id`, `allocation_status`, `department`, `email`, `emergency_contact`, `full_name`, `gender`, `phone`, `room_needs`, `student_year`, `assigned_room_id`, `user_id`) VALUES ('STU-1008','Allocated','it','sangeetha21@gmail.com','3456782456','sangeetha','Female','1234567897','ground floor','4th Year','A-101',14),('STU-1009','Allocated','Computer Science','kavitha.raman@gmail.com','Mother: 9876543217','Kavitha Raman','Female','9876543219','Single AC room on ground floor','2nd Year','A-103',15),('STU-1010','Allocated','Computer Science','rohit.kumar@gmail.com','Father - 9876543211','Rohit Kumar','Male','9876543210','Single Room','3rd Year','A-202',17),('STU-1011','Pending Allocation','it','saran123@gmail.com','murugan 8756456784','saranya','Female','9876543215','ac','1st Year',NULL,18),('STU-1012','Pending Allocation','ECE','deepak.raj@gmail.com','Rajendran 9876543231','Deepak Raj','Male','9876543230','Single AC room','2nd Year',NULL,19),('STU-1013','Pending Allocation','it','selvi123@gmail.com','8765789764','selvi','Female','9876543245','ground area','4th Year',NULL,20);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','STUDENT','WARDEN') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `created_at`, `enabled`, `password`, `role`, `username`) VALUES (1,'2026-09-02 06:47:16.053981',_binary '','$2a$10$NW2yqfi3WJ7QVoN0CD5MwuyzzT7IfaGx1c5mdmvc93ySfxaWCdK7m','ADMIN','admin@hms.edu'),(2,'2026-09-02 06:47:16.189256',_binary '','$2a$10$eQIraYYZ.71zcRkeHoAnwuClxR3FLmOoBARCVKsw/qH67PFkTLZd6','ADMIN','admin'),(13,'2026-09-09 04:37:32.118144',_binary '','$2a$10$f5fXtqTMSqEUUYs65D02XOtwlIoUzADrbilEnBDrR9mxY/.LaRcii','WARDEN','selvi96@gmail.com'),(14,'2026-09-09 05:27:18.860396',_binary '','$2a$10$32Kuu70MG717AS0wDag01u5p0FV67lbgDLYpX6A2HkMr6kI5lJrW6','STUDENT','sangeetha21@gmail.com'),(15,'2026-11-02 13:10:47.067303',_binary '','$2a$10$zFFwMDdAr2JgC8ywyiXx4uPiXZ5KqXr.onkNovBi61Hc9CVXv3ve6','STUDENT','kavitha.raman@gmail.com'),(16,'2026-11-02 13:18:14.269392',_binary '','$2a$10$IGTwXY47tsIzxPUHOvmoUOkh0jrbtqt6hy1bYgxM4C6JivqNuoC7G','WARDEN','anitha.warden@hms.edu'),(17,'2026-11-02 13:27:38.865389',_binary '','$2a$10$BCszxsFaHk5g4SDh3mkmHOQilphqh31yRzLeid1BVYSpWq7wKx7j.','STUDENT','rohit.kumar@gmail.com'),(18,'2026-11-02 13:46:46.476682',_binary '','$2a$10$C5aeZCSkRXqyUN2axiIzhOjSgfTaxD17be67I32rV8nXTcQbQLEeu','STUDENT','saran123@gmail.com'),(19,'2026-11-02 14:46:45.428939',_binary '','$2a$10$6LYT1MI7bv76JvaXLRjDP.2HLSnLwjp.l4Q440QbyHknFCvsEvi.e','STUDENT','deepak.raj@gmail.com'),(20,'2026-11-02 15:01:25.748146',_binary '','$2a$10$8DVOn/eoPqIMgF8WxgrVZunUWcTUHVI5yru9kpyZKjyOT19cLURCa','STUDENT','selvi123@gmail.com'),(21,'2026-11-02 21:07:16.000000',_binary '','$2a$10$NW2yqfi3WJ7QVoN0CD5MwuyzzT7IfaGx1c5mdmvc93ySfxaWCdK7m','ADMIN','admin123@gmail.com'),(22,'2026-11-02 15:47:38.054860',_binary '','$2a$10$PEaa8ZMPCOff8Tz3r8dV/OK73TsG5.cqFb5g9hi0V5kuVfKzMLlp2','WARDEN','devi123@gmail.com');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `wardens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wardens` (
  `id` varchar(255) NOT NULL,
  `assigned_block` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK96b9jhcfe3kr78ty7mriygqlc` (`email`),
  UNIQUE KEY `UKm5sux809y59vaq0rsrn59qkae` (`user_id`),
  CONSTRAINT `FK9ftb5slxj2d9hu30ytxqqrh4x` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `wardens` WRITE;
/*!40000 ALTER TABLE `wardens` DISABLE KEYS */;
INSERT INTO `wardens` (`id`, `assigned_block`, `email`, `name`, `phone`, `user_id`) VALUES ('WRD-204','Block C','selvi96@gmail.com','Mrs.Selvi M','8075217696',13),('WRD-205','Block A','anitha.warden@hms.edu','Dr. Anitha K','9876543216',16),('WRD-206','Block B','devi123@gmail.com','Devi','9658741235',22);
/*!40000 ALTER TABLE `wardens` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

