package test;

import excecoes.TituloDuplicadoException;
import excecoes.UsernameJaExisteException;
import modelo.Documentario;
import modelo.Midia;
import modelo.Filme;
import modelo.Serie;
import modelo.Usuario;
import java.io.IOException;
import servico.Catalogo;
import servico.GerenciadorUsuarios;

public class TesteServicos {

    public static void main(String[] args) throws Exception {
        testaUsernameDuplicado();
        testaMesmoNomeUsernamesDiferentes();
        testaEntrar();
        testaTituloDuplicado();
        testaBuscaEFiltro();
        testaCatalogoCarregaDoRepositorio();
        testaCatalogoPersisteCadastro();
        testaCatalogoNaoMudaSeGravacaoFalha();
        testaUsuariosCarregamDoRepositorio();
        testaUsuariosPersistemCadastroEMudancas();
        testaUsuariosNaoMudamSeGravacaoFalha();
        testaSalvarUsuarioDesconhecido();
        testaAvaliacaoDepoisDeReiniciar();
        System.out.println("✅ TesteServicos - todos os testes passaram.");
    }

    static void testaUsernameDuplicado() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios(new RepositoriosEmMemoria.Usuarios());
        g.cadastrar("76ope", "Joackym");
        boolean lancou = false;
        
        try {
            g.cadastrar("76OPE", "Outra Pessoa");
        } catch (UsernameJaExisteException e) {
            lancou = true;
        }
        
        assert lancou : "Username repetido (mesmo com caixa diferente) deveria lancar excecao";
        assert g.listarTodos().size() == 1;
    }

    static void testaMesmoNomeUsernamesDiferentes() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios(new RepositoriosEmMemoria.Usuarios());
        
        g.cadastrar("76ope", "Joackym");
        g.cadastrar("joackym00", "Joackym");
        
        assert g.listarTodos().size() == 2 : "Nome repetido deve ser aceito";
    }

    static void testaEntrar() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios(new RepositoriosEmMemoria.Usuarios());
        
        g.cadastrar("76ope", "Joackym");
        
        Usuario u = g.entrar("76OPE").orElseThrow();
        
        assert u.getNome().equals("Joackym");
        assert g.entrar("naoexiste").isEmpty();
    }

    static void testaTituloDuplicado() throws Exception {
        Catalogo c = new Catalogo(new RepositoriosEmMemoria.Catalogo());
        
        c.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        
        boolean lancou = false;
        
        try {
            c.cadastrar(new Filme("MATRIX", "Ficcao", 2003, 138, "Wachowski"));
        } catch (TituloDuplicadoException e) {
            lancou = true;
        }
        
        assert lancou : "Titulo repetido (sem diferenciar caixa) deveria lancar excecao";
        assert c.listarTodos().size() == 1;
    }

    static void testaBuscaEFiltro() throws Exception {
        Catalogo c = new Catalogo(new RepositoriosEmMemoria.Catalogo());
        
        c.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        c.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        c.cadastrar(new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));
        
        assert c.buscarPorTitulo("dark").isPresent();
        assert c.buscarPorTitulo("inexistente").isEmpty();
        assert c.filtrarPorGenero("ficcao").size() == 1;
        assert c.filtrarPorGenero("terror").isEmpty();
    }


    static void testaCatalogoCarregaDoRepositorio() throws Exception {
        RepositoriosEmMemoria.Catalogo repo = new RepositoriosEmMemoria.Catalogo();
        repo.salvar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));

        Catalogo c = new Catalogo(repo);

        assert c.listarTodos().size() == 1 : "Deveria carregar os titulos ja persistidos";
        assert c.buscarPorTitulo("MATRIX").isPresent();
    }

    static void testaCatalogoPersisteCadastro() throws Exception {
        RepositoriosEmMemoria.Catalogo repo = new RepositoriosEmMemoria.Catalogo();
        Catalogo c = new Catalogo(repo);
        int antes = repo.chamadasSalvar;

        c.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));

        assert repo.chamadasSalvar == antes + 1 : "Cadastrar deveria gravar no repositorio";
        assert new Catalogo(repo).buscarPorTitulo("dark").isPresent() : "Novo catalogo deveria enxergar o titulo";
    }

    static void testaCatalogoNaoMudaSeGravacaoFalha() throws Exception {
        RepositoriosEmMemoria.Catalogo repo = new RepositoriosEmMemoria.Catalogo();
        Catalogo c = new Catalogo(repo);
        repo.falharAoSalvar = true;

        boolean lancou = false;
        try {
            c.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        } catch (IOException e) {
            lancou = true;
        }

        assert lancou;
        assert c.listarTodos().isEmpty() : "Falha ao gravar nao pode deixar o titulo so em memoria";
    }

    static void testaUsuariosCarregamDoRepositorio() throws Exception {
        RepositoriosEmMemoria.Usuarios repo = new RepositoriosEmMemoria.Usuarios();
        repo.salvar(new Usuario("76ope", "Joackym"));

        GerenciadorUsuarios g = new GerenciadorUsuarios(repo);

        assert g.listarTodos().size() == 1;
        assert g.entrar("76OPE").isPresent();
    }

    static void testaUsuariosPersistemCadastroEMudancas() throws Exception {
        RepositoriosEmMemoria.Usuarios repo = new RepositoriosEmMemoria.Usuarios();
        GerenciadorUsuarios g = new GerenciadorUsuarios(repo);
        Usuario u = g.cadastrar("ana01", "Ana");
        assert repo.guardados.containsKey("ana01") : "Cadastrar deveria gravar no repositorio";

        int antes = repo.chamadasSalvar;
        u.adicionarNaLista(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        g.salvar(u);

        assert repo.chamadasSalvar == antes + 1;
        assert repo.guardados.get("ana01").getMinhaLista().contains("Matrix");
    }

    static void testaUsuariosNaoMudamSeGravacaoFalha() throws Exception {
        RepositoriosEmMemoria.Usuarios repo = new RepositoriosEmMemoria.Usuarios();
        GerenciadorUsuarios g = new GerenciadorUsuarios(repo);
        repo.falharAoSalvar = true;

        boolean lancou = false;
        try {
            g.cadastrar("ana01", "Ana");
        } catch (IOException e) {
            lancou = true;
        }

        assert lancou;
        assert g.listarTodos().isEmpty() : "Falha ao gravar nao pode deixar o usuario so em memoria";
    }

    static void testaSalvarUsuarioDesconhecido() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios(new RepositoriosEmMemoria.Usuarios());
        boolean lancou = false;
        try {
            g.salvar(new Usuario("ana01", "Ana"));
        } catch (IllegalArgumentException e) {
            lancou = true;
        }
        assert lancou : "Salvar usuario que nao foi cadastrado no gerenciador deveria falhar";
    }

    // simula fechar e abrir o sistema: as notas voltam para as midias e reavaliar continua valendo
    static void testaAvaliacaoDepoisDeReiniciar() throws Exception {
        RepositoriosEmMemoria.Catalogo repoC = new RepositoriosEmMemoria.Catalogo();
        RepositoriosEmMemoria.Usuarios repoU = new RepositoriosEmMemoria.Usuarios();

        Catalogo c1 = new Catalogo(repoC);
        GerenciadorUsuarios g1 = new GerenciadorUsuarios(repoU);
        c1.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        Usuario ana1 = g1.cadastrar("ana01", "Ana");
        Usuario beto1 = g1.cadastrar("beto02", "Beto");
        ana1.avaliar(c1.buscarPorTitulo("Matrix").orElseThrow(), 10);
        beto1.avaliar(c1.buscarPorTitulo("Matrix").orElseThrow(), 6);
        g1.salvar(ana1);
        g1.salvar(beto1);

        // "reinicio": objetos novos, so os repositorios sobrevivem (e as notas das midias se perdem)
        RepositoriosEmMemoria.Catalogo repoC2 = new RepositoriosEmMemoria.Catalogo();
        for (Midia m : repoC.listarTodos()) {
            repoC2.salvar(rebuild(m));
        }
        Catalogo c2 = new Catalogo(repoC2);
        GerenciadorUsuarios g2 = new GerenciadorUsuarios(repoU);
        Midia matrix = c2.buscarPorTitulo("Matrix").orElseThrow();
        assert matrix.getQuantidadeAvaliacoes() == 0;

        c2.aplicarAvaliacoes(g2.listarTodos());
        assert matrix.getQuantidadeAvaliacoes() == 2 && matrix.getNotaMedia() == 8.0;

        g2.entrar("beto02").orElseThrow().avaliar(matrix, 8);
        assert matrix.getQuantidadeAvaliacoes() == 2 && matrix.getNotaMedia() == 9.0
            : "Reavaliar depois de reiniciar deve substituir a nota, nao somar";
    }

    private static Midia rebuild(Midia m) {
        Filme f = (Filme) m;
        return new Filme(f.getTitulo(), f.getGenero(), f.getAnoLancamento(), f.getDuracaoTotalMinutos(), f.getDiretor());
    }
}
