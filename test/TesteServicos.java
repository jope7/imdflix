package test;

import excecoes.TituloDuplicadoException;
import excecoes.UsernameJaExisteException;
import modelo.Documentario;
import modelo.Filme;
import modelo.Serie;
import modelo.Usuario;
import servico.Catalogo;
import servico.GerenciadorUsuarios;

public class TesteServicos {

    public static void main(String[] args) throws Exception {
        testaUsernameDuplicado();
        testaMesmoNomeUsernamesDiferentes();
        testaEntrar();
        testaTituloDuplicado();
        testaBuscaEFiltro();
        System.out.println("todos os testes passaram.");
    }

    static void testaUsernameDuplicado() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios();
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
        GerenciadorUsuarios g = new GerenciadorUsuarios();
        
        g.cadastrar("76ope", "Joackym");
        g.cadastrar("joackym00", "Joackym");
        
        assert g.listarTodos().size() == 2 : "Nome repetido deve ser aceito";
    }

    static void testaEntrar() throws Exception {
        GerenciadorUsuarios g = new GerenciadorUsuarios();
        
        g.cadastrar("76ope", "Joackym");
        
        Usuario u = g.entrar("76OPE").orElseThrow();
        
        assert u.getNome().equals("Joackym");
        assert g.entrar("naoexiste").isEmpty();
    }

    static void testaTituloDuplicado() throws Exception {
        Catalogo c = new Catalogo();
        
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
        Catalogo c = new Catalogo();
        
        c.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        c.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        c.cadastrar(new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));
        
        assert c.buscarPorTitulo("dark").isPresent();
        assert c.buscarPorTitulo("inexistente").isEmpty();
        assert c.filtrarPorGenero("ficcao").size() == 1;
        assert c.filtrarPorGenero("terror").isEmpty();
    }

}
