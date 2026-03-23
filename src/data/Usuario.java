/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

public class Usuario {
    
    private int idUsuario;
    private String nomeUsuario;
    private String login;
    private String senha;
    private String nivelAcesso;
    
    // Construtores
    public Usuario() {
        this.nivelAcesso = "Consulta";
    }
    
    public Usuario(int idUsuario, String nomeUsuario, String login, 
                  String senha, String nivelAcesso) {
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.senha = senha;
        this.nivelAcesso = nivelAcesso;
    }
    
    // Getters e Setters
    public int getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getNomeUsuario() {
        return nomeUsuario;
    }
    
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public String getNivelAcesso() {
        return nivelAcesso;
    }
    
    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
    
    /**
     * Valida se o usuário tem permissão de administrador
     * @return true se for Administrador, false caso contrário
     */
    public boolean isAdministrador() {
        return "Administrador".equals(this.nivelAcesso);
    }
    
    /**
     * Valida se o usuário tem permissão de operador
     * @return true se for Operador, false caso contrário
     */
    public boolean isOperador() {
        return "Operador".equals(this.nivelAcesso);
    }
    
    /**
     * Valida se o usuário pode realizar operações críticas
     * @return true se for Administrador ou Operador
     */
    public boolean podeOperar() {
        return isAdministrador() || isOperador();
    }
}
    