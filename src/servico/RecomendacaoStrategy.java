package servico;

import java.util.ArrayList;
import java.util.List;
import modelo.Midia;
import modelo.Usuario;

/**
 * Padrão Strategy: cada implementação é um algoritmo diferente de recomendação, e o
 * {@link Recomendador} pode trocar de algoritmo em tempo de execução sem mudar o seu próprio código.
 *
 * <p>Regras comuns a todas as estratégias:
 * <ul>
 *   <li>nunca recomendam títulos que o usuário já avaliou ou já colocou na sua lista;</li>
 *   <li>devolvem no máximo {@code limite} títulos, em ordem do mais para o menos recomendado;</li>
 *   <li>a lista devolvida é somente leitura e pode ser vazia (por exemplo, sem dados suficientes).</li>
 * </ul>
 */
public interface RecomendacaoStrategy {

    /**
     * Dá o nome do critério, para mostrar ao usuário.
     *
     * @return um nome curto (por exemplo, "Mais bem avaliados")
     */
    String getNome();

    /**
     * Explica como a recomendação é calculada.
     *
     * @return uma frase descrevendo o critério
     */
    String getDescricao();

    /**
     * Calcula as recomendações para o usuário.
     *
     * @param usuario usuário que receberá as recomendações
     * @param catalogo títulos disponíveis
     * @param limite quantidade máxima de títulos (deve ser positiva)
     * @return os títulos recomendados, do mais para o menos recomendado
     * @throws IllegalArgumentException se o limite não for positivo
     */
    List<Midia> recomendar(Usuario usuario, List<Midia> catalogo, int limite);

    /**
     * Separa os títulos que o usuário ainda não avaliou nem colocou na lista.
     *
     * @param usuario o usuário
     * @param catalogo todos os títulos disponíveis
     * @return os títulos ainda não vistos pelo usuário (lista nova, que pode ser alterada)
     */
    static List<Midia> naoVistas(Usuario usuario, List<Midia> catalogo) {
        List<Midia> resultado = new ArrayList<>();
        for (Midia midia : catalogo) {
            String titulo = midia.getTitulo();
            if (!usuario.getAvaliacoes().containsKey(titulo) && !usuario.getMinhaLista().contains(titulo)) {
                resultado.add(midia);
            }
        }
        return resultado;
    }

    /**
     * Garante que o limite pedido é positivo.
     *
     * @param limite a quantidade máxima de títulos pedida
     * @throws IllegalArgumentException se o limite for zero ou negativo
     */
    static void validarLimite(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("O limite deve ser positivo.");
        }
    }

    /**
     * Corta a lista nos primeiros {@code limite} itens.
     *
     * @param ordenadas títulos já ordenados do mais para o menos recomendado
     * @param limite quantidade máxima de itens
     * @return os primeiros itens, em uma lista somente leitura
     */
    static List<Midia> limitar(List<Midia> ordenadas, int limite) {
        return List.copyOf(ordenadas.subList(0, Math.min(limite, ordenadas.size())));
    }
}
