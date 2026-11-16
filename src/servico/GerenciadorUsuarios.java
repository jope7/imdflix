package servico;

import excecoes.UsernameJaExisteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import modelo.Usuario;

/*
    cadastro de usuarios e "login"
*/

public class GerenciadorUsuarios {
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    public Usuario cadastrar(String username, String nome) throws UsernameJaExisteException {
        Usuario novo = new Usuario(username, nome);

        if (usuarios.containsKey(novo.getUsername())) {
            throw new UsernameJaExisteException(novo.getUsername());
        }

        usuarios.put(novo.getUsername(), novo);
        return novo;
    }

    public Optional<Usuario> entrar(String username) {
        return Optional.ofNullable(usuarios.get(Usuario.normalizarUsername(username)));
    }

    public List<Usuario> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(usuarios.values()));
    }

}
