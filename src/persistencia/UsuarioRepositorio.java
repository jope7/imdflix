package persistencia;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import modelo.Usuario;

/*
    persistencia dos usuarios
    lista e avaliacoes de cada
    por jope
*/

public interface UsuarioRepositorio {
    void salvar(Usuario usuario) throws IOException;

    List<Usuario> listarTodos() throws IOException;

    Optional<Usuario> buscarPorUsername(String username) throws IOException;
}
