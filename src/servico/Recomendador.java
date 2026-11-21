package servico;

import java.util.List;
import java.util.Objects;
import modelo.Midia;
import modelo.Usuario;

/**
 * Contexto do padrão Strategy: guarda a estratégia de recomendação em uso e delega o cálculo a ela.
 * A estratégia pode ser trocada a qualquer momento com {@link #setEstrategia}.
 */
public class Recomendador {
    /** Estratégia de recomendação em uso. */
    private RecomendacaoStrategy estrategia;

    /**
     * Cria o recomendador com uma estratégia inicial.
     *
     * @param estrategia o critério de recomendação (não pode ser nulo)
     * @throws NullPointerException se a estratégia for nula
     */
    public Recomendador(RecomendacaoStrategy estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "A estrategia nao pode ser nula.");
    }

    /**
     * Troca o critério de recomendação em tempo de execução.
     *
     * @param estrategia o novo critério (não pode ser nulo)
     * @throws NullPointerException se a estratégia for nula
     */
    public final void setEstrategia(RecomendacaoStrategy estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "A estrategia nao pode ser nula.");
    }

    /**
     * Informa o critério em uso.
     *
     * @return a estratégia atual
     */
    public RecomendacaoStrategy getEstrategia() {
        return estrategia;
    }

    /**
     * Recomenda até {@code limite} títulos do catálogo usando a estratégia atual.
     *
     * @param usuario usuário que receberá as recomendações
     * @param catalogo catálogo de onde saem as recomendações (não pode ser nulo)
     * @param limite quantidade máxima de títulos (deve ser positiva)
     * @return os títulos recomendados, do mais para o menos recomendado
     * @throws IllegalArgumentException se o limite não for positivo
     */
    public List<Midia> recomendar(Usuario usuario, Catalogo catalogo, int limite) {
        Objects.requireNonNull(catalogo, "O catalogo nao pode ser nulo.");
        return estrategia.recomendar(usuario, catalogo.listarTodos(), limite);
    }
}
