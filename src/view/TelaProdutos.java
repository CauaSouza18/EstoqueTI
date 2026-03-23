package view;

import dao.Conexao;
import dao.ProdutoDAO;
import data.Produto;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * Tela de Produtos — EstoqueTI
 * Listagem, cadastro e edição de produtos.
 * Usa: produtos, categoria
 *
 * Campos de Produto:
 *   idProduto, nomeProduto, descricao, unidadeMedida,
 *   idCategoria, marca, precoCusto (DECIMAL 10,2),
 *   precoVenda (DECIMAL 10,2), quantidade, status ENUM
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaProdutos extends JPanel {

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
    private static final Color COR_YELLOW    = new Color(0xC8820A);
    private static final Color COR_YELLOW_BG = new Color(0xFEF3DC);

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
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private Produto produtoEmEdicao = null; // null = novo produto

    // ── Tabela ────────────────────────────────────────────────────────────
    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JTextField        campoBusca;
    private JComboBox<String> cboFiltroStatus;

    // ── Formulário ────────────────────────────────────────────────────────
    private JTextField   campoNome;
    private JTextArea    campoDescricao;
    private JTextField   campoMarca;
    private JComboBox<String> cboCategorias;
    private int[]        idsCategoria;
    private JComboBox<String> cboUnidade;
    private JSpinner     spinQuantidade;
    private JTextField   campoPrecoCusto;
    private JTextField   campoPrecoVenda;
    private JComboBox<String> cboStatus;
    private JLabel       lblMargem;
    private JLabel       lblTituloForm;
    private JButton      btnSalvar;
    private JButton      btnCancelar;
    private JPanel       painelForm;

    public TelaProdutos(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.conn = new Conexao().getConexao();
        setLayout(new BorderLayout());
        setBackground(COR_BG);
        construirInterface();
        carregarCategorias();
        carregarProdutos();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void construirInterface() {
        JPanel interno = new JPanel(new BorderLayout());
        interno.setBackground(COR_BG);
        interno.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Cabeçalho + busca + tabela à esquerda/centro
        JPanel esquerda = new JPanel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setBackground(COR_BG);

        esquerda.add(construirHeader());
        esquerda.add(Box.createVerticalStrut(14));
        esquerda.add(construirBarraBusca());
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(construirTabela());

        // Formulário lateral
        painelForm = construirFormulario();
        painelForm.setVisible(false); // escondido até clicar em Novo/Editar

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

        JLabel lblTitulo = new JLabel("Produtos");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        JLabel lblSub = new JLabel("Produto.java · nome_produto, marca, preco_custo, preco_venda, quantidade, status");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);

        // Botão novo produto — só quem podeOperar()
        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        dir.setBackground(COR_BG);
        if (usuarioLogado.podeOperar()) {
            JButton btnNovo = criarBotao("+ Novo Produto", COR_TEXT, Color.WHITE);
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

        // Campo de busca
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
        campoBusca.setText("Buscar por nome, marca...");
        campoBusca.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (campoBusca.getText().equals("Buscar por nome, marca...")) {
                    campoBusca.setText("");
                    campoBusca.setForeground(COR_TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (campoBusca.getText().isEmpty()) {
                    campoBusca.setText("Buscar por nome, marca...");
                    campoBusca.setForeground(COR_LABEL);
                }
            }
        });
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filtrarTabela(); }
        });

        searchWrap.add(lblIco, BorderLayout.WEST);
        searchWrap.add(campoBusca, BorderLayout.CENTER);

        // Filtro status
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
            "id", "nomeProduto", "marca", "categoria",
            "unidade", "qtd", "precoCusto", "precoVenda",
            "margem %", "status", ""
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
        tabela.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(55);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(70);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(70);
        tabela.getColumnModel().getColumn(10).setPreferredWidth(70);

        // Renderer com badges
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);

                String val = value != null ? value.toString() : "";

                // id
                if (col == 0) { setFont(FONT_SMALL); setForeground(COR_LABEL); }
                // qtd — vermelho se 0, amarelo se <= 5
                else if (col == 5) {
                    try {
                        int q = Integer.parseInt(val);
                        setForeground(q == 0 ? COR_RED : q <= 5 ? COR_YELLOW : COR_TEXT);
                        setFont(FONT_BOLD);
                    } catch (NumberFormatException ignored) {}
                }
                // margem — verde
                else if (col == 8) {
                    setForeground(COR_GREEN);
                    setFont(FONT_SMALL);
                }
                // status badge
                else if (col == 9) {
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
                // botão editar (col 10)
                else if (col == 10) {
                    setForeground(COR_TEXT);
                    setFont(FONT_SMALL);
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                else { setForeground(COR_TEXT); }
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
                        int idProduto = (int) modeloTabela.getValueAt(row, 0);
                        abrirFormularioEdicao(idProduto);
                    }
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setBorder(null);
        scrollTabela.setPreferredSize(new Dimension(0, 400));
        card.add(scrollTabela, BorderLayout.CENTER);

        // Rodapé da tabela
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

        lblTituloForm = new JLabel("Novo Produto");
        lblTituloForm.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTituloForm.setForeground(COR_TEXT);
        lblTituloForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        // nomeProduto
        JLabel lblNome = criarLabel("NOMEPRODUTO *");
        campoNome = criarInput("");
        campoNome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // descricao
        JLabel lblDesc = criarLabel("DESCRICAO");
        campoDescricao = new JTextArea(3, 1);
        campoDescricao.setFont(FONT_INPUT);
        campoDescricao.setForeground(COR_TEXT);
        campoDescricao.setBackground(COR_SURFACE);
        campoDescricao.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campoDescricao.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(campoDescricao);
        scrollDesc.setBorder(null);
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // marca
        JLabel lblMarcaLbl = criarLabel("MARCA");
        campoMarca = criarInput("");
        campoMarca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // idCategoria
        JLabel lblCatLbl = criarLabel("IDCATEGORIA (nomeCategoria) *");
        cboCategorias = new JComboBox<>();
        cboCategorias.setFont(FONT_INPUT);
        cboCategorias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // unidadeMedida
        JLabel lblUnidLbl = criarLabel("UNIDADEMEDIDA");
        cboUnidade = new JComboBox<>(new String[]{"UN", "CX", "PC", "KG", "M"});
        cboUnidade.setFont(FONT_INPUT);
        cboUnidade.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // quantidade
        JLabel lblQtdLbl = criarLabel("QUANTIDADE");
        spinQuantidade = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
        spinQuantidade.setFont(FONT_INPUT);
        spinQuantidade.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // precoCusto
        JLabel lblCustoLbl = criarLabel("PRECOCUSTO (BigDecimal)");
        campoPrecoCusto = criarInput("0,00");
        campoPrecoCusto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campoPrecoCusto.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { calcularMargem(); }
        });

        // precoVenda
        JLabel lblVendaLbl = criarLabel("PRECOEVENDA (BigDecimal)");
        campoPrecoVenda = criarInput("0,00");
        campoPrecoVenda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campoPrecoVenda.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { calcularMargem(); }
        });

        // margemLucro calculado
        JLabel lblMargemLbl = criarLabel("CALCULARMARGEMLUCRO() — calculado");
        JPanel painelMargem = new JPanel(new BorderLayout());
        painelMargem.setBackground(COR_TAG);
        painelMargem.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        painelMargem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        lblMargem = new JLabel("— %");
        lblMargem.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblMargem.setForeground(COR_GREEN);
        JLabel lblMargemSub = new JLabel("(venda - custo) / custo × 100");
        lblMargemSub.setFont(FONT_SMALL);
        lblMargemSub.setForeground(COR_LABEL);
        painelMargem.add(lblMargem, BorderLayout.CENTER);
        painelMargem.add(lblMargemSub, BorderLayout.SOUTH);

        // status
        JLabel lblStatusLbl = criarLabel("STATUS");
        cboStatus = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        cboStatus.setFont(FONT_INPUT);
        cboStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 8, 0));
        painelBotoes.setBackground(COR_SURFACE);
        painelBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btnCancelar = criarBotao("Cancelar", COR_SURFACE, COR_TEXT);
        btnCancelar.setBorder(new LineBorder(COR_TEXT, 2));
        btnCancelar.addActionListener(e -> fecharFormulario());

        btnSalvar = criarBotao("Salvar Produto", COR_TEXT, Color.WHITE);
        btnSalvar.addActionListener(e -> salvarProduto());

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);

        // Montagem
        card.add(lblTituloForm);
        card.add(Box.createVerticalStrut(16));
        card.add(lblNome);          card.add(Box.createVerticalStrut(4));
        card.add(campoNome);        card.add(Box.createVerticalStrut(10));
        card.add(lblDesc);          card.add(Box.createVerticalStrut(4));
        card.add(scrollDesc);       card.add(Box.createVerticalStrut(10));
        card.add(lblMarcaLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoMarca);       card.add(Box.createVerticalStrut(10));
        card.add(lblCatLbl);        card.add(Box.createVerticalStrut(4));
        card.add(cboCategorias);    card.add(Box.createVerticalStrut(10));
        card.add(lblUnidLbl);       card.add(Box.createVerticalStrut(4));
        card.add(cboUnidade);       card.add(Box.createVerticalStrut(10));
        card.add(lblQtdLbl);        card.add(Box.createVerticalStrut(4));
        card.add(spinQuantidade);   card.add(Box.createVerticalStrut(10));
        card.add(lblCustoLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoPrecoCusto);  card.add(Box.createVerticalStrut(10));
        card.add(lblVendaLbl);      card.add(Box.createVerticalStrut(4));
        card.add(campoPrecoVenda);  card.add(Box.createVerticalStrut(10));
        card.add(lblMargemLbl);     card.add(Box.createVerticalStrut(4));
        card.add(painelMargem);     card.add(Box.createVerticalStrut(10));
        card.add(lblStatusLbl);     card.add(Box.createVerticalStrut(4));
        card.add(cboStatus);        card.add(Box.createVerticalStrut(16));
        card.add(painelBotoes);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados
    // ─────────────────────────────────────────────────────────────────────
    private void carregarCategorias() {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT id_categoria, nome_categoria FROM categoria ORDER BY nome_categoria");
            ResultSet rs = st.executeQuery();
            cboCategorias.removeAllItems();
            cboCategorias.addItem("Selecionar...");
            List<Integer> ids = new java.util.ArrayList<>();
            List<String>  nomes = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id_categoria"));
                nomes.add(rs.getString("nome_categoria"));
                cboCategorias.addItem(rs.getString("nome_categoria"));
            }
            idsCategoria = ids.stream().mapToInt(i -> i).toArray();
        } catch (SQLException e) {
            System.out.println("Erro ao carregar categorias: " + e.getMessage());
        }
    }

    private void carregarProdutos() {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String filtroStatus = cboFiltroStatus != null
                        ? cboFiltroStatus.getSelectedItem().toString() : "Todos";
                    String busca = campoBusca != null
                        ? campoBusca.getText().trim() : "";
                    if (busca.equals("Buscar por nome, marca...")) busca = "";

                    String sql = "SELECT p.id_produto, p.nome_produto, p.marca, " +
                        "c.nome_categoria, p.unidade_medida, p.quantidade, " +
                        "p.preco_custo, p.preco_venda, p.status " +
                        "FROM produtos p " +
                        "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                        "WHERE 1=1 ";
                    if (!"Todos".equals(filtroStatus))
                        sql += "AND p.status = '" + filtroStatus + "' ";
                    if (!busca.isEmpty())
                        sql += "AND (p.nome_produto LIKE '%" + busca + "%' " +
                               "OR p.marca LIKE '%" + busca + "%') ";
                    sql += "ORDER BY p.nome_produto";

                    PreparedStatement st = conn.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();

                    SwingUtilities.invokeLater(() -> modeloTabela.setRowCount(0));

                    while (rs.next()) {
                        BigDecimal custo  = rs.getBigDecimal("preco_custo");
                        BigDecimal venda  = rs.getBigDecimal("preco_venda");
                        String margem = "—";
                        if (custo != null && venda != null &&
                            custo.compareTo(BigDecimal.ZERO) != 0) {
                            // calcularMargemLucro() da classe Produto
                            BigDecimal lucro = venda.subtract(custo);
                            BigDecimal perc  = lucro.divide(custo, 4,
                                java.math.RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, java.math.RoundingMode.HALF_UP);
                            margem = perc + "%";
                        }

                        Object[] row = {
                            rs.getInt("id_produto"),
                            rs.getString("nome_produto"),
                            rs.getString("marca"),
                            rs.getString("nome_categoria"),
                            rs.getString("unidade_medida"),
                            rs.getInt("quantidade"),
                            custo != null ? "R$ " + custo : "—",
                            venda != null ? "R$ " + venda : "—",
                            margem,
                            rs.getString("status"),
                            "Editar"
                        };
                        publish(row);
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao carregar produtos: " + e.getMessage());
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
        carregarProdutos();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Formulário — abrir / fechar / salvar
    // ─────────────────────────────────────────────────────────────────────
    private void abrirFormularioNovo() {
        produtoEmEdicao = null;
        lblTituloForm.setText("Novo Produto");
        limparFormulario();
        painelForm.setVisible(true);
        revalidate(); repaint();
    }

    private void abrirFormularioEdicao(int idProduto) {
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM produtos WHERE id_produto = ?");
            st.setInt(1, idProduto);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                produtoEmEdicao = new Produto();
                produtoEmEdicao.setIdProduto(rs.getInt("id_produto"));
                produtoEmEdicao.setNomeProduto(rs.getString("nome_produto"));
                produtoEmEdicao.setDescricao(rs.getString("descricao"));
                produtoEmEdicao.setMarca(rs.getString("marca"));
                produtoEmEdicao.setIdCategoria(rs.getInt("id_categoria"));
                produtoEmEdicao.setUnidadeMedida(rs.getString("unidade_medida"));
                produtoEmEdicao.setQuantidade(rs.getInt("quantidade"));
                produtoEmEdicao.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                produtoEmEdicao.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                produtoEmEdicao.setStatus(rs.getString("status"));

                lblTituloForm.setText("Editar Produto #" + idProduto);
                preencherFormulario(produtoEmEdicao);
                painelForm.setVisible(true);
                revalidate(); repaint();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar produto: " + e.getMessage());
        }
    }

    private void preencherFormulario(Produto p) {
        campoNome.setText(p.getNomeProduto());
        campoNome.setForeground(COR_TEXT);
        campoDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "");
        campoMarca.setText(p.getMarca() != null ? p.getMarca() : "");
        campoMarca.setForeground(COR_TEXT);
        spinQuantidade.setValue(p.getQuantidade());
        campoPrecoCusto.setText(p.getPrecoCusto() != null
            ? p.getPrecoCusto().toString() : "0.00");
        campoPrecoCusto.setForeground(COR_TEXT);
        campoPrecoVenda.setText(p.getPrecoVenda() != null
            ? p.getPrecoVenda().toString() : "0.00");
        campoPrecoVenda.setForeground(COR_TEXT);
        cboStatus.setSelectedItem(p.getStatus());
        cboUnidade.setSelectedItem(p.getUnidadeMedida());

        // Seleciona categoria
        if (idsCategoria != null) {
            for (int i = 0; i < idsCategoria.length; i++) {
                if (idsCategoria[i] == p.getIdCategoria()) {
                    cboCategorias.setSelectedIndex(i + 1);
                    break;
                }
            }
        }
        calcularMargem();
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        campoMarca.setText("");
        cboCategorias.setSelectedIndex(0);
        cboUnidade.setSelectedIndex(0);
        spinQuantidade.setValue(0);
        campoPrecoCusto.setText("0,00");
        campoPrecoVenda.setText("0,00");
        cboStatus.setSelectedIndex(0);
        lblMargem.setText("— %");
    }

    private void fecharFormulario() {
        produtoEmEdicao = null;
        painelForm.setVisible(false);
        revalidate(); repaint();
    }

    private void calcularMargem() {
        try {
            String custoStr = campoPrecoCusto.getText().replace(",", ".");
            String vendaStr = campoPrecoVenda.getText().replace(",", ".");
            BigDecimal custo = new BigDecimal(custoStr);
            BigDecimal venda = new BigDecimal(vendaStr);
            if (custo.compareTo(BigDecimal.ZERO) == 0) {
                lblMargem.setText("— %");
                return;
            }
            BigDecimal lucro = venda.subtract(custo);
            BigDecimal perc  = lucro.divide(custo, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
            lblMargem.setText(perc + " %");
            lblMargem.setForeground(perc.compareTo(BigDecimal.ZERO) >= 0
                ? COR_GREEN : COR_RED);
        } catch (Exception ignored) {
            lblMargem.setText("— %");
        }
    }

    private void salvarProduto() {
        // Validações
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "nomeProduto é obrigatório.", "Campo obrigatório",
                JOptionPane.WARNING_MESSAGE);
            campoNome.requestFocus();
            return;
        }

        int idxCat = cboCategorias.getSelectedIndex() - 1;
        if (idxCat < 0 || idsCategoria == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma categoria.", "Campo obrigatório",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idCategoria = idsCategoria[idxCat];
        String descricao = campoDescricao.getText().trim();
        String marca     = campoMarca.getText().trim();
        String unidade   = cboUnidade.getSelectedItem().toString();
        int    quantidade = (Integer) spinQuantidade.getValue();
        String status    = cboStatus.getSelectedItem().toString();

        BigDecimal precoCusto, precoVenda;
        try {
            precoCusto = new BigDecimal(campoPrecoCusto.getText().replace(",", "."));
            precoVenda = new BigDecimal(campoPrecoVenda.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Preço inválido. Use o formato: 0.00",
                "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (produtoEmEdicao == null) {
                // INSERT — novo produto
                String sql = "INSERT INTO produtos " +
                    "(nome_produto, descricao, unidade_medida, id_categoria, " +
                    "marca, preco_custo, preco_venda, quantidade, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, nome);
                st.setString(2, descricao.isEmpty() ? null : descricao);
                st.setString(3, unidade);
                st.setInt(4, idCategoria);
                st.setString(5, marca.isEmpty() ? null : marca);
                st.setBigDecimal(6, precoCusto);
                st.setBigDecimal(7, precoVenda);
                st.setInt(8, quantidade);
                st.setString(9, status);
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Produto cadastrado com sucesso!",
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // UPDATE — editar produto existente
                String sql = "UPDATE produtos SET " +
                    "nome_produto=?, descricao=?, unidade_medida=?, id_categoria=?, " +
                    "marca=?, preco_custo=?, preco_venda=?, quantidade=?, status=? " +
                    "WHERE id_produto=?";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, nome);
                st.setString(2, descricao.isEmpty() ? null : descricao);
                st.setString(3, unidade);
                st.setInt(4, idCategoria);
                st.setString(5, marca.isEmpty() ? null : marca);
                st.setBigDecimal(6, precoCusto);
                st.setBigDecimal(7, precoVenda);
                st.setInt(8, quantidade);
                st.setString(9, status);
                st.setInt(10, produtoEmEdicao.getIdProduto());
                st.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "Produto atualizado com sucesso!",
                    "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            fecharFormulario();
            carregarProdutos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar produto:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.out.println("Erro SQL produto: " + e.getMessage());
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
        campo.setForeground(placeholder.isEmpty() ? COR_TEXT : COR_LABEL);
        campo.setBackground(COR_SURFACE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        if (!placeholder.isEmpty()) {
            campo.setText(placeholder);
            campo.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (campo.getText().equals(placeholder)) {
                        campo.setText(""); campo.setForeground(COR_TEXT);
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (campo.getText().isEmpty()) {
                        campo.setText(placeholder); campo.setForeground(COR_LABEL);
                    }
                }
            });
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