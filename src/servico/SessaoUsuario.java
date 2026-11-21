package servico;

import java.util.Objects;
import java.util.Optional;
import modelo.Usuario;

/**
 * Padrão Singleton: guarda o usuário que está logado.
 *
 * Existe uma única sessão por execução do programa, e todas as telas precisam consultar o mesmo
 * usuário; uma instância compartilhada evita passar o usuário de tela em tela.
 *
 * A instância é criada de forma preguiçosa e segura para várias threads (idiom do "holder"):
 * a classe interna só é carregada na primeira chamada de {@link #getInstancia()}.
 */
public final class SessaoUsuario {    

    /** Guarda a única instância; só é carregada quando {@link SessaoUsuario#getInstancia()} é chamado. */
    private static final class Holder {
        /** Classe auxiliar: não deve ser instanciada. */
        private Holder() {
        }

        /** A única instância da sessão. */
        private static final SessaoUsuario INSTANCIA = new SessaoUsuario();
    }

    /** Usuário logado, ou {@code null} se ninguém entrou. */
    private Usuario usuarioAtual;

    /**
     * Construtor privado: ninguém de fora cria sessões, só {@link #getInstancia()} entrega a instância.
     */
    private SessaoUsuario() {
    }

    /**
     * Devolve a única instância da sessão.
     *
     * @return a sessão compartilhada por todo o programa
     */
    public static SessaoUsuario getInstancia() {
        return Holder.INSTANCIA;
    }

    /**
     * Define o usuário logado, substituindo o anterior, se houver.
     *
     * @param usuario o usuário que entrou (não pode ser nulo)
     * @throws NullPointerException se o usuário for nulo
     */
    public void entrar(Usuario usuario) {
        this.usuarioAtual = Objects.requireNonNull(usuario, "O usuario nao pode ser nulo.");
    }

    /** Encerra a sessão: depois disso, não há usuário logado. */
    public void sair() {
        this.usuarioAtual = null;
    }

    /**
     * Informa se alguém está logado.
     *
     * @return {@code true} se há um usuário logado
     */
    public boolean haUsuarioLogado() {
        return usuarioAtual != null;
    }

    /**
     * Devolve o usuário logado.
     *
     * @return o usuário logado, ou vazio se ninguém entrou
     */
    public Optional<Usuario> getUsuarioAtual() {
        return Optional.ofNullable(usuarioAtual);
    }
}
