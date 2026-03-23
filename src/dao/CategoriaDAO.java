package dao;

import data.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Categoria — operações na tabela categoria
 * @author EstoqueTI
 */
public class CategoriaDAO {

    private final Conexao conexao;
    private final Connection conn;

    public CategoriaDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    /**
     * Lista todas as categorias ordenadas por nome
     */
    public List<Categoria> listar() {
        String sql = "SELECT id_categoria, nome_categoria FROM categoria ORDER BY nome_categoria";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNomeCategoria(rs.getString("nome_categoria"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar categorias: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca categoria pelo id
     */
    public Categoria buscarPorId(int idCategoria) {
        String sql = "SELECT id_categoria, nome_categoria FROM categoria WHERE id_categoria = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idCategoria);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNomeCategoria(rs.getString("nome_categoria"));
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar categoria: " + e.getMessage());
        }
        return null;
    }

    /**
     * Salva nova categoria
     */
    public boolean salvar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nome_categoria) VALUES (?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, categoria.getNomeCategoria());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar categoria: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza categoria existente
     */
    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nome_categoria = ? WHERE id_categoria = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, categoria.getNomeCategoria());
            st.setInt(2, categoria.getIdCategoria());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar categoria: " + e.getMessage());
            return false;
        }
    }
}
