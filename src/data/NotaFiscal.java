package data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;


/**
 * Classe que representa uma Nota Fiscal
 * @author Cauã Souza da Silva
 * @version 2.0
 */
public class NotaFiscal {
    
    private int idNf;
    private String numeroNf;
    private LocalDate dataEmissao;
    private BigDecimal valorTotal;
    private int idFornecedor;
    
    // Construtores
    public NotaFiscal() {
        this.dataEmissao = LocalDate.now();
    }
    
    public NotaFiscal(int idNf, String numeroNf, LocalDate dataEmissao, 
                     BigDecimal valorTotal, int idFornecedor) {
        this.idNf = idNf;
        this.numeroNf = numeroNf;
        this.dataEmissao = dataEmissao;
        this.valorTotal = valorTotal;
        this.idFornecedor = idFornecedor;
    }
    
    // Getters e Setters
    public int getIdNf() {
        return idNf;
    }
    
    public void setIdNf(int idNf) {
        this.idNf = idNf;
    }
    
    public String getNumeroNf() {
        return numeroNf;
    }
    
    public void setNumeroNf(String numeroNf) {
        this.numeroNf = numeroNf;
    }
    
    public LocalDate getDataEmissao() {
        return dataEmissao;
    }
    
    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }
    
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public int getIdFornecedor() {
        return idFornecedor;
    }
    
    public void setIdFornecedor(int idFornecedor) {
        this.idFornecedor = idFornecedor;
    }
    
    /**
     * Formata a data de emissão no padrão brasileiro
     * @return data formatada como dd/MM/yyyy
     */
    public String getDataEmissaoFormatada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return this.dataEmissao.format(formatter);
    }
}