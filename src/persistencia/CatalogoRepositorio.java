package persistencia;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import modelo.Midia;

/**
 * Persistência do catálogo. O restante do sistema depende apenas desta interface, e não de como os
 * títulos são guardados (hoje, em um arquivo JSON).
 */
public interface CatalogoRepositorio {
    /**
     * Guarda uma mídia, substituindo a que tiver o mesmo título.
     *
     * @param midia a mídia a guardar
     * @throws IOException se não for possível gravar
     */
    void salvar(Midia midia) throws IOException;

    /**
     * Lê todos os títulos guardados.
     *
     * @return os títulos, na ordem em que foram guardados
     * @throws IOException se não for possível ler
     */
    List<Midia> listarTodos() throws IOException;

    /**
     * Procura um título, sem diferenciar maiúsculas de minúsculas.
     *
     * @param titulo o título procurado
     * @return a mídia encontrada, ou vazio se não existir
     * @throws IOException se não for possível ler
     */
    Optional<Midia> buscarPorTitulo(String titulo) throws IOException;
    
}