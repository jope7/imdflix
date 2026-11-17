package servico;

import java.util.Objects;
import java.util.Optional;
import modelo.Usuario;

/*
    padrao Singleton: guarda o usuario que esta logado. 
        Existe uma unica sessao por execucao do programa
        e todas as telas precisam conltar o mesmo usuario
        entao uma instancia compartilhada evita ter que ficar
        passando o usuario de tela em tela

    instancia criada de forma preguiçosa e segura para varias threads
    a classe interna só é carregada na primeira chamada de getInstancia()
*/
public final class SessaoUsuario {    

    private static final class Holder {
        private static final SessaoUsuario INSTANCIA = new SessaoUsuario();
    }

    private Usuario usuarioAtual;

    public static SessaoUsuario getInstancia() {
        return Holder.INSTANCIA;
    }

    // define o user logad, substituindo o anterior, se houver
    public void entrar(Usuario usuario) {
        this.usuarioAtual = Objects.requireNonNull(usuario, "O usuario nao pode ser nulo.");
    }

    // encerra a sessao 
    public void sair() {
        this.usuarioAtual = null;
    }

    public boolean haUsuarioLogado() {
        return usuarioAtual != null;
    }

    public Optional<Usuario> getUsuarioAtual() {
        return Optional.ofNullable(usuarioAtual);
    }
}
