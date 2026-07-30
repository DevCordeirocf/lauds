package lauds.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import lauds.Dao.LaudoDAO;
import lauds.Dao.LaudoDAO.RegistroHistorico;
import lauds.Model.EstadoLaudo;

public final class PainelHistorico extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JanelaPrincipal janelaPrincipal;
    private final JTable tabelaHistorico;
    private final DefaultTableModel modeloTabela;
    private final JTextArea areaTextoPreview;
    private final JTextField txtPesquisa;
    private final TableRowSorter<DefaultTableModel> sorter;
    private List<RegistroHistorico> listaAtual = new ArrayList<>();

    public PainelHistorico(JanelaPrincipal janelaPrincipal) {
        this.janelaPrincipal = janelaPrincipal;
        setLayout(new BorderLayout());
        setBackground(TemaUI.FUNDO);
        setForeground(TemaUI.TEXTO);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        TemaUI.aplicarSplit(splitPane);
        splitPane.setDividerLocation(540);
        splitPane.setContinuousLayout(true);

        JPanel painelEsquerdo = new JPanel(new BorderLayout(0, 14));
        TemaUI.aplicarCard(painelEsquerdo);

        JPanel painelTopo = new JPanel(new BorderLayout(12, 0));
        painelTopo.setOpaque(false);

        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, -4));
        painelPesquisa.setOpaque(false);
        painelPesquisa.add(TemaUI.label("Pesquisar OS"));
        txtPesquisa = TemaUI.campoTexto();
        txtPesquisa.setColumns(18);
        painelPesquisa.add(txtPesquisa);
        painelTopo.add(painelPesquisa, BorderLayout.CENTER);

        JButton btnAtualizar = TemaUI.botaoPrimario("Atualizar lista");
        btnAtualizar.addActionListener(e -> carregarDados());
        painelTopo.add(btnAtualizar, BorderLayout.EAST);
        painelEsquerdo.add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nº OS", "Data/Hora", "Assinatura", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaHistorico = new JTable(modeloTabela);
        tabelaHistorico.removeColumn(tabelaHistorico.getColumnModel().getColumn(0));
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(145);
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(110);
        TemaUI.aplicarTabela(tabelaHistorico);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaHistorico.setRowSorter(sorter);
        JScrollPane scrollTabela = TemaUI.scroll(tabelaHistorico);
        scrollTabela.getViewport().setBackground(TemaUI.CARD_CLARO);
        painelEsquerdo.add(scrollTabela, BorderLayout.CENTER);

        JPanel painelDireito = new JPanel(new BorderLayout(0, 12));
        TemaUI.aplicarCard(painelDireito);
        JLabel preview = TemaUI.subtitulo("Pré-visualização do laudo");
        painelDireito.add(preview, BorderLayout.NORTH);

        areaTextoPreview = TemaUI.areaTexto(false);
        areaTextoPreview.setEditable(false);
        painelDireito.add(TemaUI.scroll(areaTextoPreview), BorderLayout.CENTER);

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelAcoes.setOpaque(false);
        JButton btnCopiar = TemaUI.botaoSecundario("Copiar laudo");
        JButton btnReabrir = TemaUI.botaoPrimario("Reabrir OS");
        btnCopiar.addActionListener(e -> copiarSelecionado());
        btnReabrir.addActionListener(e -> reabrirSelecionado());
        painelAcoes.add(btnCopiar);
        painelAcoes.add(btnReabrir);
        painelDireito.add(painelAcoes, BorderLayout.SOUTH);

        tabelaHistorico.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                RegistroHistorico registro = registroSelecionado();
                areaTextoPreview.setText(registro == null ? "" : registro.textoLaudo());
            }
        });

        tabelaHistorico.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    reabrirSelecionado();
                }
            }
        });

        txtPesquisa.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTabela();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTabela();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTabela();
            }
        });

        splitPane.setLeftComponent(painelEsquerdo);
        splitPane.setRightComponent(painelDireito);
        add(splitPane, BorderLayout.CENTER);
        carregarDados();
    }

    public final void carregarDados() {
        modeloTabela.setRowCount(0);
        listaAtual = LaudoDAO.buscarHistorico("");

        for (RegistroHistorico registro : listaAtual) {
            modeloTabela.addRow(new Object[]{
                    registro.id(),
                    registro.numeroOs(),
                    registro.dataAtualizacao(),
                    registro.assinatura(),
                    registro.status()
            });
        }

        filtrarTabela();
    }

    private void filtrarTabela() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo), 1));
        }
    }

    private RegistroHistorico registroSelecionado() {
        int linhaVisivel = tabelaHistorico.getSelectedRow();
        if (linhaVisivel < 0) {
            return null;
        }

        int linhaModelo = tabelaHistorico.convertRowIndexToModel(linhaVisivel);
        int id = (Integer) modeloTabela.getValueAt(linhaModelo, 0);
        return LaudoDAO.buscarPorId(id);
    }

    private void copiarSelecionado() {
        String texto = areaTextoPreview.getText();
        if (texto == null || texto.isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecione um laudo primeiro.");
            return;
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
        JOptionPane.showMessageDialog(this, "Laudo copiado!");
    }

    private void reabrirSelecionado() {
        RegistroHistorico registro = registroSelecionado();
        if (registro == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS primeiro.");
            return;
        }

        EstadoLaudo estado = PainelLaudoOS.estadoDeJson(registro.dadosInterface());
        if (estado == null) {
            JOptionPane.showMessageDialog(this, "Esse registro não tem dados de interface para reabrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        janelaPrincipal.abrirAbaOS(registro.numeroOs(), estado, "RASCUNHO".equalsIgnoreCase(registro.status()));
    }
}
