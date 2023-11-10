package co.edu.uniquindio.proyecto.test;


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
public class ObraLiterariaTest {

    @Autowired
    private ObraLiterariaRepo obraLiterariaRepo;

    @Test
    @Sql("classpath:pli.sql")
    public void testGuardarObraLiteraria() {
        // Crear una nueva obra literaria y guardarla en la base de datos
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);

        ObraLiteraria obraGuardada = obraLiterariaRepo.save(obraLiteraria);

        // Verificar que se haya guardado correctamente
        Assertions.assertNotNull(obraGuardada.getId());
        Assertions.assertEquals("Obra1", obraGuardada.getTitulo());
        // Agregar más aserciones según tus necesidades
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testBuscarObraLiterariaPorId() {
        // Crear una obra literaria de ejemplo y persistirla en la base de datos
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        // Buscar la obra literaria por su ID
        Long idDeLaObra = obraLiteraria.getId();
        ObraLiteraria obraEncontrada = obraLiterariaRepo.findById(idDeLaObra).orElse(null);

        // Verificar que se haya encontrado la obra literaria
        Assertions.assertNotNull(obraEncontrada);
        Assertions.assertEquals("Obra1", obraEncontrada.getTitulo());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testActualizarObraLiteraria() {
        // Crear una obra literaria de ejemplo y persistirla en la base de datos
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        // Actualizar el título de la obra literaria
        obraLiteraria.setTitulo("Nuevo título de la obra");
        obraLiterariaRepo.save(obraLiteraria);

        // Buscar la obra literaria por su ID después de la actualización
        Long idDeLaObra = obraLiteraria.getId();
        ObraLiteraria obraActualizada = obraLiterariaRepo.findById(idDeLaObra).orElse(null);

        // Verificar que se haya actualizado el título
        Assertions.assertNotNull(obraActualizada);
        Assertions.assertEquals("Nuevo título de la obra", obraActualizada.getTitulo());
    }

    @Test
    @Sql("classpath:pli.sql")
    public void testEliminarObraLiteraria() {
        // Crear una obra literaria de ejemplo y persistirla en la base de datos
        ObraLiteraria obraLiteraria = new ObraLiteraria();
        obraLiteraria.setTitulo("Obra1");
        obraLiteraria.setFechaPublicacion(new Date());
        obraLiteraria.setSinopsis("Una breve sinopsis");
        obraLiteraria.setIsbn("123456789");
        obraLiteraria.setEditorial("Editorial1");
        obraLiteraria.setEstado(Estado.ACTIVO);
        obraLiterariaRepo.save(obraLiteraria);

        // Eliminar la obra literaria de la base de datos
        obraLiterariaRepo.delete(obraLiteraria);

        // Intentar buscar la obra literaria por su ID después de la eliminación
        Long idDeLaObra = obraLiteraria.getId();
        ObraLiteraria obraEliminada = obraLiterariaRepo.findById(idDeLaObra).orElse(null);

        // Verificar que la obra literaria haya sido eliminada (debería ser nula)
        Assertions.assertNull(obraEliminada);
    }

    @Test
    @Sql("classpath:pli.sql")
    public void listarObrasLiterariasTest(){
        //Obtenemos la lista de todas las obras literarias
        List<ObraLiteraria> lista = obraLiterariaRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
        // Puedes agregar aserciones según tus necesidades
    }


}
