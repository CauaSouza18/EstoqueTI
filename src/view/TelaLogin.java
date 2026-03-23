package view;

import dao.UsuarioDAO;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tela de Login — EstoqueTI
 * @author EstoqueTI
 */
public class TelaLogin extends JFrame {

    // ── Cores ─────────────────────────────────────────────────────────────
    private static final Color COR_BG        = new Color(0xF5F3EE);
    private static final Color COR_SURFACE   = new Color(0xFFFFFF);
    private static final Color COR_TEXT      = new Color(0x1A1A1A);
    private static final Color COR_MUTED     = new Color(0x666666);
    private static final Color COR_LABEL     = new Color(0x999999);
    private static final Color COR_BORDER    = new Color(0xCCCCCC);
    private static final Color COR_BORDER_DK = new Color(0x888888);
    private static final Color COR_ACCENT    = new Color(0xE8E4DC);
    private static final Color COR_RED       = new Color(0xD94F3D);
    private static final Color COR_GREEN     = new Color(0x2D7A4F);
    private static final Color COR_BLUE      = new Color(0x2255AA);
    private static final Color COR_BLUE_BG   = new Color(0xDDEEFF);
    private static final Color COR_TAG       = new Color(0xEFEFEF);

    // ── Fontes ────────────────────────────────────────────────────────────
    private static final Font FONT_BRAND  = new Font("Monospaced", Font.BOLD,  22);
    private static final Font FONT_TITLE  = new Font("SansSerif",  Font.BOLD,  22);
    private static final Font FONT_SUB    = new Font("SansSerif",  Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Monospaced", Font.BOLD,  10);
    private static final Font FONT_INPUT  = new Font("SansSerif",  Font.PLAIN, 14);
    private static final Font FONT_BTN    = new Font("SansSerif",  Font.BOLD,  13);
    private static final Font FONT_SMALL  = new Font("Monospaced", Font.PLAIN, 10);

    // ── Componentes ───────────────────────────────────────────────────────
    private JTextField     campoLogin;
    private JPasswordField campoSenha;
    private JButton        btnEntrar;
    private JLabel         lblErro;
    private JLabel         lblCarregando;

    // ── DAO ───────────────────────────────────────────────────────────────
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public TelaLogin() {
        // Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background",            COR_BG);
            UIManager.put("OptionPane.background",       COR_SURFACE);
            UIManager.put("TextField.background",        COR_SURFACE);
            UIManager.put("TextField.foreground",        COR_TEXT);
            UIManager.put("PasswordField.background",    COR_SURFACE);
            UIManager.put("PasswordField.foreground",    COR_TEXT);
        } catch (Exception e) {
            System.out.println("Erro LookAndFeel: " + e.getMessage());
        }

        setTitle("EstoqueTI — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ContentPane com fundo bege e padding em volta
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(COR_BG);
        contentPane.setBorder(new EmptyBorder(50, 70, 50, 70));
        setContentPane(contentPane);

        // Constrói e adiciona o card centralizado
        contentPane.add(construirCard(), new GridBagConstraints());

        // pack() calcula o tamanho ideal e centraliza na tela
        pack();
        setLocationRelativeTo(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Card branco central
    // ─────────────────────────────────────────────────────────────────────
    private JPanel construirCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(36, 44, 36, 44)
        ));
        card.setPreferredSize(new Dimension(420, 510));

        // ── Brand ──────────────────────────────────────────────────────
        JLabel lblBrand = new JLabel("EstoqueTI");
        lblBrand.setFont(FONT_BRAND);
        lblBrand.setForeground(COR_TEXT);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubBrand = new JLabel("Sistema de Estoque de TI");
        lblSubBrand.setFont(FONT_SMALL);
        lblSubBrand.setForeground(COR_LABEL);
        lblSubBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(COR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Título ─────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("Entrar no sistema");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("Informe seu login e senha");
        lblDesc.setFont(FONT_SUB);
        lblDesc.setForeground(COR_MUTED);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Campo login ────────────────────────────────────────────────
        JLabel lblLoginLbl = criarLabel("LOGIN");
        campoLogin = new JTextField();
        campoLogin.setFont(FONT_INPUT);
        campoLogin.setForeground(COR_TEXT);
        campoLogin.setBackground(COR_SURFACE);
        campoLogin.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 2),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campoLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        campoLogin.addActionListener(e -> realizarLogin());

        // ── Campo senha ────────────────────────────────────────────────
        JLabel lblSenhaLbl = criarLabel("SENHA");
        campoSenha = new JPasswordField();
        campoSenha.setFont(FONT_INPUT);
        campoSenha.setForeground(COR_TEXT);
        campoSenha.setBackground(COR_SURFACE);
        campoSenha.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 2),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campoSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        campoSenha.setEchoChar('•');
        campoSenha.addActionListener(e -> realizarLogin());

        // ── Erro ───────────────────────────────────────────────────────
        lblErro = new JLabel(" ");
        lblErro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblErro.setForeground(COR_RED);
        lblErro.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Botão entrar ───────────────────────────────────────────────
        btnEntrar = criarBotao("Entrar →");
        btnEntrar.addActionListener(e -> realizarLogin());

        // ── Carregando ─────────────────────────────────────────────────
        lblCarregando = new JLabel("Verificando credenciais...");
        lblCarregando.setFont(FONT_SMALL);
        lblCarregando.setForeground(COR_LABEL);
        lblCarregando.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCarregando.setVisible(false);

        // ── Painel nível de acesso ─────────────────────────────────────
        JPanel painelNiveis = new JPanel();
        painelNiveis.setLayout(new BoxLayout(painelNiveis, BoxLayout.Y_AXIS));
        painelNiveis.setBackground(COR_ACCENT);
        painelNiveis.setBorder(new EmptyBorder(10, 14, 10, 14));
        painelNiveis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblNivelTitulo = new JLabel("NÍVEL DE ACESSO");
        lblNivelTitulo.setFont(FONT_LABEL);
        lblNivelTitulo.setForeground(COR_LABEL);

        // badges lado a lado
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badges.setBackground(COR_ACCENT);
        badges.add(criarBadge("Administrador", COR_TEXT,    Color.WHITE));
        badges.add(criarBadge("Operador",      COR_BLUE_BG, COR_BLUE));
        badges.add(criarBadge("Consulta",      COR_TAG,     COR_LABEL));

        painelNiveis.add(lblNivelTitulo);
        painelNiveis.add(Box.createVerticalStrut(5));
        painelNiveis.add(badges);

        // ── Montagem ───────────────────────────────────────────────────
        card.add(lblBrand);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSubBrand);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(22));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(5));
        card.add(lblDesc);
        card.add(Box.createVerticalStrut(24));
        card.add(lblLoginLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(campoLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(lblSenhaLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(campoSenha);
        card.add(Box.createVerticalStrut(6));
        card.add(lblErro);
        card.add(Box.createVerticalStrut(12));
        card.add(btnEntrar);
        card.add(Box.createVerticalStrut(6));
        card.add(lblCarregando);
        card.add(Box.createVerticalStrut(18));
        card.add(painelNiveis);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────
    private void realizarLogin() {
        String login = campoLogin.getText().trim();
        String senha = new String(campoSenha.getPassword()).trim();

        if (login.isEmpty()) {
            mostrarErro("Preencha o campo login.");
            campoLogin.requestFocus();
            return;
        }
        if (senha.isEmpty()) {
            mostrarErro("Preencha o campo senha.");
            campoSenha.requestFocus();
            return;
        }

        btnEntrar.setEnabled(false);
        lblCarregando.setVisible(true);
        lblErro.setText(" ");

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() {
                return usuarioDAO.autenticar(login, senha);
            }

            @Override
            protected void done() {
                lblCarregando.setVisible(false);
                btnEntrar.setEnabled(true);
                try {
                    Usuario usuarioLogado = get();
                    if (usuarioLogado != null) {
                        dispose();
                        TelaDashboard dashboard = new TelaDashboard(usuarioLogado);
                        dashboard.setVisible(true);
                    } else {
                        mostrarErro("Login ou senha incorretos.");
                        campoSenha.setText("");
                        campoSenha.requestFocus();
                    }
                } catch (Exception ex) {
                    mostrarErro("Erro ao conectar ao banco de dados.");
                    System.out.println("Erro login: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────
    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COR_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton criarBotao(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? COR_TEXT : COR_BORDER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel criarBadge(String texto, Color bg, Color fg) {
        JLabel badge = new JLabel(texto);
        badge.setFont(new Font("Monospaced", Font.BOLD, 9));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private void mostrarErro(String msg) {
        lblErro.setText("⚠ " + msg);
        Timer t = new Timer(40, null);
        int[] n = {0};
        int ox = getX();
        t.addActionListener(e -> {
            n[0]++;
            setLocation(ox + (n[0] % 2 == 0 ? -6 : 6), getY());
            if (n[0] >= 8) { setLocation(ox, getY()); t.stop(); }
        });
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}