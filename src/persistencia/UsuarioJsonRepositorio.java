package persistencia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import excecoes.DadosInvalidosException;
import excecoes.NotaInvalidaException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import modelo.Midia;
import modelo.Usuario;

/**
 * Guarda os usuários (lista e avaliações de cada um) em um arquivo JSON.
 *
 * <p>Contrato importante: os usuários são lidos <b>uma única vez</b>, por
 * {@link #carregar(Path, Function)}, e ficam em memória. Ao carregar, cada avaliação é aplicada na
 * mídia correspondente (é isso que recalcula as médias). Por isso:
 * <ul>
 *   <li>o catálogo precisa estar totalmente carregado antes de chamar {@code carregar};</li>
 *   <li>{@code listarTodos}, {@code buscarPorUsername} e {@code salvar} nunca releem o arquivo;</li>
 *   <li>títulos que não existem mais no catálogo são ignorados (somem do arquivo na próxima gravação);</li>
 *   <li>o arquivo é validado por inteiro antes de qualquer mídia ser alterada: se estiver inválido, a
 *       exceção é lançada e nenhuma média muda.</li>
 * </ul>
 */
public class UsuarioJsonRepositorio implements UsuarioRepositorio {
    
    /**
     * Dados de um usuário já validados, ainda sem tocar em nenhuma mídia.
     *
     * @param username username já normalizado
     * @param nome nome de exibição
     * @param lista títulos de "Minha lista"
     * @param notas notas dadas, por título
     */
    private record Registro(String username, String nome, List<String> lista, Map<String, Double> notas) {
    }

    /** Arquivo JSON dos usuários. */
    private final Path caminho;
    /** Usuários em memória, indexados pelo username, na ordem do arquivo. */
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    /**
     * Uso interno: quem cria o repositório é {@link #carregar(Path, Function)}.
     *
     * @param caminho o arquivo JSON dos usuários
     */
    private UsuarioJsonRepositorio(Path caminho) {
        this.caminho = caminho;
    }

    /**
     * Lê o arquivo (ausente ou em branco dá lista vazia) e monta os usuários, aplicando as avaliações
     * nas mídias do catálogo.
     *
     * @param caminho o arquivo JSON dos usuários
     * @param buscaMidia como achar uma mídia pelo título, normalmente {@code catalogo::buscarPorTitulo}
     * @return o repositório com os usuários carregados
     * @throws IOException se o arquivo não puder ser lido
     * @throws DadosInvalidosException se o arquivo estiver malformado ou com dados inválidos
     */
    public static UsuarioJsonRepositorio carregar(Path caminho, Function<String, Optional<Midia>> buscaMidia) throws IOException {
        Objects.requireNonNull(caminho, "O caminho do arquivo nao pode ser nulo.");
        Objects.requireNonNull(buscaMidia, "A busca de midias nao pode ser nula.");
        UsuarioJsonRepositorio repositorio = new UsuarioJsonRepositorio(caminho);
        repositorio.lerArquivo(buscaMidia);
        return repositorio;
    }

    /**
     * Informa o arquivo usado.
     *
     * @return o caminho do arquivo JSON
     */
    public Path getCaminho() {
        return caminho;
    }

    @Override
    public List<Usuario> listarTodos() throws IOException {
        return Collections.unmodifiableList(new ArrayList<>(usuarios.values()));
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws IOException {
        if (username == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(usuarios.get(Usuario.normalizarUsername(username)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
            // formato invalido, nao pode existir
        }
    }

    // guarda o usuário em memória (substituindo o de mesmo username) e regrava o arquivo inteiro
    @Override
    public void salvar(Usuario usuario) throws IOException {
        Objects.requireNonNull(usuario, "O usuario nao pode ser nulo.");
        usuarios.put(usuario.getUsername(), usuario);
        gravar();
    }

    // ----- leitura

    /**
     * Lê o arquivo em duas fases: primeiro valida todos os registros sem alterar nenhuma mídia e só
     * depois monta os usuários e aplica as notas. Títulos que não existem no catálogo são ignorados.
     *
     * @param buscaMidia como achar uma mídia pelo título
     * @throws IOException se o arquivo não puder ser lido ou tiver dados inválidos
     */
    private void lerArquivo(Function<String, Optional<Midia>> buscaMidia) throws IOException {
        String nome = caminho.getFileName().toString();
        JsonArray itens = ArquivoJson.lerLista(caminho);

        // fase 1: validar tudo
        List<Registro> registros = new ArrayList<>();
        Map<String, Registro> vistos = new LinkedHashMap<>();
        int posicao = 0;

        for (JsonElement item : itens) {
            posicao++;
            if (!item.isJsonObject()) {
                throw new DadosInvalidosException("Item " + posicao + " de " + nome + ": deve ser um objeto JSON.");
            }
            Registro registro;
            try {
                registro = lerRegistro(item.getAsJsonObject());
            } catch (DadosInvalidosException e) {
                throw new DadosInvalidosException("Item " + posicao + " de " + nome + ": " + e.getMessage(), e);
            }
            if (vistos.put(registro.username(), registro) != null) {
                throw new DadosInvalidosException(
                    "Item " + posicao + " de " + nome + ": username duplicado ('" + registro.username() + "').");
            }
            registros.add(registro);
        }

        // fase 2: montar os usuários e aplicar as avaliações nas mídias
        for (Registro r : registros) {
            Usuario usuario = new Usuario(r.username(), r.nome());
            
            for (String titulo : r.lista()) {
                buscaMidia.apply(titulo).ifPresent(usuario::adicionarNaLista); // ausente: ignora
            }
            
            for (Map.Entry<String, Double> nota : r.notas().entrySet()) {
                Optional<Midia> midia = buscaMidia.apply(nota.getKey());
                if (midia.isPresent()) { // ausente: ignora
                    try {
                        usuario.avaliar(midia.get(), nota.getValue());
                    } catch (NotaInvalidaException e) {
                        throw new IllegalStateException("Nota ja validada foi rejeitada", e);
                    }
                }
            }
            
            usuarios.put(usuario.getUsername(), usuario);
        }
    }

    /**
     * Valida e lê os dados de um usuário. {@code minhaLista} e {@code avaliacoes} são opcionais.
     *
     * @param o o objeto JSON de um usuário
     * @return os dados do usuário, já validados
     * @throws DadosInvalidosException se algum campo faltar, tiver o tipo errado ou uma nota estiver fora de 0 a 10
     */
    private static Registro lerRegistro(JsonObject o) throws DadosInvalidosException {
        String username;
        
        try {
            username = Usuario.normalizarUsername(ArquivoJson.texto(o, "username"));
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException(e.getMessage(), e);
        }
        
        String nome = ArquivoJson.texto(o, "nome");
        
        if (nome.isBlank()) {
            throw new DadosInvalidosException("O nome nao pode ser vazio.");
        }

        List<String> lista = new ArrayList<>();
        JsonElement elementoLista = o.get("minhaLista");
        
        if (elementoLista != null && !elementoLista.isJsonNull()) {
            
            if (!elementoLista.isJsonArray()) {
                throw new DadosInvalidosException("O campo 'minhaLista' deve ser uma lista de titulos.");
            }
            
            for (JsonElement titulo : elementoLista.getAsJsonArray()) {
                
                if (!titulo.isJsonPrimitive() || !titulo.getAsJsonPrimitive().isString()) {
                    throw new DadosInvalidosException("Todos os itens de 'minhaLista' devem ser textos.");
                }
                
                lista.add(titulo.getAsString());
            }
        }

        Map<String, Double> notas = new LinkedHashMap<>();
        JsonElement elementoNotas = o.get("avaliacoes");
        
        if (elementoNotas != null && !elementoNotas.isJsonNull()) {
            if (!elementoNotas.isJsonObject()) {
                throw new DadosInvalidosException("O campo 'avaliacoes' deve ser um objeto (titulo: nota).");
            }
            
            for (Map.Entry<String, JsonElement> entrada : elementoNotas.getAsJsonObject().entrySet()) {
                JsonElement valor = entrada.getValue();
                
                if (!valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isNumber()) {
                    throw new DadosInvalidosException("A nota de '" + entrada.getKey() + "' deve ser um numero.");
                }
                
                double nota = valor.getAsDouble();
                
                if (Double.isNaN(nota) || nota < 0 || nota > 10) {
                    throw new DadosInvalidosException("Nota invalida para '" + entrada.getKey() + "': " + nota
                        + ". A nota deve estar entre 0 e 10.");
                }
                
                notas.put(entrada.getKey(), nota);
            }
        }
        
        return new Registro(username, nome, lista, notas);
    }


    // ----- gravacao

    /**
     * Grava no arquivo todos os usuários que estão em memória.
     *
     * @throws IOException se não for possível gravar
     */
    private void gravar() throws IOException {
        JsonArray array = new JsonArray();
        
        for (Usuario usuario : usuarios.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("username", usuario.getUsername());
            o.addProperty("nome", usuario.getNome());

            JsonArray lista = new JsonArray();
        
            for (String titulo : usuario.getMinhaLista()) {
                lista.add(titulo);
            }
        
            o.add("minhaLista", lista);

            JsonObject notas = new JsonObject();
        
            for (Map.Entry<String, Double> nota : usuario.getAvaliacoes().entrySet()) {
                notas.addProperty(nota.getKey(), nota.getValue());
            }
        
            o.add("avaliacoes", notas);
            array.add(o);
        }
        
        ArquivoJson.gravarLista(caminho, array);
    }

}
