SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('ADD_SERVICE','APPROVE_CHECKOUT','APPROVE_PARTNER_POST','ASSIGN_ROLE','BACKUP_DATA','BAN_TENANT','CANCEL_INVOICE','CANCEL_RESERVATION','CHANGE_ROOM_STATUS','COMPLETE_MAINTENANCE','CONFIRM_PAYMENT','CONFIRM_RESERVATION','CREATE_CONTRACT','CREATE_EMPLOYEE','CREATE_INVOICE','CREATE_MAINTENANCE_REQUEST','CREATE_PARTNER_POST','CREATE_RESERVATION','CREATE_ROOM','CREATE_TENANT','DAMAGE_ASSESSMENT','DELETE_DATA','DELETE_PARTNER_POST','DELETE_ROOM','EXTEND_CONTRACT','FINAL_SETTLEMENT','GRANT_PERMISSION','LOGIN_FAILED','LOGIN_SUCCESS','LOGOUT','MANUAL_ADJUSTMENT','REGISTER_EMPLOYEE','REGISTER_GUEST','REGISTER_PARTNER','REGISTER_TENANT','REJECT_PARTNER_POST','REJECT_PAYMENT','REMOVE_ROLE','REMOVE_SERVICE','REVOKE_PERMISSION','SIGN_CONTRACT','SUBMIT_CHECKOUT_REQUEST','SYSTEM_AUTO_ACTION','TERMINATE_CONTRACT','UNBAN_TENANT','UPDATE_CONTRACT','UPDATE_EMPLOYEE','UPDATE_INVOICE','UPDATE_MAINTENANCE_STATUS','UPDATE_PARTNER_POST','UPDATE_PRICE','UPDATE_ROOM','UPDATE_TENANT') NOT NULL,
  `actor_id` varchar(100) NOT NULL,
  `actor_role` varchar(50) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `new_value` tinytext,
  `old_value` tinytext,
  `status` varchar(20) DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_type` varchar(50) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_actor` (`actor_id`),
  KEY `idx_action` (`action`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_timestamp` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=659 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `branches`;
CREATE TABLE `branches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `branch_code` varchar(10) DEFAULT NULL,
  `branch_name` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKaqmyw20ht3aku27r3oorfaw43` (`branch_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `checkout_requests`;
CREATE TABLE `checkout_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` enum('APPROVED','COMPLETED','PENDING','REJECTED') DEFAULT NULL,
  `contract_id` bigint DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK58lu53gnvefmnxqiybita03i7` (`contract_id`),
  KEY `FKt34lwsj96lmqw4hg515to773c` (`tenant_id`),
  CONSTRAINT `FK58lu53gnvefmnxqiybita03i7` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`),
  CONSTRAINT `FKt34lwsj96lmqw4hg515to773c` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `contract_services`;
CREATE TABLE `contract_services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_reading` decimal(38,2) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `previous_reading` decimal(38,2) DEFAULT NULL,
  `quantity` int NOT NULL,
  `start_date` date NOT NULL,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj5amvvhuo35k4sjg6f9mvo7w7` (`contract_id`),
  KEY `FK712cmy0mue4wfk1vfw4b18kw7` (`service_id`),
  CONSTRAINT `FK712cmy0mue4wfk1vfw4b18kw7` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
  CONSTRAINT `FKj5amvvhuo35k4sjg6f9mvo7w7` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `contracts`;
CREATE TABLE `contracts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_code` varchar(10) NOT NULL,
  `contract_file_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deposit` decimal(12,2) DEFAULT NULL,
  `deposit_invoice_url` varchar(255) DEFAULT NULL,
  `deposit_paid_date` datetime(6) DEFAULT NULL,
  `deposit_payment_method` enum('BANK_TRANSFER','CASH','CREDIT_CARD','MOMO') DEFAULT NULL,
  `deposit_payment_reference` varchar(255) DEFAULT NULL,
  `deposit_receipt_url` varchar(255) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `end_reminder_sent` bit(1) NOT NULL,
  `room_number` varchar(100) NOT NULL,
  `signed_contract_url` varchar(255) DEFAULT NULL,
  `start_date` date NOT NULL,
  `status` enum('ACTIVE','CANCELLED','ENDED','PENDING','SIGNED_PENDING_DEPOSIT') NOT NULL,
  `room_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKju1b0xobla9t8oexrb8lpi8jq` (`room_id`),
  KEY `FK1mvuwk3kl0okr3g8hl3lo4btx` (`tenant_id`),
  CONSTRAINT `FK1mvuwk3kl0okr3g8hl3lo4btx` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
  CONSTRAINT `FKju1b0xobla9t8oexrb8lpi8jq` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `damage_images`;
CREATE TABLE `damage_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `damage_report_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKia55fqaenwaywxiy4bujlu1gp` (`damage_report_id`),
  CONSTRAINT `FKia55fqaenwaywxiy4bujlu1gp` FOREIGN KEY (`damage_report_id`) REFERENCES `damage_reports` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `damage_reports`;
CREATE TABLE `damage_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `approver_note` longtext,
  `damage_details` longtext,
  `description` tinytext NOT NULL,
  `status` enum('APPROVED','DRAFT','REJECTED','SUBMITTED') NOT NULL,
  `total_damage_cost` decimal(12,2) NOT NULL,
  `approver_id` bigint DEFAULT NULL,
  `contract_id` bigint NOT NULL,
  `inspector_id` bigint NOT NULL,
  `settlement_invoice_id` bigint DEFAULT NULL,
  `checkout_request_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3qyc95vl8y0dj25ku6xla69m1` (`checkout_request_id`),
  KEY `FKq7q3fvwpdjv3qewa2l67tc28a` (`approver_id`),
  KEY `FK7us2gpgw1iiy9vk3mo47ulxnq` (`contract_id`),
  KEY `FK2w2fyncffosi6aviqa4308x6e` (`inspector_id`),
  CONSTRAINT `FK2w2fyncffosi6aviqa4308x6e` FOREIGN KEY (`inspector_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `FK37t2p57119yt87xjevcj9ml5n` FOREIGN KEY (`checkout_request_id`) REFERENCES `checkout_requests` (`id`),
  CONSTRAINT `FK7us2gpgw1iiy9vk3mo47ulxnq` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`),
  CONSTRAINT `FKq7q3fvwpdjv3qewa2l67tc28a` FOREIGN KEY (`approver_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `employee_code` varchar(10) DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `hire_date` date DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `position` enum('ACCOUNTANT','ADMIN','DIRECTOR','MAINTENANCE','MANAGER','RECEPTIONIST','SECURITY') DEFAULT NULL,
  `salary` decimal(12,2) DEFAULT NULL,
  `status` enum('ACTIVE','BANNED') DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `branch_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3gqbimdf7fckjbwt1kcud141m` (`username`),
  UNIQUE KEY `UKj9xgmd0ya5jmus09o0b8pqrpb` (`email`),
  UNIQUE KEY `UKetqhw9qqnad1kyjq3ks1glw8x` (`employee_code`),
  KEY `FKogwces9aes3tyw8jnks5mxesd` (`branch_code`),
  CONSTRAINT `FKogwces9aes3tyw8jnks5mxesd` FOREIGN KEY (`branch_code`) REFERENCES `branches` (`branch_code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `guest`;
CREATE TABLE `guest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `status` enum('ACTIVE','BANNED') DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl30f0fvs78rfwtjbim6nqo2cp` (`email`),
  UNIQUE KEY `UKh4e1cxt5rqxyyph3xt3gpa0uf` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `invoice_details`;
CREATE TABLE `invoice_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `description` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `invoice_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK439lfpbc6j1k0cn26wtp8f96r` (`invoice_id`),
  CONSTRAINT `FK439lfpbc6j1k0cn26wtp8f96r` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `invoices`;
CREATE TABLE `invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `billing_month` int DEFAULT NULL,
  `billing_year` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `due_date` date NOT NULL,
  `paid_date` date DEFAULT NULL,
  `paid_direct` bit(1) DEFAULT NULL,
  `payment_reference` varchar(100) DEFAULT NULL,
  `status` enum('OVERDUE','PAID','UNPAID') NOT NULL,
  `contract_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeads7q9fktwtsgdwmp1x16eqc` (`contract_id`),
  CONSTRAINT `FKeads7q9fktwtsgdwmp1x16eqc` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `maintenance_images`;
CREATE TABLE `maintenance_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) NOT NULL,
  `maintenance_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmtgnieht3i48a91ihnhb3mbq5` (`maintenance_request_id`),
  CONSTRAINT `FKmtgnieht3i48a91ihnhb3mbq5` FOREIGN KEY (`maintenance_request_id`) REFERENCES `maintenance_requests` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `maintenance_requests`;
CREATE TABLE `maintenance_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `cost` decimal(38,2) DEFAULT NULL,
  `description` tinytext NOT NULL,
  `request_code` varchar(20) NOT NULL,
  `resolution` longtext,
  `status` enum('COMPLETED','IN_PROGRESS','PENDING') NOT NULL,
  `technician_name` varchar(255) DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `invoice_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhvpr9si0ckoog99nktqt87llg` (`request_code`),
  KEY `FKcndie7sbh4o14jhu4yvro53jy` (`room_id`),
  KEY `FKhxwtbrss818eo28uqkhvnumpr` (`tenant_id`),
  CONSTRAINT `FKcndie7sbh4o14jhu4yvro53jy` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `FKhxwtbrss818eo28uqkhvnumpr` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `partner_payments`;
CREATE TABLE `partner_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) NOT NULL,
  `method` enum('BANK_TRANSFER','CASH','CREDIT_CARD','MOMO') DEFAULT NULL,
  `paid_date` datetime(6) DEFAULT NULL,
  `payment_code` varchar(20) NOT NULL,
  `partner_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK61phse2yj0b3k9mtt8srqf5el` (`payment_code`),
  KEY `FK6xrco65ww1xge7nvytjxvlv67` (`partner_id`),
  KEY `FKbm5m60nvbhareb88ewxqdoiue` (`post_id`),
  CONSTRAINT `FK6xrco65ww1xge7nvytjxvlv67` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`),
  CONSTRAINT `FKbm5m60nvbhareb88ewxqdoiue` FOREIGN KEY (`post_id`) REFERENCES `partner_posts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `partner_posts`;
CREATE TABLE `partner_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `area` decimal(5,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` tinytext NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `order_id` varchar(255) DEFAULT NULL,
  `payment_url` varchar(512) DEFAULT NULL,
  `post_type` enum('NORMAL','VIP1','VIP2','VIP3') NOT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `reject_reason` varchar(500) DEFAULT NULL,
  `status` enum('APPROVED','PENDING_APPROVAL','PENDING_PAYMENT','REJECTED') NOT NULL,
  `title` varchar(200) NOT NULL,
  `views` int NOT NULL,
  `approved_by` bigint DEFAULT NULL,
  `partner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5eun1qn3cu6bauyvdjeoiebhj` (`order_id`),
  KEY `FK6d0fvlpgqecjoyn1nfhk8kquo` (`approved_by`),
  KEY `FKkq9fm4kfsvvwhpssw44fhfn5w` (`partner_id`),
  CONSTRAINT `FK6d0fvlpgqecjoyn1nfhk8kquo` FOREIGN KEY (`approved_by`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKkq9fm4kfsvvwhpssw44fhfn5w` FOREIGN KEY (`partner_id`) REFERENCES `partners` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `partners`;
CREATE TABLE `partners` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `company_name` varchar(200) NOT NULL,
  `contact_person` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `partner_code` varchar(10) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `status` enum('ACTIVE','BANNED') DEFAULT NULL,
  `tax_code` varchar(50) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3xh6yknx9idkmird4sb1e0heq` (`username`),
  UNIQUE KEY `UK5t2oddfd2jnlns0av69cchr5l` (`email`),
  UNIQUE KEY `UKms0idr7k7tl70f6m168s9vxxb` (`partner_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `invoice_id` bigint DEFAULT NULL,
  `method` varchar(50) DEFAULT NULL,
  `processed_by` varchar(100) DEFAULT NULL,
  `provider_ref` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `post_images`;
CREATE TABLE `post_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `is_thumbnail` bit(1) NOT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmgkv9sh5mugxr58hsykaili4f` (`post_id`),
  CONSTRAINT `FKmgkv9sh5mugxr58hsykaili4f` FOREIGN KEY (`post_id`) REFERENCES `partner_posts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `reservations`;
CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_date` datetime(6) DEFAULT NULL,
  `expiration_date` datetime(6) DEFAULT NULL,
  `notes` longtext,
  `reservation_code` varchar(20) NOT NULL,
  `reservation_date` datetime(6) DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','NO_SHOW','PENDING_CONFIRMATION','RESERVED') NOT NULL,
  `visit_date` date DEFAULT NULL,
  `visit_slot` enum('AFTERNOON','MORNING') DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6g1uj544xgjuyhj6kjh6pka6l` (`reservation_code`),
  KEY `FKljt6q1tp205b0h26eiegc5mx6` (`room_id`),
  KEY `FKijxli8hjdst4on1h3au936a17` (`tenant_id`),
  CONSTRAINT `FKijxli8hjdst4on1h3au936a17` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
  CONSTRAINT `FKljt6q1tp205b0h26eiegc5mx6` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `room_images`;
CREATE TABLE `room_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `is_thumbnail` bit(1) NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtky1jnwoh1hv50m263p2vlt0y` (`room_id`),
  CONSTRAINT `FKtky1jnwoh1hv50m263p2vlt0y` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `rooms`;
CREATE TABLE `rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area` decimal(5,2) DEFAULT NULL,
  `branch_code` varchar(10) NOT NULL,
  `description` longtext,
  `price` decimal(12,2) NOT NULL,
  `room_code` varchar(20) NOT NULL,
  `room_number` varchar(100) NOT NULL,
  `status` enum('AVAILABLE','MAINTENANCE','OCCUPIED','RESERVED') NOT NULL,
  `branch_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKejc4trkinbxtajwetru2o8kdo` (`room_code`),
  KEY `FKi1eu1fwwr964lh073mt43w3g2` (`branch_id`),
  CONSTRAINT `FKi1eu1fwwr964lh073mt43w3g2` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `service_bookings`;
CREATE TABLE `service_bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_date` date NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_time` time(6) NOT NULL,
  `start_time` time(6) NOT NULL,
  `status` enum('BOOKED','CANCELED','COMPLETED') NOT NULL,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `canceled_at` datetime(6) DEFAULT NULL,
  `canceled_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_contract_service_date` (`contract_id`,`service_id`,`booking_date`),
  KEY `FK1cyr30xgaheo32v5iha15mvfn` (`service_id`),
  CONSTRAINT `FK1cyr30xgaheo32v5iha15mvfn` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
  CONSTRAINT `FKkd255uj20dbx7862xpbwsnh38` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `service_items`;
CREATE TABLE `service_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `billing_frequency` varchar(20) DEFAULT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4rsrim6cuqmc2dkowbbx23ofg` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `service_packages`;
CREATE TABLE `service_packages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(500) DEFAULT NULL,
  `duration_days` int NOT NULL,
  `is_active` bit(1) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `services`;
CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `price` decimal(12,2) NOT NULL,
  `service_name` varchar(100) NOT NULL,
  `unit` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL,
  `electric_price_per_unit` decimal(12,2) DEFAULT NULL,
  `late_fee_per_day` decimal(12,2) DEFAULT NULL,
  `momo_receiver_name` varchar(120) DEFAULT NULL,
  `momo_receiver_phone` varchar(30) DEFAULT NULL,
  `momo_receiver_qr_url` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `water_price_per_unit` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tenants`;
CREATE TABLE `tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `cccd` varchar(20) DEFAULT NULL,
  `date_of_birth` varchar(255) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `status` enum('ACTIVE','BANNED') DEFAULT NULL,
  `student_id` varchar(20) DEFAULT NULL,
  `university` varchar(100) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmcea1a7vp40m97uduo1g4xtiw` (`username`),
  UNIQUE KEY `UKmcs0f375h22cfja95y1nal5fu` (`cccd`),
  UNIQUE KEY `UK2d74g7acpo0pgsm3fbqlxiwjt` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;