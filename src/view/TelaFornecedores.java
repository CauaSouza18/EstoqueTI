package view;

import dao.Conexao;
import data.Fornecedor;
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
 * Tela de Fornecedores — EstoqueTI
 * Listagem, cadastro e edição de fornecedores.
 * Usa: fornecedores
 *
 * Campos de Fornecedor:
 *   idFornecedor, nomeFornecedor, endereco,
 *   telefone, email, cnpj (CHAR 18), status ENUM
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaFornecedores extends JPanel {

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
    private Connection conn;
    private Fornecedor fornecedorEmEdicao = null;

    // ── Tabela ────────────────────────────────────────────────────────────
    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JTextField        campoBusca;
    private JComboBox<String> cboFiltroStatus;

    // ── Formulário ────────────────────────────────────────────────────────
    private JTextField        campoNome;
    private JTextField        campoEndereco;
    private JTextField        campoCnpj;
    private JTextField        campoTelefone;
    private JTextField        campoEmail;
    private JComboBox<String> cboStatus;
    private JLabel            lblTituloForm;
    private JPanel            painelForm;

    public TelaFornecedores(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.conn = new Conexao().getConexao();
        setLayout(new BorderLayout());
        setBackground(COR_BG);
        construirInterface();
        carregarFornecedores();
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
        esquerda.add(Box.createVerticalStrut(14));
        esquerda.add(construirBarraBusca());
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(construirTabela());

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

        JLabel lblTitulo = new JLabel("Fornecedores");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        JLabel lblSub = new JLabel(
            "Fornecedor.java · nomeFornecedor, cnpj, endereco, telefone, email, status");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        dir.setBackground(COR_BG);
        if (usuarioLogado.podeOperar()) {
            JButton btnNovo = criarBotao("+ Novo Fornecedor", COR_TEXT, Color.WHITE);
            btnNovo.addActionListener(e -> abrirFormularioNovo());
            dir.add(btnNovo);
        }

        header.add(esq, BorderLayout.WEST);
        header.add(dir, BorderLayout.EAST);
        return header;
    }

    // ── Barra de busca ────────────────────────────────────────────────────
    private JPanel construirBarraBusca() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBackground(COR_BG);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(COR_SURFACE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 2),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchWrap.setPreferredSize(new Dimension(300, 36));

        JLabel lblIco = new JLabel("🔍");
        lblIco.setFont(FONT_BODY);

        campoBusca = new JTextField();
        campoBusca.setFont(FONT_BODY);
        campoBusca.setForeground(COR_LABEL);
        campoBusca.setBorder(null);
        campoBusca.setBackground(COR_SURFACE);
        campoBusca.setText("Buscar por nome, CNPJ...");
        campoBusca.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (campoBusca.getText().equals("Buscar por nome, CNPJ...")) {
                    campoBusca.setText("");
                    campoBusca.setForeground(COR_TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (campoBusca.getText().isEmpty()) {
                    campoBusca.setText("Buscar por nome, CNPJ...");
                    campoBusca.setForeground(COR_LABEL);
                }
            }
        });
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filtrarTabela(); }
        });

        searchWrap.add(lblIco, BorderLayout.WEST);
        searchWrap.add(campoBusca, BorderLayout.CENTER);

        cboFiltroStatus = new JComboBox<>(new String[]{"Todos", "Ativo", "Inativo"});
        cboFiltroStatus.setFont(FONT_BODY);
        cboFiltroStatus.setPreferredSize(new Dimension(120, 36));
        cboFiltroStatus.addActionListener(e -> filtrarTabela());

        barra.add(searchWrap);
        barra.add(cboFiltroStatus);
        return barra;
    }

    // ── Tabela ────────────────────────────────────────────────────────────
    private JPanel construirTabela() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(new LineBorder(COR_BORDER, 2));

        String[] colunas = {
            "id", "nomeFornecedor", "cnpj",
            "telefone", "email", "status", ""
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
        tabela.getColumnModel().getColumn(0).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(70);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(60);

        // Renderer
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
                    setFont(FONT_SMALL);
                    setForeground(COR_LABEL);
                }
                // cnpj — monoespaçado
                else if (col == 2) {
                    setFont(FONT_SMALL);
                    setForeground(COR_MUTED);
                }
                // telefone / email
                else if (col == 3 || col == 4) {
                    setFont(FONT_SMALL);
                    setForeground(COR_MUTED);
                }
                // status badge
                else if (col == 5) {
                    if ("Ativo".equals(val)) {
                        setBackground(sel ? COR_ACCENT : COR_GREEN_BG);
                        setForeground(COR_GREEN);
                        setFont(new Font("Monospaced", Font.BOLD, 10));
                    } else {
                        setBackground(sel ? COR_ACCENT : COR_TAG);
                        setForeground(COR_LABEL);
                        setFont(new Font("Monospaced", Font.BOLD, 10));
                    }
                }
                // botão editar
                else if (col == 6) {
                    setForeground(COR_TEXT);
                    setFont(FONT_SMALL);
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                return this;
            }
        });

        // Clique duplo para editar
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && usuarioLogado.podeOperar()) {
                    int row = tabela.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) modeloTabela.getValueAt(row, 0);
                        abrirFormularioEdicao(id);
                    }
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setBorder(null);
        scrollTabela.setPreferredSize(new Dimension(0, 420));
        card.add(scrollTabela, BorderLayout.CENTER);

        JLabel lblDica = new JLabel(usuarioLogado.podeOperar()
            ? "  Dica: clique duplo em uma linha para editar"
            : "  Somente visualização — nivelAcesso: " + usuarioLogado.getNivelAcesso());
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

    // ── Formulário lateral ────────────────────────────────────────────────
    private JPanel construirFormulario() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 2, 0, 0, COR_BORDER),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(320, 0));

        lblTituloForm = new JLabel("Novo Fornecedor");
        lblTituloForm.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTituloForm.setForeground(COR_TEXT);
        lblTituloForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        // nomeFornecedor
        JLabel lblNomeLbl = criarLabel("NOMEFORNECEDOR *");
        campoNome = criarInput("");

        // cnpj — CHAR(18) no banco
        JLabel lblCnpjLbl = criarLabel("CNPJ (CHAR 18)");
        campoCnpj = criarInput("00.000.000/0001-00");

        // endereco
        JLabel lblEndLbl = criarLabel("ENDERECO");
        campoEndereco = criarInput("Rua, número — Cidade, UF");

        // telefone
        JLabel lblTelLbl = criarLabel("TELEFONE");
        campoTelefone = criarInput("(00) 00000-0000");

        // email
        JLabel lblEmailLbl = criarLabel("EMAIL");
        campoEmail = criarInput("contato@fornecedor.com");

        // status
        JLabel lblStatusLbl = criarLabel("STATUS");
        cboStatus = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        cboStatus.setFont(FONT_INPUT);
        cboStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 8, 0));
        painelBotoes.setBackground(COR_SURFACE);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton btnCancelar = criarBotao("Cancelar", COR_SURFACE, COR_TEXT);
        btnCancelar.setBorder(new LineBorder(COR_TEXT, 2));
        btnCancelar.addActionListener(e -> fecharFormulario());

        JButton btnSalvar = criarBotao("Salvar", COR_TEXT, Color.WHITE);
        btnSalvar.addActionListener(e -> salvarFornecedor());

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        // Montagem
        card.add(lblTituloForm);
        card.add(Box.createVerticalStrut(16));
        card.add(lblNomeLbl);     card.add(Box.createVerticalStrut(4));
        card.add(campoNome);      card.add(Box.createVerticalStrut(10));
        card.add(lblCnpjLbl);     card.add(Box.createVerticalStrut(4));
        card.add(campoCnpj);      card.add(Box.createVerticalStrut(10));
        card.add(lblEndLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoEndereco);  card.add(Box.createVerticalStrut(10));
        card.add(lblTelLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoTelefone);  card.add(Box.createVerticalStrut(10));
        card.add(lblEmailLbl);    card.add(Box.createVerticalStrut(4));
        card.add(campoEmail);     card.add(Box.createVerticalStrut(10));
        card.add(lblStatusLbl);   card.add(Box.createVerticalStrut(4));
        card.add(cboStatus);      card.add(Box.createVerticalStrut(16));
        card.add(painelBotoes);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados
    // ─────────────────────────────────────────────────────────────────────
    private void carregarFornecedores() {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String filtroStatus = cboFiltroStatus != null
                        ? cboFiltroStatus.getSelectedItem().toString() : "Todos";
                    String busca = campoBusca != null
                        ? campoBusca.getText().trim() : "";
                    if (busca.equals("Buscar por nome, CNPJ...")) busca = "";

                    String sql = "SELECT id_fornecedor, nome_fornecedor, cnpj, " +
                        "telefone, email, status FROM fornecedores WHERE 1=1 ";

                    if (!"Todos".equals(filtroStatus))
                        sql += "AND status = '" + filtroStatus + "' ";
                    if (!busca.isEmpty())
                        sql += "AND (nome_fornecedor LIKE '%" + busca + "%' " +
                               "OR cnpj LIKE '%" + busca + "%') ";
                    sql += "ORDER BY nome_fornecedor";

                    PreparedStatement st = conn.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();

                    SwingUtilities.invokeLater(() -> modeloTabela.setRowCount(0));

                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("id_fornecedor"),
                            rs.getString("nome_fornecedor"),
                            rs.getString("cnpj"),
                            rs.getString("telefone"),
                            rs.getString("email"),
                            rs.getString("status"),
                            "Editar"
                        };
                        publish(row);
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao carregar fornecedores: " + e.getMessage());
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

    private void filtrarTabela() {
        modeloTabela.setRowCount(0);
        carregarFornecedores();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Formulário — abrir / fechar / salvar
    // ─────────────────────────────────────────────────────────────────────
    private void abrirFormularioNovo() {
        fornecedorEmEdicao = null;
        lblTituloForm.setText("Novo Fornecedor");
        limparFormulario();
        painelForm.setVisible(true);
        revalidate(); repaint();
    }

    private void abrirFormularioEdicao(int idFornecedor) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM fornecedores WHERE id_fornecedor = ?");
            st.setInt(1, idFornecedor);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                fornecedorEmEdicao = new Fornecedor();
                fornecedorEmEdicao.setIdFornecedor(rs.getInt("id_fornecedor"));
                fornecedorEmEdicao.setNomeFornecedor(rs.getString("nome_fornecedor"));
                fornecedorEmEdicao.setEndereco(rs.getString("endereco"));
                fornecedorEmEdicao.setTelefone(rs.getString("telefone"));
                fornecedorEmEdicao.setEmail(rs.getString("email"));
                fornecedorEmEdicao.setCnpj(rs.getString("cnpj"));
                fornecedorEmEdicao.setStatus(rs.getString("status"));

                lblTituloForm.setText("Editar Fornecedor #" + idFornecedor);
                preencherFormulario(fornecedorEmEdicao);
                painelForm.setVisible(true);
                revalidate(); repaint();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar fornecedor: " + e.getMessage());
        }
    }

    private void preencherFormulario(Fornecedor f) {
        setText(campoNome,      f.getNomeFornecedor());
        setText(campoCnpj,      f.getCnpj());
        setText(campoEndereco,  f.getEndereco());
        setText(campoTelefone,  f.getTelefone());
        setText(campoEmail,     f.getEmail());
        cboStatus.setSelectedItem(f.getStatus());
    }

    private void setText(JTextField campo, String valor) {
        campo.setText(valor != null ? valor : "");
        campo.setForeground(COR_TEXT);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoCnpj.setText("00.000.000/0001-00");
        campoCnpj.setForeground(COR_LABEL);
        campoEndereco.setText("Rua, número — Cidade, UF");
        campoEndereco.setForeground(COR_LABEL);
        campoTelefone.setText("(00) 00000-0000");
        campoTelefone.setForeground(COR_LABEL);
        campoEmail.setText("contato@fornecedor.com");
        campoEmail.setForeground(COR_LABEL);
        cboStatus.setSelectedIndex(0);
    }

    private void fecharFormulario() {
        fornecedorEmEdicao = null;
        painelForm.setVisible(false);
        revalidate(); repaint();
    }

    private void salvarFornecedor() {
        // Validação
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "nomeFornecedor é obrigatório.",
                "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            campoNome.requestFocus();
            return;
        }

        String cnpj      = limparPlaceholder(campoCnpj,      "00.000.000/0001-00");
        String endereco  = limparPlaceholder(campoEndereco,  "Rua, número — Cidade, UF");
        String telefone  = limparPlaceholder(campoTelefone,  "(00) 00000-0000");
        String email     = limparPlaceholder(campoEmail,     "contato@fornecedor.com");
        String status    = cboStatus.getSelectedItem().toString();

        try {
            if (fornecedorEmEdicao == null) {
                // INSERT
                String sql = "INSERT INTO fornecedores " +
                    "(nome_fornecedor, endereco, telefone, email, cnpj, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, nome);
                st.setString(2, endereco.isEmpty()  ? null : endereco);
                st.setString(3, telefone.isEmpty()  ? null : telefone);
                st.setString(4, email.isEmpty()     ? null : email);
                st.setString(5, cnpj.isEmpty()      ? null : cnpj);
                st.setString(6, status);
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Fornecedor cadastrado com sucesso!",
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // UPDATE
                String sql = "UPDATE fornecedores SET " +
                    "nome_fornecedor=?, endereco=?, telefone=?, " +
                    "email=?, cnpj=?, status=? " +
                    "WHERE id_fornecedor=?";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, nome);
                st.setString(2, endereco.isEmpty()  ? null : endereco);
                st.setString(3, telefone.isEmpty()  ? null : telefone);
                st.setString(4, email.isEmpty()     ? null : email);
                st.setString(5, cnpj.isEmpty()      ? null : cnpj);
                st.setString(6, status);
                st.setInt(7, fornecedorEmEdicao.getIdFornecedor());
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Fornecedor atualizado com sucesso!",
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            fecharFormulario();
            carregarFornecedores();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar fornecedor:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.out.println("Erro SQL fornecedor: " + e.getMessage());
        }
    }

    /** Retorna string vazia se o campo ainda está com o placeholder */
    private String limparPlaceholder(JTextField campo, String placeholder) {
        String val = campo.getText().trim();
        return val.equals(placeholder) ? "" : val;
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

    private JTextField criarInput(String placeholder) {
        JTextField campo = new JTextField();
        campo.setFont(FONT_INPUT);
        campo.setBackground(COR_SURFACE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        if (!placeholder.isEmpty()) {
            campo.setText(placeholder);
            campo.setForeground(COR_LABEL);
            campo.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (campo.getText().equals(placeholder)) {
                        campo.setText("");
                        campo.setForeground(COR_TEXT);
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (campo.getText().isEmpty()) {
                        campo.setText(placeholder);
                        campo.setForeground(COR_LABEL);
                    }
                }
            });
        } else {
            campo.setForeground(COR_TEXT);
        }
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