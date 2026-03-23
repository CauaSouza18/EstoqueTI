package view;

import dao.Conexao;
import dao.ProdutoDAO;
import data.MovimentacaoEstoque;
import data.Produto;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Movimentações — EstoqueTI
 * Registra Entrada e Saída de produtos no estoque.
 * Usa: movimentacao_estoque, produtos, usuarios
 *
 * Campos de MovimentacaoEstoque:
 *   idMovimentacao, idProduto, dataMovimentacao (DATETIME),
 *   tipoMovimentacao ENUM("Entrada","Saida"),
 *   quantidade, observacao, idUsuario
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaMovimentacoes extends JPanel {

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

    // ── Componentes — Formulário Entrada ──────────────────────────────────
    private JComboBox<String> cboProdutoEntrada;
    private JSpinner           spinQtdEntrada;
    private JTextField         campoObsEntrada;
    private JLabel             lblStockAtualEntrada;
    private int[]              idsProdutosEntrada;

    // ── Componentes — Formulário Saída ────────────────────────────────────
    private JComboBox<String> cboProdutoSaida;
    private JSpinner           spinQtdSaida;
    private JTextField         campoObsSaida;
    private JLabel             lblStockAtualSaida;
    private int[]              idsProdutosSaida;

    // ── Tabela histórico ──────────────────────────────────────────────────
    private JTable             tabelaHistorico;
    private DefaultTableModel  modeloTabela;
    private JComboBox<String>  cboFiltroTipo;

    public TelaMovimentacoes(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.conn = new Conexao().getConexao();
        setLayout(new BorderLayout());
        setBackground(COR_BG);
        construirInterface();
        carregarProdutos();
        carregarHistorico();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void construirInterface() {
        JPanel interno = new JPanel();
        interno.setLayout(new BoxLayout(interno, BoxLayout.Y_AXIS));
        interno.setBackground(COR_BG);
        interno.setBorder(new EmptyBorder(22, 26, 22, 26));

        interno.add(construirHeader());
        interno.add(Box.createVerticalStrut(18));
        interno.add(construirFormularios());
        interno.add(Box.createVerticalStrut(18));
        interno.add(construirTabelaHistorico());

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

        JLabel lblTitulo = new JLabel("Movimentações");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        JLabel lblSub = new JLabel("MovimentacaoEstoque.java · tipoMovimentacao: Entrada / Saida");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);
        header.add(esq, BorderLayout.WEST);
        return header;
    }

    // ── Formulários lado a lado ───────────────────────────────────────────
    private JPanel construirFormularios() {
        JPanel linha = new JPanel(new GridLayout(1, 2, 16, 0));
        linha.setBackground(COR_BG);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        linha.add(construirFormEntrada());
        linha.add(construirFormSaida());
        return linha;
    }

    // ── Formulário ENTRADA ────────────────────────────────────────────────
    private JPanel construirFormEntrada() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(16, 18, 16, 18)
        ));

        // Anotação tipo
        JLabel lblAnno = new JLabel("[ tipoMovimentacao = \"Entrada\" ]");
        lblAnno.setFont(FONT_SMALL);
        lblAnno.setForeground(COR_LABEL);
        lblAnno.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        lblAnno.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel("↑  Registrar Entrada");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(COR_GREEN);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // idProduto
        JLabel lblProd = criarLabel("PRODUTO (idProduto) *");
        cboProdutoEntrada = new JComboBox<>();
        cboProdutoEntrada.setFont(FONT_INPUT);
        cboProdutoEntrada.setBackground(COR_SURFACE);
        cboProdutoEntrada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboProdutoEntrada.addActionListener(e -> atualizarStockEntrada());

        lblStockAtualEntrada = new JLabel("Selecione um produto");
        lblStockAtualEntrada.setFont(FONT_SMALL);
        lblStockAtualEntrada.setForeground(COR_LABEL);
        lblStockAtualEntrada.setAlignmentX(Component.LEFT_ALIGNMENT);

        // quantidade
        JLabel lblQtd = criarLabel("QUANTIDADE *");
        spinQtdEntrada = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinQtdEntrada.setFont(FONT_INPUT);
        spinQtdEntrada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // observacao
        JLabel lblObs = criarLabel("OBSERVACAO (NF, lote...)");
        campoObsEntrada = criarInput("Ex: NF-03821, reposição...");

        // idUsuario automático
        JLabel lblUser = criarLabel("IDUSUARIO (automático)");
        JTextField campoUser = new JTextField(usuarioLogado.getNomeUsuario() +
            " [id=" + usuarioLogado.getIdUsuario() + "]");
        campoUser.setFont(FONT_INPUT);
        campoUser.setForeground(COR_LABEL);
        campoUser.setBackground(COR_TAG);
        campoUser.setEditable(false);
        campoUser.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campoUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // dataMovimentacao automático
        JLabel lblData = criarLabel("DATAMOVIMENTACAO (automático)");
        JTextField campoData = new JTextField("LocalDateTime.now() → " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        campoData.setFont(new Font("Monospaced", Font.PLAIN, 10));
        campoData.setForeground(COR_LABEL);
        campoData.setBackground(COR_TAG);
        campoData.setEditable(false);
        campoData.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campoData.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Botão confirmar
        JButton btnConfirmar = criarBotao("Confirmar Entrada", COR_GREEN, Color.WHITE);
        btnConfirmar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConfirmar.addActionListener(e -> registrarMovimentacao("Entrada"));

        // Montagem
        card.add(lblAnno);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(14));
        card.add(lblProd);
        card.add(Box.createVerticalStrut(4));
        card.add(cboProdutoEntrada);
        card.add(Box.createVerticalStrut(2));
        card.add(lblStockAtualEntrada);
        card.add(Box.createVerticalStrut(10));
        card.add(lblQtd);
        card.add(Box.createVerticalStrut(4));
        card.add(spinQtdEntrada);
        card.add(Box.createVerticalStrut(10));
        card.add(lblObs);
        card.add(Box.createVerticalStrut(4));
        card.add(campoObsEntrada);
        card.add(Box.createVerticalStrut(10));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(campoUser);
        card.add(Box.createVerticalStrut(10));
        card.add(lblData);
        card.add(Box.createVerticalStrut(4));
        card.add(campoData);
        card.add(Box.createVerticalStrut(14));
        card.add(btnConfirmar);

        return card;
    }

    // ── Formulário SAÍDA ──────────────────────────────────────────────────
    private JPanel construirFormSaida() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lblAnno = new JLabel("[ tipoMovimentacao = \"Saida\" ]");
        lblAnno.setFont(FONT_SMALL);
        lblAnno.setForeground(COR_LABEL);
        lblAnno.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        lblAnno.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel("↓  Registrar Saída");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(COR_RED);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // idProduto
        JLabel lblProd = criarLabel("PRODUTO (idProduto) *");
        cboProdutoSaida = new JComboBox<>();
        cboProdutoSaida.setFont(FONT_INPUT);
        cboProdutoSaida.setBackground(COR_SURFACE);
        cboProdutoSaida.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboProdutoSaida.addActionListener(e -> atualizarStockSaida());

        lblStockAtualSaida = new JLabel("Selecione um produto");
        lblStockAtualSaida.setFont(FONT_SMALL);
        lblStockAtualSaida.setForeground(COR_LABEL);
        lblStockAtualSaida.setAlignmentX(Component.LEFT_ALIGNMENT);

        // quantidade
        JLabel lblQtd = criarLabel("QUANTIDADE *");
        spinQtdSaida = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinQtdSaida.setFont(FONT_INPUT);
        spinQtdSaida.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // observacao
        JLabel lblObs = criarLabel("OBSERVACAO (motivo da saída)");
        campoObsSaida = criarInput("Ex: Venda, uso interno, devolução...");

        // idUsuario automático
        JLabel lblUser = criarLabel("IDUSUARIO (automático)");
        JTextField campoUser = new JTextField(usuarioLogado.getNomeUsuario() +
            " [id=" + usuarioLogado.getIdUsuario() + "]");
        campoUser.setFont(FONT_INPUT);
        campoUser.setForeground(COR_LABEL);
        campoUser.setBackground(COR_TAG);
        campoUser.setEditable(false);
        campoUser.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campoUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // dataMovimentacao automático
        JLabel lblData = criarLabel("DATAMOVIMENTACAO (automático)");
        JTextField campoData = new JTextField("LocalDateTime.now() → " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        campoData.setFont(new Font("Monospaced", Font.PLAIN, 10));
        campoData.setForeground(COR_LABEL);
        campoData.setBackground(COR_TAG);
        campoData.setEditable(false);
        campoData.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campoData.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Botão confirmar
        JButton btnConfirmar = criarBotao("Confirmar Saída", COR_RED, Color.WHITE);
        btnConfirmar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConfirmar.addActionListener(e -> registrarMovimentacao("Saida"));

        // Montagem
        card.add(lblAnno);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(14));
        card.add(lblProd);
        card.add(Box.createVerticalStrut(4));
        card.add(cboProdutoSaida);
        card.add(Box.createVerticalStrut(2));
        card.add(lblStockAtualSaida);
        card.add(Box.createVerticalStrut(10));
        card.add(lblQtd);
        card.add(Box.createVerticalStrut(4));
        card.add(spinQtdSaida);
        card.add(Box.createVerticalStrut(10));
        card.add(lblObs);
        card.add(Box.createVerticalStrut(4));
        card.add(campoObsSaida);
        card.add(Box.createVerticalStrut(10));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(campoUser);
        card.add(Box.createVerticalStrut(10));
        card.add(lblData);
        card.add(Box.createVerticalStrut(4));
        card.add(campoData);
        card.add(Box.createVerticalStrut(14));
        card.add(btnConfirmar);

        return card;
    }

    // ── Tabela Histórico ──────────────────────────────────────────────────
    private JPanel construirTabelaHistorico() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(new LineBorder(COR_BORDER, 2));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Header da tabela
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COR_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, COR_BORDER),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitulo = new JLabel("Histórico de Movimentações");
        lblTitulo.setFont(FONT_BOLD);
        lblTitulo.setForeground(COR_TEXT);

        // Filtro por tipo
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setBackground(COR_SURFACE);
        JLabel lblFiltro = new JLabel("Filtrar:");
        lblFiltro.setFont(FONT_SMALL);
        lblFiltro.setForeground(COR_LABEL);
        cboFiltroTipo = new JComboBox<>(new String[]{"Todos", "Entrada", "Saida"});
        cboFiltroTipo.setFont(FONT_BODY);
        cboFiltroTipo.addActionListener(e -> carregarHistorico());

        JButton btnAtualizar = new JButton("↻ Atualizar");
        btnAtualizar.setFont(FONT_SMALL);
        btnAtualizar.setForeground(COR_TEXT);
        btnAtualizar.setBackground(COR_SURFACE);
        btnAtualizar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_TEXT, 2),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btnAtualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> carregarHistorico());

        filtros.add(lblFiltro);
        filtros.add(cboFiltroTipo);
        filtros.add(btnAtualizar);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(filtros, BorderLayout.EAST);

        // Modelo da tabela
        String[] colunas = {
            "id", "Produto", "Tipo", "Quantidade",
            "Observação", "Usuário", "Data/Hora"
        };
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelaHistorico = new JTable(modeloTabela);
        tabelaHistorico.setFont(FONT_BODY);
        tabelaHistorico.setRowHeight(32);
        tabelaHistorico.setBackground(COR_SURFACE);
        tabelaHistorico.setGridColor(COR_BORDER);
        tabelaHistorico.setShowVerticalLines(false);
        tabelaHistorico.setSelectionBackground(COR_ACCENT);
        tabelaHistorico.setFillsViewportHeight(true);

        // Header da tabela
        JTableHeader th = tabelaHistorico.getTableHeader();
        th.setFont(FONT_LABEL);
        th.setBackground(COR_TAG);
        th.setForeground(COR_LABEL);
        th.setBorder(new MatteBorder(0, 0, 1, 0, COR_BORDER));

        // Larguras das colunas
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(40);  // id
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(220); // produto
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(80);  // tipo
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(80);  // qtd
        tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(180); // obs
        tabelaHistorico.getColumnModel().getColumn(5).setPreferredWidth(120); // usuario
        tabelaHistorico.getColumnModel().getColumn(6).setPreferredWidth(130); // data

        // Renderer personalizado para badges e cores
        tabelaHistorico.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(0, 12, 0, 12));

                // Fundo alternado
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                }

                // Coluna tipo — badge colorido
                if (column == 2) {
                    String tipo = value != null ? value.toString() : "";
                    if ("Entrada".equals(tipo)) {
                        setBackground(isSelected ? COR_ACCENT : COR_BLUE_BG);
                        setForeground(COR_BLUE);
                        setFont(new Font("Monospaced", Font.BOLD, 10));
                    } else if ("Saida".equals(tipo)) {
                        setBackground(isSelected ? COR_ACCENT : COR_RED_BG);
                        setForeground(COR_RED);
                        setFont(new Font("Monospaced", Font.BOLD, 10));
                    }
                }
                // Coluna quantidade — verde para entrada, vermelho para saída
                else if (column == 3) {
                    String tipo = table.getValueAt(row, 2) != null
                        ? table.getValueAt(row, 2).toString() : "";
                    setForeground("Entrada".equals(tipo) ? COR_GREEN : COR_RED);
                    setFont(FONT_BOLD);
                    if (!isSelected) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                }
                // Coluna id — monospaced cinza
                else if (column == 0) {
                    setFont(FONT_SMALL);
                    setForeground(COR_LABEL);
                    if (!isSelected) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                }
                // Data — monospaced
                else if (column == 6) {
                    setFont(FONT_SMALL);
                    setForeground(isSelected ? COR_TEXT : COR_MUTED);
                    if (!isSelected) setBackground(row % 2 == 0 ? COR_SURFACE : COR_ACCENT);
                }
                else {
                    setForeground(COR_TEXT);
                }

                return this;
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabelaHistorico);
        scrollTabela.setBorder(null);
        scrollTabela.setPreferredSize(new Dimension(0, 280));

        card.add(header, BorderLayout.NORTH);
        card.add(scrollTabela, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados
    // ─────────────────────────────────────────────────────────────────────

    /** Preenche os combos de produto com dados do banco */
    private void carregarProdutos() {
        SwingWorker<List<Produto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Produto> doInBackground() {
                return produtoDAO.listar();
            }
            @Override
            protected void done() {
                try {
                    List<Produto> produtos = get();
                    idsProdutosEntrada = new int[produtos.size()];
                    idsProdutosSaida   = new int[produtos.size()];

                    cboProdutoEntrada.removeAllItems();
                    cboProdutoSaida.removeAllItems();
                    cboProdutoEntrada.addItem("Selecionar produto...");
                    cboProdutoSaida.addItem("Selecionar produto...");

                    for (int i = 0; i < produtos.size(); i++) {
                        Produto p = produtos.get(i);
                        String item = "[" + p.getIdProduto() + "] " +
                            p.getNomeProduto() +
                            (p.getMarca() != null ? " — " + p.getMarca() : "") +
                            "  (qtd: " + p.getQuantidade() + ")";
                        cboProdutoEntrada.addItem(item);
                        cboProdutoSaida.addItem(item);
                        idsProdutosEntrada[i] = p.getIdProduto();
                        idsProdutosSaida[i]   = p.getIdProduto();
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao carregar produtos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Atualiza label de estoque atual ao selecionar produto na Entrada */
    private void atualizarStockEntrada() {
        int idx = cboProdutoEntrada.getSelectedIndex() - 1;
        if (idx < 0 || idsProdutosEntrada == null) return;
        int idProduto = idsProdutosEntrada[idx];
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT quantidade FROM produtos WHERE id_produto = ?");
            st.setInt(1, idProduto);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int qtd = rs.getInt("quantidade");
                lblStockAtualEntrada.setText("Estoque atual: " + qtd + " unidades");
                lblStockAtualEntrada.setForeground(qtd == 0 ? COR_RED : COR_GREEN);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar estoque: " + e.getMessage());
        }
    }

    /** Atualiza label de estoque atual ao selecionar produto na Saída */
    private void atualizarStockSaida() {
        int idx = cboProdutoSaida.getSelectedIndex() - 1;
        if (idx < 0 || idsProdutosSaida == null) return;
        int idProduto = idsProdutosSaida[idx];
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT quantidade FROM produtos WHERE id_produto = ?");
            st.setInt(1, idProduto);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int qtd = rs.getInt("quantidade");
                lblStockAtualSaida.setText("Estoque atual: " + qtd + " unidades");
                lblStockAtualSaida.setForeground(qtd == 0 ? COR_RED :
                    qtd <= 5 ? COR_RED.brighter() : COR_GREEN);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar estoque: " + e.getMessage());
        }
    }

    /** Carrega o histórico de movimentações com filtro por tipo */
    private void carregarHistorico() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String filtro = cboFiltroTipo.getSelectedItem().toString();
                    String sql = "SELECT m.id_movimentacao, p.nome_produto, " +
                        "m.tipo_movimentacao, m.quantidade, m.observacao, " +
                        "u.nome_usuario, m.data_movimentacao " +
                        "FROM movimentacao_estoque m " +
                        "JOIN produtos p ON m.id_produto = p.id_produto " +
                        "JOIN usuarios u ON m.id_usuario = u.id_usuario ";

                    if (!"Todos".equals(filtro)) {
                        sql += "WHERE m.tipo_movimentacao = ? ";
                    }
                    sql += "ORDER BY m.data_movimentacao DESC LIMIT 50";

                    PreparedStatement st = conn.prepareStatement(sql);
                    if (!"Todos".equals(filtro)) st.setString(1, filtro);
                    ResultSet rs = st.executeQuery();

                    SwingUtilities.invokeLater(() -> modeloTabela.setRowCount(0));

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    while (rs.next()) {
                        String tipo = rs.getString("tipo_movimentacao");
                        int qtd     = rs.getInt("quantidade");
                        String sinal = "Entrada".equals(tipo) ? "+" : "−";
                        Timestamp ts = rs.getTimestamp("data_movimentacao");
                        String data  = ts.toLocalDateTime().format(fmt);

                        Object[] row = {
                            rs.getInt("id_movimentacao"),
                            rs.getString("nome_produto"),
                            tipo,
                            sinal + qtd,
                            rs.getString("observacao"),
                            rs.getString("nome_usuario"),
                            data
                        };
                        SwingUtilities.invokeLater(() -> modeloTabela.addRow(row));
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao carregar histórico: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Lógica de registro de movimentação
    // ─────────────────────────────────────────────────────────────────────
    private void registrarMovimentacao(String tipo) {
        // Verifica permissão
        if (!usuarioLogado.podeOperar()) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para registrar movimentações.\n" +
                "nivelAcesso necessário: Operador ou Administrador.",
                "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pega os valores dos campos conforme o tipo
        JComboBox<String> cboProduto = "Entrada".equals(tipo)
            ? cboProdutoEntrada : cboProdutoSaida;
        JSpinner spinQtd = "Entrada".equals(tipo)
            ? spinQtdEntrada : spinQtdSaida;
        JTextField campoObs = "Entrada".equals(tipo)
            ? campoObsEntrada : campoObsSaida;
        int[] idsProdutos = "Entrada".equals(tipo)
            ? idsProdutosEntrada : idsProdutosSaida;

        // Validações
        int idxProd = cboProduto.getSelectedIndex() - 1;
        if (idxProd < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um produto.", "Campo obrigatório",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idProduto  = idsProdutos[idxProd];
        int quantidade = (Integer) spinQtd.getValue();
        String obs     = campoObs.getText().trim();

        // Para saída: verifica se tem estoque suficiente
        if ("Saida".equals(tipo)) {
            try {
                PreparedStatement st = conn.prepareStatement(
                    "SELECT quantidade FROM produtos WHERE id_produto = ?");
                st.setInt(1, idProduto);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    int estoqueAtual = rs.getInt("quantidade");
                    if (quantidade > estoqueAtual) {
                        JOptionPane.showMessageDialog(this,
                            "Estoque insuficiente!\n" +
                            "Estoque atual: " + estoqueAtual + " unidades\n" +
                            "Quantidade solicitada: " + quantidade,
                            "Estoque insuficiente", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao verificar estoque: " + e.getMessage());
            }
        }

        // Confirmação
        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmar " + tipo + "?\n\n" +
            "Produto: " + cboProduto.getSelectedItem() + "\n" +
            "Quantidade: " + quantidade + "\n" +
            "Observação: " + (obs.isEmpty() ? "—" : obs),
            "Confirmar " + tipo,
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Insere no banco
        try {
            // 1. Insere na tabela movimentacao_estoque
            String sqlMov = "INSERT INTO movimentacao_estoque " +
                "(id_produto, data_movimentacao, tipo_movimentacao, " +
                "quantidade, observacao, id_usuario) " +
                "VALUES (?, NOW(), ?, ?, ?, ?)";
            PreparedStatement stMov = conn.prepareStatement(sqlMov);
            stMov.setInt(1, idProduto);
            stMov.setString(2, tipo);
            stMov.setInt(3, quantidade);
            stMov.setString(4, obs.isEmpty() ? null : obs);
            stMov.setInt(5, usuarioLogado.getIdUsuario());
            stMov.executeUpdate();

            // 2. Atualiza a quantidade do produto
            String sqlProd = "Entrada".equals(tipo)
                ? "UPDATE produtos SET quantidade = quantidade + ? WHERE id_produto = ?"
                : "UPDATE produtos SET quantidade = quantidade - ? WHERE id_produto = ?";
            PreparedStatement stProd = conn.prepareStatement(sqlProd);
            stProd.setInt(1, quantidade);
            stProd.setInt(2, idProduto);
            stProd.executeUpdate();

            // Feedback de sucesso
            JOptionPane.showMessageDialog(this,
                tipo + " registrada com sucesso!\n" +
                "Estoque atualizado.",
                "✅ Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Limpa os campos
            cboProduto.setSelectedIndex(0);
            spinQtd.setValue(1);
            campoObs.setText("");

            // Recarrega lista de produtos e histórico
            carregarProdutos();
            carregarHistorico();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao registrar movimentação:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            System.out.println("Erro SQL movimentação: " + e.getMessage());
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
        campo.setForeground(COR_LABEL);
        campo.setBackground(COR_SURFACE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER_DK, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campo.setText(placeholder);
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