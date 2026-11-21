package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import excecoes.NotaInvalidaException;

/**
 * Usuário do sistema, com a sua lista ("Minha lista") e as suas avaliações.
 * <ul>
 *   <li>O {@code username} é o ID: imutável e sem diferenciar maiúsculas de minúsculas.</li>
 *   <li>O {@code nome} serve apenas para exibição e pode se repetir entre usuários.</li>
 *   <li>A lista e as avaliações guardam apenas o título da mídia, nunca o objeto.</li>
 *   <li>A lista não tem duplicados e cada usuário tem no máximo uma nota por mídia
 *       (reavaliar substitui a nota anterior).</li>
 * </ul>
 */
public class Usuario {
    /** Formato aceito para o username: de 3 a 20 caracteres entre letras minúsculas, números e {@code _}. */
    private static final Pattern FORMATO_USERNAME = Pattern.compile("^[a-z0-9_]{3,20}$");

    /** Identificador único do usuário, já normalizado (minúsculas, sem espaços nas pontas). */
    private final String username;
    /** Nome de exibição (pode se repetir entre usuários). */
    private String nome;
    /** Títulos guardados em "Minha lista", sem repetição e na ordem em que foram adicionados. */
    private final List<String> minhaLista = new ArrayList<>();
    /** Notas dadas pelo usuário: título da mídia para nota (no máximo uma por mídia). */
    private final Map<String, Double> avaliacoes = new LinkedHashMap<>();

    /**
     * Cria um usuário novo, com lista e avaliações vazias.
     *
     * @param username ID do usuário (3 a 20 caracteres entre letras, números e {@code _})
     * @param nome nome de exibição (não pode ser vazio)
     * @throws IllegalArgumentException se o username ou o nome forem inválidos
     */
    public Usuario(String username, String nome) {
        this.username = normalizarUsername(username);
        setNome(nome);
    }

    /**
     * Reconstrói um usuário a partir de dados já persistidos (ex.: lidos do JSON).
     * <p>
     * Só preenche o próprio usuário: <b>não altera nenhuma mídia</b>, ou seja, as notas
     * médias e as quantidades de avaliações das mídias continuam como estavam. Para
     * refletir as notas restauradas nas mídias use
     * {@link servico.Catalogo#aplicarAvaliacoes(java.util.Collection)} (e só uma vez: o fluxo
     * normal com o {@code UsuarioJsonRepositorio} já aplica as notas sozinho).
     * Títulos repetidos na lista são ignorados.
     *
     * @param username ID do usuário
     * @param nome nome de exibição
     * @param minhaLista títulos da lista (pode ser {@code null}, equivale a vazia)
     * @param avaliacoes nota por título (pode ser {@code null}, equivale a vazio)
     * @return o usuário restaurado
     * @throws NotaInvalidaException se alguma nota estiver fora do intervalo de 0 a 10
     * @throws IllegalArgumentException se o username, o nome ou algum título forem inválidos
     */
    public static Usuario restaurar(String username, String nome, List<String> minhaLista,
            Map<String, Double> avaliacoes) throws NotaInvalidaException {
        Usuario usuario = new Usuario(username, nome);

        if (minhaLista != null) {
            for (String titulo : minhaLista) {
                validarTitulo(titulo);
                if (!usuario.minhaLista.contains(titulo)) {
                    usuario.minhaLista.add(titulo);
                }
            }
        }

        if (avaliacoes != null) {
            for (Map.Entry<String, Double> e : avaliacoes.entrySet()) {
                validarTitulo(e.getKey());
                Double nota = e.getValue();
                if (nota == null) {
                    throw new IllegalArgumentException("Nota ausente para o titulo '" + e.getKey() + "'.");
                }
                Midia.validarNota(nota);
                usuario.avaliacoes.put(e.getKey(), nota);
            }
        }
        return usuario;
    }

    /**
     * Garante que o título recebido não é nulo nem está em branco.
     *
     * @param titulo o título a validar
     * @throws IllegalArgumentException se o título for nulo ou em branco
     */
    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O titulo nao pode ser vazio.");
        }
    }

    /**
     * Normaliza um username: remove espaços das pontas e converte para minúsculas.
     *
     * @param username username informado
     * @return o username normalizado
     * @throws IllegalArgumentException se for nulo ou não seguir o formato permitido
     */
    public static String normalizarUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("O username nao pode ser nulo");
        }

        String normalizado = username.trim().toLowerCase();

        if (!FORMATO_USERNAME.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("Username invalido: use de 3 a 20 caracteres entre letras, numeros e _.");
        }
        return normalizado;
    }

    /**
     * Adiciona a mídia à lista do usuário. Se ela já estiver na lista, nada acontece.
     *
     * @param midia mídia a adicionar
     */
    public void adicionarNaLista(Midia midia) {
        if (!minhaLista.contains(midia.getTitulo())) {
            minhaLista.add(midia.getTitulo());
        }
    }

    /**
     * Remove a mídia da lista do usuário. Se ela não estiver na lista, nada acontece.
     *
     * @param midia mídia a remover
     */
    public void removerDaLista(Midia midia) {
        minhaLista.remove(midia.getTitulo());
    }

    /**
     * Registra a nota do usuário para a mídia. Se o usuário já avaliou essa mídia, a nota
     * anterior é substituída (a mídia não conta duas avaliações do mesmo usuário). Se a nota
     * for inválida, nada é alterado.
     *
     * @param midia mídia avaliada
     * @param nota nota de 0 a 10
     * @throws NotaInvalidaException se a nota estiver fora do intervalo de 0 a 10
     */
    public void avaliar(Midia midia, double nota) throws NotaInvalidaException {
        Double anterior = avaliacoes.get(midia.getTitulo());
        if (anterior == null) {
            midia.avaliar(nota);
        } else {
            midia.substituirAvaliacao(anterior, nota);
        }

        avaliacoes.put(midia.getTitulo(), nota);
    }

    /**
     * Informa o username, que identifica o usuário e não muda.
     *
     * @return o username (ID) já normalizado
     */
    public String getUsername() {
        return username;
    }

    /**
     * Informa o nome mostrado nas telas.
     *
     * @return o nome de exibição
     */
    public String getNome() {
        return nome;
    }

    /**
     * Altera o nome de exibição.
     *
     * @param nome novo nome (não pode ser vazio)
     * @throws IllegalArgumentException se o nome for nulo ou em branco
     */
    public final void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome nao pode ser vazio.");
        }
        this.nome = nome;
    }

    /**
     * Lista os títulos que o usuário guardou em "Minha lista".
     *
     * @return visão somente leitura dos títulos da lista
     */
    public List<String> getMinhaLista() {
        return Collections.unmodifiableList(minhaLista);
    }

    /**
     * Lista as notas que o usuário deu, indexadas pelo título da mídia.
     *
     * @return visão somente leitura das notas, por título
     */
    public Map<String, Double> getAvaliacoes() {
        return Collections.unmodifiableMap(avaliacoes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Usuario outro && username.equals(outro.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return nome + "(@" + username + ")";
    }
}