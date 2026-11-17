package test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import excecoes.DadosInvalidosException;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import persistencia.MidiaFactory;

public class TesteMidiaFactory {

    public static void main(String[] args) throws Exception {
        testaCriaFilme();
        testaCriaSerie();
        testaCriaDocumentario();
        testaTipoSemDiferenciarCaixa();
        testaTipoDesconhecido();
        testaTipoAusente();
        testaCampoObrigatorioAusente();
        testaCampoNulo();
        testaCampoComTipoErrado();
        testaDadosRejeitadosPeloConstrutor();
        testaDiretorETemaObrigatorios();
        System.out.println("✅ TesteMidiaFactory - todos os testes passaram.");
    }

    static JsonObject json(String texto) {
        return JsonParser.parseString(texto.replace('\'', '"')).getAsJsonObject();
    }

    static void rejeita(String texto, String trechoDaMensagem, String motivo) {
        String mensagem = null;
        
        try {
            MidiaFactory.criar(json(texto));
        } catch (DadosInvalidosException e) {
            mensagem = e.getMessage();
        }
        
        assert mensagem != null : motivo + " (nada foi lancado)";
        assert mensagem.contains(trechoDaMensagem) : motivo + " (mensagem inesperada: " + mensagem + ")";
    }

    static void testaCriaFilme() throws Exception {
        Midia m = MidiaFactory.criar(json(
            "{'tipo':'Filme','titulo':'Matrix','genero':'Ficcao','ano':1999,"
                + "'duracaoMinutos':136,'diretor':'Wachowski'}"));
        assert m instanceof Filme : "Deveria criar um Filme";
        
        Filme f = (Filme) m;
        
        assert f.getTitulo().equals("Matrix") && f.getGenero().equals("Ficcao");
        assert f.getAnoLancamento() == 1999 && f.getDuracaoTotalMinutos() == 136;
        assert f.getDiretor().equals("Wachowski");
    }

    static void testaCriaSerie() throws Exception {
        Midia m = MidiaFactory.criar(json("{'tipo':'Serie','titulo':'Dark','genero':'Suspense','ano':2017,"
                                            + "'temporadas':3,'episodiosPorTemporada':8,'duracaoMediaEpisodio':50}"));
        
        assert m instanceof Serie : "Deveria criar uma Serie";
        assert m.getTitulo().equals("Dark");
        
        assert ((Serie) m).getTemporadas() == 3;
        assert ((Serie) m).getDuracaoTotalMinutos() == 1200;
    }

    static void testaCriaDocumentario() throws Exception {
        Midia m = MidiaFactory.criar(json("{'tipo':'Documentario','titulo':'Free Solo','genero':'Aventura','ano':2018,"
                                            + "'duracaoMinutos':100,'tema':'Escalada'}"));
        
        assert m instanceof Documentario : "Deveria criar um Documentario";
        assert ((Documentario) m).getTema().equals("Escalada");
    }

    static void testaTipoSemDiferenciarCaixa() throws Exception {
        Midia m = MidiaFactory.criar(json(
            "{'tipo':' FILME ','titulo':'Matrix','genero':'Ficcao','ano':1999," + "'duracaoMinutos':136,'diretor':'Wachowski'}"));
    
        assert m instanceof Filme : "O tipo nao deveria diferenciar maiusculas nem espacos";
    }

    static void testaTipoDesconhecido() {
        rejeita("{'tipo':'Anime','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "Tipo desconhecido", "Tipo desconhecido deveria ser rejeitado");
    }

    static void testaTipoAusente() {
        rejeita("{'titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "'tipo'", "Tipo ausente deveria ser rejeitado");
    }

    static void testaCampoObrigatorioAusente() {
        rejeita("{'tipo':'Filme','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "'titulo'", "Filme sem titulo deveria ser rejeitado");
    
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'diretor':'D'}",
            "'duracaoMinutos'", "Filme sem duracao deveria ser rejeitado");
    
        rejeita("{'tipo':'Serie','titulo':'X','genero':'Y','ano':2000,'temporadas':1,'episodiosPorTemporada':5}",
            "'duracaoMediaEpisodio'", "Serie sem duracaoMediaEpisodio deveria ser rejeitada");
    }

    static void testaCampoNulo() {
        rejeita("{'tipo':'Filme','titulo':null,'genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "'titulo'", "Titulo nulo deveria ser rejeitado");
    }

    static void testaCampoComTipoErrado() {
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':'90','diretor':'D'}",
            "'duracaoMinutos'", "Duracao como texto deveria ser rejeitada");
    
        rejeita("{'tipo':'Filme','titulo':123,'genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "'titulo'", "Titulo numerico deveria ser rejeitado");
    
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':1999.5,'duracaoMinutos':90,'diretor':'D'}",
            "'ano'", "Ano nao inteiro deveria ser rejeitado");
    
        rejeita("{'tipo':'Filme','titulo':'X','genero':['Y'],'ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "'genero'", "Genero como lista deveria ser rejeitado");
    }

    static void testaDadosRejeitadosPeloConstrutor() {
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':-5,'diretor':'D'}",
            "A duracao deve ser positiva", "Duracao negativa deveria ser rejeitada");
    
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':1500,'duracaoMinutos':90,'diretor':'D'}",
            "Ano de lancamento invalido", "Ano anterior ao cinema deveria ser rejeitado");
    
        rejeita("{'tipo':'Filme','titulo':'  ','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'D'}",
            "O titulo nao pode ser vazio", "Titulo em branco deveria ser rejeitado");
    }

    static void testaDiretorETemaObrigatorios() {
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90}",
            "'diretor'", "Filme sem diretor deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':null}",
            "'diretor'", "Filme com diretor nulo deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90,'diretor':'  '}",
            "O diretor nao pode ser vazio", "Filme com diretor em branco deveria ser rejeitado");
        
        rejeita("{'tipo':'Documentario','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90}",
            "'tema'", "Documentario sem tema deveria ser rejeitado");
        
        rejeita("{'tipo':'Documentario','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90,'tema':''}",
            "O tema nao pode ser vazio", "Documentario com tema vazio deveria ser rejeitado");

        // o próprio construtor também exige os campos (vale para cadastros feitos pela tela)
        boolean filme = false;
        
        try {
            new Filme("X", "Y", 2000, 90, null);
        } catch (IllegalArgumentException e) {
            filme = true;
        }
        
        assert filme : "O construtor de Filme deveria exigir o diretor";

        boolean documentario = false;
        
        try {
            new Documentario("X", "Y", 2000, 90, " ");
        } catch (IllegalArgumentException e) {
            documentario = true;
        }
        
        assert documentario : "O construtor de Documentario deveria exigir o tema";
    }
}
