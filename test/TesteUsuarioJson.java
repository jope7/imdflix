package test;

import excecoes.DadosInvalidosException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import modelo.Documentario;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import modelo.Usuario;
import persistencia.CatalogoJsonRepositorio;
import persistencia.UsuarioJsonRepositorio;
import servico.Catalogo;

public class TesteUsuarioJson {

    interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        testaArquivoAusente();
        testaArquivoEmBranco();
        testaCarregaListaEAvaliacoes();
        testaDoisUsuariosMesmaMidia();
        testaTituloInexistenteEhIgnorado();
        testaMesmoNomeUsernamesDiferentes();
        testaUsernameDuplicadoNoArquivo();
        testaUsernameInvalido();
        testaNotasInvalidas();
        testaCamposObrigatoriosETipos();
        testaJsonMalformadoERaiz();
        testaArquivoInvalidoNaoAlteraMedias();
        testaSalvarELerDeVolta();
        testaCacheNaoContaNotasEmDobro();
        testaSalvarReavaliacao();
        testaTituloRemovidoSomeNaProximaGravacao();
        testaBuscarPorUsername();
        testaAcentosNoNome();
        testaArquivosDeExemplo();
        System.out.println("✅ TesteUsuarioJson - todos os testes passaram.");
    }

    // ---------- auxiliares ----------

    static Path novoCaminho() throws IOException {
        return Files.createTempDirectory("cinetech-teste").resolve("usuarios.json");
    }

    static void apagar(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(arquivo.getParent());
        } catch (IOException e) {

        }
    }

    static void escrever(Path p, String json) throws IOException {
        Files.writeString(p, json.replace('\'', '"'));
    }

    static Catalogo catalogo() throws Exception {
        Catalogo c = new Catalogo(new RepositoriosEmMemoria.Catalogo());

        c.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"));
        c.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        c.cadastrar(new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));

        return c;
    }

    static UsuarioJsonRepositorio carregar(Path p, Catalogo c) throws IOException {
        return UsuarioJsonRepositorio.carregar(p, c::buscarPorTitulo);
    }

    static Midia midia(Catalogo c, String titulo) {
        return c.buscarPorTitulo(titulo).orElseThrow();
    }

    static String mensagemDeErro(Acao acao) throws Exception {
        try {
            acao.executar();
            return null;
        } catch (DadosInvalidosException e) {
            return e.getMessage();
        }
    }

    static void rejeita(String json, String trecho, String motivo) throws Exception {
        Path p = novoCaminho();

        escrever(p, json);
        Catalogo c = catalogo();
        String msg = mensagemDeErro(() -> carregar(p, c));

        assert msg != null : motivo + " (nada foi lancado)";
        assert msg.contains(trecho) : motivo + " (mensagem inesperada: " + msg + ")";

        apagar(p);
    }

    // ---------- testes ----------

    static void testaArquivoAusente() throws Exception {
        Path p = novoCaminho();

        assert carregar(p, catalogo()).listarTodos().isEmpty() : "Arquivo ausente: sem usuarios";
        assert Files.notExists(p) : "Carregar nao deve criar o arquivo";

        apagar(p);
    }

    static void testaArquivoEmBranco() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, " \n ");

        assert carregar(p, catalogo()).listarTodos().isEmpty() : "Arquivo em branco: sem usuarios";

        apagar(p);
    }

    static void testaCarregaListaEAvaliacoes() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'76ope','nome':'Joackym','minhaLista':['Matrix','Dark']," + "'avaliacoes':{'Matrix':9.0,'Dark':8.5}}]");
        
        Catalogo c = catalogo();
        Usuario u = carregar(p, c).buscarPorUsername("76ope").orElseThrow();
        
        assert u.getNome().equals("Joackym");
        assert u.getMinhaLista().equals(List.of("Matrix", "Dark"));
        assert u.getAvaliacoes().get("Matrix") == 9.0 && u.getAvaliacoes().get("Dark") == 8.5;
        assert midia(c, "Matrix").getNotaMedia() == 9.0 && midia(c, "Matrix").getQuantidadeAvaliacoes() == 1 
            : "A media da midia deveria ser recalculada ao carregar";
        assert midia(c, "Dark").getNotaMedia() == 8.5;
        assert midia(c, "Free Solo").getQuantidadeAvaliacoes() == 0;
        
        apagar(p);
    }

    static void testaDoisUsuariosMesmaMidia() throws Exception {
        Path p = novoCaminho();
        
        escrever(p, "[{'username':'ana01','nome':'Ana','minhaLista':[],'avaliacoes':{'Matrix':10}},"
            + "{'username':'beto02','nome':'Beto','minhaLista':[],'avaliacoes':{'Matrix':6.0}}]");
        
        Catalogo c = catalogo();
        
        carregar(p, c);
        
        assert midia(c, "Matrix").getQuantidadeAvaliacoes() == 2 && midia(c, "Matrix").getNotaMedia() == 8.0
            : "A media deveria considerar as duas notas";
        
        apagar(p);
    }

    static void testaTituloInexistenteEhIgnorado() throws Exception {
        Path p = novoCaminho();
        
        escrever(p, "[{'username':'ana01','nome':'Ana','minhaLista':['Matrix','Fantasma'],"
            + "'avaliacoes':{'Matrix':8.0,'Fantasma':3.0}}]");
        Catalogo c = catalogo();
        Usuario u = carregar(p, c).buscarPorUsername("ana01").orElseThrow();
        
        assert u.getMinhaLista().equals(List.of("Matrix")) : "Titulo inexistente deveria sair da lista";
        assert u.getAvaliacoes().size() == 1 && u.getAvaliacoes().containsKey("Matrix")
            : "Avaliacao de titulo inexistente deveria ser ignorada";
        assert midia(c, "Matrix").getQuantidadeAvaliacoes() == 1;
        
        apagar(p);
    }

    static void testaMesmoNomeUsernamesDiferentes() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'76ope','nome':'Joackym'},{'username':'joack_dev','nome':'Joackym'}]");
        UsuarioJsonRepositorio repo = carregar(p, catalogo());
        
        assert repo.listarTodos().size() == 2 : "Nome repetido deve ser aceito (lista e avaliacoes sao opcionais)";
        
        apagar(p);
    }

    static void testaUsernameDuplicadoNoArquivo() throws Exception {
        rejeita("[{'username':'76ope','nome':'A'},{'username':'76OPE','nome':'B'}]",
            "duplicado", "Username repetido (sem diferenciar caixa) deveria ser rejeitado");
    }

    static void testaUsernameInvalido() throws Exception {
        rejeita("[{'username':'ab','nome':'Ana'}]", "Item 1", "Username curto demais deveria ser rejeitado");
        
        rejeita("[{'username':'com espaco','nome':'Ana'}]", "Username invalido", "Username com espaco");
    }

    static void testaNotasInvalidas() throws Exception {
        rejeita("[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':11}}]",
            "entre 0 e 10", "Nota acima de 10 deveria ser rejeitada");
        
        rejeita("[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':-1}}]",
            "entre 0 e 10", "Nota negativa deveria ser rejeitada");
        
        rejeita("[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':'9'}}]",
            "deve ser um numero", "Nota como texto deveria ser rejeitada");

        rejeita("[{'username':'ana01','nome':'Ana','avaliacoes':{'Fantasma':50}}]",
            "entre 0 e 10", "Nota invalida de titulo inexistente tambem deveria ser rejeitada");
    }

    static void testaCamposObrigatoriosETipos() throws Exception {
        rejeita("[{'nome':'Ana'}]", "'username'", "Usuario sem username");

        rejeita("[{'username':'ana01'}]", "'nome'", "Usuario sem nome");
    
        rejeita("[{'username':'ana01','nome':'  '}]", "O nome nao pode ser vazio", "Nome em branco");
    
        rejeita("[{'username':'ana01','nome':'Ana','minhaLista':'Matrix'}]", "'minhaLista'",
            "Lista que e um texto");
    
        rejeita("[{'username':'ana01','nome':'Ana','minhaLista':[1,2]}]", "'minhaLista'",
            "Lista com itens que nao sao texto");
    
        rejeita("[{'username':'ana01','nome':'Ana','avaliacoes':[9.0]}]", "'avaliacoes'",
            "Avaliacoes que e uma lista");
    }

    static void testaJsonMalformadoERaiz() throws Exception {
        Path p = novoCaminho();
        Files.writeString(p, "[ {\"username\": ");
        Catalogo c = catalogo();
        String msg = mensagemDeErro(() -> carregar(p, c));
        
        assert msg != null && msg.contains("malformado") : "JSON malformado deveria ser rejeitado";
        
        apagar(p);

        rejeita("{'username':'ana01'}", "lista", "Objeto na raiz deveria ser rejeitado");
    
        rejeita("[1, 2]", "objeto JSON", "Itens que nao sao objetos deveriam ser rejeitados");
    }

    static void testaArquivoInvalidoNaoAlteraMedias() throws Exception {
        Path p = novoCaminho();
        // o primeiro usuario e valido; o segundo tem uma nota invalida
        escrever(p, "[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':9}},"
            + "{'username':'beto02','nome':'Beto','avaliacoes':{'Matrix':99}}]");
        Catalogo c = catalogo();
        String msg = mensagemDeErro(() -> carregar(p, c));
        
        assert msg != null && msg.contains("Item 2") : "O segundo usuario deveria ser rejeitado: " + msg;
        assert midia(c, "Matrix").getQuantidadeAvaliacoes() == 0 && midia(c, "Matrix").getNotaMedia() == 0.0
            : "Arquivo invalido nao deve alterar nenhuma media";
        
        apagar(p);
    }

    static void testaSalvarELerDeVolta() throws Exception {
        Path p = novoCaminho();
        Catalogo c1 = catalogo();
        UsuarioJsonRepositorio repo = carregar(p, c1);
        Usuario ana = new Usuario("ana01", "Ana");
        ana.adicionarNaLista(midia(c1, "Matrix"));
        ana.adicionarNaLista(midia(c1, "Dark"));
        ana.avaliar(midia(c1, "Matrix"), 10);
        repo.salvar(ana);

        Catalogo c2 = catalogo(); // catalogo novo, sem nenhuma nota
        Usuario lida = carregar(p, c2).buscarPorUsername("ana01").orElseThrow();
        
        assert lida.getNome().equals("Ana");
        assert lida.getMinhaLista().equals(List.of("Matrix", "Dark"));
        assert lida.getAvaliacoes().size() == 1 && lida.getAvaliacoes().get("Matrix") == 10.0;
        assert midia(c2, "Matrix").getNotaMedia() == 10.0 && midia(c2, "Matrix").getQuantidadeAvaliacoes() == 1;
        
        apagar(p);
    }

    static void testaCacheNaoContaNotasEmDobro() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':8}}]");
        Catalogo c = catalogo();
        UsuarioJsonRepositorio repo = carregar(p, c);
        
        repo.listarTodos();
        repo.listarTodos();
        repo.buscarPorUsername("ana01");
        
        assert midia(c, "Matrix").getQuantidadeAvaliacoes() == 1 && midia(c, "Matrix").getNotaMedia() == 8.0
            : "Consultar o repositorio varias vezes nao pode contar a nota de novo";
        
        apagar(p);
    }

    static void testaSalvarReavaliacao() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'ana01','nome':'Ana','avaliacoes':{'Matrix':9}}]");
        Catalogo c1 = catalogo();
        UsuarioJsonRepositorio repo = carregar(p, c1);
        Usuario ana = repo.buscarPorUsername("ana01").orElseThrow();
        ana.avaliar(midia(c1, "Matrix"), 5);
        repo.salvar(ana);

        Catalogo c2 = catalogo();
        carregar(p, c2);
        
        assert midia(c2, "Matrix").getNotaMedia() == 5.0 && midia(c2, "Matrix").getQuantidadeAvaliacoes() == 1
            : "A nota nova deveria substituir a antiga";
        
        apagar(p);
    }

    static void testaTituloRemovidoSomeNaProximaGravacao() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'ana01','nome':'Ana','minhaLista':['Matrix','Dark'],"
            + "'avaliacoes':{'Matrix':9.0,'Dark':8.0}}]");
        Catalogo semMatrix = new Catalogo(new RepositoriosEmMemoria.Catalogo());
        semMatrix.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        UsuarioJsonRepositorio repo = carregar(p, semMatrix);
        
        Usuario ana = repo.buscarPorUsername("ana01").orElseThrow();
        
        assert ana.getMinhaLista().equals(List.of("Dark"));
        
        repo.salvar(ana);
        
        assert !Files.readString(p).contains("Matrix") : "O titulo removido deveria sumir do arquivo";
        assert Files.readString(p).contains("Dark");
        
        apagar(p);
    }

    static void testaBuscarPorUsername() throws Exception {
        Path p = novoCaminho();
        escrever(p, "[{'username':'76ope','nome':'Joackym'}]");
        UsuarioJsonRepositorio repo = carregar(p, catalogo());
        
        assert repo.buscarPorUsername("76OPE").isPresent() : "Busca nao diferencia maiusculas";
        assert repo.buscarPorUsername("  76ope ").isPresent() : "Busca ignora espacos nas pontas";
        assert repo.buscarPorUsername("naoexiste").isEmpty();
        assert repo.buscarPorUsername("x").isEmpty() : "Username de formato invalido: vazio, sem excecao";
        assert repo.buscarPorUsername(null).isEmpty();
        
        apagar(p);
    }

    static void testaAcentosNoNome() throws Exception {
        Path p = novoCaminho();
        Catalogo c = catalogo();
        UsuarioJsonRepositorio repo = carregar(p, c);
        String nome = "Jo\u00e3o da Concei\u00e7\u00e3o";
        repo.salvar(new Usuario("joao01", nome));
        Usuario lido = carregar(p, catalogo()).buscarPorUsername("joao01").orElseThrow();
        
        assert lido.getNome().equals(nome) : "Acentos deveriam sobreviver (UTF-8)";
        
        apagar(p);
    }

    static void testaArquivosDeExemplo() throws Exception {
        Path catalogoExemplo = Path.of("dados", "catalogo.json");
        Path usuariosExemplo = Path.of("dados", "usuarios.json");
        
        if (Files.notExists(catalogoExemplo) || Files.notExists(usuariosExemplo)) {
            System.out.println("(aviso) arquivos de exemplo nao encontrados: execute a partir da raiz do projeto.");
            return;
        }
        
        Catalogo c = new Catalogo(new CatalogoJsonRepositorio(catalogoExemplo));
        
        UsuarioJsonRepositorio repo = carregar(usuariosExemplo, c);
        
        assert repo.listarTodos().size() == 3 : "O arquivo de exemplo deveria ter 3 usuarios";
        assert midia(c, "Matrix").getQuantidadeAvaliacoes() == 2 && midia(c, "Matrix").getNotaMedia() == 8.0
            : "Matrix: notas 9.0 e 7.0, media 8.0";
        assert midia(c, "Dark").getNotaMedia() == 8.5;
        assert midia(c, "Free Solo").getNotaMedia() == 10.0;
        assert repo.buscarPorUsername("vitinhozip").orElseThrow().getMinhaLista().equals(List.of("Free Solo"));
    }
}