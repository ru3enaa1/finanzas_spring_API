-- Schema actualizado para finanzas_personales
-- Compatible con MySQL 8.x

DROP TABLE IF EXISTS `registro_gasto_fijo`;
DROP TABLE IF EXISTS `transaccion`;
DROP TABLE IF EXISTS `registro_fondo`;
DROP TABLE IF EXISTS `resumen_mensual`;
DROP TABLE IF EXISTS `informe`;
DROP TABLE IF EXISTS `presupuesto`;
DROP TABLE IF EXISTS `fondo`;
DROP TABLE IF EXISTS `gasto_fijo`;
DROP TABLE IF EXISTS `cuenta`;
DROP TABLE IF EXISTS `alerta`;
DROP TABLE IF EXISTS `usuario`;

CREATE TABLE `usuario` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  `apellido` VARCHAR(40) DEFAULT NULL,
  `correo` VARCHAR(50) NOT NULL,
  `contrasena` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `alerta` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `descripcion` VARCHAR(100) NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alerta_usuario` (`usuario_id`),
  CONSTRAINT `fk_alerta_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cuenta` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `tipo` VARCHAR(50) NOT NULL,
  `saldo` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cuenta_usuario` (`usuario_id`),
  CONSTRAINT `fk_cuenta_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fondo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `monto_anual` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_fondo_usuario` (`usuario_id`),
  CONSTRAINT `fk_fondo_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `gasto_fijo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `monto_estimado` DECIMAL(12,2) NOT NULL,
  `categoria` VARCHAR(50) DEFAULT NULL,
  `descripcion` VARCHAR(200) DEFAULT NULL,
  `dia_vencimiento` TINYINT DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_gasto_fijo_usuario` (`usuario_id`),
  CONSTRAINT `fk_gasto_fijo_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `informe` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `tipo` ENUM('Mensual','Anual') NOT NULL,
  `contenido` TEXT NOT NULL,
  `fecha_generacion` DATE NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_informe_usuario` (`usuario_id`),
  CONSTRAINT `fk_informe_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `presupuesto` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `categoria` VARCHAR(50) NOT NULL,
  `monto_maximo` DECIMAL(12,2) NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_presupuesto_usuario` (`usuario_id`),
  CONSTRAINT `fk_presupuesto_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `registro_fondo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fondo_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `monto_aportado` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_registro_fondo_fondo` (`fondo_id`),
  CONSTRAINT `fk_registro_fondo_fondo` FOREIGN KEY (`fondo_id`) REFERENCES `fondo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `registro_gasto_fijo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `gasto_fijo_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `pagado` TINYINT(1) NOT NULL DEFAULT 0,
  `monto_pagado` DECIMAL(12,2) DEFAULT NULL,
  `fecha_pago` DATE DEFAULT NULL,
  `alerta_generada` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registro_gasto_mes` (`gasto_fijo_id`,`anio`,`mes`),
  KEY `idx_registro_gasto_fijo_gasto` (`gasto_fijo_id`),
  CONSTRAINT `fk_registro_gasto_fijo_gasto` FOREIGN KEY (`gasto_fijo_id`) REFERENCES `gasto_fijo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `resumen_mensual` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `total_ingresos` DECIMAL(12,2) NOT NULL,
  `total_gastos` DECIMAL(12,2) NOT NULL,
  `total_aportes_fondos` DECIMAL(12,2) NOT NULL,
  `ahorro_neto` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resumen_usuario_mes` (`usuario_id`,`anio`,`mes`),
  CONSTRAINT `fk_resumen_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transaccion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `cuenta_id` INT NOT NULL,
  `tipo` VARCHAR(10) NOT NULL,
  `categoria` VARCHAR(50) NOT NULL,
  `fecha` DATE NOT NULL,
  `monto` DECIMAL(12,2) NOT NULL,
  `descripcion` VARCHAR(100) DEFAULT NULL,
  `fijo` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_transaccion_cuenta` (`cuenta_id`),
  CONSTRAINT `fk_transaccion_cuenta` FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
