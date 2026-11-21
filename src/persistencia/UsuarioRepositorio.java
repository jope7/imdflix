package persistencia;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import modelo.Usuario;

/**
 * Persistência dos usuários, com a lista e as avaliações de cada um.
 *
 * @author jope
 */
public interface UsuarioRepositorio {
    /**
     * Guarda um usuário, substituindo o que tiver o mesmo username.
     *
     * @param usuario o usuário a guardar
     * @throws IOException se não for possível gravar
     */
    void salvar(Usuario usuario) throws IOException;

    /**
     * Lista todos os usuários guardados.
     *
     * @return os usuários, na ordem em que foram guardados
     * @throws IOException se não for possível ler
     */
    List<Usuario> listarTodos() throws IOException;

    /**
     * Procura um usuário pelo username, sem diferenciar maiúsculas de minúsculas.
     *
     * @param username o username procurado
     * @return o usuário encontrado, ou vazio se não existir
     * @throws IOException se não for possível ler
     */
    Optional<Usuario> buscarPorUsername(String username) throws IOException;
}
