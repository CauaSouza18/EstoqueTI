package dao;
import data.Produto;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class ProdutoDAO {
    
    private final Conexao conexao;
    private final Connection conn;
   
    
    public List<Produto> listar() {
    String sql = "SELECT * FROM produtos";
    List<Produto> lista = new ArrayList<>();

    try (PreparedStatement st = conn.prepareStatement(sql);
         ResultSet rs = st.executeQuery()) {

        while (rs.next()) {
            Produto p = new Produto();
            p.setIdProduto(rs.getInt("id_produto"));
            p.setNomeProduto(rs.getString("nome_produto"));
            p.setDescricao(rs.getString("descricao"));
            p.setUnidadeMedida(rs.getString("unidade_medida"));
            p.setIdCategoria(rs.getInt("id_categoria"));
            p.setMarca(rs.getString("marca"));
            
            // Convertendo o DECIMAL do banco de volta para BigDecimal no Java
            p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
            p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
            
            p.setQuantidade(rs.getInt("quantidade"));
            p.setStatus(rs.getString("status"));

            lista.add(p);
        }
    } catch (SQLException e) {
        System.out.println("Erro ao listar produtos: " + e.getMessage());
    }
    return lista;
}

    public ProdutoDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    public boolean salvar(Produto produto) {
      
        String sql = "INSERT INTO produtos (nome_produto, descricao, unidade_medida, " +
                     "id_categoria, marca, preco_custo, preco_venda, quantidade, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, produto.getNomeProduto());
            st.setString(2, produto.getDescricao());
            st.setString(3, produto.getUnidadeMedida());
            st.setInt(4, produto.getIdCategoria());
            st.setString(5, produto.getMarca());
            
            
            st.setBigDecimal(6, produto.getPrecoCusto()); 
            st.setBigDecimal(7, produto.getPrecoVenda());
            
            st.setInt(8, produto.getQuantidade());
            st.setString(9, produto.getStatus());

            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar produto: " + e.getMessage());
            return false;
        }
        


    }
}

