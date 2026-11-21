package servico;

import excecoes.NotaInvalidaException;
import excecoes.TituloDuplicadoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import modelo.Midia;
import modelo.Usuario;
import persistencia.CatalogoRepositorio;

/**
 * Reúne todos os títulos disponíveis. O título é o ID e não pode se repetir (sem diferenciar
 * maiúsculas de minúsculas).
 * <p>
 * Depende apenas da interface {@link CatalogoRepositorio}: ao ser criado carrega os títulos do
 * repositório e cada cadastro novo é persistido por ele. Mantém os objetos em memória para que
 * as notas das mídias sejam compartilhadas por todos os usuários.
 */
public class Catalogo {
    /** Onde os títulos são lidos e gravados. */
    private final CatalogoRepositorio repositorio;
    /** Títulos em memória, indexados pelo título em minúsculas e na ordem de cadastro. */
    private final Map<String, Midia> midias = new LinkedHashMap<>();

    /**
     * Cria o catálogo e carrega os títulos já persistidos.
     *
     * @param repositorio onde os títulos são lidos e gravados
     * @throws IOException se não for possível ler o repositório
     */
    public Catalogo(CatalogoRepositorio repositorio) throws IOException {
        this.repositorio = Objects.requireNonNull(repositorio, "O repositorio nao pode ser nulo.");
        for (Midia m : repositorio.listarTodos()) {
            midias.put(chave(m.getTitulo()), m);
        }
    }

    /**
     * Normaliza um título para comparação: sem espaços nas pontas e em minúsculas.
     *
     * @param titulo o título original
     * @return a chave usada para indexar e comparar títulos
     */
    private static String chave(String titulo) {
        return titulo.trim().toLowerCase();
    }

    /**
     * Cadastra uma mídia nova e a persiste. Se a gravação falhar, o catálogo em memória não muda.
     *
     * @param midia mídia a cadastrar
     * @throws TituloDuplicadoException se já existe um título igual (sem diferenciar maiúsculas)
     * @throws IOException se não for possível gravar no repositório
     */
    public void cadastrar(Midia midia) throws TituloDuplicadoException, IOException {
        String chave = chave(midia.getTitulo());

        if (midias.containsKey(chave)) {
            throw new TituloDuplicadoException(midia.getTitulo());
        }

        repositorio.salvar(midia);
        midias.put(chave, midia);
    }

    /**
     * Procura uma mídia pelo título, sem diferenciar maiúsculas de minúsculas.
     *
     * @param titulo título procurado
     * @return a mídia, ou vazio se não existir
     */
    public Optional<Midia> buscarPorTitulo(String titulo) {
        return Optional.ofNullable(midias.get(chave(titulo)));
    }

    /**
     * Filtra as mídias de um gênero, sem diferenciar maiúsculas de minúsculas.
     *
     * @param genero gênero desejado
     * @return as mídias do gênero, na ordem de cadastro
     */
    public List<Midia> filtrarPorGenero(String genero) {
        List<Midia> resultado = new ArrayList<>();

        for (Midia m : midias.values()) {
            if (m.getGenero().equalsIgnoreCase(genero.trim())) {
                resultado.add(m);
            }
        }

        return resultado;
    }

    /**
     * Lista todos os títulos do catálogo.
     *
     * @return todas as mídias, na ordem de cadastro (lista somente leitura)
     */
    public List<Midia> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(midias.values()));
    }

    /**
     * Reflete nas mídias as notas guardadas nos usuários. As notas não são gravadas junto com o
     * catálogo, então isto deve ser chamado <b>uma única vez</b> ao iniciar o sistema, depois de
     * restaurar os usuários (chamar de novo contaria as mesmas notas em dobro). Notas de títulos
     * que não existem mais no catálogo são ignoradas.
     *
     * <p><b>Atenção:</b> o fluxo normal do sistema ({@link CarregadorDados} com o
     * {@code UsuarioJsonRepositorio}) já aplica as notas nas mídias ao carregar. Não chame este método
     * depois dele, ou as notas serão contadas em dobro. Ele serve só para quem restaura usuários sem
     * tocar nas mídias (por exemplo, testes com repositórios em memória).
     *
     * @param usuarios usuários restaurados
     */
    public void aplicarAvaliacoes(Collection<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            for (Map.Entry<String, Double> nota : u.getAvaliacoes().entrySet()) {
                Midia m = midias.get(chave(nota.getKey()));
                if (m != null) {
                    try {
                        m.avaliar(nota.getValue());
                    } catch (NotaInvalidaException e) {
                        // nao deve ocorrer: Usuario so guarda notas ja validadas
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
