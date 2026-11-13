package persistencia;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import modelo.Midia;

/*
    persistencia do catalogo
    restante do sistema depende apenas dessa interface
*/
public interface CatalogoRepositorio {
    void salvar(Midia midia) throws IOException;

    List<Midia> listarTodos() throws IOException;

    Optional<Midia> buscarPorTitulo(String titulo) throws IOException;
    
}