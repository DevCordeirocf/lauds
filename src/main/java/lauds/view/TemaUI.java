package lauds.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;

final class TemaUI {

    static final Color FUNDO = new Color(232, 239, 245);
    static final Color PAINEL = new Color(10, 44, 70);
    static final Color CARD = Color.WHITE;
    static final Color CARD_CLARO = new Color(247, 250, 253);
    static final Color TEXTO = new Color(246, 250, 253);
    static final Color TEXTO_ESCURO = new Color(20, 39, 56);
    static final Color TEXTO_SUAVE = new Color(92, 113, 130);
    static final Color AZUL = new Color(14, 86, 135);
    static final Color AZUL_CLARO = new Color(211, 231, 244);
    static final Color VERDE = new Color(24, 139, 98);
    static final Color ALERTA = new Color(200, 59, 69);
    static final Color BORDA = new Color(205, 218, 228);

    static final Font TITULO = new Font("Segoe UI", Font.BOLD, 23);
    static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 18);
    static final Font CORPO = new Font("Segoe UI", Font.PLAIN, 15);
    static final Font CORPO_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    static final Font TEXTO_AREA = new Font("Consolas", Font.PLAIN, 16);

    private TemaUI() {
    }

    static void aplicarFundo(JComponent componente) {
        componente.setBackground(FUNDO);
        componente.setForeground(TEXTO);
    }

    static void aplicarPainel(JComponent componente) {
        componente.setBackground(PAINEL);
        componente.setForeground(TEXTO);
    }

    static void aplicarCard(JComponent componente) {
        componente.setBackground(CARD);
        componente.setForeground(TEXTO_ESCURO);
        componente.setBorder(new CompoundBorder(new LineBorder(BORDA, 1, true), new EmptyBorder(18, 18, 18, 18)));
    }

    static JLabel titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(TITULO);
        label.setForeground(TEXTO_ESCURO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel subtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(SUBTITULO);
        label.setForeground(TEXTO_ESCURO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel label(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(CORPO_BOLD);
        label.setForeground(TEXTO_SUAVE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JButton botaoPrimario(String texto) {
        return botaoBase(texto, AZUL, Color.WHITE, AZUL);
    }

    static JButton botaoDestaque(String texto) {
        JButton botao = botaoBase(texto, VERDE, Color.WHITE, VERDE);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 17));
        botao.setPreferredSize(new Dimension(230, 46));
        return botao;
    }

    static JButton botaoSecundario(String texto) {
        return botaoBase(texto, new Color(240, 246, 250), TEXTO_ESCURO, BORDA);
    }

    static JTextField campoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(CORPO);
        campo.setBackground(Color.WHITE);
        campo.setForeground(TEXTO_ESCURO);
        campo.setCaretColor(TEXTO_ESCURO);
        campo.setBorder(campoBorda());
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        campo.setPreferredSize(new Dimension(180, 42));
        return campo;
    }

    static JTextArea areaTexto(boolean escura) {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(TEXTO_AREA);
        area.setMargin(new java.awt.Insets(18, 18, 18, 18));
        if (escura) {
            area.setBackground(new Color(248, 251, 253));
            area.setForeground(TEXTO_ESCURO);
            area.setCaretColor(TEXTO_ESCURO);
        } else {
            area.setBackground(new Color(248, 251, 253));
            area.setForeground(TEXTO_ESCURO);
            area.setCaretColor(TEXTO_ESCURO);
        }
        return area;
    }

    static void aplicarCheck(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setFont(CORPO_BOLD);
        checkBox.setForeground(TEXTO_ESCURO);
        checkBox.setFocusPainted(false);
    }

    static void aplicarRadio(JRadioButton radioButton) {
        radioButton.setOpaque(false);
        radioButton.setFont(CORPO);
        radioButton.setForeground(TEXTO_ESCURO);
        radioButton.setFocusPainted(false);
    }

    static JScrollPane scroll(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(new LineBorder(BORDA, 1, true));
        scroll.getViewport().setBackground(CARD_CLARO);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBlockIncrement(160);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setBlockIncrement(160);
        return scroll;
    }

    static void aplicarSplit(JSplitPane splitPane) {
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(8);
        splitPane.setBackground(FUNDO);
    }

    static void aplicarTabela(JTable tabela) {
        tabela.setFont(CORPO);
        tabela.setRowHeight(34);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setSelectionBackground(AZUL_CLARO);
        tabela.setSelectionForeground(TEXTO_ESCURO);
        tabela.setBackground(Color.WHITE);
        tabela.setForeground(TEXTO_ESCURO);
        tabela.setGridColor(new Color(226, 235, 242));

        JTableHeader header = tabela.getTableHeader();
        header.setFont(CORPO_BOLD);
        header.setBackground(new Color(236, 244, 249));
        header.setForeground(TEXTO_ESCURO);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
    }

    private static JButton botaoBase(String texto, Color fundo, Color textoCor, Color borda) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fundo.darker() : fundo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        botao.setFont(CORPO_BOLD);
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setForeground(textoCor);
        botao.setBorder(new CompoundBorder(new LineBorder(borda, 1, true), new EmptyBorder(10, 18, 10, 18)));
        botao.setPreferredSize(new Dimension(Math.max(150, texto.length() * 10), 42));
        return botao;
    }

    private static Border campoBorda() {
        return new CompoundBorder(new LineBorder(new Color(164, 195, 216), 1, true), new EmptyBorder(8, 10, 8, 10));
    }
}
