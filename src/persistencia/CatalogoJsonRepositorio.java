package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import excecoes.DadosInvalidosException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;

/**
 * Guarda o catálogo em um arquivo JSON (uma lista de títulos).
 *
 * <p>Regras:
 * <ul>
 *   <li>arquivo ausente ou em branco: catálogo vazio (o arquivo é criado ao salvar);</li>
 *   <li>arquivo malformado ou com dados inválidos: {@link DadosInvalidosException};</li>
 *   <li>as notas não são gravadas aqui: ficam nos usuários e as médias são recalculadas ao carregar;</li>
 *   <li>ao salvar, se o arquivo atual estiver inválido, nada é sobrescrito (a exceção é lançada).</li>
 * </ul>
 */
public class CatalogoJsonRepositorio implements CatalogoRepositorio {

    /** Gson configurado para gravar com indentação e sem escapar caracteres como aspas simples. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** Arquivo JSON do catálogo. */
    private final Path caminho;

    /**
     * Cria o repositório para o arquivo informado. O arquivo só é lido quando for preciso.
     *
     * @param caminho o arquivo JSON do catálogo
     * @throws NullPointerException se o caminho for nulo
     */
    public CatalogoJsonRepositorio(Path caminho) {
        this.caminho = Objects.requireNonNull(caminho, "O caminho do arquivo nao pode ser nulo.");
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
    public List<Midia> listarTodos() throws IOException {
        if (!Files.exists(caminho)) {
            return new ArrayList<>();
        }
         
        String nome = caminho.getFileName().toString();

        String texto;
        
        try {
            texto = Files.readString(caminho, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new DadosInvalidosException("O arquivo " + nome + " nao esta codificado em UTF-8.", e);
        }
        
        if (texto.startsWith("\uFEFF")) {
            texto = texto.substring(1);
        }
        
        if (texto.isBlank()) {
            return new ArrayList<>();
        }

        JsonElement raiz;
        
        try {
            raiz = JsonParser.parseString(texto);
        } catch (JsonParseException e) {
            throw new DadosInvalidosException("JSON malformado em " + nome + ": " + e.getMessage(), e);
        }
        
        if (!raiz.isJsonArray()) {
            throw new DadosInvalidosException(nome + " deve conter uma lista de titulos ([ ... ]).");
        }

        List<Midia> midias = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        int posicao = 0;
        
        for (JsonElement item : raiz.getAsJsonArray()) {
            posicao++;
            
            if (!item.isJsonObject()) {
                throw new DadosInvalidosException("Item " + posicao + " de " + nome + ": deve ser um objeto JSON.");
            }
            
            Midia midia;
            
            try {
                midia = MidiaFactory.criar(item.getAsJsonObject());
            } catch (DadosInvalidosException e) {
                throw new DadosInvalidosException("Item " + posicao + " de " + nome + ": " + e.getMessage(), e);
            }
            
            if (!vistos.add(chave(midia.getTitulo()))) {
                throw new DadosInvalidosException(
                    "Item " + posicao + " de " + nome + ": titulo duplicado ('" + midia.getTitulo() + "').");
            }
            midias.add(midia);
        }
        return midias;
    }

    @Override
    public Optional<Midia> buscarPorTitulo(String titulo) throws IOException {
        if (titulo == null) {
            return Optional.empty();
        }
        
        String procurado = chave(titulo);
        
        for (Midia midia : listarTodos()) {
            if (chave(midia.getTitulo()).equals(procurado)) {
                return Optional.of(midia);
            }
        }
        
        return Optional.empty();
    }

    /** Adiciona a mídia ao arquivo ou, se já existir um título igual (sem diferenciar maiúsculas), a substitui. */
    @Override
    public void salvar(Midia midia) throws IOException {
        Objects.requireNonNull(midia, "A midia nao pode ser nula.");
        List<Midia> atuais = listarTodos();
        String nova = chave(midia.getTitulo());
        boolean substituiu = false;
        
        for (int i = 0; i < atuais.size(); i++) {
            if (chave(atuais.get(i).getTitulo()).equals(nova)) {
                atuais.set(i, midia);
                substituiu = true;
                break;
            }
        }
        
        if (!substituiu) {
            atuais.add(midia);
        }
        
        gravar(atuais);
    }

    /**
     * Grava todas as mídias no arquivo, criando a pasta se for preciso.
     *
     * @param midias as mídias a gravar
     * @throws IOException se não for possível gravar
     */
    private void gravar(List<Midia> midias) throws IOException {
        JsonArray array = new JsonArray();
        
        for (Midia midia : midias) {
            array.add(paraJson(midia));
        }
        
        Path pasta = caminho.toAbsolutePath().getParent();
        
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        
        Files.writeString(caminho, GSON.toJson(array) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    /**
     * Converte uma mídia em objeto JSON, com o campo {@code tipo} e os campos da sua subclasse.
     *
     * @param midia a mídia a converter
     * @return o objeto JSON da mídia
     * @throws DadosInvalidosException se a mídia for de um tipo que não sabemos gravar
     */
    private static JsonObject paraJson(Midia midia) throws DadosInvalidosException {
        JsonObject o = new JsonObject();
        
        if (midia instanceof Filme f) {
            o.addProperty("tipo", "Filme");
            comuns(o, midia);
            o.addProperty("duracaoMinutos", f.getDuracaoTotalMinutos());
            o.addProperty("diretor", f.getDiretor());
        } else if (midia instanceof Serie s) {
            o.addProperty("tipo", "Serie");
            comuns(o, midia);
            o.addProperty("temporadas", s.getTemporadas());
            o.addProperty("episodiosPorTemporada", s.getEpisodiosPorTemporada());
            o.addProperty("duracaoMediaEpisodio", s.getDuracaoMediaEpisodio());
        } else if (midia instanceof Documentario d) {
            o.addProperty("tipo", "Documentario");
            comuns(o, midia);
            o.addProperty("duracaoMinutos", d.getDuracaoTotalMinutos());
            o.addProperty("tema", d.getTema());
        } else {
            throw new DadosInvalidosException("Tipo de midia nao suportado: " + midia.getClass().getSimpleName());
        }
        return o;
    }

    /**
     * Grava no objeto JSON os campos que todas as mídias têm.
     *
     * @param o o objeto JSON de destino
     * @param midia a mídia de onde vêm os dados
     */
    private static void comuns(JsonObject o, Midia midia) {
        o.addProperty("titulo", midia.getTitulo());
        o.addProperty("genero", midia.getGenero());
        o.addProperty("ano", midia.getAnoLancamento());
    }

    /**
     * Normaliza um título para comparação: sem espaços nas pontas e em minúsculas.
     *
     * @param titulo o título original
     * @return a chave usada para comparar títulos
     */
    private static String chave(String titulo) {
        return titulo.trim().toLowerCase();
    }
}