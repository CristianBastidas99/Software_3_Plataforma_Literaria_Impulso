package co.edu.uniquindio.proyecto.test.consultas;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FragmentoTest {

    @Autowired
    private FragmentoRepo fragmentoRepo;

    @Autowired
    private ObraLiterariaRepo obraLiterariaRepo;


    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarFragmento() {
        // Crear un nuevo fragmento y guardarlo en la base de datos
        Fragmento fragmento = new Fragmento();
        fragmento.setUrl("https://example.com/fragmento1");
        fragmento.setEstado(Estado.ACTIVO);

        // Asociar el fragmento a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        fragmento.setObraLiteraria(obraLiteraria);

        Fragmento fragmentoGuardado = fragmentoRepo.save(fragmento);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(fragmentoGuardado.getId());
        Assertions.assertEquals("https://example.com/fragmento1", fragmentoGuardado.getUrl());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarFragmentoPorId() {
        // Crear un fragmento de ejemplo y persistirlo en la base de datos
        Fragmento fragmento = new Fragmento();
        fragmento.setUrl("https://example.com/fragmento1");
        fragmento.setEstado(Estado.ACTIVO);

        // Asociar el fragmento a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        fragmento.setObraLiteraria(obraLiteraria);
        fragmentoRepo.save(fragmento);

        // Buscar el fragmento por su ID
        Long idDelFragmento = fragmento.getId();
        Fragmento fragmentoEncontrado = fragmentoRepo.findById(idDelFragmento).orElse(null);

        // Verificar que se haya encontrado el fragmento
        Assertions.assertNotNull(fragmentoEncontrado);
        Assertions.assertEquals("https://example.com/fragmento1", fragmentoEncontrado.getUrl());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarFragmento() {
        // Crear un fragmento de ejemplo y persistirlo en la base de datos
        Fragmento fragmento = new Fragmento();
        fragmento.setUrl("https://example.com/fragmento1");
        fragmento.setEstado(Estado.ACTIVO);

        // Asociar el fragmento a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        fragmento.setObraLiteraria(obraLiteraria);
        fragmentoRepo.save(fragmento);

        // Actualizar la URL del fragmento
        fragmento.setUrl("https://example.com/nuevo-fragmento");
        fragmentoRepo.save(fragmento);

        // Buscar el fragmento por su ID después de la actualización
        Long idDelFragmento = fragmento.getId();
        Fragmento fragmentoActualizado = fragmentoRepo.findById(idDelFragmento).orElse(null);

        // Verificar que se haya actualizado la URL
        Assertions.assertNotNull(fragmentoActualizado);
        Assertions.assertEquals("https://example.com/nuevo-fragmento", fragmentoActualizado.getUrl());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarFragmento() {
        // Crear un fragmento de ejemplo y persistirlo en la base de datos
        Fragmento fragmento = new Fragmento();
        fragmento.setUrl("https://example.com/fragmento1");
        fragmento.setEstado(Estado.ACTIVO);

        // Asociar el fragmento a una obra literaria existente (puedes crearla o cargarla desde la base de datos)
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        fragmento.setObraLiteraria(obraLiteraria);
        fragmentoRepo.save(fragmento);

        // Eliminar el fragmento de la base de datos
        fragmentoRepo.delete(fragmento);

        // Intentar buscar el fragmento por su ID después de la eliminación
        Long idDelFragmento = fragmento.getId();
        Fragmento fragmentoEliminado = fragmentoRepo.findById(idDelFragmento).orElse(null);

        // Verificar que el fragmento haya sido eliminado (debería ser nulo)
        Assertions.assertNull(fragmentoEliminado);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarFragmentosTest(){
        //Obtenemos la lista de todos los fragmentos
        List<Fragmento> lista = fragmentoRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }


}
