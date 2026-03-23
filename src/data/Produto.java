package data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classe que representa um Produto com precisão decimal financeira
 * @author Cauã Souza da Silva
 * @version 3.0
 */
public class Produto {
    
    private int idProduto;
    private String nomeProduto;
    private String descricao;
    private String unidadeMedida;
    private int idCategoria;
    private String marca;
    private BigDecimal precoCusto; // Alterado para BigDecimal
    private BigDecimal precoVenda; // Alterado para BigDecimal
    private int quantidade;
    private String status;
    
    // Construtor Vazio
    public Produto() {
        this.quantidade = 0;
        this.status = "Ativo";
        this.precoCusto = BigDecimal.ZERO;
        this.precoVenda = BigDecimal.ZERO;
    }
    
    // Construtor Completo
    public Produto(int idProduto, String nomeProduto, String descricao, 
                  String unidadeMedida, int idCategoria, String marca,
                  BigDecimal precoCusto, BigDecimal precoVenda, int quantidade, String status) {
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.idCategoria = idCategoria;
        this.marca = marca;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidade = quantidade;
        this.status = status;
    }
    
    // Getters e Setters
    public int getIdProduto() {
        return idProduto;
    }
    
    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }
    
    public String getNomeProduto() {
        return nomeProduto;
    }
    
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getUnidadeMedida() {
        return unidadeMedida;
    }
    
    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
    
    public int getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }
    
    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }
    
    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }
    
    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Calcula o lucro unitário do produto (Venda - Custo)
     * @return valor do lucro como BigDecimal
     */
    public BigDecimal calcularLucro() {
        if (this.precoVenda == null || this.precoCusto == null) {
            return BigDecimal.ZERO;
        }
        return this.precoVenda.subtract(this.precoCusto);
    }
    
    /**
     * Calcula a margem de lucro percentual sobre o custo
     * @return percentual de lucro (ex: 50.00 para 50%)
     */
    public BigDecimal calcularMargemLucro() {
        // Verifica se o custo é nulo ou zero para evitar erro de divisão
        if (this.precoCusto == null || this.precoCusto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal lucro = calcularLucro();
        
        // (Lucro / Custo) * 100
        return lucro.divide(this.precoCusto, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
    }
}