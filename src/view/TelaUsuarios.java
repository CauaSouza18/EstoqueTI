package view;

import dao.Conexao;
import dao.UsuarioDAO;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.List;

/**
 * Tela de Usuários — EstoqueTI
 * Restrita a nivelAcesso = "Administrador" (isAdministrador() = true)
 * Listagem, cadastro e edição de usuários.
 * Usa: usuarios
 *
 * Campos de Usuario:
 *   idUsuario, nomeUsuario, login, senha,
 *   nivelAcesso ENUM("Administrador","Operador","Consulta")
 *
 * Métodos usados: isAdministrador(), isOperador(), podeOperar()
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaUsuarios extends JPanel {

    // ── Cores ─────────────────────────────────────────────────────────────
    private static final Color COR_BG        = new Color(0xF5F3EE);
    private static final Color COR_SURFACE   = new Color(0xFFFFFF);
    private static final Color COR_TEXT      = new Color(0x1A1A1A);
    private static final Color COR_MUTED     = new Color(0x666666);
    private static final Color COR_LABEL     = new Color(0x999999);
    private static final Color COR_BORDER    = new Color(0xCCCCCC);
    private static final Color COR_BORDER_DK = new Color(0x888888);
    private static final Color COR_ACCENT    = new Color(0xE8E4DC);
    private static final Color COR_TAG       = new Color(0xEFEFEF);
    private static final Color COR_GREEN     = new Color(0x2D7A4F);
    private static final Color COR_GREEN_BG  = new Color(0xD9EFE3);
    private static final Color COR_RED       = new Color(0xD94F3D);
    private static final Color COR_RED_BG    = new Color(0xFDECEA);
    private static final Color COR_BLUE      = new Color(0x2255AA);
    private static final Color COR_BLUE_BG   = new Color(0xDDEEFF);
    private static final Color COR_YELLOW    = new Color(0xC8820A);

    // ── Fontes ────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("SansSerif",  Font.BOLD,  20);
    private static final Font FONT_LABEL  = new Font("Monospaced", Font.BOLD,  10);
    private static final Font FONT_BODY   = new Font("SansSerif",  Font.PLAIN, 12);
    private static final Font FONT_BOLD   = new Font("SansSerif",  Font.BOLD,  12);
    private static final Font FONT_SMALL  = new Font("Monospaced", Font.PLAIN, 10);
    private static final Font FONT_BTN    = new Font("SansSerif",  Font.BOLD,  12);
    private static final Font FONT_INPUT  = new Font("SansSerif",  Font.PLAIN, 13);

    // ── Estado ────────────────────────────────────────────────────────────
    private final Usuario usuarioLogado;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioEmEdicao = null;

    // ── Tabela ────────────────────────────────────────────────────────────
    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JComboBox<String> cboFiltroNivel;

    // ── Formulário ────────────────────────────────────────────────────────
    private JTextField        campoNome;
    private JTextField        campoLogin;
    private JPasswordField    campoSenha;
    private JPasswordField    campoSenhaConfirm;
    private JComboBox<String> cboNivelAcesso;
    private JLabel            lblPermissoes;
    private JLabel            lblTituloForm;
    private JPanel            painelForm;
    private JLabel            lblAviso;

    public TelaUsuarios(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        setLayout(new BorderLayout());
        setBackground(COR_BG);

        // Verifica permissão — só Administrador acessa
        if (!usuarioLogado.isAdministrador()) {
            construirTelaAcessoNegado();
            return;
        }

        construirInterface();
        carregarUsuarios();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Tela de acesso negado
    // ─────────────────────────────────────────────────────────────────────
    private void construirTelaAcessoNegado() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(COR_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_RED, 2),
            new EmptyBorder(32, 40, 32, 40)
        ));

        JLabel lblIco = new JLabel("🔒");
        lblIco.setFont(new Font("SansSerif", Font.PLAIN, 36));
        lblIco.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Acesso Restrito");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(COR_RED);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMsg = new JLabel(
            "<html><center>Esta tela é restrita a <b>Administradores</b>.<br>" +
            "Seu nível de acesso: <b>" + usuarioLogado.getNivelAcesso() + "</b><br><br>" +
            "<code>isAdministrador() = false</code></center></html>");
        lblMsg.setFont(FONT_BODY);
        lblMsg.setForeground(COR_MUTED);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblIco);
        card.add(Box.createVerticalStrut(12));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));
        card.add(lblMsg);

        painel.add(card);
        add(painel, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────
    private void construirInterface() {
        JPanel interno = new JPanel(new BorderLayout());
        interno.setBackground(COR_BG);
        interno.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel esquerda = new JPanel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setBackground(COR_BG);

        esquerda.add(construirHeader());
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(construirAvisoAdmin());
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(construirBarraFiltro());
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(construirTabela());
        esquerda.add(Box.createVerticalStrut(18));
        esquerda.add(construirMatrizPermissoes());

        painelForm = construirFormulario();
        painelForm.setVisible(false);

        interno.add(esquerda, BorderLayout.CENTER);
        interno.add(painelForm, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(interno);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(COR_BG);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COR_BG);
        header.setBorder(new MatteBorder(0, 0, 1, 0, COR_BORDER));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel esq = new JPanel();
        esq.setLayout(new BoxLayout(esq, BoxLayout.Y_AXIS));
        esq.setBackground(COR_BG);
        esq.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitulo = new JLabel("Usuários");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        JLabel lblSub = new JLabel(
            "Usuario.java · nomeUsuario, login, senha, nivelAcesso — isAdministrador() = true");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        dir.setBackground(COR_BG);
        JButton btnNovo = criarBotao("+ Novo Usuário", COR_TEXT, Color.WHITE);
        btnNovo.addActionListener(e -> abrirFormularioNovo());
        dir.add(btnNovo);

        header.add(esq, BorderLayout.WEST);
        header.add(dir, BorderLayout.EAST);
        return header;
    }

    // ── Aviso de área restrita ────────────────────────────────────────────
    private JPanel construirAvisoAdmin() {
        JPanel aviso = new JPanel(new BorderLayout());
        aviso.setBackground(COR_YELLOW);
        aviso.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 3, 0, 0, COR_YELLOW),
            new EmptyBorder(8, 12, 8, 12)
        ));
        aviso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        aviso.setBackground(new Color(0xFEF3DC));
        aviso.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 3, 0, 0, COR_YELLOW),
            new EmptyBorder(8, 12, 8, 12)
        ));

        lblAviso = new JLabel(
            "🔒  Área restrita — logado como: " + usuarioLogado.getNomeUsuario() +
            "  ·  isAdministrador() = " + usuarioLogado.isAdministrador());
        lblAviso.setFont(FONT_SMALL);
        lblAviso.setForeground(COR_YELLOW);
        aviso.add(lblAviso, BorderLayout.WEST);
        return aviso;
    }

    // ── Filtro ────────────────────────────────────────────────────────────
    private JPanel construirBarraFiltro() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBackground(COR_BG);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblFiltro = new JLabel("Filtrar por nivelAcesso:");
        lblFiltro.setFont(FONT_SMALL);
        lblFiltro.setForeground(COR_LABEL);

        cboFiltroNivel = new JComboBox<>(
            new String[]{"Todos", "Administrador", "Operador", "Consulta"});
        cboFiltroNivel.setFont(FONT_BODY);
        cboFiltroNivel.setPreferredSize(new Dimension(160, 34));
        cboFiltroNivel.addActionListener(e -> carregarUsuarios());

        barra.add(lblFiltro);
        barra.add(cboFiltroNivel);
        return barra;
    }

    // ── Tabela ────────────────────────────────────────────────────────────
    private JPanel construirTabela() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(new LineBorder(COR_BORDER, 2));

        String[] colunas = {
            "idUsuario", "nomeUsuario", "login",
            "nivelAcesso", "podeOperar()", "isAdministrador()", ""
        };

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(modeloTabela);
        tabela.setFont(FONT_BODY);
        tabela.setRowHeight(34);
        tabela.setBackground(COR_SURFACE);
        tabela.setGridColor(COR_BORDER);
        tabela.setShowVerticalLines(false);
        tabela.setSelectionBackground(COR_ACCENT);
        tabela.setFillsViewportHeight(true);

        JTableHeader th = tabela.getTableHeader();
        th.setFont(FONT_LABEL);
        th.setBackground(COR_TAG);
        th.setForeground(COR_LABEL);
        th.setBorder(new MatteBorder(0, 0, 1, 0, COR_BORDER));

        // Larguras
        tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(60);

        // Renderer com badges
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                setForeground(COR_TEXT);
                String val = value != null ? value.toString() : "";

                // id
                if (col == 0) {
                    setFont(FONT_SMALL); setForeground(COR_LABEL);
                }
                // login — monoespaçado
                else if (col == 2) {
                    setFont(FONT_SMALL); setForeground(COR_MUTED);
                }
                // nivelAcesso — badge colorido
                else if (col == 3) {
                    setFont(new Font("Monospaced", Font.BOLD, 10));
                    switch (val) {
                        case "Administrador" -> {
                            setBackground(sel ? COR_ACCENT : COR_TEXT);
                            setForeground(Color.WHITE);
                        }
                        case "Operador" -> {
                            setBackground(sel ? COR_ACCENT : COR_BLUE_BG);
                            setForeground(COR_BLUE);
                        }
                        default -> {
                            setBackground(sel ? COR_ACCENT : COR_TAG);
                            setForeground(COR_LABEL);
                        }
                    }
                }
                // podeOperar() e isAdministrador() — true/false badge
                else if (col == 4 || col == 5) {
                    setFont(new Font("Monospaced", Font.BOLD, 10));
                    if ("true".equals(val)) {
                        setBackground(sel ? COR_ACCENT : COR_GREEN_BG);
                        setForeground(COR_GREEN);
                    } else {
                        setBackground(sel ? COR_ACCENT : COR_TAG);
                        setForeground(COR_LABEL);
                    }
                }
                // botão editar
                else if (col == 6) {
                    setForeground(COR_TEXT);
                    setFont(FONT_SMALL);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (!sel) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                }
                return this;
            }
        });

        // Clique duplo para editar
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabela.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) modeloTabela.getValueAt(row, 0);
                        // Impede editar o próprio usuário logado
                        if (id == usuarioLogado.getIdUsuario()) {
                            JOptionPane.showMessageDialog(TelaUsuarios.this,
                                "Você não pode editar seu próprio usuário aqui.\n" +
                                "Use as configurações de perfil.",
                                "Atenção", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        abrirFormularioEdicao(id);
                    }
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setBorder(null);
        scrollTabela.setPreferredSize(new Dimension(0, 220));
        card.add(scrollTabela, BorderLayout.CENTER);

        JLabel lblDica = new JLabel(
            "  Dica: clique duplo para editar · Você não pode editar seu próprio usuário");
        lblDica.setFont(FONT_SMALL);
        lblDica.setForeground(COR_LABEL);
        lblDica.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, COR_BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        card.add(lblDica, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(COR_BG);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    // ── Matriz de permissões ──────────────────────────────────────────────
    private JPanel construirMatrizPermissoes() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel lblTitulo = new JLabel("MATRIZ DE PERMISSÕES POR NIVELACESSO");
        lblTitulo.setFont(FONT_LABEL);
        lblTitulo.setForeground(COR_LABEL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] colunas = {"Tela / Ação", "Consulta", "Operador", "Administrador"};
        Object[][] dados = {
            {"Dashboard",                    "✅", "✅", "✅"},
            {"Visualizar Produtos",          "✅", "✅", "✅"},
            {"Cadastrar / Editar Produto",   "❌", "✅", "✅"},
            {"Registrar Movimentação",       "❌", "✅ podeOperar()", "✅"},
            {"Notas Fiscais",                "❌", "✅", "✅"},
            {"Fornecedores",                 "❌", "✅", "✅"},
            {"Usuários",                     "❌", "❌", "✅ isAdministrador()"},
        };

        JTable tabelaPerm = new JTable(dados, colunas);
        tabelaPerm.setFont(FONT_BODY);
        tabelaPerm.setRowHeight(28);
        tabelaPerm.setBackground(COR_SURFACE);
        tabelaPerm.setGridColor(COR_BORDER);
        tabelaPerm.setShowVerticalLines(false);
        tabelaPerm.setEnabled(false);

        JTableHeader thPerm = tabelaPerm.getTableHeader();
        thPerm.setFont(FONT_LABEL);
        thPerm.setBackground(COR_TEXT);
        thPerm.setForeground(Color.WHITE);

        tabelaPerm.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setFont(col == 0 ? FONT_BODY : FONT_SMALL);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                String val = value != null ? value.toString() : "";
                if (val.startsWith("✅")) setForeground(COR_GREEN);
                else if (val.startsWith("❌")) setForeground(COR_RED);
                else setForeground(COR_TEXT);
                return this;
            }
        });

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(tabelaPerm.getTableHeader(), BorderLayout.CENTER);

        JPanel tabelaWrap = new JPanel(new BorderLayout());
        tabelaWrap.add(tabelaPerm.getTableHeader(), BorderLayout.NORTH);
        tabelaWrap.add(tabelaPerm, BorderLayout.CENTER);
        card.add(tabelaWrap, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(COR_BG);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    // ── Formulário lateral ────────────────────────────────────────────────
    private JPanel construirFormulario() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 2, 0, 0, COR_BORDER),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(300, 0));

        lblTituloForm = new JLabel("Novo Usuário");
        lblTituloForm.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTituloForm.setForeground(COR_TEXT);
        lblTituloForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        // nomeUsuario
        JLabel lblNomeLbl = criarLabel("NOMEUSUARIO *");
        campoNome = criarInput("");

        // login
        JLabel lblLoginLbl = criarLabel("LOGIN * (sem espaços)");
        campoLogin = criarInput("");

        // senha
        JLabel lblSenhaLbl = criarLabel("SENHA *");
        campoSenha = new JPasswordField();
        estilizarPasswordField(campoSenha);

        // confirmar senha
        JLabel lblConfirmLbl = criarLabel("CONFIRMAR SENHA *");
        campoSenhaConfirm = new JPasswordField();
        estilizarPasswordField(campoSenhaConfirm);

        // nivelAcesso
        JLabel lblNivelLbl = criarLabel("NIVELACESSO *");
        cboNivelAcesso = new JComboBox<>(
            new String[]{"Consulta", "Operador", "Administrador"});
        cboNivelAcesso.setFont(FONT_INPUT);
        cboNivelAcesso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboNivelAcesso.addActionListener(e -> atualizarInfoPermissoes());

        // Info permissões
        lblPermissoes = new JLabel();
        lblPermissoes.setFont(FONT_SMALL);
        lblPermissoes.setForeground(COR_LABEL);
        lblPermissoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        atualizarInfoPermissoes();

        // Botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 8, 0));
        painelBotoes.setBackground(COR_SURFACE);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton btnCancelar = criarBotao("Cancelar", COR_SURFACE, COR_TEXT);
        btnCancelar.setBorder(new LineBorder(COR_TEXT, 2));
        btnCancelar.addActionListener(e -> fecharFormulario());

        JButton btnSalvar = criarBotao("Salvar", COR_TEXT, Color.WHITE);
        btnSalvar.addActionListener(e -> salvarUsuario());

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        // Montagem
        card.add(lblTituloForm);
        card.add(Box.createVerticalStrut(16));
        card.add(lblNomeLbl);       card.add(Box.createVerticalStrut(4));
        card.add(campoNome);        card.add(Box.createVerticalStrut(10));
        card.add(lblLoginLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoLogin);       card.add(Box.createVerticalStrut(10));
        card.add(lblSenhaLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoSenha);       card.add(Box.createVerticalStrut(10));
        card.add(lblConfirmLbl);    card.add(Box.createVerticalStrut(4));
        card.add(campoSenhaConfirm);card.add(Box.createVerticalStrut(10));
        card.add(lblNivelLbl);      card.add(Box.createVerticalStrut(4));
        card.add(cboNivelAcesso);   card.add(Box.createVerticalStrut(6));
        card.add(lblPermissoes);    card.add(Box.createVerticalStrut(16));
        card.add(painelBotoes);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados
    // ─────────────────────────────────────────────────────────────────────
    private void carregarUsuarios() {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String filtro = cboFiltroNivel != null
                        ? cboFiltroNivel.getSelectedItem().toString() : "Todos";

                    String sql = "SELECT id_usuario, nome_usuario, login, nivel_acesso " +
                        "FROM usuarios ";
                    if (!"Todos".equals(filtro))
                        sql += "WHERE nivel_acesso = '" + filtro + "' ";
                    sql += "ORDER BY nome_usuario";

                    Connection c = new Conexao().getConexao();
                    PreparedStatement st = c.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();

                    SwingUtilities.invokeLater(() -> modeloTabela.setRowCount(0));

                    while (rs.next()) {
                        String nivel = rs.getString("nivel_acesso");
                        // Calcula podeOperar() e isAdministrador() da classe Usuario
                        boolean podeOperar = "Administrador".equals(nivel)
                            || "Operador".equals(nivel);
                        boolean isAdmin = "Administrador".equals(nivel);

                        // Destaca o usuário logado
                        int id = rs.getInt("id_usuario");
                        String nome = rs.getString("nome_usuario") +
                            (id == usuarioLogado.getIdUsuario() ? "  ← você" : "");

                        Object[] row = {
                            id, nome,
                            rs.getString("login"),
                            nivel,
                            String.valueOf(podeOperar),
                            String.valueOf(isAdmin),
                            "Editar"
                        };
                        publish(row);
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao carregar usuários: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) modeloTabela.addRow(row);
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Formulário — abrir / fechar / salvar
    // ─────────────────────────────────────────────────────────────────────
    private void abrirFormularioNovo() {
        usuarioEmEdicao = null;
        lblTituloForm.setText("Novo Usuário");
        campoNome.setText("");
        campoNome.setForeground(COR_TEXT);
        campoLogin.setText("");
        campoLogin.setForeground(COR_TEXT);
        campoSenha.setText("");
        campoSenhaConfirm.setText("");
        cboNivelAcesso.setSelectedIndex(0);
        atualizarInfoPermissoes();
        painelForm.setVisible(true);
        revalidate(); repaint();
    }

    private void abrirFormularioEdicao(int idUsuario) {
        try {
            Connection c = new Conexao().getConexao();
            PreparedStatement st = c.prepareStatement(
                "SELECT * FROM usuarios WHERE id_usuario = ?");
            st.setInt(1, idUsuario);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                usuarioEmEdicao = new Usuario();
                usuarioEmEdicao.setIdUsuario(rs.getInt("id_usuario"));
                usuarioEmEdicao.setNomeUsuario(rs.getString("nome_usuario"));
                usuarioEmEdicao.setLogin(rs.getString("login"));
                usuarioEmEdicao.setSenha(rs.getString("senha"));
                usuarioEmEdicao.setNivelAcesso(rs.getString("nivel_acesso"));

                lblTituloForm.setText("Editar Usuário #" + idUsuario);
                campoNome.setText(usuarioEmEdicao.getNomeUsuario());
                campoNome.setForeground(COR_TEXT);
                campoLogin.setText(usuarioEmEdicao.getLogin());
                campoLogin.setForeground(COR_TEXT);
                campoSenha.setText("");
                campoSenhaConfirm.setText("");
                cboNivelAcesso.setSelectedItem(usuarioEmEdicao.getNivelAcesso());
                atualizarInfoPermissoes();
                painelForm.setVisible(true);
                revalidate(); repaint();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }
    }

    private void fecharFormulario() {
        usuarioEmEdicao = null;
        painelForm.setVisible(false);
        revalidate(); repaint();
    }

    private void atualizarInfoPermissoes() {
        if (cboNivelAcesso == null || lblPermissoes == null) return;
        String nivel = cboNivelAcesso.getSelectedItem().toString();
        boolean podeOperar = !"Consulta".equals(nivel);
        boolean isAdmin    = "Administrador".equals(nivel);
        lblPermissoes.setText("<html>" +
            "podeOperar() = <b>" + podeOperar + "</b>  · " +
            "isAdministrador() = <b>" + isAdmin + "</b></html>");
    }

    private void salvarUsuario() {
        String nome  = campoNome.getText().trim();
        String login = campoLogin.getText().trim();
        String senha = new String(campoSenha.getPassword()).trim();
        String senhaConf = new String(campoSenhaConfirm.getPassword()).trim();
        String nivel = cboNivelAcesso.getSelectedItem().toString();

        // Validações
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "nomeUsuario é obrigatório.",
                "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            campoNome.requestFocus(); return;
        }
        if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "login é obrigatório.",
                "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            campoLogin.requestFocus(); return;
        }
        if (login.contains(" ")) {
            JOptionPane.showMessageDialog(this, "O login não pode conter espaços.",
                "Login inválido", JOptionPane.WARNING_MESSAGE);
            campoLogin.requestFocus(); return;
        }

        // Senha obrigatória para novo usuário
        if (usuarioEmEdicao == null && senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe uma senha para o novo usuário.",
                "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            campoSenha.requestFocus(); return;
        }
        // Confirmar senha se foi digitada
        if (!senha.isEmpty() && !senha.equals(senhaConf)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.",
                "Senhas diferentes", JOptionPane.WARNING_MESSAGE);
            campoSenhaConfirm.requestFocus(); return;
        }

        try {
            Connection c = new Conexao().getConexao();

            if (usuarioEmEdicao == null) {
                // INSERT
                String sql = "INSERT INTO usuarios (nome_usuario, login, senha, nivel_acesso) " +
                    "VALUES (?, ?, ?, ?)";
                PreparedStatement st = c.prepareStatement(sql);
                st.setString(1, nome);
                st.setString(2, login);
                st.setString(3, senha);
                st.setString(4, nivel);
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Usuário cadastrado com sucesso!\n" +
                    "nivelAcesso: " + nivel + "\n" +
                    "podeOperar(): " + (!"Consulta".equals(nivel)),
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // UPDATE — senha só atualiza se foi digitada
                String sql;
                PreparedStatement st;
                if (!senha.isEmpty()) {
                    sql = "UPDATE usuarios SET nome_usuario=?, login=?, senha=?, " +
                        "nivel_acesso=? WHERE id_usuario=?";
                    st = c.prepareStatement(sql);
                    st.setString(1, nome);
                    st.setString(2, login);
                    st.setString(3, senha);
                    st.setString(4, nivel);
                    st.setInt(5, usuarioEmEdicao.getIdUsuario());
                } else {
                    // Não altera a senha
                    sql = "UPDATE usuarios SET nome_usuario=?, login=?, nivel_acesso=? " +
                        "WHERE id_usuario=?";
                    st = c.prepareStatement(sql);
                    st.setString(1, nome);
                    st.setString(2, login);
                    st.setString(3, nivel);
                    st.setInt(4, usuarioEmEdicao.getIdUsuario());
                }
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Usuário atualizado com sucesso!" +
                    (senha.isEmpty() ? "\nSenha não alterada." : "\nSenha atualizada."),
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            fecharFormulario();
            carregarUsuarios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar usuário:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.out.println("Erro SQL usuário: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────
    private void estilizarPasswordField(JPasswordField campo) {
        campo.setFont(FONT_INPUT);
        campo.setForeground(COR_TEXT);
        campo.setBackground(COR_SURFACE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campo.setEchoChar('•');
    }

    private JLabel criarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COR_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField criarInput(String placeholder) {
        JTextField campo = new JTextField();
        campo.setFont(FONT_INPUT);
        campo.setForeground(COR_TEXT);
        campo.setBackground(COR_SURFACE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return campo;
    }

    private JButton criarBotao(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : COR_BORDER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}