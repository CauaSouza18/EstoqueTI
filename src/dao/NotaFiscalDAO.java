package dao;

import data.NotaFiscal;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de NotaFiscal — operações na tabela nota_fiscal
 * Campos: id_nf, numero_nf (VARCHAR 50), data_emissao (DATE),
 *         valor_total (DECIMAL 10,2), id_fornecedor
 * @author EstoqueTI
 */
public class NotaFiscalDAO {

    private final Conexao conexao;
    private final Connection conn;

    public NotaFiscalDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    /**
     * Lista todas as notas fiscais ordenadas da mais recente
     */
    public List<NotaFiscal> listar() {
        String sql = "SELECT * FROM nota_fiscal ORDER BY data_emissao DESC";
        List<NotaFiscal> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar notas fiscais: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista notas fiscais de um fornecedor específico
     */
    public List<NotaFiscal> listarPorFornecedor(int idFornecedor) {
        String sql = "SELECT * FROM nota_fiscal WHERE id_fornecedor = ? " +
            "ORDER BY data_emissao DESC";
        List<NotaFiscal> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idFornecedor);
            ResultSet rs = st.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Erro ao listar por fornecedor: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca nota fiscal pelo id
     */
    public NotaFiscal buscarPorId(int idNf) {
        String sql = "SELECT * FROM nota_fiscal WHERE id_nf = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idNf);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Erro ao buscar nota fiscal: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca nota fiscal pelo número (numeroNf)
     */
    public NotaFiscal buscarPorNumero(String numeroNf) {
        String sql = "SELECT * FROM nota_fiscal WHERE numero_nf = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, numeroNf);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Erro ao buscar por número: " + e.getMessage());
        }
        return null;
    }

    /**
     * Salva nova nota fiscal
     * dataEmissao: LocalDate → DATE (usa getDataEmissaoFormatada() para exibição)
     * valorTotal: BigDecimal → DECIMAL(10,2)
     */
    public boolean salvar(NotaFiscal nf) {
        String sql = "INSERT INTO nota_fiscal " +
            "(numero_nf, data_emissao, valor_total, id_fornecedor) " +
            "VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nf.getNumeroNf());
            st.setDate(2, Date.valueOf(nf.getDataEmissao()));
            st.setBigDecimal(3, nf.getValorTotal());
            st.setInt(4, nf.getIdFornecedor());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar nota fiscal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza nota fiscal existente
     */
    public boolean atualizar(NotaFiscal nf) {
        String sql = "UPDATE nota_fiscal SET " +
            "numero_nf=?, data_emissao=?, valor_total=?, id_fornecedor=? " +
            "WHERE id_nf=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nf.getNumeroNf());
            st.setDate(2, Date.valueOf(nf.getDataEmissao()));
            st.setBigDecimal(3, nf.getValorTotal());
            st.setInt(4, nf.getIdFornecedor());
            st.setInt(5, nf.getIdNf());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar nota fiscal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Conta total de notas fiscais
     */
    public int contarTotal() {
        try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM nota_fiscal");
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erro ao contar notas: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Soma valor total das notas do mês atual
     */
    public BigDecimal somarValorMesAtual() {
        String sql = "SELECT SUM(valor_total) FROM nota_fiscal " +
            "WHERE MONTH(data_emissao) = MONTH(CURDATE()) " +
            "AND YEAR(data_emissao) = YEAR(CURDATE())";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next() && rs.getBigDecimal(1) != null)
                return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.out.println("Erro ao somar valor do mês: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Mapeia ResultSet para objeto NotaFiscal
     * Converte DATE do banco para LocalDate (usado em getDataEmissaoFormatada())
     */
    private NotaFiscal mapear(ResultSet rs) throws SQLException {
        NotaFiscal nf = new NotaFiscal();
        nf.setIdNf(rs.getInt("id_nf"));
        nf.setNumeroNf(rs.getString("numero_nf"));
        nf.setValorTotal(rs.getBigDecimal("valor_total"));
        nf.setIdFornecedor(rs.getInt("id_fornecedor"));

        // Converte DATE do banco para LocalDate
        Date dataSQL = rs.getDate("data_emissao");
        if (dataSQL != null) nf.setDataEmissao(dataSQL.toLocalDate());

        return nf;
    }
}
