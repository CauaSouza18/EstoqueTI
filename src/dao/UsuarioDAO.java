package dao;

import data.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Usuario — operações no banco para a tabela usuarios
 * @author EstoqueTI
 */
public class UsuarioDAO {

    private final Conexao conexao;
    private final Connection conn;

    public UsuarioDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    /**
     * Autentica o usuário no banco pelo login e senha
     * @return Usuario encontrado ou null se credenciais inválidas
     */
    public Usuario autenticar(String login, String senha) {
        String sql = "SELECT * FROM usuarios WHERE login = ? AND senha = ? LIMIT 1";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, login);
            st.setString(2, senha);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNomeUsuario(rs.getString("nome_usuario"));
                u.setLogin(rs.getString("login"));
                u.setSenha(rs.getString("senha"));
                u.setNivelAcesso(rs.getString("nivel_acesso"));
                return u;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao autenticar: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos os usuários cadastrados
     */
    public List<Usuario> listar() {
        String sql = "SELECT * FROM usuarios";
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNomeUsuario(rs.getString("nome_usuario"));
                u.setLogin(rs.getString("login"));
                u.setSenha(rs.getString("senha"));
                u.setNivelAcesso(rs.getString("nivel_acesso"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Salva novo usuário no banco
     */
    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome_usuario, login, senha, nivel_acesso) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, usuario.getNomeUsuario());
            st.setString(2, usuario.getLogin());
            st.setString(3, usuario.getSenha());
            st.setString(4, usuario.getNivelAcesso());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao salvar usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza usuário existente
     */
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome_usuario=?, login=?, senha=?, nivel_acesso=? " +
                     "WHERE id_usuario=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, usuario.getNomeUsuario());
            st.setString(2, usuario.getLogin());
            st.setString(3, usuario.getSenha());
            st.setString(4, usuario.getNivelAcesso());
            st.setInt(5, usuario.getIdUsuario());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }
}