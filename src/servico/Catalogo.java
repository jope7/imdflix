package servico;

import excecoes.TituloDuplicadoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import modelo.Midia;

/*
    reune todos os titulos disponiveis 
    o titulo é o ID nao pode se repetir (sem diferenciar maiusculas)
*/
public class Catalogo {
    private final Map<String, Midia> midias = new LinkedHashMap<>();

    private static String chave(String titulo) {
        return titulo.trim().toLowerCase();
    }

    public void cadastrar(Midia midia) throws TituloDuplicadoException {
        String chave = chave(midia.getTitulo());

        if (midias.containsKey(chave)) {
            throw new TituloDuplicadoException(midia.getTitulo());
        }
        
        midias.put(chave, midia);
    }

    public Optional<Midia> buscarPorTitulo(String titulo) {
        return Optional.ofNullable(midias.get(chave(titulo)));
    }

    public List<Midia> filtrarPorGenero(String genero) {
        List<Midia> resultado = new ArrayList<>();
        
        for (Midia m : midias.values()) {
            if (m.getGenero().equalsIgnoreCase(genero.trim())) {
                resultado.add(m);
            }
        }
        
        return resultado;
    }

    public List<Midia> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(midias.values()));
    }
}
