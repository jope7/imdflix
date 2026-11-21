package servico;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import persistencia.CatalogoJsonRepositorio;
import persistencia.CatalogoRepositorio;
import persistencia.UsuarioJsonRepositorio;
import persistencia.UsuarioRepositorio;


/**
 * Carrega os dados do sistema na ordem correta:
 * <ol>
 *   <li>o catálogo (títulos);</li>
 *   <li>os usuários, que precisam do catálogo pronto para recalcular as médias das avaliações.</li>
 * </ol>
 * Inverter a ordem faria todos os títulos dos usuários parecerem "inexistentes" e serem ignorados.
 *
 * Se qualquer arquivo estiver inválido, é lançada uma {@link excecoes.DadosInvalidosException} e o
 * programa não deve seguir com dados pela metade.
 */
public class CarregadorDados {
    

    /**
     * Tudo que o restante do sistema precisa depois do carregamento.
     *
     * @param catalogo o catálogo em memória, com as médias das notas já calculadas
     * @param catalogoRepositorio onde os títulos são gravados
     * @param usuarioRepositorio onde os usuários são lidos e gravados
     */
    public record DadosCarregados(Catalogo catalogo, CatalogoRepositorio catalogoRepositorio, 
                                    UsuarioRepositorio usuarioRepositorio) {

    }

    /** Arquivo JSON com os títulos. */
    private final Path caminhoCatalogo;
    /** Arquivo JSON com os usuários. */
    private final Path caminhoUsuarios;

    /**
     * Cria um carregador para os arquivos informados.
     *
     * @param caminhoCatalogo arquivo JSON com os títulos
     * @param caminhoUsuarios arquivo JSON com os usuários
     * @throws NullPointerException se algum caminho for nulo
     */
    public CarregadorDados(Path caminhoCatalogo, Path caminhoUsuarios) {
        this.caminhoCatalogo = Objects.requireNonNull(caminhoCatalogo, "O caminho do catalogo nao pode ser nulo.");
        this.caminhoUsuarios = Objects.requireNonNull(caminhoUsuarios, "O caminho dos usuarios nao pode ser nulo.");
    }

    /**
     * Cria um carregador para {@code dados/catalogo.json} e {@code dados/usuarios.json}, relativos à
     * pasta de onde o programa é executado.
     *
     * @return o carregador com os caminhos padrão
     */
    public static CarregadorDados padrao() {
        return new CarregadorDados(Path.of("dados", "catalogo.json"), Path.of("dados", "usuarios.json"));
    }

    /**
     * Informa o arquivo de títulos.
     *
     * @return o caminho do arquivo do catálogo
     */
    public Path getCaminhoCatalogo() {
        return caminhoCatalogo;
    }

    /**
     * Informa o arquivo de usuários.
     *
     * @return o caminho do arquivo de usuários
     */
    public Path getCaminhoUsuarios() {
        return caminhoUsuarios;
    }

    /**
     * Lê os dois arquivos: primeiro o catálogo e depois os usuários. Cada chamada monta objetos novos,
     * independentes dos de outras chamadas.
     *
     * @return o catálogo, os usuários e os repositórios
     * @throws IOException se um arquivo não puder ser lido
     * @throws excecoes.DadosInvalidosException se um arquivo estiver malformado ou com dados inválidos
     */
    public DadosCarregados carregar() throws IOException {
        // 1 catalogo
        CatalogoJsonRepositorio catalogoRepositorio = new CatalogoJsonRepositorio(caminhoCatalogo);
        Catalogo catalogo = new Catalogo(catalogoRepositorio);

        // 2 usuarios (recalcula as medias das midias do catalogo ja carregado)
        UsuarioJsonRepositorio usuarioRepositorio = UsuarioJsonRepositorio.carregar(caminhoUsuarios, catalogo::buscarPorTitulo);

        return new DadosCarregados(catalogo, catalogoRepositorio, usuarioRepositorio);
    }
}