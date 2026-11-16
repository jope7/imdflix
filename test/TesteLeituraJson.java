package test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
    confirma que o Gson está configurado e que os arquivos de dados de exemplo são lidos
*/

public class TesteLeituraJson {

    public static void main(String[] args) throws Exception {
        System.out.println("Pasta de execucao: " + Paths.get("").toAbsolutePath());

        JsonArray catalogo = lerArray(Paths.get("dados", "catalogo.json"));
        
        System.out.println("Titulos no catalogo: " + catalogo.size());
        
        for (JsonElement e : catalogo) {
            JsonObject o = e.getAsJsonObject();
            System.out.println(" - " + o.get("tipo").getAsString() + ": " + o.get("titulo").getAsString());
        }
        
        assert catalogo.size() == 5 : "O catalogo de exemplo deveria ter 5 titulos";

        JsonArray usuarios = lerArray(Paths.get("dados", "usuarios.json"));
        
        System.out.println("Usuarios: " + usuarios.size());
        
        for (JsonElement e : usuarios) {
            JsonObject o = e.getAsJsonObject();
            System.out.println(o.get("nome") + " - @" + o.get("username").getAsString()
                + " avaliacoes: " + o.getAsJsonObject("avaliacoes").size());
        }
        
        assert usuarios.size() == 3 : "O arquivo de usuarios de exemplo deveria ter 3 usuarios";

        System.out.println("TesteLeituraJson: Gson configurado e dados lidos com sucesso.");
    }

    private static JsonArray lerArray(Path caminho) throws Exception {
        String texto = Files.readString(caminho);
        
        return JsonParser.parseString(texto).getAsJsonArray();
    }
}
