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
        testaCamposOpcionaisAusentes();
        System.out.println("✅ TesteMidiaFactory - todos os testes passaram.");
    }

    static JsonObject json(String texto) {
        return JsonParser.parseString(texto.replace('\'', '"')).getAsJsonObject();
    }

    static void rejeita(String texto, String motivo) {
        boolean lancou = false;
        
        try {
            MidiaFactory.criar(json(texto));
        } catch (DadosInvalidosException e) {
            lancou = true;
        }
        
        assert lancou : motivo;
    }

    static void testaCriaFilme() throws Exception {
        Midia m = MidiaFactory.criar(json("{'tipo':'Filme','titulo':'Matrix','genero':'Ficcao','ano':1999," 
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
            "{'tipo':' FILME ','titulo':'Matrix','genero':'Ficcao','ano':1999,'duracaoMinutos':136}"));
        assert m instanceof Filme : "O tipo nao deveria diferenciar maiusculas nem espacos";
    }

    static void testaTipoDesconhecido() {
        rejeita("{'tipo':'Anime','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90}", "Tipo desconhecido deveria ser rejeitado");
    }

    static void testaTipoAusente() {
        rejeita("{'titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90}", "Tipo ausente deveria ser rejeitado");
    }

    static void testaCampoObrigatorioAusente() {
        rejeita("{'tipo':'Filme','genero':'Y','ano':2000,'duracaoMinutos':90}",
            "Filme sem titulo deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000}",
            "Filme sem duracao deveria ser rejeitado");
        
        rejeita("{'tipo':'Serie','titulo':'X','genero':'Y','ano':2000,'temporadas':1,'episodiosPorTemporada':5}", 
            "Serie sem duracaoMediaEpisodio deveria ser rejeitada");
    }

    static void testaCampoNulo() {
        rejeita("{'tipo':'Filme','titulo':null,'genero':'Y','ano':2000,'duracaoMinutos':90}",
            "Titulo nulo deveria ser rejeitado");
    }

    static void testaCampoComTipoErrado() {
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':'90'}",
            "Duracao como texto deveria ser rejeitada");
        
        rejeita("{'tipo':'Filme','titulo':123,'genero':'Y','ano':2000,'duracaoMinutos':90}",
            "Titulo numerico deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':1999.5,'duracaoMinutos':90}",
            "Ano nao inteiro deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':['Y'],'ano':2000,'duracaoMinutos':90}",
            "Genero como lista deveria ser rejeitado");
    }

    static void testaDadosRejeitadosPeloConstrutor() {
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':-5}",
            "Duracao negativa deveria ser rejeitada");
        
        rejeita("{'tipo':'Filme','titulo':'X','genero':'Y','ano':1500,'duracaoMinutos':90}",
            "Ano anterior ao cinema deveria ser rejeitado");
        
        rejeita("{'tipo':'Filme','titulo':'  ','genero':'Y','ano':2000,'duracaoMinutos':90}",
            "Titulo em branco deveria ser rejeitado");
    }

    static void testaCamposOpcionaisAusentes() throws Exception {
        Midia m = MidiaFactory.criar(json("{'tipo':'Filme','titulo':'X','genero':'Y','ano':2000,'duracaoMinutos':90}"));
        
        assert ((Filme) m).getDiretor() == null : "Diretor ausente deveria ficar nulo";
    }
}