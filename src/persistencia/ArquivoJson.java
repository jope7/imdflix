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

/*
    leitura e gravação dos arquivos JSON
*/
final class ArquivoJson {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ArquivoJson(){}

    // lê  a lista de raiz do arquivo. 
    // arquivo ausente ou em branco dá lista vazia.
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

    //  grava a lista
    static void gravarLista(Path caminho, JsonArray lista) throws IOException {
        Path pasta = caminho.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Files.writeString(caminho, GSON.toJson(lista) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    // le um campo de texto obrigatorio
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