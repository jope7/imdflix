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
    private RecomendacaoStrategy estrategia;

    public Recomendador(RecomendacaoStrategy estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "A estrategia nao pode ser nula.");
    }

    public final void setEstrategia(RecomendacaoStrategy estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "A estrategia nao pode ser nula.");
    }

    public RecomendacaoStrategy getEstrategia() {
        return estrategia;
    }

    /** Recomenda até {@code limite} títulos do catálogo usando a estratégia atual. */
    public List<Midia> recomendar(Usuario usuario, Catalogo catalogo, int limite) {
        Objects.requireNonNull(catalogo, "O catalogo nao pode ser nulo.");
        return estrategia.recomendar(usuario, catalogo.listarTodos(), limite);
    }
}
