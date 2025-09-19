# Finanzas Spring v2

## Flujo general de la aplicación
1. **Landing / Registro / Login**
   - Usuario llega a `/` (landing) y puede registrarse en `/auth/register` o iniciar sesión en `/auth/login`.
   - Spring Security maneja autenticación con contraseñas encriptadas (BCrypt) y sesión.

2. **Dashboard**
   - Tras autenticarse, se redirige a `/dashboard`.
   - Se cargan resumen de cuentas, gastos fijos pendientes y transacciones recientes mediante servicios y repositorios JPA.

3. **Gestión de cuentas**
   - En `/cuentas` el usuario puede crear, editar, eliminar cuentas y ajustar saldos.
   - Cada cuenta está vinculada al usuario autenticado.

4. **Gestión de gastos fijos**
   - En `/gastos-fijos` se registran gastos recurrentes, con estado activo, monto estimado y vencimiento.
   - Cada registro mensual se administra en `/gastos-fijos/{id}/registros`, permitiendo marcar pago, monto real y fecha.

5. **Gestión de transacciones**
   - En `/transacciones` se registran ingresos/gastos asociados a cuentas.
   - Al guardar o eliminar transacciones, se recalcula el saldo de la cuenta.

6. **API de registros de gastos fijos**
   - Endpoints REST bajo `/api/registros-gastos-fijos` exponen registros por gasto y permiten persistir nuevos registros.

## Tecnologías
- Java 17, Spring Boot 3.5.6, Spring Security, Spring Data JPA, Thymeleaf.
- MySQL 8.x (schema en `database/finanzas_personales.sql`).
- Recursos estáticos en `src/main/resources/static`, vistas en `src/main/resources/templates`.

## Ejecución
```bash
mvnw.cmd spring-boot:run
```

## Tag recomendado
Tras validar, crear etiqueta:
```bash
git tag -a v2.0.0 -m "Version 2 - migracion a Spring + gastos fijos + transacciones"
```
