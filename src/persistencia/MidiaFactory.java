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
 * Padrão Factory: cria a subclasse correta de {@link Midia} a partir de um objeto JSON, de acordo com o
 * campo {@code tipo} ({@code Filme}, {@code Serie} ou {@code Documentario}, sem diferenciar maiúsculas).
 *
 * <p>Qualquer problema nos dados (campo ausente, valor do tipo errado ou que o construtor rejeita) vira
 * uma {@link DadosInvalidosException} com uma mensagem clara.
 */
public final class MidiaFactory {

    /** Classe utilitária: não deve ser instanciada. */
    private MidiaFactory() {}

    /**
     * Cria a mídia descrita pelos dados.
     *
     * @param dados objeto JSON de um título, com o campo {@code tipo} e os campos da subclasse
     * @return um {@link Filme}, uma {@link Serie} ou um {@link Documentario}
     * @throws DadosInvalidosException se o tipo for desconhecido ou algum campo faltar ou for inválido
     */
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

    /**
     * Lê um campo obrigatório que deve ser um valor simples (texto, número ou booleano).
     *
     * @param dados o objeto JSON
     * @param campo o nome do campo
     * @return o valor do campo
     * @throws DadosInvalidosException se o campo faltar, for nulo ou não for um valor simples
     */
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

    /**
     * Lê um campo obrigatório de texto.
     *
     * @param dados o objeto JSON
     * @param campo o nome do campo
     * @return o texto do campo
     * @throws DadosInvalidosException se o campo faltar ou não for um texto
     */
    private static String texto(JsonObject dados, String campo) throws DadosInvalidosException {
        JsonPrimitive p = primitivo(dados, campo);
        
        if (!p.isString()) {
            throw new DadosInvalidosException("O campo '" + campo + "' deve ser um texto.");
        }
        
        return p.getAsString();
    }

    /**
     * Lê um campo obrigatório que deve ser um número inteiro.
     *
     * @param dados o objeto JSON
     * @param campo o nome do campo
     * @return o número do campo
     * @throws DadosInvalidosException se o campo faltar, não for número ou não for inteiro
     */
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
