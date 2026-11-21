package modelo;

/**
 * Tudo que pode ser exibido ao usuário com uma duração final.
 */
public interface Exibivel {
    /**
     * Informa quanto tempo leva para assistir por completo.
     *
     * @return a duração total, em minutos
     */
    int getDuracaoTotalMinutos();
    
    /**
     * Monta um resumo curto de como o título é exibido (duração, diretor, temporadas etc.).
     *
     * @return um texto curto descrevendo o que será exibido
     */
    String getResumoExibicao();    
} 