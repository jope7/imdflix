package servico;

import excecoes.DadosInvalidosException;
import excecoes.TituloDuplicadoException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import modelo.Midia;
import persistencia.CatalogoJsonRepositorio;
import persistencia.CatalogoRepositorio;
import persistencia.UsuarioJsonRepositorio;
import persistencia.UsuarioRepositorio;


/*
    carrega os dados do sistema na ordem correta:
        1 catalogo
        2 os usuarios (precisam do catálogo pronto para recalcular as médias das avaliações)
    
    se qualquer arquivo estiver inválido, a exceção DadosInvalidosException é lançada e 
    o programa não deve seguir com dados pela metade
*/
public class CarregadorDados {
    

    public record DadosCarregados(Catalogo catalogo, CatalogoRepositorio catalogoRepositorio, 
                                    UsuarioRepositorio usuarioRepositorio) {

    }

    private final Path caminhoCatalogo;
    private final Path caminhoUsuarios;

    public CarregadorDados(Path caminhoCatalogo, Path caminhoUsuarios) {
        this.caminhoCatalogo = Objects.requireNonNull(caminhoCatalogo, "O caminho do catalogo nao pode ser nulo.");
        this.caminhoUsuarios = Objects.requireNonNull(caminhoUsuarios, "O caminho dos usuarios nao pode ser nulo.");
    }

    // Usa dados/catalogo.json e dados/usuarios.json
    public static CarregadorDados padrao() {
        return new CarregadorDados(Path.of("dados", "catalogo.json"), Path.of("dados", "usuarios.json"));
    }

    public Path getCaminhoCatalogo() {
        return caminhoCatalogo;
    }

    public Path getCaminhoUsuarios() {
        return caminhoUsuarios;
    }

    // cada chamada monta objetos novos, um catálogo e usuários independentes dos de outras chamadas
    public DadosCarregados carregar() throws IOException {
        // 1 catalogo
        CatalogoJsonRepositorio catalogoRepositorio = new CatalogoJsonRepositorio(caminhoCatalogo);
        Catalogo catalogo = new Catalogo();

        for (Midia midia : catalogoRepositorio.listarTodos()) {
            try {
                catalogo.cadastrar(midia);
            } catch (TituloDuplicadoException e) {
                // o repositorio ja rejeita duplicados, isso é so uma segunda garantia
                throw new DadosInvalidosException("Catalogo invalido: " + e.getMessage(), e);
            }
        }

        // 2 usuarios (recalcula as medias das midias do catalogo ja carregado)
        UsuarioJsonRepositorio usuarioRepositorio = UsuarioJsonRepositorio.carregar(caminhoUsuarios, catalogo::buscarPorTitulo);

        return new DadosCarregados(catalogo, catalogoRepositorio, usuarioRepositorio);
    }
}