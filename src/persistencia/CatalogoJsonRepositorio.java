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

/*
    guarda o catalogo em um arquivo json

    arquivo ausente ou em branco: catalogo vazio (o arquivo é criado ao salvar)
    arquivo malformatado ou com dados inválidos: DadosInvalidosException
    as notas não são gravadas aqui, ficam nos usuários e as medias são recalculadas ao carregar
    ao salvar, se o arquivo atual estiver invalido, nada é sobrescrito (exceção é lançada)
*/
public class CatalogoJsonRepositorio implements CatalogoRepositorio {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path caminho;

    public CatalogoJsonRepositorio(Path caminho) {
        this.caminho = Objects.requireNonNull(caminho, "O caminho do arquivo nao pode ser nulo.");
    }

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

    private static void comuns(JsonObject o, Midia midia) {
        o.addProperty("titulo", midia.getTitulo());
        o.addProperty("genero", midia.getGenero());
        o.addProperty("ano", midia.getAnoLancamento());
    }

    private static String chave(String titulo) {
        return titulo.trim().toLowerCase();
    }
}