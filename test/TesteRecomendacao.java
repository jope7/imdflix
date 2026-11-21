package test;

import excecoes.NotaInvalidaException;
import java.util.ArrayList;
import java.util.List;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import modelo.Usuario;
import servico.Catalogo;
import servico.RecomendacaoPorGenero;
import servico.RecomendacaoPorNota;
import servico.RecomendacaoStrategy;
import servico.Recomendador;

/** Testes com assert puro. Execute com a flag -ea. */
public class TesteRecomendacao {

    public static void main(String[] args) throws Exception {
        testaPorNotaOrdemEEmpates();
        testaPorNotaIgnoraTitulosSemAvaliacao();
        testaPorNotaExcluiVistos();
        testaPorNotaRespeitaLimite();
        testaPorGeneroUmGenero();
        testaPorGeneroVariosGeneros();
        testaPorGeneroSemHistoricoOuSemGosto();
        testaPorGeneroLimiarDeGosto();
        testaPorGeneroNaoDiferenciaCaixa();
        testaCatalogoVazioELimiteInvalido();
        testaListaDevolvidaEhSomenteLeitura();
        testaPolimorfismo();
        testaRecomendadorTrocaDeEstrategia();
        System.out.println("TesteRecomendacao: todos os testes passaram.");
    }

    // ---------- auxiliares ----------

    static Midia filme(String titulo, String genero) {
        return new Filme(titulo, genero, 2000, 100, "Diretor");
    }

    static Midia buscar(List<Midia> catalogo, String titulo) {
        return catalogo.stream().filter(m -> m.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    static List<String> titulos(List<Midia> midias) {
        List<String> nomes = new ArrayList<>();
        for (Midia m : midias) {
            nomes.add(m.getTitulo());
        }
        return nomes;
    }

    static void avaliacoes(List<Midia> catalogo, String username, Object... tituloENota) throws NotaInvalidaException {
        Usuario u = new Usuario(username, username);
        for (int i = 0; i < tituloENota.length; i += 2) {
            u.avaliar(buscar(catalogo, (String) tituloENota[i]), ((Number) tituloENota[i + 1]).doubleValue());
        }
    }

    /**
     * Catálogo de teste, com notas dadas por outros dois usuários. Médias resultantes:
     * Cosmos 10,0 (2 notas); Matrix 8,0 (2); Dark 8,0 (2); Blade Runner 8,0 (1);
     * Interestelar 6,0 (2); Planeta Terra e Free Solo sem nenhuma nota.
     */
    static List<Midia> catalogo() throws NotaInvalidaException {
        List<Midia> c = new ArrayList<>(List.of(
            filme("Matrix", "Ficcao"),
            filme("Interestelar", "Ficcao"),
            filme("Blade Runner", "Ficcao"),
            new Serie("Dark", "Suspense", 2017, 3, 8, 50),
            new Serie("Cosmos", "Ciencia", 2014, 1, 13, 45),
            new Serie("Planeta Terra", "Ciencia", 2006, 1, 11, 50),
            new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada")));
        avaliacoes(c, "ana01", "Matrix", 9, "Interestelar", 6, "Dark", 8, "Cosmos", 10, "Blade Runner", 8);
        avaliacoes(c, "beto02", "Matrix", 7, "Interestelar", 6, "Dark", 8, "Cosmos", 10);
        return c;
    }

    // ---------- testes ----------

    static void testaPorNotaOrdemEEmpates() throws Exception {
        List<Midia> c = catalogo();
        List<Midia> r = new RecomendacaoPorNota().recomendar(new Usuario("novo01", "Novo"), c, 10);
        // Cosmos (10); empate em 8,0: Dark e Matrix (2 notas, ordem alfabetica) antes de Blade Runner (1 nota); Interestelar (6)
        assert titulos(r).equals(List.of("Cosmos", "Dark", "Matrix", "Blade Runner", "Interestelar"))
            : "Ordem inesperada: " + titulos(r);
    }

    static void testaPorNotaIgnoraTitulosSemAvaliacao() throws Exception {
        List<String> r = titulos(new RecomendacaoPorNota().recomendar(new Usuario("novo01", "Novo"), catalogo(), 10));
        assert !r.contains("Planeta Terra") && !r.contains("Free Solo") : "Titulos sem nota nao devem ser recomendados";
    }

    static void testaPorNotaExcluiVistos() throws Exception {
        List<Midia> c = catalogo();
        Usuario joao = new Usuario("joao01", "Joao");
        joao.avaliar(buscar(c, "Cosmos"), 5);          // ja avaliou
        joao.adicionarNaLista(buscar(c, "Matrix"));    // ja esta na lista
        List<String> r = titulos(new RecomendacaoPorNota().recomendar(joao, c, 10));
        assert r.equals(List.of("Dark", "Blade Runner", "Interestelar")) : "Vistos deveriam sair: " + r;
    }

    static void testaPorNotaRespeitaLimite() throws Exception {
        List<String> r = titulos(new RecomendacaoPorNota().recomendar(new Usuario("novo01", "Novo"), catalogo(), 3));
        assert r.equals(List.of("Cosmos", "Dark", "Matrix")) : "Limite 3 deveria cortar a lista: " + r;
    }

    static void testaPorGeneroUmGenero() throws Exception {
        List<Midia> c = catalogo();
        Usuario u = new Usuario("joao01", "Joao");
        u.avaliar(buscar(c, "Matrix"), 9);   // gosta de Ficcao
        u.avaliar(buscar(c, "Dark"), 3);     // nao gosta de Suspense
        List<String> r = titulos(new RecomendacaoPorGenero().recomendar(u, c, 10));
        // so Ficcao; dentro do genero, melhor nota media primeiro (Blade Runner 8,0 antes de Interestelar 6,0)
        assert r.equals(List.of("Blade Runner", "Interestelar")) : "Recomendacao por genero inesperada: " + r;
    }

    static void testaPorGeneroVariosGeneros() throws Exception {
        List<Midia> c = catalogo();
        Usuario u = new Usuario("maria01", "Maria");
        u.avaliar(buscar(c, "Matrix"), 10);  // Ficcao: 10
        u.avaliar(buscar(c, "Cosmos"), 8);   // Ciencia: 8
        List<String> r = titulos(new RecomendacaoPorGenero().recomendar(u, c, 10));
        // genero preferido primeiro (Ficcao), depois Ciencia (Planeta Terra, mesmo sem nenhuma nota de outros)
        assert r.equals(List.of("Blade Runner", "Interestelar", "Planeta Terra")) : "Ordem entre generos: " + r;
    }

    static void testaPorGeneroSemHistoricoOuSemGosto() throws Exception {
        List<Midia> c = catalogo();
        assert new RecomendacaoPorGenero().recomendar(new Usuario("novo01", "Novo"), c, 10).isEmpty()
            : "Sem historico, nao ha recomendacao por genero";
        Usuario critico = new Usuario("critico", "Critico");
        critico.avaliar(buscar(c, "Matrix"), 2);
        assert new RecomendacaoPorGenero().recomendar(critico, c, 10).isEmpty()
            : "Quem so deu notas baixas nao tem genero preferido";
    }

    static void testaPorGeneroLimiarDeGosto() throws Exception {
        List<Midia> c = List.of(filme("A", "Drama"), filme("B", "Drama"), filme("C", "Comedia"), filme("D", "Comedia"));
        Usuario u = new Usuario("joao01", "Joao");
        u.avaliar(buscar(c, "A"), RecomendacaoPorGenero.NOTA_MINIMA_GOSTO);        // exatamente no limiar: gosta
        u.avaliar(buscar(c, "C"), RecomendacaoPorGenero.NOTA_MINIMA_GOSTO - 0.1);  // abaixo: nao gosta
        assert titulos(new RecomendacaoPorGenero().recomendar(u, c, 10)).equals(List.of("B"))
            : "A nota no limiar conta como gosto; abaixo dele, nao";
    }

    static void testaPorGeneroNaoDiferenciaCaixa() throws Exception {
        List<Midia> c = List.of(filme("X", "Ficcao"), filme("Y", "ficcao"));
        Usuario u = new Usuario("joao01", "Joao");
        u.avaliar(buscar(c, "X"), 9);
        assert titulos(new RecomendacaoPorGenero().recomendar(u, c, 10)).equals(List.of("Y"))
            : "O genero nao deveria diferenciar maiusculas";
    }

    static void testaCatalogoVazioELimiteInvalido() throws Exception {
        Usuario u = new Usuario("novo01", "Novo");
        for (RecomendacaoStrategy e : List.of(new RecomendacaoPorNota(), new RecomendacaoPorGenero())) {
            assert e.recomendar(u, List.of(), 5).isEmpty() : e.getNome() + ": catalogo vazio da lista vazia";

            for (int limiteInvalido : new int[] {0, -1}) {
                boolean lancou = false;
                try {
                    e.recomendar(u, catalogo(), limiteInvalido);
                } catch (IllegalArgumentException ex) {
                    lancou = true;
                }
                assert lancou : e.getNome() + ": limite " + limiteInvalido + " deveria ser rejeitado";
            }

            boolean nulo = false;
            try {
                e.recomendar(null, List.of(), 5);
            } catch (NullPointerException ex) {
                nulo = true;
            }
            assert nulo : e.getNome() + ": usuario nulo deveria ser rejeitado";
        }
    }

    static void testaListaDevolvidaEhSomenteLeitura() throws Exception {
        List<Midia> r = new RecomendacaoPorNota().recomendar(new Usuario("novo01", "Novo"), catalogo(), 3);
        boolean lancou = false;
        try {
            r.add(filme("Intruso", "Drama"));
        } catch (UnsupportedOperationException e) {
            lancou = true;
        }
        assert lancou : "A lista devolvida deveria ser somente leitura";
    }

    static void testaPolimorfismo() throws Exception {
        List<RecomendacaoStrategy> todas = List.of(new RecomendacaoPorNota(), new RecomendacaoPorGenero());
        List<String> nomes = new ArrayList<>();
        for (RecomendacaoStrategy e : todas) {
            assert !e.getNome().isBlank() && !e.getDescricao().isBlank() : "Nome e descricao devem existir";
            nomes.add(e.getNome());
            e.recomendar(new Usuario("novo01", "Novo"), catalogo(), 3); // nao lanca
        }
        assert nomes.size() == 2 && !nomes.get(0).equals(nomes.get(1)) : "Os nomes devem ser diferentes";
    }

    static void testaRecomendadorTrocaDeEstrategia() throws Exception {
        Catalogo catalogo = new Catalogo(new RepositoriosEmMemoria.Catalogo());
        for (Midia m : catalogo()) {
            catalogo.cadastrar(m);
        }
        Usuario novo = new Usuario("novo01", "Novo");

        Recomendador recomendador = new Recomendador(new RecomendacaoPorNota());
        assert recomendador.getEstrategia() instanceof RecomendacaoPorNota;
        assert titulos(recomendador.recomendar(novo, catalogo, 3)).equals(List.of("Cosmos", "Dark", "Matrix"));

        recomendador.setEstrategia(new RecomendacaoPorGenero()); // troca em tempo de execucao
        assert recomendador.getEstrategia() instanceof RecomendacaoPorGenero;
        assert recomendador.recomendar(novo, catalogo, 3).isEmpty() : "A nova estrategia deveria dar outro resultado";

        boolean construtor = false;
        try {
            new Recomendador(null);
        } catch (NullPointerException e) {
            construtor = true;
        }
        boolean setter = false;
        try {
            recomendador.setEstrategia(null);
        } catch (NullPointerException e) {
            setter = true;
        }
        assert construtor && setter : "Estrategia nula deveria ser rejeitada";
    }
}
