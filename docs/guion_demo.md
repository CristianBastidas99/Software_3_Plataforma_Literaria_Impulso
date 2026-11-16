# Guion de demostración (5 minutos)

Recorrido por los tres roles de la Plataforma Literaria Impulso. Cada paso indica lo que hay que hacer y lo que se debe ver.

## Preparación (antes de presentar)

1. Levanta los servicios y la aplicación con los datos de demostración, sobre una base de datos vacía:
   ```bash
   docker compose up -d
   ./gradlew :web:bootRun --args='--spring.profiles.active=demo'
   ```
2. Abre dos pestañas: http://localhost:8080 (la aplicación) y http://localhost:8025 (Mailpit, la bandeja de los correos).
3. Ten a mano una imagen JPG o PNG y un PDF para publicar la obra. Sirven los de la demo: `web/src/main/webapp/resources/img/demo_portada_faro.jpg` y `web/src/main/webapp/resources/pdf/demo_fragmento_faro.pdf`.

| Rol | Correo | Contraseña |
|---|---|---|
| Escritora | valentina@impulso.com | escritor123 |
| Lectora | camila@impulso.com | lector123 |
| Administrador | admin@impulso.com | admin123 |

## 1. Visitante (1 minuto)

| # | Acción | Resultado esperado |
|---|---|---|
| 1.1 | Abre el inicio. | Portada de bienvenida, carrusel de publicaciones recientes y de escritores. |
| 1.2 | Pulsa el botón de la luna en la cabecera. | Toda la página, incluidos los componentes, pasa a modo oscuro sin recargar. Vuelve a pulsarlo para regresar al claro (o déjalo en oscuro). |
| 1.3 | Ve a **Explorar**, escribe `niebla` y pulsa **Buscar**. | Aparece "La niebla del cafetal". |
| 1.4 | Cambia el género a **Fantasía** y el orden a **Más populares**; pulsa **Buscar**. | La lista se filtra por género. |
| 1.5 | Abre "La niebla del cafetal". | Detalle con portada, autora, géneros, sinopsis, vistas y recomendaciones, y el fragmento en PDF dentro de la página. |
| 1.6 | Escribe en la barra de direcciones `localhost:8080/no-existe`. | Página 404 propia con el diseño del sitio. |

## 2. Escritora (1 minuto y medio)

| # | Acción | Resultado esperado |
|---|---|---|
| 2.1 | **Entrar** con la cuenta de Valentina. | En la cabecera aparecen su avatar y su nombre. |
| 2.2 | Menú del nombre → **Mis obras**. | Tabla con sus obras, estado y acciones. |
| 2.3 | **Nueva obra**: título `El último tren a Armenia`, sinopsis y descripción breves, ISBN `978-0-306-40615-7`, género **Ficción**, una fecha pasada. Sube la portada y el PDF, y pulsa **Publicar obra**. | Vuelve a Mis obras con el mensaje de éxito y la obra nueva como publicada. |
| 2.4 | Prueba un ISBN incorrecto (`978-0-306-40615-0`) al editarla. | Mensaje: el ISBN no es válido. Cancela sin guardar. |
| 2.5 | **Cerrar sesión**. | Vuelve al inicio sin sesión. |

## 3. Lectora (1 minuto y medio)

| # | Acción | Resultado esperado |
|---|---|---|
| 3.1 | **Entrar** con la cuenta de Camila. | Sesión iniciada; en el inicio aparece "Recomendado para ti". |
| 3.2 | Abre "El último tren a Armenia" (Explorar o inicio). | El detalle de la obra recién publicada. |
| 3.3 | Pulsa **Recomendar**. | El botón pasa a "Recomendada" y el contador sube a 1. |
| 3.4 | Comenta `Me encantó, aunque el revisor es un bobalicón` y pulsa **Publicar comentario**. | El comentario aparece con la palabra censurada con asteriscos. |
| 3.5 | En Mailpit (pestaña 2). | Valentina recibió el correo de aviso del comentario nuevo. |
| 3.6 | En **Explorar**, cambia a **Buscar escritores**, busca `Mateo` y abre su perfil. Pulsa **Seguir**. | El botón pasa a "Siguiendo". (Camila ya sigue a Valentina en los datos demo.) |
| 3.7 | Menú del nombre → **Mi perfil**. | Sus géneros preferidos, los escritores que sigue (ahora también Mateo), sus recomendaciones y el historial de lectura con "El último tren a Armenia". |
| 3.8 | **Cerrar sesión**. | |

## 4. Administrador (1 minuto)

| # | Acción | Resultado esperado |
|---|---|---|
| 4.1 | **Entrar** con la cuenta del administrador y abrir **Panel de control**. | Resumen con contadores de escritores, lectores, obras y comentarios, y publicaciones más vistas. |
| 4.2 | Pestaña **Comentarios**: **Ocultar** el comentario de Camila. | Pasa a oculto; ya no se ve en la obra. |
| 4.3 | Pestaña **Géneros**: crear `Crónica`. | El género aparece en la lista y ya se puede elegir al publicar. |
| 4.4 | Pestaña **Usuarios**: **Bloquear** a un lector (por ejemplo Laura). | Pasa a bloqueado; esa persona ya no puede iniciar sesión. |
| 4.5 | (Opcional) Desbloquearla para dejar la demo como estaba. | |

## Al terminar

- Para volver a empezar desde cero: `docker compose down -v` (borra la base de datos de la demo) y vuelve a la preparación.
- Las páginas no deben mostrar errores en ningún momento. Si aparece "Algo salió mal" (página 500), revisa la consola de la aplicación.
