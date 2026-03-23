package data;

import dao.Conexao;
import view.TelaLogin;

import javax.swing.*;
import java.sql.Connection;

/**
 * Classe principal — EstoqueTI
 * Ponto de entrada do sistema de estoque de TI.
 * Verifica a conexão com o banco antes de abrir a tela de login.
 *
 * Banco: sistema_estoque (MySQL)
 * Tabelas: produtos, categoria, fornecedores,
 *          movimentacao_estoque, nota_fiscal, usuarios
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class EstoqueTI {

    public static void main(String[] args) {

        // 1. Aplica Look and Feel antes de qualquer componente Swing
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background",         new java.awt.Color(0xF5F3EE));
            UIManager.put("OptionPane.background",    new java.awt.Color(0xFFFFFF));
            UIManager.put("OptionPane.messageForeground", new java.awt.Color(0x1A1A1A));
        } catch (Exception e) {
            System.out.println("Erro ao configurar LookAndFeel: " + e.getMessage());
        }

        // 2. Verifica conexão com o banco antes de abrir a UI
        if (!verificarConexao()) {
            JOptionPane.showMessageDialog(
                null,
                "Não foi possível conectar ao banco de dados.\n\n" +
                "Verifique se o MySQL está rodando e se as configurações\n" +
                "de conexão em Conexao.java estão corretas:\n\n" +
                "  URL:  jdbc:mysql://localhost:3306/sistema_estoque\n" +
                "  User: root",
                "Erro de Conexão",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1); // Encerra se não conseguir conectar
        }

        // 3. Abre a tela de login na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            TelaLogin login = new TelaLogin();
            login.setVisible(true);
        });
    }

    /**
     * Testa a conexão com o banco de dados
     * @return true se a conexão foi estabelecida com sucesso
     */
    private static boolean verificarConexao() {
        try {
            Connection conn = new Conexao().getConexao();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexão com o banco estabelecida com sucesso.");
                conn.close();
                return true;
            }
        } catch (Exception e) {
            System.out.println("❌ Falha na conexão: " + e.getMessage());
        }
        return false;
    }
}