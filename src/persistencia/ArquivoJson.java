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

/**
 * Leitura e gravação de arquivos JSON que têm uma lista na raiz. Uso interno do pacote.
 */
final class ArquivoJson {

    /** Gson configurado para gravar com indentação e sem escapar caracteres como aspas simples. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** Classe utilitária: não deve ser instanciada. */
    private ArquivoJson(){}

    /**
     * Lê a lista da raiz do arquivo. Arquivo ausente ou em branco dá lista vazia.
     *
     * @param caminho o arquivo JSON
     * @return os itens da lista
     * @throws IOException se o arquivo não puder ser lido
     * @throws DadosInvalidosException se o arquivo não estiver em UTF-8, estiver malformado ou não tiver uma lista na raiz
     */
    static JsonArray lerLista(Path caminho) throws IOException {
        if (!Files.exists(caminho)) {
            return new JsonArray();
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
            return new JsonArray();
        }

        JsonElement raiz;
        
        try {
            raiz = JsonParser.parseString(texto);
        } catch (JsonParseException e) {
            throw new DadosInvalidosException("JSON malformado em " + nome + ": " + e.getMessage(), e);
        }
        
        if (!raiz.isJsonArray()) {
            throw new DadosInvalidosException(nome + " deve conter uma lista ([ ... ]).");
        }
        
        return raiz.getAsJsonArray();
    }

    /**
     * Grava a lista no arquivo, criando a pasta se for preciso.
     *
     * @param caminho o arquivo JSON
     * @param lista os itens a gravar
     * @throws IOException se não for possível gravar
     */
    static void gravarLista(Path caminho, JsonArray lista) throws IOException {
        Path pasta = caminho.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Files.writeString(caminho, GSON.toJson(lista) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    /**
     * Lê um campo de texto obrigatório.
     *
     * @param objeto o objeto JSON
     * @param campo o nome do campo
     * @return o texto do campo
     * @throws DadosInvalidosException se o campo faltar ou não for um texto
     */
    static String texto(JsonObject objeto, String campo) throws DadosInvalidosException {
        JsonElement e = objeto.get(campo);
        if (e == null || e.isJsonNull()) {
            throw new DadosInvalidosException("Campo obrigatorio ausente: '" + campo + "'.");
        }
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            throw new DadosInvalidosException("O campo '" + campo + "' deve ser um texto.");
        }
        return e.getAsString();
    }

}