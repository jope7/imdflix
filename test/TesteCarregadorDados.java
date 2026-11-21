package test;

import excecoes.DadosInvalidosException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import modelo.Usuario;
import persistencia.CatalogoJsonRepositorio;
import servico.CarregadorDados;
import servico.CarregadorDados.DadosCarregados;

public class TesteCarregadorDados {

    interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        testaSemArquivos();
        testaFluxoCompletoRecalculaMedias();
        testaDuasCargasSaoIndependentes();
        testaTituloInexistenteEhIgnorado();
        testaSemCatalogoUsuariosSobrevivem();
        testaCatalogoInvalido();
        testaUsuariosInvalidos();
        testaCaminhosPadrao();
        testaArquivosDeExemplo();
        System.out.println("✅ TesteCarregadorDados - todos os testes passaram.");
    }

    // ---------- auxiliares ----------

    static Path novaPasta() throws IOException {
        return Files.createTempDirectory("cinetech-teste");
    }

    static void apagar(Path pasta) {
        try {
            Files.deleteIfExists(pasta.resolve("catalogo.json"));
            Files.deleteIfExists(pasta.resolve("usuarios.json"));
            Files.deleteIfExists(pasta);
        } catch (IOException e) {
            // arquivo temporário: o sistema operacional limpa depois
        }
    }

    static CarregadorDados carregadorEm(Path pasta) {
        return new CarregadorDados(pasta.resolve("catalogo.json"), pasta.resolve("usuarios.json"));
    }

    /** Grava um catálogo com Matrix (filme) e Dark (série), usando o próprio repositório. */
    static void gravarCatalogo(Path pasta) throws IOException {
        CatalogoJsonRepositorio repo = new CatalogoJsonRepositorio(pasta.resolve("catalogo.json"));
        
        repo.salvar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        repo.salvar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
    }

    static void gravarUsuarios(Path pasta, String json) throws IOException {
        Files.writeString(pasta.resolve("usuarios.json"), json.replace('\'', '"'));
    }

    static Midia midia(DadosCarregados dados, String titulo) {
        return dados.catalogo().buscarPorTitulo(titulo).orElseThrow();
    }

    static String mensagemDeErro(Acao acao) throws Exception {
        try {
            acao.executar();
            return null;
        } catch (DadosInvalidosException e) {
            return e.getMessage();
        }
    }

    // ---------- testes ----------

    static void testaSemArquivos() throws Exception {
        Path pasta = novaPasta();
        DadosCarregados dados = carregadorEm(pasta).carregar();
        
        assert dados.catalogo().listarTodos().isEmpty() : "Sem arquivos: catalogo vazio";
        assert dados.usuarioRepositorio().listarTodos().isEmpty() : "Sem arquivos: sem usuarios";
        assert Files.notExists(pasta.resolve("catalogo.json")) && Files.notExists(pasta.resolve("usuarios.json"))
            : "Carregar nao deve criar arquivos";
        
            apagar(pasta);
    }

    static void testaFluxoCompletoRecalculaMedias() throws Exception {
        Path pasta = novaPasta();
        
        gravarCatalogo(pasta);
        gravarUsuarios(pasta,
            "[{'username':'ana01','nome':'Ana','minhaLista':['Matrix'],'avaliacoes':{'Matrix':10,'Dark':8}},"
                + "{'username':'beto02','nome':'Beto','avaliacoes':{'Matrix':6}}]");

        DadosCarregados dados = carregadorEm(pasta).carregar();
        
        assert dados.catalogo().listarTodos().size() == 2;
        assert midia(dados, "Matrix").getQuantidadeAvaliacoes() == 2 && midia(dados, "Matrix").getNotaMedia() == 8.0
            : "A media do Matrix deveria ser 8.0 (prova que o catalogo foi carregado antes dos usuarios)";
        assert midia(dados, "Dark").getNotaMedia() == 8.0 && midia(dados, "Dark").getQuantidadeAvaliacoes() == 1;

        Usuario ana = dados.usuarioRepositorio().buscarPorUsername("ana01").orElseThrow();
        
        assert ana.getMinhaLista().equals(List.of("Matrix"));
        assert ana.getAvaliacoes().get("Matrix") == 10.0;
        assert dados.catalogoRepositorio().buscarPorTitulo("Dark").isPresent();
        
        apagar(pasta);
    }

    static void testaDuasCargasSaoIndependentes() throws Exception {
        Path pasta = novaPasta();
        
        gravarCatalogo(pasta);
        gravarUsuarios(pasta, "[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':9},"
            + "'minhaLista':[]},{'username':'beto02','nome':'Beto','avaliacoes':{'Matrix':7}}]");
        
            CarregadorDados carregador = carregadorEm(pasta);
        DadosCarregados a = carregador.carregar();
        DadosCarregados b = carregador.carregar();
        
        assert a.catalogo() != b.catalogo() : "Cada carga deveria montar um catalogo novo";
        assert midia(b, "Matrix").getQuantidadeAvaliacoes() == 2 && midia(b, "Matrix").getNotaMedia() == 8.0
            : "A segunda carga nao pode acumular notas da primeira";
        assert midia(a, "Matrix").getQuantidadeAvaliacoes() == 2 : "A primeira carga nao deve ser afetada";
        
        apagar(pasta);
    }

    static void testaTituloInexistenteEhIgnorado() throws Exception {
        Path pasta = novaPasta();
        
        gravarCatalogo(pasta);
        gravarUsuarios(pasta, "[{'username':'ana01','nome':'Ana','minhaLista':['Matrix','Fantasma'],"
            + "'avaliacoes':{'Matrix':8.0,'Fantasma':3.0}}]");
        
            DadosCarregados dados = carregadorEm(pasta).carregar();
        
        Usuario ana = dados.usuarioRepositorio().buscarPorUsername("ana01").orElseThrow();
        
        assert ana.getMinhaLista().equals(List.of("Matrix"));
        assert ana.getAvaliacoes().size() == 1;
        assert midia(dados, "Matrix").getQuantidadeAvaliacoes() == 1;
        
        apagar(pasta);
    }

    static void testaSemCatalogoUsuariosSobrevivem() throws Exception {
        Path pasta = novaPasta();
        
        gravarUsuarios(pasta, "[{'username':'ana01','nome':'Ana','minhaLista':['Matrix'],"
            + "'avaliacoes':{'Matrix':8.0}}]");
        
            DadosCarregados dados = carregadorEm(pasta).carregar();
        Usuario ana = dados.usuarioRepositorio().buscarPorUsername("ana01").orElseThrow();
        
        assert ana.getMinhaLista().isEmpty() && ana.getAvaliacoes().isEmpty()
            : "Sem catalogo, os titulos dos usuarios sao ignorados, mas o usuario continua existindo";
        
        apagar(pasta);
    }

    static void testaCatalogoInvalido() throws Exception {
        Path pasta = novaPasta();
        Files.writeString(pasta.resolve("catalogo.json"), "[ isto nao e json");
        gravarUsuarios(pasta, "[{'username':'ana01','nome':'Ana'}]");
        String msg = mensagemDeErro(() -> carregadorEm(pasta).carregar());
        
        assert msg != null && msg.contains("catalogo.json") : "Catalogo invalido deveria ser rejeitado: " + msg;
        
        apagar(pasta);
    }

    static void testaUsuariosInvalidos() throws Exception {
        Path pasta = novaPasta();
        
        gravarCatalogo(pasta);
        gravarUsuarios(pasta, "[{'username':'ab','nome':'Ana'}]");
        
        String msg = mensagemDeErro(() -> carregadorEm(pasta).carregar());
        
        assert msg != null && msg.contains("usuarios.json") && msg.contains("Item 1")
            : "Usuarios invalidos deveriam ser rejeitados com a posicao: " + msg;
        
        apagar(pasta);
    }

    static void testaCaminhosPadrao() {
        CarregadorDados padrao = CarregadorDados.padrao();
        
        assert padrao.getCaminhoCatalogo().equals(Path.of("dados", "catalogo.json"));
        assert padrao.getCaminhoUsuarios().equals(Path.of("dados", "usuarios.json"));
    }

    static void testaArquivosDeExemplo() throws Exception {
        CarregadorDados padrao = CarregadorDados.padrao();
        
        if (Files.notExists(padrao.getCaminhoCatalogo()) || Files.notExists(padrao.getCaminhoUsuarios())) {
            System.out.println("(aviso) arquivos de exemplo nao encontrados: execute a partir da raiz do projeto.");
            return;
        }
        
        DadosCarregados dados = padrao.carregar();
        
        assert dados.catalogo().listarTodos().size() == 100 : "O catalogo de exemplo deveria ter 100 titulos";
        assert dados.usuarioRepositorio().listarTodos().size() == 25 : "Deveria haver 25 usuarios de exemplo";
        assert midia(dados, "Matrix").getNotaMedia() == 8.8 && midia(dados, "Matrix").getQuantidadeAvaliacoes() == 5;
        assert midia(dados, "Dark").getNotaMedia() == 9.0;
        assert Math.abs(midia(dados, "Free Solo").getNotaMedia() - (27.5 / 3.0)) < 0.001;
    }
}
