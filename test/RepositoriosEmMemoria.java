package test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import modelo.Midia;
import modelo.Usuario;
import persistencia.CatalogoRepositorio;
import persistencia.UsuarioRepositorio;

/** Repositórios falsos (só em memória) para testar os serviços sem tocar em arquivos. */
public class RepositoriosEmMemoria {

    public static class Catalogo implements CatalogoRepositorio {
        public final Map<String, Midia> guardadas = new LinkedHashMap<>();
        public int chamadasSalvar = 0;
        public boolean falharAoSalvar = false;

        @Override
        public void salvar(Midia midia) throws java.io.IOException {
            if (falharAoSalvar) {
                throw new java.io.IOException("falha simulada");
            }
            chamadasSalvar++;
            guardadas.put(midia.getTitulo().toLowerCase(), midia);
        }

        @Override
        public List<Midia> listarTodos() {
            return new ArrayList<>(guardadas.values());
        }

        @Override
        public Optional<Midia> buscarPorTitulo(String titulo) {
            return Optional.ofNullable(guardadas.get(titulo.toLowerCase()));
        }
    }

    public static class Usuarios implements UsuarioRepositorio {
        public final Map<String, Usuario> guardados = new LinkedHashMap<>();
        public int chamadasSalvar = 0;
        public boolean falharAoSalvar = false;

        @Override
        public void salvar(Usuario usuario) throws java.io.IOException {
            if (falharAoSalvar) {
                throw new java.io.IOException("falha simulada");
            }
            chamadasSalvar++;
            guardados.put(usuario.getUsername(), usuario);
        }

        @Override
        public List<Usuario> listarTodos() {
            return new ArrayList<>(guardados.values());
        }

        @Override
        public Optional<Usuario> buscarPorUsername(String username) {
            return Optional.ofNullable(guardados.get(username));
        }
    }
}
