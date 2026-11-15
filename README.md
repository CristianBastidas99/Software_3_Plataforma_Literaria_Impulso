# Plataforma Literaria Impulso

Plataforma web donde **escritores** publican sus obras literarias (con portada y un fragmento en PDF), los **lectores** las exploran, comentan y recomiendan, y un **administrador** gestiona el contenido.

Proyecto académico de la asignatura *Software 3*, Universidad del Quindío.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 11 |
| Framework | Spring Boot 2.6.4 |
| Persistencia | Spring Data JPA / Hibernate + MySQL 8 |
| Vista | JSF (JoinFaces 4.6.6) + PrimeFaces 13 + Bootstrap |
| Correo | Spring Boot Mail (SMTP) |
| Build | Gradle 7.6.4 (wrapper incluido) |
| Utilidades | Lombok, Commons IO |

## Estructura del proyecto

```
Plataforma_Literaria_Impulso_sw3
├── negocio/                     # Lógica de negocio y acceso a datos
│   └── src/main/java/co/edu/uniquindio/proyecto/
│       ├── entidades/           # Usuario, Administrador, Escritor, Lector, ObraLiteraria,
│       │                        # Publicacion, Fragmento, Comentario, Genero, Estado
│       ├── repositorios/        # Repositorios JPA con consultas JPQL
│       └── servicios/           # Interfaces + implementaciones (CRUD, login, búsqueda, email)
│   └── src/test/                # Pruebas de repositorios, consultas y servicios (+ pli.sql)
├── web/                         # Aplicación web JSF (módulo ejecutable)
│   ├── src/main/java/.../bean/  # Managed beans (Inicio, Explorar, Login, Registrar,
│   │                            # Escritor, Publicacion, EditarPerfil)
│   └── src/main/webapp/         # Páginas .xhtml, plantilla y recursos (css, js, img, pdf)
├── build.gradle                 # Configuración común de los módulos
└── settings.gradle
```

## Requisitos

1. **JDK 11** (recomendado [Eclipse Temurin 11](https://adoptium.net/temurin/releases/?version=11)). También compila con JDK 17.
2. **Docker** (para MySQL y el servidor de correo de pruebas) o, en su defecto, MySQL 8 instalado localmente.
3. No hace falta instalar Gradle: se usa el wrapper `gradlew`.

En Windows, el JDK 11 se puede instalar con:

```powershell
winget install --id EclipseAdoptium.Temurin.11.JDK -e
```

## Servicios externos

La aplicación necesita dos servicios, pensados para correr en contenedores Docker:

| Servicio | Imagen | Puerto | Uso |
|---|---|---|---|
| MySQL | `mysql:8.0` | `3306` | Base de datos `plisw3` |
| Mailpit | `axllent/mailpit` | `1025` (SMTP) / `8025` (web) | Captura los correos enviados por la app |

Si aún no tienes los contenedores, puedes crearlos así:

```bash
docker run -d --name impulso-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=12345 mysql:8.0

docker run -d --name impulso-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

> Sirve cualquier contenedor de MySQL 8 y Mailpit que publique esos puertos en `localhost`; no es necesario que se llamen así.

- **Base de datos:** no hace falta crearla a mano. La URL de conexión incluye `createDatabaseIfNotExist=true` y Hibernate crea o actualiza las tablas al arrancar (`ddl-auto=update`).
- **Correo:** Mailpit no envía nada a internet; los correos que manda la aplicación se ven en **http://localhost:8025**.

## Configuración

La configuración está en `web/src/main/resources/application.properties` (y una copia equivalente en `negocio/`, que usan las pruebas). Los valores se pueden cambiar con variables de entorno sin editar los archivos:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_USER` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | `12345` | Contraseña de MySQL |
| `MAIL_HOST` | `localhost` | Host SMTP |
| `MAIL_PORT` | `1025` | Puerto SMTP |

Además puedes crear un archivo **opcional** `application-secret.properties` (en `web/src/main/resources/`) para sobrescribir cualquier propiedad en tu máquina, por ejemplo, para usar Gmail real en lugar de Mailpit:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_correo@gmail.com
spring.mail.password=tu_contrasena_de_aplicacion
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Este archivo está en `.gitignore` y **no debe subirse al repositorio**.

## Cómo ejecutar

1. Verifica que los contenedores de MySQL y Mailpit estén corriendo (`docker ps`).
2. Desde la raíz del proyecto:

   **Windows (PowerShell)**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.0.33.10-hotspot"   # ajusta a tu ruta
   .\gradlew :web:bootRun
   ```

   **Linux / macOS**
   ```bash
   ./gradlew :web:bootRun
   ```

3. Abre **http://localhost:8080/index.xhtml**.

Para acceder desde otro dispositivo de la misma red, usa `http://<IP-de-tu-PC>:8080/index.xhtml`. En Windows puede que necesites abrir el puerto en el firewall (PowerShell como administrador):

```powershell
New-NetFirewallRule -DisplayName "Impulso 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
```

### Otros comandos

```bash
./gradlew build -x test     # Compilar sin ejecutar pruebas
./gradlew :negocio:test     # Ejecutar pruebas (requieren MySQL corriendo)
```

### Datos de prueba

La base de datos arranca vacía. Para cargar datos de ejemplo:

```bash
docker exec -i <contenedor-mysql> mysql -uroot -p12345 plisw3 < negocio/src/test/resources/pli.sql
```

## Páginas disponibles

| Página | Descripción |
|---|---|
| `index.xhtml` | Inicio con publicaciones destacadas |
| `explorar.xhtml` | Búsqueda de publicaciones y escritores por frase |
| `registro.xhtml` | Registro de escritores y lectores |
| `login.xhtml` | Inicio de sesión (administrador, escritor o lector) |
| `publicacion.xhtml?idProd=<id>` | Detalle de una publicación y su autor |
| `escritor.xhtml?idEscr=<id>` | Perfil de un escritor y sus publicaciones |
| `editar_perfil.xhtml` | Edición de perfil, contraseña e imagen *(en desarrollo)* |

## Estado del proyecto

**Implementado**
- Modelo de datos completo con relaciones JPA.
- Repositorios con consultas JPQL y pruebas unitarias.
- Servicios CRUD para todas las entidades, login por tipo de usuario y búsqueda por frase.
- Servicio de envío de correo.
- Interfaz web: inicio, explorar, registro, login con sesión, detalle de publicación y perfil de escritor.

**Pendiente / en desarrollo**
- Edición de perfil y subida de imágenes y PDF (a medias).
- Pantallas de administración, comentarios, géneros y creación de obras/publicaciones.
- Uso del filtro de palabras ofensivas (`malaspalabras.json`) en los comentarios.
- Uso del servicio de correo en algún flujo (registro, recuperación de contraseña, etc.).

## Problemas conocidos

- Las contraseñas se guardan y comparan en texto plano.
- Las subidas se guardan en `src/main/webapp/resources/...` (ruta relativa); solo funciona en desarrollo.
- Las pruebas de `negocio` usan la base de datos de desarrollo y el script `pli.sql` no coincide con el esquema actual, por lo que fallan.
- El formulario para comentar en `publicacion.xhtml` todavía no está conectado a ningún bean.

## Historial de cambios de configuración

Ajustes hechos para que el proyecto compile y arranque en un entorno nuevo:

- **Gradle** actualizado de 7.1.1 a **7.6.4** (la 7.1.1 no funciona con JDK 17).
- **`build.gradle` raíz:** se desactivó `bootJar` en el proyecto raíz, que no tiene clase `main` y hacía fallar el build.
- **Base de datos:** URL parametrizada con variables de entorno, `createDatabaseIfNotExist=true`, `allowPublicKeyRetrieval=true` y `useSSL=false` para conectar con MySQL 8 en Docker.
- **Correo:** se reemplazó Gmail por **Mailpit** (SMTP `localhost:1025`, sin autenticación ni TLS).
- **`application-secret.properties`:** ahora es opcional (`optional:classpath:`). Antes era obligatorio y, como no estaba en el repositorio, la aplicación no arrancaba. Se añadió a `.gitignore`.
