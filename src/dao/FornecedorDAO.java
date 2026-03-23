package dao;

import data.Fornecedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Fornecedor — operações na tabela fornecedores
 * @author EstoqueTI
 */
public class FornecedorDAO {

    private final Conexao conexao;
    private final Connection conn;

    public FornecedorDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    /**
     * Lista todos os fornecedores ordenados por nome
     */
    public List<Fornecedor> listar() {
        String sql = "SELECT * FROM fornecedores ORDER BY nome_fornecedor";
        List<Fornecedor> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar fornecedores: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista apenas fornecedores ativos
     */
    public List<Fornecedor> listarAtivos() {
        String sql = "SELECT * FROM fornecedores WHERE status = 'Ativo' ORDER BY nome_fornecedor";
        List<Fornecedor> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar fornecedores ativos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca fornecedor pelo id
     */
    public Fornecedor buscarPorId(int idFornecedor) {
        String sql = "SELECT * FROM fornecedores WHERE id_fornecedor = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idFornecedor);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Erro ao buscar fornecedor: " + e.getMessage());
        }
        return null;
    }

    /**
     * Salva novo fornecedor
     */
    public boolean salvar(Fornecedor fornecedor) {
        String sql = "INSERT INTO fornecedores " +
            "(nome_fornecedor, endereco, telefone, email, cnpj, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, fornecedor.getNomeFornecedor());
            st.setString(2, fornecedor.getEndereco());
            st.setString(3, fornecedor.getTelefone());
            st.setString(4, fornecedor.getEmail());
            st.setString(5, fornecedor.getCnpj());
            st.setString(6, fornecedor.getStatus());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar fornecedor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza fornecedor existente
     */
    public boolean atualizar(Fornecedor fornecedor) {
        String sql = "UPDATE fornecedores SET " +
            "nome_fornecedor=?, endereco=?, telefone=?, email=?, cnpj=?, status=? " +
            "WHERE id_fornecedor=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, fornecedor.getNomeFornecedor());
            st.setString(2, fornecedor.getEndereco());
            st.setString(3, fornecedor.getTelefone());
            st.setString(4, fornecedor.getEmail());
            st.setString(5, fornecedor.getCnpj());
            st.setString(6, fornecedor.getStatus());
            st.setInt(7, fornecedor.getIdFornecedor());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar fornecedor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapeia ResultSet para objeto Fornecedor
     */
    private Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setIdFornecedor(rs.getInt("id_fornecedor"));
        f.setNomeFornecedor(rs.getString("nome_fornecedor"));
        f.setEndereco(rs.getString("endereco"));
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));
        f.setCnpj(rs.getString("cnpj"));
        f.setStatus(rs.getString("status"));
        return f;
    }
}
