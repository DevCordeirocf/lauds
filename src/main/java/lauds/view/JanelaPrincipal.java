package lauds.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import lauds.Model.EstadoLaudo;

@SuppressWarnings({"serial", "this-escape"})
public class JanelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane sistemaDeAbas;
    private final PainelHistorico abaHistorico;

    public JanelaPrincipal() {
        setTitle("Gerador de Laudos Técnicos");
        setSize(1360, 820);
        setMinimumSize(new Dimension(1100, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(TemaUI.FUNDO);

        UIManager.put("TabbedPane.selected", TemaUI.FUNDO);

        sistemaDeAbas = new JTabbedPane();
        sistemaDeAbas.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sistemaDeAbas.setBackground(TemaUI.FUNDO);
        sistemaDeAbas.setForeground(TemaUI.TEXTO_ESCURO);
        sistemaDeAbas.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        sistemaDeAbas.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 14, 14));

        abaHistorico = new PainelHistorico(this);
        sistemaDeAbas.addTab("Histórico", abaHistorico);

        JPanel barraSuperior = new JPanel(new BorderLayout(18, 0));
        barraSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 22, 16, 22));
        TemaUI.aplicarPainel(barraSuperior);

        JLabel titulo = TemaUI.titulo("Gerador de Laudos Técnicos");
        titulo.setForeground(TemaUI.TEXTO);
        titulo.setHorizontalAlignment(SwingConstants.LEFT);
        barraSuperior.add(titulo, BorderLayout.WEST);

        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        TemaUI.aplicarPainel(painelBotao);
        JButton btnNovaOS = TemaUI.botaoPrimario("+ Adicionar Nova OS");
        btnNovaOS.addActionListener(e -> criarNovaAbaOS());
        painelBotao.add(btnNovaOS);
        barraSuperior.add(painelBotao, BorderLayout.EAST);

        add(barraSuperior, BorderLayout.NORTH);
        add(sistemaDeAbas, BorderLayout.CENTER);
    }

    private void criarNovaAbaOS() {
        String numeroOS = JOptionPane.showInputDialog(this, "Digite o número da Ordem de Serviço (OS):");
        if (numeroOS != null && !numeroOS.trim().isEmpty()) {
            abrirAbaOS(numeroOS.trim(), null, false);
        }
    }

    public void abrirAbaOS(String numeroOS, EstadoLaudo estado, boolean rascunho) {
        PainelLaudoOS novaAbaOS = new PainelLaudoOS(numeroOS, estado, this);
        String marcador = rascunho ? "RASCUNHO" : "aberta";
        sistemaDeAbas.addTab("OS: " + numeroOS + " - " + marcador, novaAbaOS);
        sistemaDeAbas.setSelectedIndex(sistemaDeAbas.getTabCount() - 1);
    }

    public void fecharAba(PainelLaudoOS painel) {
        sistemaDeAbas.remove(painel);
        atualizarHistorico();
    }

    public void atualizarHistorico() {
        abaHistorico.carregarDados();
    }
}
