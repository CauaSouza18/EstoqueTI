package data;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa uma Movimentação de Estoque
 * @author Cauã Souza da Silva
 * @version 2.0
 */
public class MovimentacaoEstoque {
    
    private int idMovimentacao;
    private int idProduto;
    private LocalDateTime dataMovimentacao;
    private String tipoMovimentacao; // "Entrada" ou "Saida"
    private int quantidade;
    private String observacao;
    private int idUsuario;
    
    // Construtores
    public MovimentacaoEstoque() {
        this.dataMovimentacao = LocalDateTime.now();
    }
    
    public MovimentacaoEstoque(int idMovimentacao, int idProduto, 
                              LocalDateTime dataMovimentacao, String tipoMovimentacao,
                              int quantidade, String observacao, int idUsuario) {
        this.idMovimentacao = idMovimentacao;
        this.idProduto = idProduto;
        this.dataMovimentacao = dataMovimentacao;
        this.tipoMovimentacao = tipoMovimentacao;
        this.quantidade = quantidade;
        this.observacao = observacao;
        this.idUsuario = idUsuario;
    }
    
    // Getters e Setters
    public int getIdMovimentacao() {
        return idMovimentacao;
    }
    
    public void setIdMovimentacao(int idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }
    
    public int getIdProduto() {
        return idProduto;
    }
    
    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }
    
    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }
    
    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }
    
    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }
    
    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    /**
     * Verifica se a movimentação é uma entrada
     * @return true se for entrada, false caso contrário
     */
    public boolean isEntrada() {
        return "Entrada".equals(this.tipoMovimentacao);
    }
    
    /**
     * Verifica se a movimentação é uma saída
     * @return true se for saída, false caso contrário
     */
    public boolean isSaida() {
        return "Saida".equals(this.tipoMovimentacao);
    }
    
    /**
     * Formata a data/hora da movimentação no padrão brasileiro
     * @return data formatada como dd/MM/yyyy HH:mm:ss
     */
    public String getDataMovimentacaoFormatada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return this.dataMovimentacao.format(formatter);
    }
}
