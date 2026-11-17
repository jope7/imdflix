package persistencia;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import excecoes.DadosInvalidosException;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;

/**
    cria a subclasse correta de Midia a partir de um objeto JSON,
    de acordo com o campo "tipo" (Filme, Serie ou Documentario)
 */
public final class MidiaFactory {

    private MidiaFactory() {}

    public static Midia criar(JsonObject dados) throws DadosInvalidosException {
        String tipo = texto(dados, "tipo");
        String titulo = texto(dados, "titulo");
        
        try {
            return switch (tipo.trim().toLowerCase()) {
                case "filme" -> new Filme(
                    titulo,
                    texto(dados, "genero"),
                    inteiro(dados, "ano"),
                    inteiro(dados, "duracaoMinutos"),
                    texto(dados, "diretor"));
                
                case "serie" -> new Serie(
                    titulo,
                    texto(dados, "genero"),
                    inteiro(dados, "ano"),
                    inteiro(dados, "temporadas"),
                    inteiro(dados, "episodiosPorTemporada"),
                    inteiro(dados, "duracaoMediaEpisodio"));
                
                case "documentario" -> new Documentario(
                    titulo,
                    texto(dados, "genero"),
                    inteiro(dados, "ano"),
                    inteiro(dados, "duracaoMinutos"),
                    texto(dados, "tema"));
                
                default -> throw new DadosInvalidosException(
                    "Tipo desconhecido: '" + tipo + "'. Use Filme, Serie ou Documentario.");
            };

        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Dados invalidos para '" + titulo + "': " + e.getMessage(), e);
        }
    }

    private static JsonPrimitive primitivo(JsonObject dados, String campo) throws DadosInvalidosException {
        JsonElement elemento = dados.get(campo);
        
        if (elemento == null || elemento.isJsonNull()) {
            throw new DadosInvalidosException("Campo obrigatorio ausente: '" + campo + "'.");
        }
        
        if (!elemento.isJsonPrimitive()) {
            throw new DadosInvalidosException("O campo '" + campo + "' tem um formato invalido.");
        }
        
        return elemento.getAsJsonPrimitive();
    }

    private static String texto(JsonObject dados, String campo) throws DadosInvalidosException {
        JsonPrimitive p = primitivo(dados, campo);
        
        if (!p.isString()) {
            throw new DadosInvalidosException("O campo '" + campo + "' deve ser um texto.");
        }
        
        return p.getAsString();
    }

    private static int inteiro(JsonObject dados, String campo) throws DadosInvalidosException {
        JsonPrimitive p = primitivo(dados, campo);
        
        if (!p.isNumber()) {
            throw new DadosInvalidosException("O campo '" + campo + "' deve ser um numero inteiro.");
        }
        
        double valor = p.getAsDouble();
        
        if (valor != Math.rint(valor) || Math.abs(valor) > Integer.MAX_VALUE) {
            throw new DadosInvalidosException("O campo '" + campo + "' deve ser um numero inteiro.");
        }

        return (int) valor;
    }
}
