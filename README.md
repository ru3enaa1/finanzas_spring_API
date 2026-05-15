<div align="center">

# PFM · Personal Finance Management

**Una plataforma de finanzas personales pensada para Latinoamérica con proyección europea.**

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf)](https://www.thymeleaf.org/)
[![Maven](https://img.shields.io/badge/Maven-3-C71A36?logo=apachemaven)](https://maven.apache.org/)

</div>

---

## ¿Qué es PFM?

PFM es una aplicación web de gestión financiera personal que combina **simplicidad de Nubank**, **claridad de Apple Cash** y **rigor de YNAB** para darte el control real de tu dinero en una sola pantalla.

Diseñada para usuarios hispanohablantes — sin tecnicismos contables, sin curva de aprendizaje, sin fricción.

```
┌─────────────────────────────────────────────────┐
│  💰 Saldo                    $ 4,250,000  COP   │
│                                                 │
│  ╴ Este mes ╴      ╴ Filtrar ╴                  │
│  +2,300,000 ingresos              8 movimientos │
│  -1,150,000 gastos                              │
└─────────────────────────────────────────────────┘
```

---

## Funcionalidades principales

### 🏦 Cuentas financieras
- Registra tantas cuentas como necesites: corriente, ahorro, tarjeta de crédito, efectivo o inversión
- Selección de cuenta activa global desde el topbar — toda la app se adapta al contexto
- Recálculo automático e inmediato del saldo con cada movimiento

### 💸 Transacciones
- Modal de registro **simplificado** estilo Apple Cash — solo monto y categoría
- Vinculación opcional a presupuestos para tracking automático
- Timeline agrupado por fecha con filtros dinámicos por periodo (Hoy / Semana / Mes / Todo) y categoría
- Conversión de divisa en tiempo real sin alterar datos históricos

### 📊 Presupuestos inteligentes
- **Dos tipos en uno**: límite mensual variable (Mercado, Salidas) o pago fijo recurrente (Arriendo, Netflix)
- Barras horizontales con código de color gradual: verde → amarillo → naranja → rojo
- Click directo para añadir un gasto al presupuesto sin salir del módulo
- Pre-llenado automático de montos para pagos recurrentes
- Contexto dual: ve el límite global Y cuánto aporta tu cuenta activa (estilo YNAB)

### 🎯 Fondos de ahorro (bolsas)
- Define metas anuales para Vacaciones, Vehículo, Casa propia o lo que necesites
- Cada bolsa con su color único — barras horizontales con progreso visual hacia la meta
- Click directo para aportar a la bolsa sin salir del módulo
- Aportes generan transacciones reales (el saldo baja, refleja realidad económica)
- Indicador "Meta alcanzada" con glow dorado cuando llegas al 100%
- Archivado en lugar de borrado: los aportes históricos se preservan
- Contexto dual igual que presupuestos: ves el total aportado Y cuánto desde la cuenta activa

### 🌍 Multi-divisa global
- Cambia entre **COP, USD y EUR** desde un solo chip en el topbar
- Todos los saldos, transacciones y presupuestos se reconvierten al instante
- Almacenamiento siempre en moneda base — sin ambigüedades históricas

### 🔐 Seguridad
- Autenticación con Spring Security + BCrypt
- Protección CSRF en todos los formularios
- Validación anti-IDOR en cada endpoint
- Whitelist de redirects para prevenir open-redirect

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| **Lenguaje** | Java 17 |
| **Framework** | Spring Boot 3.x (Web + Security + Data JPA) |
| **ORM** | Hibernate 6 |
| **Base de datos** | MySQL 8 |
| **Plantillas** | Thymeleaf 3 |
| **Build** | Maven |
| **Frontend** | HTML5 + CSS3 modular + Vanilla JS |

---

## Arquitectura

Patrón MVC clásico de Spring Boot con separación estricta de responsabilidades:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Controller  │ →  │   Service    │ →  │  Repository  │ →  │   Database   │
│ (HTTP layer) │    │  (Business)  │    │  (JPA/SQL)   │    │   (MySQL)    │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
        ↓
┌──────────────┐
│  Thymeleaf   │
│   Template   │
└──────────────┘
```

Para detalles técnicos profundos de cada módulo, consulta [DOCUMENTACION.md](DOCUMENTACION.md).

---

## Setup local

### Requisitos

- **Java 17** o superior
- **MySQL 8** corriendo en localhost:3306
- **Maven 3.x**

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/ru3enaa1/finanzas_spring_API.git
   cd finanzas_spring_API
   ```

2. **Crear la base de datos**
   ```bash
   mysql -u root -p < database/schema.sql
   ```

3. **Configurar credenciales locales**
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   # Edita el archivo con tus credenciales MySQL
   ```

4. **Compilar y ejecutar**
   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

5. **Abrir en el navegador**
   ```
   http://localhost:8080
   ```

---


## Estructura del proyecto

```
finanzas_spring_API/
├── database/
│   └── schema.sql              # Estructura limpia de la BD
├── src/
│   ├── main/
│   │   ├── java/com/app/finanzas/
│   │   │   ├── config/         # Spring Security, ControllerAdvice, rutas
│   │   │   ├── controller/     # Endpoints HTTP (web + REST API)
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # Modelos JPA
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   └── service/        # Lógica de negocio
│   │   └── resources/
│   │       ├── application.properties.example
│   │       ├── static/css/     # Hojas de estilo modulares
│   │       └── templates/      # Vistas Thymeleaf
│   └── test/                   # Tests unitarios
├── DOCUMENTACION.md            # Documentación técnica detallada
├── README.md
└── pom.xml
```

---

## Roadmap

Esta versión establece las bases del producto: cuentas, transacciones, presupuestos, fondos de ahorro y un sistema multi-divisa robusto.

Las próximas iteraciones traerán capacidades pensadas para ampliar el alcance del producto:

- 📈 **Módulo de informes** descargables (PDF / Excel / CSV) con histórico mensual y anual
- 🔔 **Sistema de alertas y recordatorios** automáticos por umbrales de presupuesto y metas alcanzadas
- 🏢 **Modo empresa** para pequeñas y medianas organizaciones
- 🤖 **Asistente IA financiero** contextualizado al estado real del usuario
- 📱 **App móvil nativa** complementaria

> Cada función se desarrolla en una rama feature separada y se prueba aisladamente antes de integrarse a `main`. La rama `main` mantiene siempre la versión estable y desplegable.

---

## Convenciones de desarrollo

- **Branches**: `feat/<modulo>` para nuevas funcionalidades, `fix/<bug>` para correcciones
- **Commits**: descriptivos y atómicos. Cambios grandes se dividen en commits lógicos
- **Migraciones**: cada cambio de schema se versiona en `database/` con número incremental (v3, v4, ...)
- **Sin secrets en repo**: credenciales locales vía `application.properties` (gitignored), nunca commiteadas

---

## Licencia

Proyecto académico desarrollado como parte del programa de formación ADSO. Uso educativo y de portafolio. Los logos, marcas y nombres referenciados pertenecen a sus respectivos propietarios.

---

<div align="center">

*Construido con Spring Boot y atención al detalle. Pensado para escalar.*

</div>
