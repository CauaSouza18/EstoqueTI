package dao;

import data.MovimentacaoEstoque;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de MovimentacaoEstoque — operações na tabela movimentacao_estoque
 * Campos: id_movimentacao, id_produto, data_movimentacao (DATETIME),
 *         tipo_movimentacao ENUM("Entrada","Saida"), quantidade, observacao, id_usuario
 * @author EstoqueTI
 */
public class MovimentacaoEstoqueDAO {

    private final Conexao conexao;
    private final Connection conn;

    public MovimentacaoEstoqueDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    /**
     * Lista todas as movimentações ordenadas da mais recente para a mais antiga
     */
    public List<MovimentacaoEstoque> listar() {
        String sql = "SELECT * FROM movimentacao_estoque ORDER BY data_movimentacao DESC";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar movimentações: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista movimentações filtradas por tipo ("Entrada" ou "Saida")
     */
    public List<MovimentacaoEstoque> listarPorTipo(String tipo) {
        String sql = "SELECT * FROM movimentacao_estoque " +
            "WHERE tipo_movimentacao = ? ORDER BY data_movimentacao DESC";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, tipo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar por tipo: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista movimentações de um produto específico
     */
    public List<MovimentacaoEstoque> listarPorProduto(int idProduto) {
        String sql = "SELECT * FROM movimentacao_estoque " +
            "WHERE id_produto = ? ORDER BY data_movimentacao DESC";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idProduto);
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar por produto: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista as N movimentações mais recentes
     */
    public List<MovimentacaoEstoque> listarUltimas(int limite) {
        String sql = "SELECT * FROM movimentacao_estoque " +
            "ORDER BY data_movimentacao DESC LIMIT ?";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, limite);
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar últimas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Conta movimentações do dia atual
     */
    public int contarHoje() {
        String sql = "SELECT COUNT(*) FROM movimentacao_estoque " +
            "WHERE DATE(data_movimentacao) = CURDATE()";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erro ao contar hoje: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Registra uma movimentação E atualiza a quantidade do produto
     * Usa transação para garantir consistência
     * @return true se ambas as operações foram bem-sucedidas
     */
    public boolean registrar(MovimentacaoEstoque mov) {
        String sqlMov = "INSERT INTO movimentacao_estoque " +
            "(id_produto, data_movimentacao, tipo_movimentacao, " +
            "quantidade, observacao, id_usuario) " +
            "VALUES (?, NOW(), ?, ?, ?, ?)";

        String sqlProd = mov.isEntrada()
            ? "UPDATE produtos SET quantidade = quantidade + ? WHERE id_produto = ?"
            : "UPDATE produtos SET quantidade = quantidade - ? WHERE id_produto = ?";

        try {
            // Inicia transação
            conn.setAutoCommit(false);

            // 1. Insere movimentação
            PreparedStatement stMov = conn.prepareStatement(sqlMov);
            stMov.setInt(1, mov.getIdProduto());
            stMov.setString(2, mov.getTipoMovimentacao());
            stMov.setInt(3, mov.getQuantidade());
            stMov.setString(4, mov.getObservacao());
            stMov.setInt(5, mov.getIdUsuario());
            stMov.executeUpdate();

            // 2. Atualiza estoque do produto
            PreparedStatement stProd = conn.prepareStatement(sqlProd);
            stProd.setInt(1, mov.getQuantidade());
            stProd.setInt(2, mov.getIdProduto());
            stProd.executeUpdate();

            // Confirma transação
            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            // Reverte em caso de erro
            try { conn.rollback(); conn.setAutoCommit(true); }
            catch (SQLException ex) { System.out.println("Erro no rollback: " + ex.getMessage()); }
            System.out.println("Erro ao registrar movimentação: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se há estoque suficiente para uma saída
     */
    public boolean temEstoqueSuficiente(int idProduto, int quantidade) {
        String sql = "SELECT quantidade FROM produtos WHERE id_produto = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idProduto);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getInt("quantidade") >= quantidade;
        } catch (SQLException e) {
            System.out.println("Erro ao verificar estoque: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mapeia ResultSet para objeto MovimentacaoEstoque
     */
    private MovimentacaoEstoque mapear(ResultSet rs) throws SQLException {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setIdMovimentacao(rs.getInt("id_movimentacao"));
        m.setIdProduto(rs.getInt("id_produto"));
        m.setTipoMovimentacao(rs.getString("tipo_movimentacao"));
        m.setQuantidade(rs.getInt("quantidade"));
        m.setObservacao(rs.getString("observacao"));
        m.setIdUsuario(rs.getInt("id_usuario"));

        // Converte DATETIME do banco para LocalDateTime
        Timestamp ts = rs.getTimestamp("data_movimentacao");
        if (ts != null) m.setDataMovimentacao(ts.toLocalDateTime());

        return m;
    }
}
