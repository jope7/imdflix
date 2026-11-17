package test;

import modelo.Usuario;
import servico.SessaoUsuario;

public class TesteSessaoUsuario {

    public static void main(String[] args) {
        testaInstanciaUnica();
        testaEntrarESair();
        testaEntrarSubstituiUsuario();
        testaEntrarComNulo();
        SessaoUsuario.getInstancia().sair(); // deixa o estado limpo
        System.out.println("✅ TesteSessaoUsuario - todos os testes passaram.");
    }

    static void testaInstanciaUnica() {
        assert SessaoUsuario.getInstancia() == SessaoUsuario.getInstancia() : "O Singleton deveria devolver sempre a mesma instancia";
    }

    static void testaEntrarESair() {
        SessaoUsuario sessao = SessaoUsuario.getInstancia();
        sessao.sair();
        assert !sessao.haUsuarioLogado() && sessao.getUsuarioAtual().isEmpty() : "Deveria comecar sem usuario";

        Usuario ana = new Usuario("ana01", "Ana");

        sessao.entrar(ana);

        assert sessao.haUsuarioLogado();
        assert sessao.getUsuarioAtual().orElseThrow() == ana;

        sessao.sair();

        assert !sessao.haUsuarioLogado() && sessao.getUsuarioAtual().isEmpty() : "Sair deveria limpar a sessao";
    }

    static void testaEntrarSubstituiUsuario() {
        SessaoUsuario sessao = SessaoUsuario.getInstancia();

        sessao.entrar(new Usuario("ana01", "Ana"));

        Usuario beto = new Usuario("beto02", "Beto");

        sessao.entrar(beto);

        assert sessao.getUsuarioAtual().orElseThrow() == beto : "O novo login deveria substituir o anterior";

        sessao.sair();
    }

    static void testaEntrarComNulo() {
        boolean lancou = false;

        try {
            SessaoUsuario.getInstancia().entrar(null);
        } catch (NullPointerException e) {
            lancou = true;
        }

        assert lancou : "Entrar com usuario nulo deveria ser rejeitado";
    }
}
