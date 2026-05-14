# Documentación técnica — PFM v2

Recorrido detallado de los módulos desde **Panel de usuario** hasta **Presupuestos**, con los cambios de interfaz, lógica de negocio, migraciones de base de datos y cómo se integran en el flujo completo de la aplicación.

Este documento está pensado como guía para llenar bitácoras y entender cómo cada decisión técnica conecta con la experiencia del usuario final.

---

## Índice

1. [Arquitectura general](#1-arquitectura-general)
2. [Sistema de cuenta activa (sesión)](#2-sistema-de-cuenta-activa-sesión)
3. [Sistema de divisas multi-moneda](#3-sistema-de-divisas-multi-moneda)
4. [Panel de usuario (Dashboard)](#4-panel-de-usuario-dashboard)
5. [Creación de cuentas](#5-creación-de-cuentas)
6. [Transacciones](#6-transacciones)
7. [Presupuestos](#7-presupuestos)
8. [Migraciones de base de datos](#8-migraciones-de-base-de-datos)
9. [Patrones transversales](#9-patrones-transversales)

---

## 1. Arquitectura general

La aplicación sigue el patrón **MVC clásico de Spring Boot**:

| Capa | Responsabilidad | Ejemplos |
|---|---|---|
| **Entity** | Mapeo objeto-relacional con JPA/Hibernate | `Cuenta`, `Transaccion`, `Presupuesto` |
| **Repository** | Consultas a base de datos (Spring Data JPA) | `CuentaRepository`, `TransaccionRepository` |
| **Service** | Lógica de negocio, transacciones | `CuentaService`, `TransaccionService` |
| **Controller** | Endpoints HTTP, recibe forms, prepara modelo | `CuentaController`, `PresupuestoController` |
| **Template** | Thymeleaf renderiza HTML server-side | `cuentas/lista.html`, `presupuestos/lista.html` |

**Por qué importa:** cada módulo de la app (cuentas, transacciones, presupuestos, fondos) sigue este mismo esquema, lo que facilita mantener consistencia y añadir módulos nuevos sin tocar todo.

### Stack tecnológico

- **Java 17** — lenguaje base
- **Spring Boot 3.x** — framework principal
- **Spring Security** — autenticación y sesión
- **Spring Data JPA + Hibernate 6** — persistencia
- **Thymeleaf** — motor de plantillas server-side
- **MySQL 8** — base de datos
- **Maven** — gestión de dependencias

---

## 2. Sistema de cuenta activa (sesión)

### Qué es

El usuario puede tener **varias cuentas** (Bancolombia, BBVA, efectivo, etc.). En cada momento, solo **una está activa** y todo el contexto (saldo, transacciones que se ven, gasto añadido a un presupuesto desde cuál cuenta) gira alrededor de esa selección.

### Cómo funciona técnicamente

El ID de la cuenta activa se guarda en `HttpSession` con la clave `cuentaActivaId`. Está expuesto globalmente a todas las vistas mediante un `@ControllerAdvice`:

- **`GlobalModelAttributes.java`** — un `@ControllerAdvice` que añade al modelo de cualquier petición las variables `cuentaActivaId`, `cuentaActivaNombre`, `cuentas`, `monedaActual`, `tasaActual`, `simboloMoneda`. Esto evita que cada controller las pase manualmente.
- **Auto-resolución**: si la sesión no tiene `cuentaActivaId` pero el usuario tiene cuentas, el sistema elige automáticamente la primera y la persiste en sesión. Sin esto, recién logueado el formulario de transacción decía "Sin cuenta activa" hasta que el usuario hiciera click en el topbar — bug que se corrigió.
- **Endpoint `POST /cuentas/seleccionar`** — cambia la cuenta activa. Recibe `cuentaId` y `redirect` (URL de retorno). Valida que la cuenta pertenezca al usuario logueado (anti-IDOR).

### Cambio de cuenta sin perder contexto

Cuando el usuario está en `/presupuestos` y cambia de cuenta desde el chip del topbar, el sistema lo **mantiene en `/presupuestos`** mostrando los datos actualizados para la nueva cuenta. Esto se logra con la lista whitelisted **`RutasModulos`** (`config/RutasModulos.java`), que contiene todas las rutas autorizadas. Cualquier endpoint que acepte `redirect` (cambio de cuenta, cambio de moneda) valida contra esta lista para prevenir **open-redirect**.

> 💡 *UX:* Si el usuario no incluyera la lista whitelisted, sería vulnerable a redirigir hacia páginas externas maliciosas. Antes, además, cambiar de cuenta en `/presupuestos` redirigía a `/cuentas` porque la ruta nueva no estaba en la lista — bug arreglado al centralizar.

---

## 3. Sistema de divisas multi-moneda

### Qué es

El usuario puede ver toda la aplicación en **COP, USD o EUR**. El cambio es inmediato y aplica a todos los montos visibles (saldos, transacciones, presupuestos, totales).

### Cómo funciona técnicamente

- **`MonedaService.java`** — servicio centralizado con:
  - `convertir(monto, moneda)` — multiplica por la tasa correspondiente
  - `simbolo(moneda)` — devuelve `€` para EUR, `$` para COP/USD
  - `tasa(moneda)` — `BigDecimal` para multiplicar
  - Tasas hardcoded (1 COP, 0.00024 USD, 0.00022 EUR). Punto de extensión claro para conectar API real (`exchangerate.host`) en el futuro sin tocar el resto del código.
- **`PreferenciasController.java`** — `POST /preferencias/moneda` cambia `monedaPreferida` en sesión. Mismo patrón de redirect whitelist que cambio de cuenta.
- **Regla de almacenamiento**: en base de datos **todo se guarda en COP** (moneda base). La conversión a la divisa elegida ocurre **solo al renderizar**. Esto evita ambigüedad histórica (un gasto registrado hace 6 meses sigue valiendo lo mismo en COP sin importar cómo fluctúe el dólar).
- **UI**: chip `[COP ▾]` en el topbar global (estilo Revolut). Al cambiar, todas las vistas se reconvierten manteniendo proporciones.

### Cobertura

| Vista | Aplica conversión |
|---|---|
| Dashboard | Saldo total, lista de cuentas, transacciones recientes |
| Cuentas | Saldo cuenta activa, saldo total, badge de divisa |
| Transacciones | Header saldo, pills (ingresos/gastos), cada monto del timeline |
| Presupuestos | Saldo header, totales, gastado/límite de cada barra, contexto por cuenta |

### Decisión de UX importante

El modal de nueva transacción muestra **explícitamente `COP $`** y el label *"Monto (en COP)"*. El usuario siempre tipea en COP, sin importar la moneda visible. Esto previene errores como teclear 1000 pensando que son COP cuando realmente serían USD.

---

## 4. Panel de usuario (Dashboard)

### Vista principal después del login

Tarjetas resumen organizadas en grid:

| Card | Contenido |
|---|---|
| **Mis cuentas** | Lista con saldo de cada cuenta (convertido) + saldo total |
| **Gastos fijos pendientes** | Presupuestos tipo `FIJO` (Arriendo, Netflix) no pagados este mes |
| **Transacciones recientes** | Últimas 5 transacciones de la **cuenta activa** (no todas las cuentas) |
| **Alertas recientes** | Notificaciones generadas por el sistema |

### Lógica clave

- **`DashboardController.java`** consulta servicios por separado y los compone en el modelo. Calcula `saldoTotal` sumando todas las cuentas y lo convierte a la moneda activa.
- **Transacciones recientes filtradas por cuenta activa**: usa `transaccionService.listarRecientesPorCuenta(activaId, 5)` en vez de `listarRecientesPorUsuario`. Esto da coherencia con `/transacciones` (que solo muestra movimientos de la cuenta activa). Antes mezclaba transacciones de todas las cuentas, lo cual confundía al usuario.
- **Gastos fijos pendientes**: filtra `Presupuesto` con `tipo=FIJO` y verifica si tiene `PresupuestoPeriodo` del mes actual con `pagado=true`. Si no, aparece como pendiente.

---

## 5. Creación de cuentas

### Funcionalidades

- Crear cuenta con nombre, tipo (Corriente, Ahorro, Tarjeta, Efectivo, Inversión) y saldo inicial
- Editar nombre, tipo y saldo (con lógica de reajuste — ver abajo)
- Eliminar cuenta (limpia sesión si era la activa)
- Cambiar cuenta activa desde el dropdown del topbar
- Cambiar divisa de visualización

### Lógica clave — separación `saldo` vs `saldoInicial`

Originalmente la tabla `cuenta` solo tenía `saldo`. Cuando el sistema "recalculaba" el saldo a partir de las transacciones, **perdía** el saldo inicial que el usuario había ingresado al crear la cuenta. Era un bug grave: añadir una transacción reseteaba el balance.

**Fix arquitectónico**: separar conceptos.

- **`saldo_inicial`** — el monto que el usuario tenía al crear la cuenta
- **`saldo`** — calculado dinámicamente como `saldo_inicial + Σ(ingresos) − Σ(gastos)`

Esto requirió:

1. Añadir columna `saldo_inicial DECIMAL(12,2)` (migración v5)
2. Cambiar `TransaccionService.recalcularSaldoCuenta()` para tomar `saldoInicial` como base
3. Al editar cuenta, si el usuario cambia el saldo manualmente, el sistema ajusta `saldoInicial` para mantener la invariante `saldo = saldoInicial + Σ(transacciones)`

### UI

- **Tarjetas de cuenta** con color por tipo (gold corporativo + acento por tipo)
- **Cuenta activa**: card con outline dorado + leve glow (`box-shadow`) para distinguirla visualmente
- **Selector de divisa**: integrado al chip global del topbar

---

## 6. Transacciones

### Vista principal

Estructura inspirada en apps fintech profesionales (Nubank, Apple Cash):

1. **Header** — saldo de cuenta activa + badge de divisa
2. **Dropdowns inteligentes**:
   - *Periodo*: `Hoy / Esta semana / Este mes / Todo` — muestra totales `+ingresos / −gastos` del rango aplicado
   - *Filtro*: `Todos / Solo ingresos / Solo gastos / [Presupuesto X]` — muestra conteo de movimientos
3. **Timeline agrupado por fecha** — cada movimiento con icono direccional, hora, categoría, badge de presupuesto vinculado (con color) y monto con signo + color (verde ingreso / naranja gasto)
4. **Modal de nueva transacción** — flujo de 2 pasos

### Modal de creación (simplificado tipo Apple Wallet)

- **Paso 1**: elegir `INGRESO` o `GASTO` con tarjetas grandes
- **Paso 2**:
  - Para INGRESO: dropdown de categorías predefinidas (Salario, Dividendos, Freelance, etc.)
  - Para GASTO: dropdown de "Añadir a presupuesto" o categoría libre si no hay presupuesto
  - Solo `Monto` visible siempre
  - Link "Más opciones" colapsable para fecha personalizada y descripción

**UX intencional**: campos como cuenta y fecha **NO** se piden en el flujo normal — la cuenta se infiere de la sesión activa, la fecha se asume `hoy`. Esto reduce fricción y replica el comportamiento de Nubank/Apple Cash.

### Lógica clave — vinculación con presupuesto

Al crear una transacción de tipo `GASTO` con `presupuesto_id`, el `TransaccionService.registrar()` ejecuta automáticamente:

1. `transaccionRepository.save()` — persiste el movimiento
2. `recalcularSaldoCuenta()` — actualiza el saldo con `@Query` directo en BD (más eficiente que cargar todas las transacciones)
3. `actualizarPeriodoPresupuesto(t, true)` — crea o actualiza el `PresupuestoPeriodo` del mes:
   - Si no existe periodo del mes para ese presupuesto, lo crea con `monto_limite_snapshot` del presupuesto actual
   - Suma el monto del gasto a `monto_gastado`

Al eliminar transacción, se resta del periodo (operación inversa). Esto mantiene **consistencia en tiempo real** entre transacciones y la barra de progreso en `/presupuestos`.

### Filtrado por cuenta activa

`prepararModelo()` consulta `transaccionService.listarPorCuenta(activaId)`, **no** todas las del usuario. El timeline muestra solo la cuenta activa, coherente con el modelo mental "estoy revisando esta cuenta específica". Al cambiar de cuenta, el timeline se reinicia.

---

## 7. Presupuestos

### Concepto unificado

Antes existían dos entidades separadas: `GastoFijo` (Arriendo, Netflix — pago recurrente fijo) y un concepto vago de "presupuesto por categoría" sin tabla propia. Esto causaba confusión en el código y en la UI.

**Fix arquitectónico** (migración v6): unificar todo bajo una sola entidad **`Presupuesto`** con un campo `tipo`:

| Tipo | Caso de uso | Comportamiento |
|---|---|---|
| `LIMITE` | Mercado, Transporte, Salidas | Muchos gastos pequeños se suman contra un tope mensual. Barra de progreso |
| `FIJO` | Arriendo, Netflix, Servicios | Un pago recurrente. Marca día de vencimiento, se marca como "pagado" o "pendiente" |

### Vista

1. **Header** — saldo cuenta activa + divisa (igual que transacciones)
2. **Dos pills**:
   - *Periodo* — "Mensual" estático (presupuestos son intrínsecamente mensuales)
   - *Filtro* — Todos / cada presupuesto individual
3. **Lista horizontal** estilo YNAB:
   - Cada presupuesto: dot de color + nombre + barra horizontal con `width: %` + porcentaje + `gastado / límite`
   - Estados visuales graduales: verde (`< 60%`), amarillo (`60-85%`), naranja (`85-100%`), rojo (`> 100%`)
   - Subtitle opcional `Desde [cuenta]: $X` que aparece **solo si la cuenta activa ha contribuido** — patrón "contexto dual" de YNAB
4. **Stats**: Total presupuestado | Total gastado del mes
5. **Historial de movimientos** del mes vinculados a presupuestos (filtrable)

### Click en presupuesto → modal "Agregar gasto rápido"

UX inspirado en Nubank/Monzo: una sola interacción para registrar un gasto vinculado.

- Click en una fila abre modal con presupuesto ya pre-seleccionado (oculto en hidden inputs)
- Header del modal muestra `Agregar gasto a 🟠 Mercado` con el dot de color del presupuesto
- Para tipo `LIMITE`: campo monto vacío, focus automático
- Para tipo `FIJO`: campo monto pre-llenado con `montoEstimado` (Arriendo $1.5M ya viene escrito) y texto auto-seleccionado para edición rápida
- "Más opciones" colapsado: fecha personalizada y descripción
- Footer `Se registrará en [cuenta activa]`
- Al guardar, redirige a `/presupuestos` (no a `/transacciones`) para mantener el contexto

### Filas clickeables con affordance visual

- `cursor: pointer` para indicar que son interactivas
- Hover: leve `translateY(-1px)` + sombra dorada sutil
- Flecha `›` que aparece a la derecha al hover
- **Modo edición** (botón lápiz): desactiva el click. Aparece botón `×` para eliminar y el cursor vuelve a default

### Backend — DTO `PresupuestoVista`

`PresupuestoController.prepararModelo()` precalcula en Java toda la lógica que antes vivía en el template Thymeleaf (porcentajes, estados, valores convertidos). El template solo consume valores listos. Esto hizo el código más legible, más rápido y eliminó errores de Thymeleaf bloqueando expresiones con `new BigDecimal(0)` o concatenaciones inseguras.

```java
public static class PresupuestoVista {
    private final Presupuesto presupuesto;
    private final BigDecimal gastado, limite, gastadoConv, limiteConv;
    private final BigDecimal gastadoCuentaActivaConv;  // contexto dual
    private final boolean hayGastoEnCuentaActiva;
    private final double pct, pctBar;
    private final String estado;  // "ok" | "warn" | "danger" | "over"
}
```

### Modelo de datos: presupuestos globales (no por cuenta)

**Decisión de diseño profesional** (alineada con YNAB, Mint, Nubank): los presupuestos pertenecen al **usuario**, no a una cuenta. Si tu presupuesto Mercado es $250K, da igual si pagas con débito o crédito — el límite es uno solo.

Lo que sí varía con la cuenta activa es:
- El saldo del header
- El historial de transacciones visibles
- El subtitle "Desde [cuenta]" en cada presupuesto

Esto se comunica visualmente con un microtexto en el título de sección: *"Presupuestos · Compartidos en todas tus cuentas"*.

---

## 8. Migraciones de base de datos

### Filosofía

`spring.jpa.hibernate.ddl-auto=none` — JPA **no** modifica el schema automáticamente. Todas las migraciones son scripts SQL versionados y se aplican manualmente. Esto previene cambios destructivos no intencionados en producción.

### Histórico de migraciones

| Versión | Cambio | Razón |
|---|---|---|
| **v3** | `cuenta.nombre VARCHAR(60) NOT NULL` | Antes las cuentas se identificaban solo por tipo |
| **v4** | `transaccion.fecha_registro DATETIME` + `gasto_fijo_id INT NULL` con FK | Separar fecha contable de timestamp técnico. Permitir vincular transacciones a gastos fijos |
| **v5** | `cuenta.saldo_inicial DECIMAL(12,2)` | Separar saldo calculado del saldo inicial (fix del bug de "saldo se borra al añadir transacción") |
| **v6** | `RENAME TABLE gasto_fijo → presupuesto` + columnas `tipo`, `color` + tabla `presupuesto_periodo` con snapshots | Unificar gasto fijo y presupuesto en una sola entidad con flag tipo |

### Patrón profesional

Las migraciones usan `ADD COLUMN IF NOT EXISTS` y, donde MySQL no soporta condicional puro (como `DROP FOREIGN KEY`), se usan **stored procedures temporales** que validan contra `INFORMATION_SCHEMA` antes de actuar. Esto las hace **idempotentes**: ejecutarlas dos veces no causa errores.

---

## 9. Patrones transversales

### Seguridad

- **CSRF**: todos los forms incluyen el token con `th:name="${_csrf.parameterName}"`
- **Anti-IDOR**: los servicios validan que la entidad consultada pertenezca al usuario logueado (`buscarPorIdYUsuario`)
- **Open-redirect**: el parámetro `redirect` se valida contra `RutasModulos.AUTORIZADAS` (whitelist)
- **Autocomplete del browser**: en campos sensibles (nombre de presupuesto), `name` único + `autocomplete="off"` para evitar que Chrome sugiera valores de otros formularios

### Manejo de errores

- **`GlobalExceptionHandler`** — un `@ControllerAdvice` que captura `RuntimeException`s no manejadas, las loguea con stacktrace completo y renderiza una página de error con mensaje útil
- **Logging contextual** — `TransaccionController.crear()` y otros loguean errores de validación con detalle de campo + mensaje, para depurar fallos sin pantallas en blanco

### UI consistente

- **CSS modular**: `layout.css` (sidebar, topbar, alerts), `transacciones.css`, `presupuestos.css`, `creacion-cuentas.css`, etc. Cada vista carga solo lo que necesita
- **Variables CSS para tipos de cuenta**: `--card-accent` permite que el mismo template se vea con colores distintos por tipo de cuenta
- **Auto-fade de alertas**: las alertas verdes desaparecen a los 3.5s, las rojas a los 5s, click cierra inmediato — patrón profesional de Nubank/Google Pay

### Thymeleaf — restricciones de Thymeleaf 3.x

Thymeleaf bloquea por seguridad XSS:
- **Strings en `th:onclick`** → solución: pasar el valor por `data-*` y leerlo desde JS (`button.dataset.label`)
- **Strings en `th:style` con concatenación** → solución: pasar color en `data-color`, aplicarlo desde JS al cargar la página

Ambos patrones se documentan en el código con comentarios para que el siguiente desarrollador entienda por qué no se usa la forma "obvia".

### Eficiencia JPA

- **`@EntityGraph(attributePaths = {"cuenta", "presupuesto"})`** en repositorios — evita N+1 queries cargando relaciones eagerly cuando se sabe que la vista las necesita
- **`@Query` agregada en BD** para `sumarDeltaPorCuenta` — calcula el saldo directamente en MySQL sin traer entidades a memoria, además de evitar problemas de `LazyInitializationException`
- **Auto-flush controlado**: separar lectura/escritura en orden correcto cuando hay anidamiento transaccional

---

## Cómo se integra el flujo completo

1. Usuario se loguea → **Dashboard** muestra resumen general
2. Crea una **Cuenta** (Banco principal, saldo inicial $5M) → aparece en el sidebar topbar como cuenta activa
3. Crea un **Presupuesto** "Mercado" tipo LIMITE $250K mensual
4. Crea un **Presupuesto** "Arriendo" tipo FIJO $1.5M día 1
5. Va a **Transacciones**, crea gasto de $50K vinculado a Mercado → barra de Mercado se actualiza al 20%
6. Va a **Presupuestos**, click en Arriendo → modal abre con $1.500.000 pre-llenado → guarda → Arriendo queda marcado pagado el día 1
7. Cambia divisa a USD desde el chip → todos los montos se reconvierten manteniendo proporciones
8. Cambia cuenta a "Tarjeta de crédito" desde el dropdown → saldo del header cambia, historial filtrado a esa cuenta, barras de presupuesto siguen iguales (son globales del usuario)

Todo orquestado por `HttpSession` (`cuentaActivaId`, `monedaPreferida`) + `@ControllerAdvice` que expone variables globalmente + whitelist de rutas para preservar contexto al navegar.

---

*Este documento se actualiza con cada hito mayor. Para la bitácora de la fase productiva, las secciones más relevantes son **5 (Cuentas)**, **6 (Transacciones)**, **7 (Presupuestos)** y **8 (Migraciones)**.*
