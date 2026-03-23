package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    public Connection getConexao() {
        try {
         
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/sistema_estoque", "root", "27870015");
        } catch (SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
            return null;
        }
    }
}