package servico;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import modelo.Midia;
import modelo.Usuario;

/**
 * Recomenda os títulos com a melhor nota média entre todos os usuários.
 * Só entram títulos que já têm pelo menos uma avaliação. Empates: mais avaliações primeiro e, se
 * ainda empatar, ordem alfabética do título.
 */
public class RecomendacaoPorNota implements RecomendacaoStrategy {

    @Override
    public String getNome() {
        return "Mais bem avaliados";
    }

    @Override
    public String getDescricao() {
        return "Títulos com as melhores notas médias entre todos os usuários, "
            + "que você ainda não avaliou nem adicionou à sua lista.";
    }

    @Override
    public List<Midia> recomendar(Usuario usuario, List<Midia> catalogo, int limite) {
        Objects.requireNonNull(usuario, "O usuario nao pode ser nulo.");
        Objects.requireNonNull(catalogo, "O catalogo nao pode ser nulo.");
        RecomendacaoStrategy.validarLimite(limite);

        List<Midia> candidatas = RecomendacaoStrategy.naoVistas(usuario, catalogo);
        candidatas.removeIf(m -> m.getQuantidadeAvaliacoes() == 0);
        candidatas.sort(Comparator.comparingDouble(Midia::getNotaMedia).reversed()
            .thenComparing(Comparator.comparingInt(Midia::getQuantidadeAvaliacoes).reversed())
            .thenComparing(Midia::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return RecomendacaoStrategy.limitar(candidatas, limite);
    }
}
