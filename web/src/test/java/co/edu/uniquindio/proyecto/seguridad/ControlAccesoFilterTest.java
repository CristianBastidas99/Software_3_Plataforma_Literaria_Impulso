package co.edu.uniquindio.proyecto.seguridad;

import co.edu.uniquindio.proyecto.bean.LoginBean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class ControlAccesoFilterTest {

    private final ControlAccesoFilter filtro = new ControlAccesoFilter();

    private static MockHttpServletRequest peticion(String servletPath, String pathInfo, Integer tipo) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", servletPath + (pathInfo == null ? "" : pathInfo));
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        if (tipo != null) {
            LoginBean login = new LoginBean();
            login.setAutenticado(true);
            login.setTipo(tipo);
            MockHttpSession sesion = new MockHttpSession();
            sesion.setAttribute("loginBean", login);
            request.setSession(sesion);
        }
        return request;
    }

    /** Devuelve la URL de redirección, o {@code null} si la petición pasó. */
    private String filtrar(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(request, response, cadena);
        if (cadena.getRequest() != null) {
            return null;
        }
        return response.getRedirectedUrl() != null ? response.getRedirectedUrl() : response.getContentAsString();
    }

    @Test
    void sinSesionVaAlLogin() throws Exception {
        assertEquals("/login.xhtml?acceso=sesion", filtrar(peticion("/editar_perfil.xhtml", null, null)));
        assertEquals("/login.xhtml?acceso=sesion", filtrar(peticion("/admin.xhtml", null, null)));
        assertEquals("/login.xhtml?acceso=sesion", filtrar(peticion("/faces", "/mis_obras.xhtml", null)));
        assertEquals("/login.xhtml?acceso=sesion", filtrar(peticion("/nueva_obra.jsf", null, null)));
        // Las páginas públicas pasan
        assertNull(filtrar(peticion("/index.xhtml", null, null)));
        assertNull(filtrar(peticion("/publicacion.xhtml", null, null)));
        assertNull(filtrar(peticion("/javax.faces.resource", "/admin.css", null)));
    }

    @Test
    void cadaRolSoloAbreSusPaginas() throws Exception {
        assertEquals("/index.xhtml", filtrar(peticion("/admin.xhtml", null, LoginBean.LECTOR)));
        assertEquals("/index.xhtml", filtrar(peticion("/admin.xhtml", null, LoginBean.ESCRITOR)));
        assertNull(filtrar(peticion("/admin.xhtml", null, LoginBean.ADMINISTRADOR)));
        assertEquals("/index.xhtml", filtrar(peticion("/mis_obras.xhtml", null, LoginBean.LECTOR)));
        assertNull(filtrar(peticion("/mis_obras.xhtml", null, LoginBean.ESCRITOR)));
        assertEquals("/index.xhtml", filtrar(peticion("/mi_perfil.xhtml", null, LoginBean.ESCRITOR)));
        assertNull(filtrar(peticion("/editar_perfil.xhtml", null, LoginBean.LECTOR)));
        assertEquals("/index.xhtml", filtrar(peticion("/editar_perfil.xhtml", null, LoginBean.ADMINISTRADOR)));
    }

    @Test
    void lasPeticionesAjaxRecibenUnaRedireccionDeJsf() throws Exception {
        MockHttpServletRequest ajax = peticion("/admin.xhtml", null, null);
        ajax.setMethod("POST");
        ajax.addHeader("Faces-Request", "partial/ajax");
        String respuesta = filtrar(ajax);
        assertTrue(respuesta.contains("<partial-response><redirect url=\"/login.xhtml?acceso=sesion\">"), respuesta);
    }

    @Test
    void elNombreDeLaPaginaNoSeEnganaConMayusculasNiPrefijos() {
        assertEquals("admin", ControlAccesoFilter.pagina(peticion("/ADMIN.xhtml", null, null)));
        assertEquals("admin", ControlAccesoFilter.pagina(peticion("/faces", "/admin.faces", null)));
        assertEquals("", ControlAccesoFilter.pagina(peticion("/WEB-INF/template.xhtml", null, null)));
        assertEquals("", ControlAccesoFilter.pagina(peticion("/uploads/img/admin.png", null, null)));
    }
}
