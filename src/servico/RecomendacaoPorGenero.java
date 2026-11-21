package servico;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import modelo.Midia;
import modelo.Usuario;

/**
 * Recomenda pelo gosto do próprio usuário: descobre os gêneros em que as notas que ele deu têm média
 * de pelo menos {@value #NOTA_MINIMA_GOSTO} e sugere os títulos desses gêneros que ele ainda não viu.
 * Os gêneros preferidos vêm primeiro; dentro do mesmo gênero, a melhor nota média primeiro.
 * Sem avaliações altas, não há o que recomendar e a lista é vazia.
 */
public class RecomendacaoPorGenero implements RecomendacaoStrategy {

    /** Média mínima das notas do usuário em um gênero para considerá-lo um gênero de que ele gosta. */
    public static final double NOTA_MINIMA_GOSTO = 7.0;

    /** Cria a estratégia, que não guarda nenhum estado. */
    public RecomendacaoPorGenero() {
    }

    @Override
    public String getNome() {
        return "Pelo seu gosto";
    }

    @Override
    public String getDescricao() {
        return "Títulos dos gêneros em que suas notas têm média " + (int) NOTA_MINIMA_GOSTO
            + " ou mais. Avalie alguns títulos para receber sugestões.";
    }

    @Override
    public List<Midia> recomendar(Usuario usuario, List<Midia> catalogo, int limite) {
        Objects.requireNonNull(usuario, "O usuario nao pode ser nulo.");
        Objects.requireNonNull(catalogo, "O catalogo nao pode ser nulo.");
        RecomendacaoStrategy.validarLimite(limite);

        Map<String, Double> afinidade = afinidadePorGenero(usuario, catalogo);
        if (afinidade.isEmpty()) {
            return List.of();
        }

        List<Midia> candidatas = RecomendacaoStrategy.naoVistas(usuario, catalogo);
        candidatas.removeIf(m -> !afinidade.containsKey(chave(m.getGenero())));
        candidatas.sort(Comparator.comparingDouble((Midia m) -> afinidade.get(chave(m.getGenero()))).reversed()
            .thenComparing(Comparator.comparingDouble(Midia::getNotaMedia).reversed())
            .thenComparing(Midia::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return RecomendacaoStrategy.limitar(candidatas, limite);
    }

    /**
     * Calcula a média das notas do usuário em cada gênero, mantendo só os gêneros de que ele gosta.
     *
     * @param usuario o usuário, de quem vêm as notas
     * @param catalogo os títulos, que informam o gênero de cada nota
     * @return a média das notas, por gênero (em minúsculas), só para médias de pelo menos {@link #NOTA_MINIMA_GOSTO}
     */
    private static Map<String, Double> afinidadePorGenero(Usuario usuario, List<Midia> catalogo) {
        Map<String, double[]> acumulado = new LinkedHashMap<>(); // gênero -> {soma, quantidade}
        for (Midia midia : catalogo) {
            Double nota = usuario.getAvaliacoes().get(midia.getTitulo());
            if (nota != null) {
                double[] a = acumulado.computeIfAbsent(chave(midia.getGenero()), g -> new double[2]);
                a[0] += nota;
                a[1]++;
            }
        }
        Map<String, Double> afinidade = new HashMap<>();
        for (Map.Entry<String, double[]> e : acumulado.entrySet()) {
            double media = e.getValue()[0] / e.getValue()[1];
            if (media >= NOTA_MINIMA_GOSTO) {
                afinidade.put(e.getKey(), media);
            }
        }
        return afinidade;
    }

    /**
     * Normaliza o gênero para comparação: sem espaços nas pontas e em minúsculas.
     *
     * @param genero o gênero original
     * @return a chave usada para agrupar gêneros
     */
    private static String chave(String genero) {
        return genero.trim().toLowerCase();
    }
}
