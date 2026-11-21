package test;

import excecoes.DadosInvalidosException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import persistencia.CatalogoJsonRepositorio;

public class TesteCatalogoJson {

    interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        testaArquivoAusente();
        testaArquivoEmBranco();
        testaIdaEVolta();
        testaSalvarMesmoTituloSubstitui();
        testaNotasNaoSaoGravadas();
        testaAcentosEAspas();
        testaBuscarPorTitulo();
        testaCriaPastaQueNaoExiste();
        testaJsonMalformado();
        testaRaizNaoEhLista();
        testaItemNaoEhObjeto();
        testaTituloDuplicadoNoArquivo();
        testaItemInvalidoInformaPosicao();
        testaSalvarNaoSobrescreveArquivoInvalido();
        testaArquivoDeExemplo();
        
        System.out.println("✅ TesteCatalogoJson - todos os testes passaram.");
    }

    // ---------- auxiliares ----------

    static Path novoCaminho() throws IOException {
        return Files.createTempDirectory("cinetech-teste").resolve("catalogo.json");
    }

    static void apagar(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(arquivo.getParent());
        } catch (IOException e) {
            // arquivo temporário: o sistema operacional limpa depois
        }
    }

    /** Devolve a mensagem da DadosInvalidosException lançada pela ação, ou null se nada foi lançado. */
    static String mensagemDeErro(Acao acao) throws Exception {
        try {
            acao.executar();
            return null;
        } catch (DadosInvalidosException e) {
            return e.getMessage();
        }
    }

    static Filme matrix() {
        return new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski");
    }

    // ---------- testes ----------

    static void testaArquivoAusente() throws Exception {
        Path p = novoCaminho();
        
        assert new CatalogoJsonRepositorio(p).listarTodos().isEmpty() : "Arquivo ausente: catalogo vazio";
        assert Files.notExists(p) : "Ler nao deve criar o arquivo";
        
        apagar(p);
    }

    static void testaArquivoEmBranco() throws Exception {
        Path p = novoCaminho();
        
        Files.writeString(p, "  \n ");
        
        assert new CatalogoJsonRepositorio(p).listarTodos().isEmpty() : "Arquivo em branco: catalogo vazio";
        
        apagar(p);
    }

    static void testaIdaEVolta() throws Exception {
        Path p = novoCaminho();
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(p);
        
        repo.salvar(matrix());
        repo.salvar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        repo.salvar(new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));

        List<Midia> lidas = new CatalogoJsonRepositorio(p).listarTodos(); // outra instancia: le do arquivo
        
        assert lidas.size() == 3 : "Deveria ler 3 titulos, leu " + lidas.size();
        assert lidas.get(0).getTitulo().equals("Matrix") && lidas.get(1).getTitulo().equals("Dark")
            && lidas.get(2).getTitulo().equals("Free Solo") : "A ordem deveria ser preservada";

        Filme f = (Filme) lidas.get(0);
        assert f.getDiretor().equals("Wachowski") && f.getDuracaoTotalMinutos() == 136 && f.getAnoLancamento() == 1999;
        
        Serie s = (Serie) lidas.get(1);
        assert s.getTemporadas() == 3 && s.getEpisodiosPorTemporada() == 8 && s.getDuracaoMediaEpisodio() == 50;
        
        Documentario d = (Documentario) lidas.get(2);
        assert d.getTema().equals("Escalada") && d.getGenero().equals("Aventura");
        
        apagar(p);
    }

    static void testaSalvarMesmoTituloSubstitui() throws Exception {
        Path p = novoCaminho();
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(p);
        
        repo.salvar(matrix());
        repo.salvar(new Filme("MATRIX", "Acao", 2003, 138, "Outro"));
        
        List<Midia> lidas = repo.listarTodos();
        
        assert lidas.size() == 1 : "Salvar titulo repetido nao deve duplicar";
        assert lidas.get(0).getGenero().equals("Acao") && lidas.get(0).getAnoLancamento() == 2003 : "Deveria substituir";
        
        apagar(p);
    }

    static void testaNotasNaoSaoGravadas() throws Exception {
        Path p = novoCaminho();
        
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(p);
        Filme m = matrix();
        
        m.avaliar(9);
        repo.salvar(m);
        Midia lida = repo.listarTodos().get(0);
        
        assert lida.getQuantidadeAvaliacoes() == 0 && lida.getNotaMedia() == 0.0
            : "As notas ficam nos usuarios, nao no catalogo";
        
        apagar(p);
    }

    static void testaAcentosEAspas() throws Exception {
        Path p = novoCaminho();
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(p);
        String acentuado = "A\u00e7\u00e3o Cl\u00e1ssica";
        
        repo.salvar(new Filme(acentuado, "Drama", 1990, 120, "Jo\u00e3o"));
        repo.salvar(new Filme("Schindler's List", "Drama", 1993, 195, "Spielberg"));
        
        List<Midia> lidas = new CatalogoJsonRepositorio(p).listarTodos();
        
        assert lidas.get(0).getTitulo().equals(acentuado) : "Acentos deveriam sobreviver (UTF-8)";
        assert lidas.get(1).getTitulo().equals("Schindler's List") : "Aspas simples deveriam sobreviver";
        assert Files.readString(p).contains("Schindler's List") : "O arquivo deveria ficar legivel, sem \\u0027";
        
        apagar(p);
    }

    static void testaBuscarPorTitulo() throws Exception {
        Path p = novoCaminho();
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(p);
        repo.salvar(matrix());
        
        assert repo.buscarPorTitulo("matrix").isPresent() : "Busca nao diferencia maiusculas";
        assert repo.buscarPorTitulo("  Matrix ").isPresent() : "Busca ignora espacos nas pontas";
        assert repo.buscarPorTitulo("Inexistente").isEmpty();
        assert repo.buscarPorTitulo(null).isEmpty();
        
        apagar(p);
    }

    static void testaCriaPastaQueNaoExiste() throws Exception {
        Path base = Files.createTempDirectory("cinetech-teste");
        Path p = base.resolve("dados").resolve("catalogo.json");
        new CatalogoJsonRepositorio(p).salvar(matrix());
        
        assert Files.exists(p) : "A pasta e o arquivo deveriam ser criados";
        
        Files.deleteIfExists(p);
        Files.deleteIfExists(p.getParent());
        Files.deleteIfExists(base);
    }

    static void testaJsonMalformado() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "[ {\"tipo\": ");
        String msg = mensagemDeErro(() -> new CatalogoJsonRepositorio(p).listarTodos());
        
        assert msg != null && msg.contains("malformado") : "JSON malformado deveria lancar DadosInvalidosException";
        
        apagar(p);
    }

    static void testaRaizNaoEhLista() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "{\"tipo\":\"Filme\"}");
        
        assert mensagemDeErro(() -> new CatalogoJsonRepositorio(p).listarTodos()) != null
            : "Um objeto na raiz deveria ser rejeitado";
        
        apagar(p);
    }

    static void testaItemNaoEhObjeto() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "[1, 2]");
        
        assert mensagemDeErro(() -> new CatalogoJsonRepositorio(p).listarTodos()) != null
            : "Itens que nao sao objetos deveriam ser rejeitados";
        
        apagar(p);
    }

    static void testaTituloDuplicadoNoArquivo() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "["
            + "{\"tipo\":\"Filme\",\"titulo\":\"Matrix\",\"genero\":\"A\",\"ano\":1999,\"duracaoMinutos\":100,\"diretor\":\"D\"},"
            + "{\"tipo\":\"Filme\",\"titulo\":\"matrix\",\"genero\":\"B\",\"ano\":2003,\"duracaoMinutos\":100,\"diretor\":\"D\"}]");
        String msg = mensagemDeErro(() -> new CatalogoJsonRepositorio(p).listarTodos());
        
        assert msg != null && msg.contains("duplicado") : "Titulo duplicado no arquivo deveria ser rejeitado";
        
        apagar(p);
    }

    static void testaItemInvalidoInformaPosicao() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "["
            + "{\"tipo\":\"Filme\",\"titulo\":\"Matrix\",\"genero\":\"A\",\"ano\":1999,\"duracaoMinutos\":100,\"diretor\":\"D\"},"
            + "{\"tipo\":\"Filme\",\"titulo\":\"Quebrado\",\"genero\":\"A\",\"ano\":1999,\"diretor\":\"D\"}]");
        String msg = mensagemDeErro(() -> new CatalogoJsonRepositorio(p).listarTodos());
        
        assert msg != null && msg.contains("Item 2") && msg.contains("duracaoMinutos")
            : "A mensagem deveria dizer qual item e qual campo estao errados: " + msg;
        
        apagar(p);
    }

    static void testaSalvarNaoSobrescreveArquivoInvalido() throws Exception {
        Path p = novoCaminho();
        String conteudoRuim = "[ isto nao e json";
        Files.writeString(p, conteudoRuim);
        String msg = mensagemDeErro(() -> new CatalogoJsonRepositorio(p).salvar(matrix()));
        
        assert msg != null : "Salvar sobre arquivo invalido deveria lancar excecao";
        assert Files.readString(p).equals(conteudoRuim) : "O arquivo invalido nao deve ser sobrescrito";
        
        apagar(p);
    }

    static void testaArquivoDeExemplo() throws Exception {
        Path exemplo = Path.of("dados", "catalogo.json");
        
        if (Files.notExists(exemplo)) {
            System.out.println("(aviso) dados/catalogo.json nao encontrado: execute a partir da raiz do projeto.");
            return;
        }
        
        List<Midia> lidas = new CatalogoJsonRepositorio(exemplo).listarTodos();
        
        assert lidas.size() == 100 : "O catalogo de exemplo deveria ter 100 titulos, tem " + lidas.size();
        
        Midia dark = lidas.stream().filter(m -> m.getTitulo().equals("Dark")).findFirst().orElseThrow();
        
        assert dark instanceof Serie && ((Serie) dark).getDuracaoTotalMinutos() == 1200;
    }
}