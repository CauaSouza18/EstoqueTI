package view;

import dao.Conexao;
import data.NotaFiscal;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Notas Fiscais — EstoqueTI
 * Listagem e cadastro de notas fiscais vinculadas a fornecedores.
 * Usa: nota_fiscal, fornecedores
 *
 * Campos de NotaFiscal:
 *   idNf, numeroNf (VARCHAR 50), dataEmissao (DATE),
 *   valorTotal (DECIMAL 10,2), idFornecedor
 *
 * Método: getDataEmissaoFormatada() → dd/MM/yyyy
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaNotasFiscais extends JPanel {

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
    private static final Color COR_YELLOW    = new Color(0xC8820A);
    private static final Color COR_BLUE      = new Color(0x2255AA);
    private static final Color COR_BLUE_BG   = new Color(0xDDEEFF);

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
    private NotaFiscal notaEmEdicao = null;

    // ── Tabela ────────────────────────────────────────────────────────────
    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JTextField        campoBusca;

    // ── Formulário ────────────────────────────────────────────────────────
    private JTextField        campoNumeroNf;
    private JTextField        campoDataEmissao;
    private JTextField        campoValorTotal;
    private JComboBox<String> cboFornecedor;
    private int[]             idsFornecedor;
    private JLabel            lblTituloForm;
    private JPanel            painelForm;

    // ── KPIs ──────────────────────────────────────────────────────────────
    private JLabel lblKpiTotal;
    private JLabel lblKpiMes;
    private JLabel lblKpiValor;

    public TelaNotasFiscais(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.conn = new Conexao().getConexao();
        setLayout(new BorderLayout());
        setBackground(COR_BG);
        construirInterface();
        carregarFornecedores();
        carregarNotas();
        carregarKpis();
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
        esquerda.add(construirKpiRow());
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

        JLabel lblTitulo = new JLabel("Notas Fiscais");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        JLabel lblSub = new JLabel(
            "NotaFiscal.java · numeroNf, dataEmissao (LocalDate), valorTotal (BigDecimal), idFornecedor");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        dir.setBackground(COR_BG);
        if (usuarioLogado.podeOperar()) {
            JButton btnNovo = criarBotao("+ Nova Nota Fiscal", COR_TEXT, Color.WHITE);
            btnNovo.addActionListener(e -> abrirFormularioNovo());
            dir.add(btnNovo);
        }

        header.add(esq, BorderLayout.WEST);
        header.add(dir, BorderLayout.EAST);
        return header;
    }

    // ── KPI Row ───────────────────────────────────────────────────────────
    private JPanel construirKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setBackground(COR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblKpiTotal = new JLabel("...");
        lblKpiMes   = new JLabel("...");
        lblKpiValor = new JLabel("...");

        row.add(construirKpiCard("TOTAL DE NOTAS",      lblKpiTotal, COR_BLUE));
        row.add(construirKpiCard("NOTAS ESTE MÊS",      lblKpiMes,   COR_YELLOW));
        row.add(construirKpiCard("VALOR TOTAL (MÊS)",   lblKpiValor, COR_GREEN));

        return row;
    }

    private JPanel construirKpiCard(String label, JLabel lblValor, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRect(0, 0, getWidth(), 3);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_LABEL);
        lblLabel.setForeground(COR_LABEL);

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblValor.setForeground(COR_TEXT);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(lblValor);
        return card;
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
        searchWrap.setPreferredSize(new Dimension(320, 36));

        JLabel lblIco = new JLabel("🔍");
        lblIco.setFont(FONT_BODY);

        campoBusca = new JTextField();
        campoBusca.setFont(FONT_BODY);
        campoBusca.setForeground(COR_LABEL);
        campoBusca.setBorder(null);
        campoBusca.setBackground(COR_SURFACE);
        campoBusca.setText("Buscar por número da NF, fornecedor...");
        campoBusca.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (campoBusca.getText().startsWith("Buscar")) {
                    campoBusca.setText("");
                    campoBusca.setForeground(COR_TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (campoBusca.getText().isEmpty()) {
                    campoBusca.setText("Buscar por número da NF, fornecedor...");
                    campoBusca.setForeground(COR_LABEL);
                }
            }
        });
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { carregarNotas(); }
        });

        searchWrap.add(lblIco, BorderLayout.WEST);
        searchWrap.add(campoBusca, BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("↻ Atualizar");
        btnAtualizar.setFont(FONT_SMALL);
        btnAtualizar.setForeground(COR_TEXT);
        btnAtualizar.setBackground(COR_SURFACE);
        btnAtualizar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_TEXT, 2),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btnAtualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> { carregarNotas(); carregarKpis(); });

        barra.add(searchWrap);
        barra.add(btnAtualizar);
        return barra;
    }

    // ── Tabela ────────────────────────────────────────────────────────────
    private JPanel construirTabela() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(new LineBorder(COR_BORDER, 2));

        String[] colunas = {
            "idNf", "numeroNf", "dataEmissao (dd/MM/yyyy)",
            "valorTotal", "nomeFornecedor", ""
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
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(60);

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
                // numeroNf — badge azul
                else if (col == 1) {
                    setBackground(sel ? COR_ACCENT : COR_BLUE_BG);
                    setForeground(COR_BLUE);
                    setFont(new Font("Monospaced", Font.BOLD, 11));
                }
                // data — monoespaçado
                else if (col == 2) {
                    setFont(FONT_SMALL);
                    setForeground(COR_MUTED);
                }
                // valorTotal — verde bold
                else if (col == 3) {
                    setFont(FONT_BOLD);
                    setForeground(COR_GREEN);
                }
                // botão ver
                else if (col == 5) {
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
        scrollTabela.setPreferredSize(new Dimension(0, 400));
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
        card.setPreferredSize(new Dimension(300, 0));

        lblTituloForm = new JLabel("Nova Nota Fiscal");
        lblTituloForm.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTituloForm.setForeground(COR_TEXT);
        lblTituloForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        // numeroNf
        JLabel lblNumeroLbl = criarLabel("NUMERONF * (VARCHAR 50)");
        campoNumeroNf = criarInput("Ex: NF-00001");

        // dataEmissao — LocalDate
        JLabel lblDataLbl = criarLabel("DATAEMISSAO * (LocalDate → DATE)");
        campoDataEmissao = criarInput(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        campoDataEmissao.setForeground(COR_TEXT);

        JLabel lblDataHint = new JLabel("Formato: dd/MM/yyyy · getDataEmissaoFormatada()");
        lblDataHint.setFont(FONT_SMALL);
        lblDataHint.setForeground(COR_LABEL);
        lblDataHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // valorTotal — BigDecimal
        JLabel lblValorLbl = criarLabel("VALORTOTAL * (BigDecimal DECIMAL 10,2)");
        campoValorTotal = criarInput("0,00");

        // idFornecedor
        JLabel lblFornLbl = criarLabel("IDFORNECEDOR * (nomeFornecedor)");
        cboFornecedor = new JComboBox<>();
        cboFornecedor.setFont(FONT_INPUT);
        cboFornecedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 8, 0));
        painelBotoes.setBackground(COR_SURFACE);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton btnCancelar = criarBotao("Cancelar", COR_SURFACE, COR_TEXT);
        btnCancelar.setBorder(new LineBorder(COR_TEXT, 2));
        btnCancelar.addActionListener(e -> fecharFormulario());

        JButton btnSalvar = criarBotao("Salvar NF", COR_TEXT, Color.WHITE);
        btnSalvar.addActionListener(e -> salvarNotaFiscal());

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        // Montagem
        card.add(lblTituloForm);
        card.add(Box.createVerticalStrut(16));
        card.add(lblNumeroLbl);     card.add(Box.createVerticalStrut(4));
        card.add(campoNumeroNf);    card.add(Box.createVerticalStrut(10));
        card.add(lblDataLbl);       card.add(Box.createVerticalStrut(4));
        card.add(campoDataEmissao); card.add(Box.createVerticalStrut(2));
        card.add(lblDataHint);      card.add(Box.createVerticalStrut(10));
        card.add(lblValorLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoValorTotal);  card.add(Box.createVerticalStrut(10));
        card.add(lblFornLbl);       card.add(Box.createVerticalStrut(4));
        card.add(cboFornecedor);    card.add(Box.createVerticalStrut(16));
        card.add(painelBotoes);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados
    // ─────────────────────────────────────────────────────────────────────
    private void carregarFornecedores() {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT id_fornecedor, nome_fornecedor FROM fornecedores " +
                "WHERE status = 'Ativo' ORDER BY nome_fornecedor");
            ResultSet rs = st.executeQuery();
            cboFornecedor.removeAllItems();
            cboFornecedor.addItem("Selecionar fornecedor...");
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id_fornecedor"));
                cboFornecedor.addItem(
                    "[" + rs.getInt("id_fornecedor") + "] " +
                    rs.getString("nome_fornecedor"));
            }
            idsFornecedor = ids.stream().mapToInt(i -> i).toArray();
        } catch (SQLException e) {
            System.out.println("Erro ao carregar fornecedores: " + e.getMessage());
        }
    }

    private void carregarNotas() {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String busca = campoBusca != null
                        ? campoBusca.getText().trim() : "";
                    if (busca.startsWith("Buscar")) busca = "";

                    String sql =
                        "SELECT n.id_nf, n.numero_nf, n.data_emissao, " +
                        "n.valor_total, f.nome_fornecedor " +
                        "FROM nota_fiscal n " +
                        "JOIN fornecedores f ON n.id_fornecedor = f.id_fornecedor ";

                    if (!busca.isEmpty())
                        sql += "WHERE n.numero_nf LIKE '%" + busca + "%' " +
                               "OR f.nome_fornecedor LIKE '%" + busca + "%' ";
                    sql += "ORDER BY n.data_emissao DESC";

                    PreparedStatement st = conn.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();

                    SwingUtilities.invokeLater(() -> modeloTabela.setRowCount(0));

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    while (rs.next()) {
                        // getDataEmissaoFormatada() da classe NotaFiscal
                        Date dataSQL = rs.getDate("data_emissao");
                        String dataFormatada = dataSQL != null
                            ? dataSQL.toLocalDate().format(fmt) : "—";

                        BigDecimal valor = rs.getBigDecimal("valor_total");
                        String valorStr = valor != null
                            ? "R$ " + String.format("%,.2f", valor) : "—";

                        Object[] row = {
                            rs.getInt("id_nf"),
                            rs.getString("numero_nf"),
                            dataFormatada,
                            valorStr,
                            rs.getString("nome_fornecedor"),
                            "Ver"
                        };
                        publish(row);
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao carregar notas: " + e.getMessage());
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

    private void carregarKpis() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int total = 0, mes = 0;
            BigDecimal valorMes = BigDecimal.ZERO;

            @Override
            protected Void doInBackground() {
                try {
                    // Total de notas
                    ResultSet rs1 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM nota_fiscal").executeQuery();
                    if (rs1.next()) total = rs1.getInt(1);

                    // Notas este mês
                    ResultSet rs2 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM nota_fiscal " +
                        "WHERE MONTH(data_emissao) = MONTH(CURDATE()) " +
                        "AND YEAR(data_emissao) = YEAR(CURDATE())").executeQuery();
                    if (rs2.next()) mes = rs2.getInt(1);

                    // Valor total este mês
                    ResultSet rs3 = conn.prepareStatement(
                        "SELECT SUM(valor_total) FROM nota_fiscal " +
                        "WHERE MONTH(data_emissao) = MONTH(CURDATE()) " +
                        "AND YEAR(data_emissao) = YEAR(CURDATE())").executeQuery();
                    if (rs3.next() && rs3.getBigDecimal(1) != null)
                        valorMes = rs3.getBigDecimal(1);

                } catch (SQLException e) {
                    System.out.println("Erro KPIs NF: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                lblKpiTotal.setText(String.valueOf(total));
                lblKpiMes.setText(String.valueOf(mes));
                lblKpiValor.setText("R$ " + String.format("%,.2f", valorMes));
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Formulário — abrir / fechar / salvar
    // ─────────────────────────────────────────────────────────────────────
    private void abrirFormularioNovo() {
        notaEmEdicao = null;
        lblTituloForm.setText("Nova Nota Fiscal");
        campoNumeroNf.setText("");
        campoNumeroNf.setForeground(COR_TEXT);
        campoDataEmissao.setText(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        campoDataEmissao.setForeground(COR_TEXT);
        campoValorTotal.setText("0,00");
        campoValorTotal.setForeground(COR_TEXT);
        cboFornecedor.setSelectedIndex(0);
        painelForm.setVisible(true);
        revalidate(); repaint();
    }

    private void abrirFormularioEdicao(int idNf) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM nota_fiscal WHERE id_nf = ?");
            st.setInt(1, idNf);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                notaEmEdicao = new NotaFiscal();
                notaEmEdicao.setIdNf(rs.getInt("id_nf"));
                notaEmEdicao.setNumeroNf(rs.getString("numero_nf"));
                notaEmEdicao.setIdFornecedor(rs.getInt("id_fornecedor"));
                notaEmEdicao.setValorTotal(rs.getBigDecimal("valor_total"));

                Date dataSQL = rs.getDate("data_emissao");
                if (dataSQL != null) {
                    notaEmEdicao.setDataEmissao(dataSQL.toLocalDate());
                }

                lblTituloForm.setText("Editar NF #" + idNf);

                campoNumeroNf.setText(notaEmEdicao.getNumeroNf());
                campoNumeroNf.setForeground(COR_TEXT);

                // getDataEmissaoFormatada() da classe NotaFiscal
                campoDataEmissao.setText(notaEmEdicao.getDataEmissaoFormatada());
                campoDataEmissao.setForeground(COR_TEXT);

                campoValorTotal.setText(notaEmEdicao.getValorTotal() != null
                    ? notaEmEdicao.getValorTotal().toString() : "0.00");
                campoValorTotal.setForeground(COR_TEXT);

                // Seleciona fornecedor no combo
                if (idsFornecedor != null) {
                    for (int i = 0; i < idsFornecedor.length; i++) {
                        if (idsFornecedor[i] == notaEmEdicao.getIdFornecedor()) {
                            cboFornecedor.setSelectedIndex(i + 1);
                            break;
                        }
                    }
                }

                painelForm.setVisible(true);
                revalidate(); repaint();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar nota: " + e.getMessage());
        }
    }

    private void fecharFormulario() {
        notaEmEdicao = null;
        painelForm.setVisible(false);
        revalidate(); repaint();
    }

    private void salvarNotaFiscal() {
        // Validações
        String numeroNf = campoNumeroNf.getText().trim();
        if (numeroNf.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "numeroNf é obrigatório.", "Campo obrigatório",
                JOptionPane.WARNING_MESSAGE);
            campoNumeroNf.requestFocus();
            return;
        }

        // Parse da data — dd/MM/yyyy → LocalDate → DATE
        LocalDate dataEmissao;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dataEmissao = LocalDate.parse(campoDataEmissao.getText().trim(), fmt);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Data inválida. Use o formato dd/MM/yyyy.",
                "Data inválida", JOptionPane.WARNING_MESSAGE);
            campoDataEmissao.requestFocus();
            return;
        }

        // Parse do valor — BigDecimal
        BigDecimal valorTotal;
        try {
            valorTotal = new BigDecimal(
                campoValorTotal.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Valor inválido. Use o formato: 0.00",
                "Valor inválido", JOptionPane.WARNING_MESSAGE);
            campoValorTotal.requestFocus();
            return;
        }

        int idxForn = cboFornecedor.getSelectedIndex() - 1;
        if (idxForn < 0 || idsFornecedor == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione um fornecedor.", "Campo obrigatório",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idFornecedor = idsFornecedor[idxForn];

        try {
            if (notaEmEdicao == null) {
                // INSERT
                String sql = "INSERT INTO nota_fiscal " +
                    "(numero_nf, data_emissao, valor_total, id_fornecedor) " +
                    "VALUES (?, ?, ?, ?)";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, numeroNf);
                st.setDate(2, Date.valueOf(dataEmissao));
                st.setBigDecimal(3, valorTotal);
                st.setInt(4, idFornecedor);
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Nota Fiscal cadastrada com sucesso!\n" +
                    "dataEmissao: " + dataEmissao.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // UPDATE
                String sql = "UPDATE nota_fiscal SET " +
                    "numero_nf=?, data_emissao=?, valor_total=?, id_fornecedor=? " +
                    "WHERE id_nf=?";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, numeroNf);
                st.setDate(2, Date.valueOf(dataEmissao));
                st.setBigDecimal(3, valorTotal);
                st.setInt(4, idFornecedor);
                st.setInt(5, notaEmEdicao.getIdNf());
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Nota Fiscal atualizada com sucesso!",
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            fecharFormulario();
            carregarNotas();
            carregarKpis();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar nota fiscal:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.out.println("Erro SQL nota fiscal: " + e.getMessage());
        }
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