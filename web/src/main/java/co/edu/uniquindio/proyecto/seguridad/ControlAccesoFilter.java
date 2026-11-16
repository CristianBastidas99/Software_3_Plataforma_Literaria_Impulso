package co.edu.uniquindio.proyecto.seguridad;

import co.edu.uniquindio.proyecto.bean.LoginBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Protege las páginas por tipo de usuario antes de que JSF las procese.
 * <ul>
 *     <li>Sin sesión: redirige a {@code login.xhtml?acceso=sesion}, que muestra un aviso.</li>
 *     <li>Con sesión de otro tipo de usuario: redirige al inicio.</li>
 * </ul>
 * Las peticiones AJAX de JSF reciben la redirección en el formato que entiende JSF (si no, el
 * navegador recibiría la página de login como respuesta parcial y no haría nada visible).
 *
 * <p>Los beans siguen comprobando el permiso en cada acción: el filtro es la primera barrera, no la única.</p>
 */
public class ControlAccesoFilter extends HttpFilter {

    /** Página (sin extensión) → tipos de usuario que pueden abrirla. Las demás son públicas. */
    static final Map<String, Set<Integer>> PAGINAS_PROTEGIDAS = Map.of(
            "admin", Set.of(LoginBean.ADMINISTRADOR),
            "editar_perfil", Set.of(LoginBean.ESCRITOR, LoginBean.LECTOR),
            "mis_obras", Set.of(LoginBean.ESCRITOR),
            "nueva_obra", Set.of(LoginBean.ESCRITOR),
            "mi_perfil", Set.of(LoginBean.LECTOR));

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Set<Integer> permitidos = PAGINAS_PROTEGIDAS.get(pagina(request));
        if (permitidos == null) {
            chain.doFilter(request, response);
            return;
        }
        LoginBean sesion = loginBean(request.getSession(false));
        boolean autenticado = sesion != null && Boolean.TRUE.equals(sesion.getAutenticado());
        if (autenticado && permitidos.contains(sesion.getTipo())) {
            // Páginas con datos personales: que el navegador no las guarde en caché
            response.setHeader("Cache-Control", "no-store");
            chain.doFilter(request, response);
            return;
        }
        String destino = request.getContextPath() + (autenticado ? "/index.xhtml" : "/login.xhtml?acceso=sesion");
        redirigir(request, response, destino);
    }

    /**
     * Nombre de la página pedida, sin extensión ni prefijo de JSF: {@code /faces/admin.xhtml},
     * {@code /admin.jsf} y {@code /admin.xhtml} dan {@code admin}.
     */
    static String pagina(HttpServletRequest request) {
        // servletPath y pathInfo ya vienen decodificados y normalizados por el contenedor (a diferencia
        // de getRequestURI): "/%61dmin.xhtml" o "/x/../admin.xhtml" no se saltan la comprobación
        String ruta = (request.getServletPath() == null ? "" : request.getServletPath())
                + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if (ruta.startsWith("/faces/")) {
            ruta = ruta.substring("/faces".length());
        }
        ruta = ruta.replaceAll("/+", "/");
        if (!ruta.startsWith("/") || ruta.indexOf('/', 1) >= 0) {
            return ""; // solo hay páginas en la raíz; WEB-INF y recursos no se protegen aquí
        }
        String nombre = ruta.substring(1);
        int punto = nombre.lastIndexOf('.');
        String extension = punto >= 0 ? nombre.substring(punto + 1).toLowerCase() : "";
        if (!(extension.equals("xhtml") || extension.equals("jsf") || extension.equals("faces"))) {
            return "";
        }
        return nombre.substring(0, punto).toLowerCase();
    }

    /** El bean de sesión lo guarda Spring en la sesión HTTP con el nombre del bean. */
    private static LoginBean loginBean(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object bean = sesion.getAttribute("loginBean");
        if (bean == null) {
            bean = sesion.getAttribute("scopedTarget.loginBean");
        }
        return bean instanceof LoginBean ? (LoginBean) bean : null;
    }

    private static void redirigir(HttpServletRequest request, HttpServletResponse response, String destino) throws IOException {
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\""
                    + destino.replace("&", "&amp;") + "\"></redirect></partial-response>");
        } else {
            response.sendRedirect(destino);
        }
    }
}
