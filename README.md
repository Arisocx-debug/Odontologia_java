# Odontologia Dr. Wilson Montenegro — Spring Boot

Migracion completa del sistema original en **Laravel (PHP)** a **Spring Boot 3 (Java 17)**,
reorganizado siguiendo buenas practicas: arquitectura en capas
`controller → service → repository → entity (JPA)`, autenticacion con **JWT**,
vistas con **Thymeleaf** (equivalente a Blade) y generacion de reportes en **PDF** (iText) y **Excel** (Apache POI).

## Stack tecnico

| Capa | Tecnologia | Equivalente en el proyecto Laravel original |
|---|---|---|
| Lenguaje | Java 17 | PHP 8 |
| Framework | Spring Boot 3.3.4 | Laravel |
| Persistencia | Spring Data JPA + Hibernate | Eloquent ORM |
| Base de datos | MySQL | MySQL |
| Vistas | Thymeleaf + Bootstrap 5 | Blade |
| Seguridad | Spring Security + JWT (cookie HttpOnly) | Sesiones + Middlewares (Admin/Empleado/Cliente) |
| PDF | iText 7 | barryvdh/laravel-dompdf |
| Excel | Apache POI | maatwebsite/excel |
| Build | Maven | Composer |

## Estructura del proyecto

```
src/main/java/com/wilsonmontenegro/odontologia/
├── config/          # SecurityConfig, CORS, manejadores 401/403
├── security/         # JWT (provider, filtro), UserDetails
├── model/             # Entidades JPA (Usuario, Cliente, Cita, Servicio, Inventario, Venta, Proveedor, MovimientoStock)
│   └── enums/         # Rol, EstadoCita, EstadoInventario, TipoMovimiento
├── repository/       # Interfaces Spring Data JPA
├── service/           # Logica de negocio (equivalente a los metodos de los Controllers de Laravel)
├── controller/        # Controladores MVC (Thymeleaf)
│   └── api/           # Controladores REST (JSON) — ejemplo: /api/auth/**
├── dto/               # Request/Response DTOs
├── exception/         # Excepciones de negocio + manejador global (solo para /api/**)
└── util/               # AuthUtil (equivalente a Auth::user() de Laravel)

src/main/resources/
├── application.yml
├── templates/          # Vistas Thymeleaf (equivalente a resources/views de Laravel)
└── static/css/         # CSS (Bootstrap 5 + estilos propios)
```

## Roles y seguridad

Se replican exactamente los 3 middlewares del proyecto original:

| Rol | Prefijo de rutas | Middleware Laravel equivalente |
|---|---|---|
| `ADMINISTRADOR` | `/admin/**` | `AdminMiddleware` |
| `EMPLEADO` | `/empleado/**` | `EmpleadoMiddleware` |
| `CLIENTE` | `/cliente/**` | `ClienteMiddleware` |

La autenticacion es **stateless** (JWT), pero el token tambien se guarda en una **cookie HttpOnly**
para que la navegacion normal del navegador (sin JavaScript) funcione igual que con las vistas Blade
originales. El mismo JWT tambien puede enviarse como header `Authorization: Bearer {token}` si se
consume la API REST desde un frontend separado (Angular, React, etc.).

## Instalacion y ejecucion

### 1. Requisitos
- Java 17 (LTS; usar esta versión para compilar y ejecutar)
- Maven 3.9+
- MySQL 8+

### 2. Base de datos
Crea la base de datos (o deja que Hibernate la cree sola, ver `application.yml`):
```sql
CREATE DATABASE odontologia_dr_wilson_montenegro;
```

### 3. Variables de entorno (opcional, tienen valores por defecto en `application.yml`)
```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_DATABASE=odontologia_dr_wilson_montenegro
export DB_USERNAME=root
export DB_PASSWORD=tu_password
export JWT_SECRET=<clave-base64-larga-y-secreta>   # obligatorio, generar una propia
```

En PowerShell, por ejemplo:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 4. Ejecutar
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
La aplicacion queda disponible en `http://localhost:8080`.

El perfil `dev` crea/actualiza las tablas (`ddl-auto: update`). El perfil por defecto usa
`validate`, por lo que en producción el esquema debe gestionarse mediante migraciones.
Para el primer usuario **administrador**, insertalo manualmente (la contrasena debe ir
encriptada con BCrypt, por ejemplo generada con `new BCryptPasswordEncoder().encode("clave")`):

```sql
INSERT INTO users (name, email, password, rol, telefono, created_at, updated_at)
VALUES ('Admin', 'admin@wilsonmontenegro.com', '<hash_bcrypt>', 'ADMINISTRADOR', '3000000000', NOW(), NOW());
```

## Modulos migrados

- **Autenticacion**: login, registro de clientes, logout (JWT + cookie)
- **Citas**: agendar, editar, cancelar, listar/buscar — con las mismas reglas de negocio originales
  (horario laboral 6:00–20:00, sin fechas pasadas, sin solapamiento de horarios), separado por rol
  (Administrador, Empleado, Cliente)
- **Servicios odontologicos**: CRUD (Administrador/Empleado)
- **Inventario**: CRUD + activar/desactivar productos, con la misma restriccion de no poder
  editar/eliminar productos inactivos
- **Ventas**: registro de ventas con descuento automatico de stock y generacion de movimiento
  de inventario; portal de compras para el Cliente (tienda)
- **Proveedores**: CRUD (Administrador)
- **Usuarios**: gestion de administradores/empleados/clientes (Administrador)
- **Dashboard**: estadisticas de citas e ingresos por año/mes
- **Reportes**: generacion de facturas en PDF (iText) y Excel (Apache POI) para citas y ventas

## Decisiones de migracion / diferencias con el original

1. **Sesiones → JWT**: Laravel guardaba el usuario en `session()`. Aqui se usa un JWT firmado,
   guardado en una cookie HttpOnly para las vistas y disponible via header `Authorization` para la API.
2. **Vistas Blade → Thymeleaf**: se recreo la misma logica y flujos de cada vista, con un diseño
   Bootstrap 5 limpio en lugar de portar el CSS personalizado original pixel a pixel.
3. **DomPDF/Maatwebsite Excel → iText / Apache POI**: son las librerias equivalentes estandar
   en el ecosistema Java.
4. **Validaciones de negocio**: se mantuvieron identicas (horario laboral, solapamiento de citas,
   stock insuficiente, proveedor duplicado, administrador unico no eliminable, etc.).

## Extender el proyecto

- Para agregar mas endpoints REST (ademas de `/api/auth/**`), sigue el patron de
  `AuthApiController` en `controller/api/`, y el `GlobalRestExceptionHandler` ya
  devolvera JSON de error automaticamente para ese paquete.
- Para produccion, cambia `ddl-auto` a `validate` e introduce Flyway o Liquibase para
  versionar el esquema de base de datos.
