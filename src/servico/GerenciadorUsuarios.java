package servico;

import excecoes.UsernameJaExisteException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import modelo.Usuario;
import persistencia.UsuarioRepositorio;

/**
 * Cadastro de usuários e "login".
 * <p>
 * Depende apenas da interface {@link UsuarioRepositorio}: ao ser criado carrega os usuários do
 * repositório; cadastros novos são persistidos na hora e mudanças em usuários existentes (lista,
 * avaliações) são persistidas com {@link #salvar(Usuario)}.
 */
public class GerenciadorUsuarios {
    private final UsuarioRepositorio repositorio;
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    /**
     * Cria o gerenciador e carrega os usuários já persistidos.
     *
     * @param repositorio onde os usuários são lidos e gravados
     * @throws IOException se não for possível ler o repositório
     */
    public GerenciadorUsuarios(UsuarioRepositorio repositorio) throws IOException {
        this.repositorio = Objects.requireNonNull(repositorio, "O repositorio nao pode ser nulo.");
        for (Usuario u : repositorio.listarTodos()) {
            usuarios.put(u.getUsername(), u);
        }
    }

    /**
     * Cadastra um usuário novo e o persiste. Se a gravação falhar, nada muda em memória.
     *
     * @param username ID do usuário
     * @param nome nome de exibição
     * @return o usuário criado
     * @throws UsernameJaExisteException se o username já está em uso (sem diferenciar maiúsculas)
     * @throws IOException se não for possível gravar no repositório
     * @throws IllegalArgumentException se o username ou o nome forem inválidos
     */
    public Usuario cadastrar(String username, String nome) throws UsernameJaExisteException, IOException {
        Usuario novo = new Usuario(username, nome);

        if (usuarios.containsKey(novo.getUsername())) {
            throw new UsernameJaExisteException(novo.getUsername());
        }

        repositorio.salvar(novo);
        usuarios.put(novo.getUsername(), novo);
        return novo;
    }

    /**
     * Persiste o estado atual de um usuário (lista e avaliações). Deve ser chamado depois de
     * alterar a lista ou avaliar uma mídia.
     *
     * @param usuario usuário já cadastrado neste gerenciador
     * @throws IllegalArgumentException se o usuário não foi cadastrado aqui
     * @throws IOException se não for possível gravar no repositório
     */
    public void salvar(Usuario usuario) throws IOException {
        if (usuarios.get(usuario.getUsername()) != usuario) {
            throw new IllegalArgumentException("Usuario nao cadastrado: " + usuario.getUsername());
        }
        repositorio.salvar(usuario);
    }

    /**
     * Procura um usuário pelo username ("login").
     *
     * @param username username informado (sem diferenciar maiúsculas)
     * @return o usuário, ou vazio se não existir
     * @throws IllegalArgumentException se o username tiver formato inválido
     */
    public Optional<Usuario> entrar(String username) {
        return Optional.ofNullable(usuarios.get(Usuario.normalizarUsername(username)));
    }

    /** @return todos os usuários, na ordem de cadastro (lista somente leitura) */
    public List<Usuario> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(usuarios.values()));
    }

}
