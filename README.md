# Finanzas Spring v2

## Flujo general de la aplicacion
1. **Landing / Registro / Login**
   - Usuario llega a `/` (landing) y puede registrarse en `/auth/register` o iniciar sesion en `/auth/login`.
   - Spring Security maneja autenticacion con contrasenas encriptadas (BCrypt) y sesion.

2. **Dashboard**
   - Tras autenticarse, se redirige a `/dashboard`.
   - Se cargan resumen de cuentas, gastos fijos pendientes y transacciones recientes mediante servicios y repositorios JPA.

3. **Gestion de cuentas**
   - En `/cuentas` el usuario puede crear, editar, eliminar cuentas y ajustar saldos.
   - Cada cuenta esta vinculada al usuario autenticado.

4. **Gestion de gastos fijos**
   - En `/gastos-fijos` se registran gastos recurrentes, con estado activo, monto estimado y vencimiento.
   - Cada registro mensual se administra en `/gastos-fijos/{id}/registros`, permitiendo marcar pago, monto real y fecha.

5. **Gestion de transacciones**
   - En `/transacciones` se registran ingresos/gastos asociados a cuentas.
   - Al guardar o eliminar transacciones, se recalcula el saldo de la cuenta.

6. **API de registros de gastos fijos**
   - Endpoints REST bajo `/api/registros-gastos-fijos` exponen registros por gasto y permiten persistir nuevos registros.

## Novedades contra version anterior
- Nueva API publica en `/api/auth/register` y `/api/auth/login` para registro e inicio de sesion desde clientes externos (Postman, SPA, apps moviles).
- Capas reutilizadas: `UsuarioService` incorpora `autenticar` para validar credenciales con BCrypt sin duplicar logica.
- Nuevos DTOs (`RegisterRequest`, `LoginRequest`, `AuthResponse`, `UsuarioResumen`) estandarizan las cargas y respuestas JSON.
- Comentarios agregados en controladores y DTOs para documentar comportamiento, cumpliendo los lineamientos de la evidencia.
- Seguridad ajustada para permitir el acceso publico a la API de autenticacion mientras se mantiene protegido el resto de `/api/**`.

## Tecnologias
- Java 17, Spring Boot 3.5.6, Spring Security, Spring Data JPA, Thymeleaf.
- MySQL 8.x (schema en `database/finanzas_personales.sql`).
- Recursos estaticos en `src/main/resources/static`, vistas en `src/main/resources/templates`.

## Ejecucion local
```bash
mvnw.cmd spring-boot:run
```

## Como probar la nueva API
1. Levanta la aplicacion (ver comando anterior).
2. En Postman envia `POST http://localhost:8080/api/auth/register` con cuerpo JSON:
   ```json
   {
     "nombre": "Ana",
     "apellido": "Diaz",
     "correo": "ana@example.com",
     "contrasena": "claveSegura123"
   }
   ```
3. Inicia sesion con `POST http://localhost:8080/api/auth/login` y el mismo cuerpo (sin el campo `apellido`).
4. Confirma que recibes mensajes de exito y que los estados HTTP son 201 (registro) y 200 (login). Un error 401 indicara credenciales invalidas.

## Tag recomendado
Tras validar, crear etiqueta:
```bash
git tag -a v2.1.0 -m "Version 2.1 - API de autenticacion"
```
