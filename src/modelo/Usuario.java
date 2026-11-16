package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import excecoes.NotaInvalidaException;

/*
    o username eh o ID (imutavel e n diferencia maiusculas e minusculas)
    o nome é apenas para exibicao e pode se repetir entre usuarios
    lista e avaliacoes guardam apenas o titulo da midia, n o obj
*/
public class Usuario {
    // padrao
    private static final Pattern FORMATO_USERNAME = Pattern.compile("^[a-z0-9_]{3,20}$");

    private final String username;
    private String nome;
    private final List<String> minhaLista = new ArrayList<>();
    private final Map<String, Double> avaliacoes = new LinkedHashMap<>();

    // public Usuario(String username, String nome) {
        
    // }

    public static String normalizarUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("O username nao pode ser nulo");
        }

        String normalizado = username.trim().toLowerCase();

        if (!FORMATO_USERNAME.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("Username invalido: use de 3 a 20 caracteres entre letras, numeros e _.");
        }
        return normalizado;
    }

    public void adicionarNaLista(Midia midia) {
        if (!minhaLista.contains(midia.getTitulo())) {
            minhaLista.add(midia.getTitulo());
        }
    }

    public void removerDaLista(Midia midia) {
        minhaLista.remove(midia.getTitulo());
    }

    public void avaliar(Midia midia, double nota) throws NotaInvalidaException {
        Double anterior = avaliacoes.get(midia.getTitulo());
        if (anterior == null) {
            midia.avaliar(nota);
        } else {
            midia.substituirAvaliacao(anterior, nota);
        }

        avaliacoes.put(midia.getTitulo(), nota);
    }

    public String getUsername() {
        return username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome nao pode ser vazio.");
        }
        this.nome = nome;
    }

    public List<String> getMinhaLista() {
        return Collections.unmodifiableList(minhaLista);
    }

    public Map<String, Double> getAvaliacoes() {
        return Collections.unmodifiableMap(avaliacoes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Usuario outro && username.equals(outro.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return nome + "(@" + username + ")";
    }
}