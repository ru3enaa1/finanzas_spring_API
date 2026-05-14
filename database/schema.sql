-- ============================================================
-- Personal Finance Management (PFM) — Schema completo
-- ============================================================
-- Este archivo contiene la estructura limpia de la base de datos
-- al estado actual (v6). Para una instalacion nueva, ejecutalo
-- de principio a fin. Las migraciones individuales (v3, v4, v5, v6)
-- al final de este archivo NO deben ejecutarse en bases nuevas —
-- solo en instalaciones existentes que vengan de versiones previas.
--
-- Configuracion de Spring: spring.jpa.hibernate.ddl-auto=none
--   (las migraciones se aplican manualmente, JPA no toca el schema)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `finanzas_personales`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `finanzas_personales`;

SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────
-- USUARIO
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  `apellido` VARCHAR(40) DEFAULT NULL,
  `correo` VARCHAR(50) NOT NULL,
  `contrasena` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- CUENTA — cuentas financieras del usuario (corriente, ahorro, tarjeta, etc.)
--   saldo:         calculado dinamicamente (saldo_inicial + SUM(transacciones))
--   saldo_inicial: lo que el usuario teclea al crear la cuenta
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `cuenta`;
CREATE TABLE `cuenta` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `nombre` VARCHAR(60) NOT NULL DEFAULT '',
  `tipo` VARCHAR(50) NOT NULL,
  `saldo` DECIMAL(10,2) NOT NULL,
  `saldo_inicial` DECIMAL(12,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `cuenta_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- PRESUPUESTO — categorias de gasto con limite mensual
--   tipo:  LIMITE = gasto variable (Mercado, Transporte)
--          FIJO   = pago recurrente (Arriendo, Netflix) — usa dia_vencimiento
--   color: hex para identificar visualmente en UI
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `presupuesto`;
CREATE TABLE `presupuesto` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `monto_estimado` DECIMAL(10,2) NOT NULL,
  `tipo` VARCHAR(10) NOT NULL DEFAULT 'FIJO',
  `color` VARCHAR(20) NOT NULL DEFAULT '#C4985A',
  `categoria` VARCHAR(50) DEFAULT NULL,
  `descripcion` VARCHAR(200) DEFAULT NULL,
  `dia_vencimiento` TINYINT DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `presupuesto_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- PRESUPUESTO_PERIODO — snapshot mensual de cada presupuesto
--   monto_limite_snapshot: limite congelado del mes
--   monto_gastado:         acumulado real de transacciones del mes
--   pagado / monto_pagado: aplica a tipo FIJO (pago recurrente)
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `presupuesto_periodo`;
CREATE TABLE `presupuesto_periodo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `presupuesto_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `monto_limite_snapshot` DECIMAL(12,2) DEFAULT 0,
  `monto_gastado` DECIMAL(12,2) NOT NULL DEFAULT 0,
  `pagado` TINYINT(1) NOT NULL DEFAULT 0,
  `monto_pagado` DECIMAL(12,2) DEFAULT NULL,
  `fecha_pago` DATE DEFAULT NULL,
  `alerta_generada` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_presupuesto_periodo_mes` (`presupuesto_id`, `anio`, `mes`),
  CONSTRAINT `fk_presupuesto_periodo_presupuesto`
      FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuesto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- TRANSACCION — ingresos/gastos contra una cuenta
--   presupuesto_id: opcional, vincula el gasto a un presupuesto
--   fecha_registro: timestamp tecnico, distinto a `fecha` (que es la fecha contable)
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `transaccion`;
CREATE TABLE `transaccion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `cuenta_id` INT DEFAULT NULL,
  `tipo` ENUM('INGRESO','GASTO') NOT NULL,
  `categoria` VARCHAR(50) NOT NULL,
  `fecha` DATE NOT NULL,
  `fecha_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `monto` DECIMAL(10,2) NOT NULL,
  `descripcion` VARCHAR(100) DEFAULT NULL,
  `fijo` TINYINT(1) NOT NULL DEFAULT 0,
  `presupuesto_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cuenta_id` (`cuenta_id`),
  KEY `fk_transaccion_presupuesto` (`presupuesto_id`),
  CONSTRAINT `transaccion_ibfk_1` FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`),
  CONSTRAINT `fk_transaccion_presupuesto`
      FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuesto` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- FONDO — metas de ahorro anuales (vacaciones, vehiculo, etc.)
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `fondo`;
CREATE TABLE `fondo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `monto_anual` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `fondo_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- REGISTRO_FONDO — aportes mensuales a cada fondo
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `registro_fondo`;
CREATE TABLE `registro_fondo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fondo_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `monto_aportado` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fondo_id` (`fondo_id`),
  CONSTRAINT `registro_fondo_ibfk_1` FOREIGN KEY (`fondo_id`) REFERENCES `fondo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- ALERTA — notificaciones generadas por el sistema
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `alerta`;
CREATE TABLE `alerta` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `descripcion` VARCHAR(100) NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `alerta_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- INFORME — informes generados por usuario (reservado para modulo futuro)
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `informe`;
CREATE TABLE `informe` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `tipo` ENUM('Mensual','Anual') NOT NULL,
  `contenido` TEXT NOT NULL,
  `fecha_generacion` DATE NOT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `informe_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ──────────────────────────────────────────────
-- RESUMEN_MENSUAL — agregado de ingresos/gastos por mes (reservado)
-- ──────────────────────────────────────────────
DROP TABLE IF EXISTS `resumen_mensual`;
CREATE TABLE `resumen_mensual` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `usuario_id` INT NOT NULL,
  `anio` INT NOT NULL,
  `mes` INT NOT NULL,
  `total_ingresos` DECIMAL(10,2) NOT NULL,
  `total_gastos` DECIMAL(10,2) NOT NULL,
  `total_aportes_fondos` DECIMAL(10,2) NOT NULL,
  `ahorro_neto` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resumen_usuario_mes` (`usuario_id`,`anio`,`mes`),
  CONSTRAINT `resumen_mensual_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- FIN del schema base.
--
-- Para instalaciones existentes que vengan de versiones previas,
-- consultar los archivos de migracion en database/migrations/
-- (v3 nombre en cuenta, v4 transaccion timestamps, v5 saldo_inicial,
-- v6 unificar gasto_fijo -> presupuesto). No los ejecutes si estas
-- instalando desde cero, ya que las tablas arriba ya estan al ultimo
-- estado.
-- ============================================================
