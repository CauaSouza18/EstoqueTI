package view;

import dao.Conexao;
import dao.ProdutoDAO;
import data.Produto;
import data.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela Dashboard — EstoqueTI
 * Tela principal após login. Exibe KPIs, alertas, últimas movimentações
 * e ações rápidas. Recebe o Usuario logado para controle de permissões.
 *
 * Queries no banco: sistema_estoque
 * Tabelas usadas: produtos, movimentacao_estoque, usuarios, categoria
 *
 * @author EstoqueTI
 * @version 1.0
 */
public class TelaDashboard extends JFrame {

    // ── Cores ─────────────────────────────────────────────────────────────
    private static final Color COR_BG        = new Color(0xF5F3EE);
    private static final Color COR_SURFACE   = new Color(0xFFFFFF);
    private static final Color COR_TEXT      = new Color(0x1A1A1A);
    private static final Color COR_MUTED     = new Color(0x666666);
    private static final Color COR_LABEL     = new Color(0x999999);
    private static final Color COR_BORDER    = new Color(0xCCCCCC);
    private static final Color COR_ACCENT    = new Color(0xE8E4DC);
    private static final Color COR_TAG       = new Color(0xEFEFEF);
    private static final Color COR_GREEN     = new Color(0x2D7A4F);
    private static final Color COR_RED       = new Color(0xD94F3D);
    private static final Color COR_YELLOW    = new Color(0xC8820A);
    private static final Color COR_BLUE      = new Color(0x2255AA);

    // ── Fontes ────────────────────────────────────────────────────────────
    private static final Font FONT_BRAND   = new Font("Monospaced", Font.BOLD,  14);
    private static final Font FONT_TITLE   = new Font("SansSerif",  Font.BOLD,  20);
    private static final Font FONT_LABEL   = new Font("Monospaced", Font.BOLD,  10);
    private static final Font FONT_BODY    = new Font("SansSerif",  Font.PLAIN, 12);
    private static final Font FONT_SMALL   = new Font("Monospaced", Font.PLAIN, 10);
    private static final Font FONT_BTN     = new Font("SansSerif",  Font.BOLD,  12);
    private static final Font FONT_KPI_VAL = new Font("SansSerif",  Font.BOLD,  26);
    private static final Font FONT_KPI_LBL = new Font("Monospaced", Font.BOLD,  10);

    // ── Usuário logado ────────────────────────────────────────────────────
    private final Usuario usuarioLogado;

    // ── DAO / Conexão ─────────────────────────────────────────────────────
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private Connection conn;

    // ── Painel principal (CardLayout para trocar telas) ───────────────────
    private JPanel painelConteudo;

    // ── Labels dos KPIs (atualizados ao carregar) ─────────────────────────
    private JLabel lblKpiProdutos;
    private JLabel lblKpiValor;
    private JLabel lblKpiAlertas;
    private JLabel lblKpiMovimentos;

    // ── Painel de alertas e movimentações ─────────────────────────────────
    private JPanel painelAlertas;
    private JPanel painelMovimentacoes;

    public TelaDashboard(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.conn = new Conexao().getConexao();
        configurarJanela();
        construirInterface();
        carregarDados();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void configurarJanela() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background",  COR_BG);
            UIManager.put("Label.foreground",  COR_TEXT);
            UIManager.put("Button.background", COR_TEXT);
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            System.out.println("Erro LookAndFeel: " + e.getMessage());
        }
        setTitle("EstoqueTI — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        JPanel fundo = new JPanel(new BorderLayout());
        fundo.setBackground(COR_BG);
        fundo.setOpaque(true);
        setContentPane(fundo);
        getRootPane().setBackground(COR_BG);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Interface principal
    // ─────────────────────────────────────────────────────────────────────
    private void construirInterface() {
        JPanel raiz = (JPanel) getContentPane();
        raiz.setBackground(COR_BG);
        raiz.add(construirTopbar(), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(COR_BG);
        centro.add(construirSidebar(), BorderLayout.WEST);
        painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_BG);
        painelConteudo.add(construirDashboard(), BorderLayout.CENTER);
        centro.add(painelConteudo, BorderLayout.CENTER);
        raiz.add(centro, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Topbar
    // ─────────────────────────────────────────────────────────────────────
    private JPanel construirTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(COR_SURFACE);
        topbar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, COR_BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));
        topbar.setPreferredSize(new Dimension(0, 48));

        // Info
        String data = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        JLabel lblInfo = new JLabel("EstoqueTI  ·  " + data);
        lblInfo.setFont(FONT_SMALL);
        lblInfo.setForeground(COR_MUTED);

        // Usuário + nível
        JPanel painelUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelUser.setBackground(COR_SURFACE);

        // Avatar com iniciais
        String iniciais = pegarIniciais(usuarioLogado.getNomeUsuario());
        JLabel avatar = new JLabel(iniciais) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_TEXT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("SansSerif", Font.BOLD, 10));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(28, 28));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblNome = new JLabel(usuarioLogado.getNomeUsuario());
        lblNome.setFont(FONT_BODY);
        lblNome.setForeground(COR_TEXT);

        // Badge nível de acesso
        JLabel lblNivel = criarBadgeNivel(usuarioLogado.getNivelAcesso());

        painelUser.add(avatar);
        painelUser.add(lblNome);
        painelUser.add(lblNivel);

        topbar.add(lblInfo, BorderLayout.WEST);
        topbar.add(painelUser, BorderLayout.EAST);
        return topbar;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Sidebar
    // ─────────────────────────────────────────────────────────────────────
    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COR_SURFACE);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 2, COR_TEXT));
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Logo
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBackground(COR_SURFACE);
        logo.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, COR_BORDER),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lblBrand = new JLabel("EstoqueTI");
        lblBrand.setFont(FONT_BRAND);
        lblBrand.setForeground(COR_TEXT);
        JLabel lblSubBrand = new JLabel("SISTEMA DE ESTOQUE");
        lblSubBrand.setFont(new Font("Monospaced", Font.PLAIN, 9));
        lblSubBrand.setForeground(COR_LABEL);
        logo.add(lblBrand);
        logo.add(Box.createVerticalStrut(2));
        logo.add(lblSubBrand);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        sidebar.add(logo);

        // Nav items — Dashboard é a tela ativa
        sidebar.add(criarNavItem("▦  Dashboard",     true,  () -> {}));
        sidebar.add(criarNavItem("◫  Produtos",       false, () -> abrirTela("produtos")));
        sidebar.add(criarNavItem("⇅  Movimentações",  false, () -> abrirTela("movimentacoes")));
        sidebar.add(criarNavItem("🧾  Notas Fiscais", false, () -> abrirTela("notas")));
        sidebar.add(criarNavItem("🏭  Fornecedores",  false, () -> abrirTela("fornecedores")));

        // Usuários — só Administrador vê
        if (usuarioLogado.isAdministrador()) {
            sidebar.add(criarNavItem("👤  Usuários", false, () -> abrirTela("usuarios")));
        }

        sidebar.add(Box.createVerticalGlue());

        // Botão sair
        JButton btnSair = new JButton("↩  Sair");
        btnSair.setFont(FONT_BODY);
        btnSair.setForeground(COR_LABEL);
        btnSair.setBackground(COR_SURFACE);
        btnSair.setBorderPainted(false);
        btnSair.setFocusPainted(false);
        btnSair.setHorizontalAlignment(SwingConstants.LEFT);
        btnSair.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, COR_BORDER),
            new EmptyBorder(10, 16, 10, 16)
        ));
        btnSair.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSair.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSair.addActionListener(e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });
        sidebar.add(btnSair);

        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Dashboard
    // ─────────────────────────────────────────────────────────────────────
    private JPanel construirDashboard() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_BG);

        // Scroll para caso o conteúdo seja grande
        JPanel interno = new JPanel();
        interno.setLayout(new BoxLayout(interno, BoxLayout.Y_AXIS));
        interno.setBackground(COR_BG);
        interno.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Cabeçalho da página
        interno.add(construirPageHeader());
        interno.add(Box.createVerticalStrut(18));

        // KPI Cards
        interno.add(construirKpiRow());
        interno.add(Box.createVerticalStrut(18));

        // Linha central: movimentações + alertas + ações rápidas
        interno.add(construirLinhaCentral());
        interno.add(Box.createVerticalStrut(18));

        // Gráfico de barras mockado
        interno.add(construirGraficoCard());

        JScrollPane scroll = new JScrollPane(interno);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(COR_BG);

        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    private JPanel construirPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COR_BG);
        header.setBorder(new MatteBorder(0, 0, 1, 0, COR_BORDER));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel esq = new JPanel();
        esq.setLayout(new BoxLayout(esq, BoxLayout.Y_AXIS));
        esq.setBackground(COR_BG);
        esq.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitulo = new JLabel("Dashboard");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(COR_TEXT);

        String data = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel lblSub = new JLabel("Visão geral · " + data);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COR_LABEL);

        esq.add(lblTitulo);
        esq.add(Box.createVerticalStrut(3));
        esq.add(lblSub);

        // Botão registrar movimento — só quem podeOperar()
        if (usuarioLogado.podeOperar()) {
            JButton btnMov = criarBotao("+ Registrar Movimento", COR_TEXT, Color.WHITE);
            btnMov.addActionListener(e -> abrirTela("movimentacoes"));
            JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
            dir.setBackground(COR_BG);
            dir.add(btnMov);
            header.add(dir, BorderLayout.EAST);
        }

        header.add(esq, BorderLayout.WEST);
        return header;
    }

    // ── KPI Cards ─────────────────────────────────────────────────────────
    private JPanel construirKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setBackground(COR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblKpiProdutos  = new JLabel("...");
        lblKpiValor     = new JLabel("...");
        lblKpiAlertas   = new JLabel("...");
        lblKpiMovimentos= new JLabel("...");

        row.add(construirKpiCard("PRODUTOS ATIVOS",    lblKpiProdutos,  "↑ carregando...", COR_BLUE));
        row.add(construirKpiCard("VALOR EM ESTOQUE",   lblKpiValor,     "precoCusto × qtd", COR_GREEN));
        row.add(construirKpiCard("ALERTAS DE ESTOQUE", lblKpiAlertas,   "quantidade = 0",   COR_RED));
        row.add(construirKpiCard("MOVIMENTOS HOJE",    lblKpiMovimentos,"Entrada + Saida",  COR_YELLOW));

        return row;
    }

    private JPanel construirKpiCard(String label, JLabel lblValor,
                                    String delta, Color accentColor) {
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
            new LineBorder(COR_BORDER, 2, false),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_KPI_LBL);
        lblLabel.setForeground(COR_LABEL);

        lblValor.setFont(FONT_KPI_VAL);
        lblValor.setForeground(COR_TEXT);

        JLabel lblDelta = new JLabel(delta);
        lblDelta.setFont(FONT_SMALL);
        lblDelta.setForeground(COR_MUTED);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(lblValor);
        card.add(Box.createVerticalStrut(4));
        card.add(lblDelta);

        return card;
    }

    // ── Linha Central ─────────────────────────────────────────────────────
    private JPanel construirLinhaCentral() {
        JPanel linha = new JPanel(new GridLayout(1, 2, 16, 0));
        linha.setBackground(COR_BG);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        // Movimentações
        JPanel cardMov = new JPanel(new BorderLayout());
        cardMov.setBackground(COR_SURFACE);
        cardMov.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lblMovTitulo = new JLabel("ÚLTIMAS MOVIMENTAÇÕES");
        lblMovTitulo.setFont(FONT_LABEL);
        lblMovTitulo.setForeground(COR_LABEL);
        lblMovTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        painelMovimentacoes = new JPanel();
        painelMovimentacoes.setLayout(new BoxLayout(painelMovimentacoes, BoxLayout.Y_AXIS));
        painelMovimentacoes.setBackground(COR_SURFACE);

        JButton btnVerTodas = criarBotao("Ver todas →", COR_SURFACE, COR_TEXT);
        btnVerTodas.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_TEXT, 2),
            new EmptyBorder(5, 12, 5, 12)
        ));
        btnVerTodas.addActionListener(e -> abrirTela("movimentacoes"));
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.setBackground(COR_SURFACE);
        rodape.add(btnVerTodas);

        cardMov.add(lblMovTitulo, BorderLayout.NORTH);
        cardMov.add(painelMovimentacoes, BorderLayout.CENTER);
        cardMov.add(rodape, BorderLayout.SOUTH);

        // Alertas + Ações rápidas
        JPanel colDir = new JPanel();
        colDir.setLayout(new BoxLayout(colDir, BoxLayout.Y_AXIS));
        colDir.setBackground(COR_BG);

        // Card alertas
        JPanel cardAlertas = new JPanel(new BorderLayout());
        cardAlertas.setBackground(COR_SURFACE);
        cardAlertas.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lblAlertasTitulo = new JLabel("⚠  PRECISA DE ATENÇÃO");
        lblAlertasTitulo.setFont(FONT_LABEL);
        lblAlertasTitulo.setForeground(COR_LABEL);
        lblAlertasTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        painelAlertas = new JPanel();
        painelAlertas.setLayout(new BoxLayout(painelAlertas, BoxLayout.Y_AXIS));
        painelAlertas.setBackground(COR_SURFACE);

        cardAlertas.add(lblAlertasTitulo, BorderLayout.NORTH);
        cardAlertas.add(painelAlertas, BorderLayout.CENTER);

        // Card ações rápidas — só quem podeOperar()
        JPanel cardAcoes = new JPanel(new BorderLayout());
        cardAcoes.setBackground(COR_SURFACE);
        cardAcoes.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lblAcoesTitulo = new JLabel("AÇÕES RÁPIDAS");
        lblAcoesTitulo.setFont(FONT_LABEL);
        lblAcoesTitulo.setForeground(COR_LABEL);
        lblAcoesTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel acoes = new JPanel(new GridLayout(3, 1, 0, 8));
        acoes.setBackground(COR_SURFACE);

        if (usuarioLogado.podeOperar()) {
            JButton btnEntrada = criarBotao("↑  Registrar Entrada", COR_GREEN, Color.WHITE);
            btnEntrada.addActionListener(e -> abrirTela("movimentacoes"));
            JButton btnSaida = criarBotao("↓  Registrar Saída", COR_RED, Color.WHITE);
            btnSaida.addActionListener(e -> abrirTela("movimentacoes"));
            acoes.add(btnEntrada);
            acoes.add(btnSaida);
        }

        JButton btnProduto = criarBotao("+ Novo Produto", COR_SURFACE, COR_TEXT);
        btnProduto.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_TEXT, 2),
            new EmptyBorder(5, 12, 5, 12)
        ));
        btnProduto.addActionListener(e -> abrirTela("produtos"));
        acoes.add(btnProduto);

        cardAcoes.add(lblAcoesTitulo, BorderLayout.NORTH);
        cardAcoes.add(acoes, BorderLayout.CENTER);

        colDir.add(cardAlertas);
        colDir.add(Box.createVerticalStrut(14));
        colDir.add(cardAcoes);

        linha.add(cardMov);
        linha.add(colDir);
        return linha;
    }

    // ── Gráfico de barras ─────────────────────────────────────────────────
    private JPanel construirGraficoCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel titulo = new JLabel("MOVIMENTAÇÕES — ÚLTIMOS 7 DIAS");
        titulo.setFont(FONT_LABEL);
        titulo.setForeground(COR_LABEL);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Painel de barras customizado
        int[] alturas = {45, 60, 90, 50, 70, 30, 55};
        String[] dias  = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Hoje"};
        boolean[] ativo = {false, false, true, false, false, false, true};

        JPanel grafico = new JPanel(new GridLayout(1, 7, 8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Linha do eixo X
                g.setColor(COR_BORDER);
                g.drawLine(0, getHeight() - 20, getWidth(), getHeight() - 20);
            }
        };
        grafico.setBackground(COR_SURFACE);
        grafico.setPreferredSize(new Dimension(0, 110));

        for (int i = 0; i < 7; i++) {
            final int altura = alturas[i];
            final boolean hi  = ativo[i];
            final String dia  = dias[i];

            JPanel col = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    int barW = getWidth() - 8;
                    int barX = 4;
                    int barH = altura;
                    int barY = getHeight() - 20 - barH;
                    g2.setColor(hi ? COR_TEXT : COR_BORDER);
                    g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
                    g2.setColor(COR_LABEL);
                    g2.setFont(FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    int lx = (getWidth() - fm.stringWidth(dia)) / 2;
                    g2.drawString(dia, lx, getHeight() - 5);
                    g2.dispose();
                }
            };
            col.setBackground(COR_SURFACE);
            grafico.add(col);
        }

        card.add(titulo, BorderLayout.NORTH);
        card.add(grafico, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Carregamento de dados do banco
    // ─────────────────────────────────────────────────────────────────────
    private void carregarDados() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int totalProdutos = 0;
            BigDecimal valorEstoque = BigDecimal.ZERO;
            int alertas = 0;
            int movHoje = 0;

            @Override
            protected Void doInBackground() {
                try {
                    // KPI 1: total de produtos ativos
                    PreparedStatement st1 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM produtos WHERE status = 'Ativo'");
                    ResultSet rs1 = st1.executeQuery();
                    if (rs1.next()) totalProdutos = rs1.getInt(1);

                    // KPI 2: valor total em estoque (SUM precoCusto * quantidade)
                    PreparedStatement st2 = conn.prepareStatement(
                        "SELECT SUM(preco_custo * quantidade) FROM produtos WHERE status = 'Ativo'");
                    ResultSet rs2 = st2.executeQuery();
                    if (rs2.next() && rs2.getBigDecimal(1) != null)
                        valorEstoque = rs2.getBigDecimal(1);

                    // KPI 3: produtos com quantidade = 0
                    PreparedStatement st3 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM produtos WHERE quantidade = 0 AND status = 'Ativo'");
                    ResultSet rs3 = st3.executeQuery();
                    if (rs3.next()) alertas = rs3.getInt(1);

                    // KPI 4: movimentações de hoje
                    PreparedStatement st4 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM movimentacao_estoque WHERE DATE(data_movimentacao) = CURDATE()");
                    ResultSet rs4 = st4.executeQuery();
                    if (rs4.next()) movHoje = rs4.getInt(1);

                    // Alertas: produtos zerados ou abaixo de 5
                    PreparedStatement stA = conn.prepareStatement(
                        "SELECT nome_produto, quantidade, marca FROM produtos " +
                        "WHERE quantidade <= 5 AND status = 'Ativo' ORDER BY quantidade ASC LIMIT 5");
                    ResultSet rsA = stA.executeQuery();
                    SwingUtilities.invokeLater(() -> {
                        painelAlertas.removeAll();
                        try {
                            boolean achouAlerta = false;
                            while (rsA.next()) {
                                achouAlerta = true;
                                String nome = rsA.getString("nome_produto");
                                int qtd = rsA.getInt("quantidade");
                                String marca = rsA.getString("marca");
                                painelAlertas.add(construirItemAlerta(nome, qtd, marca));
                                painelAlertas.add(Box.createVerticalStrut(4));
                            }
                            if (!achouAlerta) {
                                JLabel ok = new JLabel("✅  Nenhum alerta no momento");
                                ok.setFont(FONT_BODY);
                                ok.setForeground(COR_GREEN);
                                painelAlertas.add(ok);
                            }
                        } catch (SQLException e) {
                            System.out.println("Erro alertas: " + e.getMessage());
                        }
                        painelAlertas.revalidate();
                        painelAlertas.repaint();
                    });

                    // Últimas movimentações
                    PreparedStatement stM = conn.prepareStatement(
                        "SELECT m.tipo_movimentacao, m.quantidade, m.data_movimentacao, " +
                        "p.nome_produto, u.nome_usuario " +
                        "FROM movimentacao_estoque m " +
                        "JOIN produtos p ON m.id_produto = p.id_produto " +
                        "JOIN usuarios u ON m.id_usuario = u.id_usuario " +
                        "ORDER BY m.data_movimentacao DESC LIMIT 5");
                    ResultSet rsM = stM.executeQuery();
                    SwingUtilities.invokeLater(() -> {
                        painelMovimentacoes.removeAll();
                        try {
                            while (rsM.next()) {
                                String tipo  = rsM.getString("tipo_movimentacao");
                                int qtd      = rsM.getInt("quantidade");
                                String prod  = rsM.getString("nome_produto");
                                String user  = rsM.getString("nome_usuario");
                                Timestamp ts = rsM.getTimestamp("data_movimentacao");
                                String hora  = ts.toLocalDateTime()
                                    .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
                                painelMovimentacoes.add(
                                    construirItemMovimentacao(tipo, prod, qtd, user, hora));
                                painelMovimentacoes.add(Box.createVerticalStrut(2));
                            }
                        } catch (SQLException e) {
                            System.out.println("Erro movimentações: " + e.getMessage());
                        }
                        painelMovimentacoes.revalidate();
                        painelMovimentacoes.repaint();
                    });

                } catch (SQLException e) {
                    System.out.println("Erro ao carregar dashboard: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                // Atualiza KPIs na EDT
                lblKpiProdutos.setText(String.valueOf(totalProdutos));
                lblKpiValor.setText("R$ " + String.format("%,.2f", valorEstoque));
                lblKpiAlertas.setText(String.valueOf(alertas));
                lblKpiMovimentos.setText(String.valueOf(movHoje));
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Itens da lista
    // ─────────────────────────────────────────────────────────────────────
    private JPanel construirItemMovimentacao(String tipo, String produto,
                                              int qtd, String usuario, String hora) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(COR_SURFACE);
        item.setBorder(new MatteBorder(0, 0, 1, 0, COR_BORDER));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Dot colorido
        boolean entrada = "Entrada".equals(tipo);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(entrada ? COR_GREEN : COR_RED);
                g2.fillOval(2, 8, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 0));
        dot.setBackground(COR_SURFACE);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(COR_SURFACE);
        info.setBorder(new EmptyBorder(6, 6, 6, 0));

        String sinal = entrada ? "+" : "−";
        JLabel lblProd = new JLabel(produto + "   " + sinal + qtd + " un.");
        lblProd.setFont(FONT_BODY);
        lblProd.setForeground(COR_TEXT);

        JLabel lblMeta = new JLabel(tipo + "  ·  " + hora + "  ·  " + usuario);
        lblMeta.setFont(FONT_SMALL);
        lblMeta.setForeground(COR_LABEL);

        info.add(lblProd);
        info.add(lblMeta);

        item.add(dot, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);
        return item;
    }

    private JPanel construirItemAlerta(String nome, int qtd, String marca) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        Color bgAlerta = qtd == 0 ? new Color(0xFDECEA) : new Color(0xFEF3DC);
        Color borda    = qtd == 0 ? COR_RED : COR_YELLOW;

        item.setBackground(bgAlerta);
        item.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 3, 0, 0, borda),
            new EmptyBorder(6, 10, 6, 10)
        ));

        String icone = qtd == 0 ? "✖" : "!";
        JLabel lblIco = new JLabel(icone);
        lblIco.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIco.setForeground(borda);
        lblIco.setPreferredSize(new Dimension(20, 0));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(bgAlerta);

        JLabel lblNome = new JLabel(nome);
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblNome.setForeground(COR_TEXT);

        String descQtd = qtd == 0 ? "Estoque zerado" : "quantidade: " + qtd;
        JLabel lblQtd = new JLabel(descQtd + (marca != null ? "  ·  " + marca : ""));
        lblQtd.setFont(FONT_SMALL);
        lblQtd.setForeground(COR_MUTED);

        info.add(lblNome);
        info.add(lblQtd);

        item.add(lblIco, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Navegação entre telas
    // ─────────────────────────────────────────────────────────────────────
    private void abrirTela(String tela) {
        painelConteudo.removeAll();
        switch (tela) {
            case "movimentacoes" ->
                painelConteudo.add(new TelaMovimentacoes(usuarioLogado), BorderLayout.CENTER);
            case "produtos" ->
                painelConteudo.add(new TelaProdutos(usuarioLogado), BorderLayout.CENTER);
            case "fornecedores" ->
                painelConteudo.add(new TelaFornecedores(usuarioLogado), BorderLayout.CENTER);
            case "usuarios" ->
                painelConteudo.add(new TelaUsuarios(usuarioLogado), BorderLayout.CENTER);
            case "notas" ->
                painelConteudo.add(new TelaNotasFiscais(usuarioLogado), BorderLayout.CENTER);
            case "dashboard" ->
                painelConteudo.add(construirDashboard(), BorderLayout.CENTER);
            default -> JOptionPane.showMessageDialog(this,
                "Tela '" + tela + "' ainda não implementada.",
                "Em construção", JOptionPane.INFORMATION_MESSAGE);
        }
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers de componentes
    // ─────────────────────────────────────────────────────────────────────
    private JButton criarNavItem(String texto, boolean ativo, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(ativo
            ? new Font("SansSerif", Font.BOLD, 12)
            : FONT_BODY);
        btn.setForeground(ativo ? COR_TEXT : COR_MUTED);
        btn.setBackground(ativo ? COR_ACCENT : COR_SURFACE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, ativo ? 3 : 0, 0, 0, ativo ? COR_TEXT : COR_SURFACE),
            new EmptyBorder(10, ativo ? 13 : 16, 10, 16)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> acao.run());
        return btn;
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
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel criarBadgeNivel(String nivel) {
        JLabel badge = new JLabel(nivel);
        badge.setFont(new Font("Monospaced", Font.BOLD, 9));
        badge.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COR_BORDER, 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        switch (nivel) {
            case "Administrador" -> {
                badge.setBackground(COR_TEXT);
                badge.setForeground(Color.WHITE);
            }
            case "Operador" -> {
                badge.setBackground(new Color(0xDDEEFF));
                badge.setForeground(COR_BLUE);
            }
            default -> {
                badge.setBackground(COR_TAG);
                badge.setForeground(COR_LABEL);
            }
        }
        badge.setOpaque(true);
        return badge;
    }

    private String pegarIniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isEmpty()) return "?";
        String[] partes = nomeCompleto.trim().split(" ");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase();
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1))
            .toUpperCase();
    }

    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Teste com usuário mockado — remover quando TelaLogin estiver integrado
        Usuario teste = new Usuario(1, "Cauã Silva", "cauasilva", "", "Administrador");
        SwingUtilities.invokeLater(() -> new TelaDashboard(teste).setVisible(true));
    }
}