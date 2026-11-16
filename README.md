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
│       ├── dto/                 # DatosObra: datos del formulario de obra con Bean Validation
│       ├── validacion/          # Validador de ISBN (@Isbn)
│       └── servicios/           # Interfaces + implementaciones (CRUD, login, búsqueda, email,
│                                # GestionObra, Interaccion y FiltroPalabras)
│   └── src/test/                # Pruebas de repositorios, consultas y servicios (+ pli.sql)
├── web/                         # Aplicación web JSF (módulo ejecutable)
│   ├── src/main/java/.../bean/  # Managed beans (Inicio, Explorar, Login, Registrar,
│   │                            # Escritor, Publicacion, EditarPerfil, MiPerfil, MisObras,
│   │                            # ObraForm, Recursos)
│   ├── src/main/java/.../archivos/ # Subida y validación de imágenes y PDFs (ArchivoServicio)
│   ├── src/main/resources/demo/ # Datos del perfil demo (datos-demo.json)
│   └── src/main/webapp/         # Páginas .xhtml, plantilla y recursos (css, js, img, pdf)
├── scripts/                     # Generador de portadas, avatares y PDFs de la demo
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
  -e MYSQL_ROOT_PASSWORD=<tu_contraseña> mysql:8.0

docker run -d --name impulso-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

> Sirve cualquier contenedor de MySQL 8 y Mailpit que publique esos puertos en `localhost`; no es necesario que se llamen así.

- **Base de datos:** no hace falta crearla a mano. La URL de conexión incluye `createDatabaseIfNotExist=true` y Hibernate crea o actualiza las tablas al arrancar (`ddl-auto=update`).
- **Correo:** Mailpit no envía nada a internet; los correos que manda la aplicación se ven en **http://localhost:8025**.

## Configuración

La configuración está en `web/src/main/resources/application.properties` (y una copia equivalente en `negocio/`, que usan las pruebas).

### Credenciales (obligatorio)

El repositorio no contiene credenciales. Antes de arrancar la aplicación o las pruebas, copia la plantilla y pon el usuario y la contraseña de tu MySQL:

```bash
cp application-secret.properties.example application-secret.properties   # en la raíz del proyecto
```

```properties
DB_USER=root
DB_PASSWORD=<la contraseña de tu contenedor MySQL>
```

`application-secret.properties` está en `.gitignore` y **no debe subirse al repositorio**. Lo leen la aplicación (`./gradlew :web:bootRun`) y las pruebas (`./gradlew test`). También sirve para sobrescribir cualquier otra propiedad en tu máquina, por ejemplo un SMTP real en lugar de Mailpit (la plantilla tiene un ejemplo). Si falta, la aplicación no arranca y avisa con `Could not resolve placeholder 'DB_USER'`.

### Variables de entorno

Los valores se pueden cambiar con variables de entorno sin editar los archivos (tienen prioridad sobre `application-secret.properties`):

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_USER` | — (obligatoria) | Usuario de MySQL |
| `DB_PASSWORD` | — (obligatoria) | Contraseña de MySQL |
| `MAIL_HOST` | `localhost` | Host SMTP |
| `MAIL_PORT` | `1025` | Puerto SMTP |
| `MAIL_FROM` | `Plataforma Literaria Impulso <no-responder@impulso.com>` | Remitente de los correos |
| `APP_URL` | `http://localhost:8080` | Dirección pública de la app, usada en los enlaces de los correos |
| `UPLOAD_DIR` | `uploads` | Carpeta donde se guardan las imágenes y PDFs que suben los usuarios |

### Correos

La aplicación envía tres correos, en segundo plano (`@Async`), así que un servidor SMTP lento o caído no bloquea la interfaz. Si el envío falla, el error solo queda en el log.

- **Bienvenida** al registrarse.
- **Recuperación de contraseña** desde "¿Olvidaste tu contraseña?" en el login. El enlace caduca a los 30 minutos (`app.recuperacion.minutos`), sirve una sola vez y deja de valer si se pide otro. En la base de datos solo se guarda el hash del token. La página responde lo mismo exista o no el correo, y no se envía nada a cuentas bloqueadas.
- **Aviso al escritor** cuando comentan una de sus obras (no cuando la comenta él mismo).

En desarrollo los correos llegan a Mailpit: http://localhost:8025.

### Archivos subidos

Las imágenes y PDFs que suben los usuarios se guardan **fuera del código**, en la carpeta `UPLOAD_DIR` (subcarpetas `img/` y `pdf/`), y se sirven en `/uploads/**`. Si la ruta es relativa, se toma desde el directorio en el que se arranca la aplicación: con `./gradlew :web:bootRun` queda en `web/uploads`. La carpeta se crea al arrancar y está en `.gitignore`.

- Imágenes: JPG, PNG o WEBP, máximo 5 MB (`upload.imagen.max-mb`).
- Fragmentos: PDF, máximo 20 MB (`upload.pdf.max-mb`).
- El tipo se comprueba por la extensión **y** por el contenido del archivo, y cada archivo se guarda con un nombre aleatorio (UUID).

### Seguridad

- **Contraseñas:** se guardan cifradas con BCrypt. Una base de datos anterior se actualiza sola al arrancar: se amplía la columna `password` y se cifran las contraseñas que estaban en claro (cada usuario conserva la suya).
- **Acceso por rol:** un filtro protege las páginas antes de que JSF las procese. Sin sesión, redirige al login; con una sesión de otro tipo de usuario, al inicio.

| Página | Quién puede abrirla |
|---|---|
| `admin.xhtml` | Administrador |
| `mis_obras.xhtml`, `nueva_obra.xhtml` | Escritor |
| `mi_perfil.xhtml` | Lector |
| `editar_perfil.xhtml` | Escritor o lector |

- **Login:** el mensaje "Correo o contraseña incorrectos" es el mismo si el correo no existe y si la contraseña es incorrecta. El identificador de sesión se renueva al iniciar sesión.
- **Bloqueo:** bloquear a un usuario desde el panel cierra también las sesiones que tenga abiertas.

## Cómo ejecutar

1. Verifica que los contenedores de MySQL y Mailpit estén corriendo (`docker ps`).
2. Si es la primera vez, crea `application-secret.properties` con tus credenciales de MySQL (ver [Credenciales](#credenciales-obligatorio)).
3. Desde la raíz del proyecto:

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
./gradlew :negocio:test     # Pruebas de negocio (requieren MySQL corriendo)
./gradlew :web:test         # Pruebas de web (no requieren MySQL)
```

Las pruebas usan una base de datos aparte, `plisw3_test`, que se crea automáticamente. No modifican la base de desarrollo.

### Datos de demostración (perfil `demo`)

La base de datos arranca vacía. Para llenarla con contenido de ejemplo, arranca la aplicación con el perfil `demo`:

**Windows (PowerShell)**
```powershell
.\gradlew :web:bootRun --args="--spring.profiles.active=demo"
```

**Linux / macOS**
```bash
./gradlew :web:bootRun --args='--spring.profiles.active=demo'
```

Al arrancar se cargan 8 géneros, 1 administrador, 4 escritores, 3 lectores, 10 obras publicadas (cada una con portada y fragmento en PDF), 15 comentarios, y los favoritos, recomendaciones y visitas de los lectores. Los datos solo se cargan si la base no tiene usuarios ni publicaciones, así que arrancar varias veces con el perfil no los duplica. Si quieres volver a cargarlos, vacía antes la base de datos.

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@impulso.com` | `admin123` |
| Escritor | `valentina@impulso.com` | `escritor123` |
| Escritor | `mateo@impulso.com` | `escritor123` |
| Escritor | `lucia@impulso.com` | `escritor123` |
| Escritor | `andres@impulso.com` | `escritor123` |
| Lector | `camila@impulso.com` | `lector123` |
| Lector | `santiago@impulso.com` | `lector123` |
| Lector | `laura@impulso.com` | `lector123` |

Los datos están en `web/src/main/resources/demo/datos-demo.json`. Las portadas, los avatares y los PDFs (`demo_*` en `web/src/main/webapp/resources/img` y `resources/pdf`) son originales del proyecto y se generan a partir de ese JSON con:

```bash
pip install pillow
python scripts/generar_recursos_demo.py
```

## Páginas disponibles

| Página | Descripción |
|---|---|
| `index.xhtml` | Inicio con publicaciones destacadas |
| `explorar.xhtml` | Búsqueda de publicaciones (frase, género y orden por fecha o popularidad) y de escritores |
| `registro.xhtml` | Registro de escritores y lectores |
| `login.xhtml` | Inicio de sesión (administrador, escritor o lector) |
| `publicacion.xhtml?idProd=<id>` | Detalle de una publicación y su autor |
| `escritor.xhtml?idEscr=<id>` | Perfil de un escritor y sus publicaciones |
| `editar_perfil.xhtml` | Edición de nombre, correo, biografía, imagen y contraseña (escritor o lector con sesión) |
| `mis_obras.xhtml` | Obras del escritor con sesión: editar, retirar y volver a publicar |
| `nueva_obra.xhtml` | Publicar una obra (`?idObra=<id>` para editarla) con portada, fragmento PDF, géneros y coautores |
| `recuperar.xhtml` | "¿Olvidaste tu contraseña?": pide el correo y envía el enlace |
| `restablecer.xhtml?token=<token>` | Elegir una contraseña nueva desde el enlace del correo |
| `mi_perfil.xhtml` | Géneros preferidos, escritores favoritos, recomendaciones e historial del lector con sesión |
| `admin.xhtml` | Panel de control del administrador: resumen, usuarios, publicaciones, comentarios y géneros |

## Estado del proyecto

**Implementado**
- Modelo de datos completo con relaciones JPA.
- Repositorios con consultas JPQL y pruebas unitarias.
- Servicios CRUD para todas las entidades, login por tipo de usuario y búsqueda por frase.
- Seguridad: contraseñas cifradas con BCrypt, páginas protegidas por rol, login que no revela qué correos existen y credenciales fuera del repositorio.
- Correos HTML en segundo plano: bienvenida, recuperación de contraseña y aviso de comentarios.
- Interfaz web: inicio, explorar, registro, login con sesión, detalle de publicación y perfil de escritor.
- Edición de perfil (nombre, correo, biografía, imagen y contraseña) y página "Mi perfil" del lector.
- Interacción: comentarios con filtro de palabras ofensivas, recomendaciones, contador de vistas, seguir escritores, géneros preferidos y sección "Recomendado para ti".
- Publicación de obras por los escritores: creación con portada, PDF, géneros y coautores; edición, retiro y nueva publicación, con validación de datos (incluido el ISBN) y control de autoría.
- Subida de imágenes y PDF a una carpeta externa, con validación de tipo y tamaño.
- Panel de administración: estadísticas, bloqueo de usuarios (no pueden iniciar sesión), moderación de publicaciones y comentarios (ocultar, volver a mostrar o eliminar) y gestión de géneros.

**Pendiente / en desarrollo**

## Problemas conocidos

- Los cambios en las páginas `.xhtml` pueden no verse hasta reiniciar la aplicación (JSF guarda las vistas en caché).

## Historial de cambios de configuración

Ajustes hechos para que el proyecto compile y arranque en un entorno nuevo:

- **Gradle** actualizado de 7.1.1 a **7.6.4** (la 7.1.1 no funciona con JDK 17).
- **`build.gradle` raíz:** se desactivó `bootJar` en el proyecto raíz, que no tiene clase `main` y hacía fallar el build.
- **Base de datos:** URL parametrizada con variables de entorno, `createDatabaseIfNotExist=true`, `allowPublicKeyRetrieval=true` y `useSSL=false` para conectar con MySQL 8 en Docker.
- **Correo:** se reemplazó Gmail por **Mailpit** (SMTP `localhost:1025`, sin autenticación ni TLS).
- **`application-secret.properties`:** ahora es opcional (`optional:classpath:`). Antes era obligatorio y, como no estaba en el repositorio, la aplicación no arrancaba. Se añadió a `.gitignore`.
- **Credenciales (Fase 9):** se quitaron del repositorio los valores por defecto de `DB_USER` y `DB_PASSWORD`. Ahora se leen de variables de entorno o de `application-secret.properties` en la raíz del proyecto (plantilla: `application-secret.properties.example`).
