package lauds.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings({"this-escape"})
public class PainelComponente extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String nomeItem;
    private final JButton btnOk;
    private final JButton btnDefeito;
    private final JPanel painelOpcoes;
    private final JRadioButton rbNecessario;
    private final JRadioButton rbRecomendado;
    private final JTextField txtCodigo;
    private final Runnable aoAlterar;
    
    private boolean isOk = false;
    private boolean foiVerificado = false;

    public PainelComponente(String nomeItem, Runnable aoAlterar) {
        this.nomeItem = nomeItem;
        this.aoAlterar = aoAlterar;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JPanel painelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelHeader.setOpaque(false);
        painelHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNome = new JLabel(nomeItem);
        lblNome.setFont(TemaUI.CORPO_BOLD);
        lblNome.setPreferredSize(new Dimension(120, 30));
        
        btnOk = new JButton("OK");
        btnDefeito = new JButton("Defeito");
        
        configurarBotaoCheck(btnOk, TemaUI.VERDE);
        configurarBotaoCheck(btnDefeito, TemaUI.ALERTA);

        painelHeader.add(lblNome);
        painelHeader.add(btnOk);
        painelHeader.add(btnDefeito);

        painelOpcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        painelOpcoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelOpcoes.setOpaque(false);

        rbNecessario = new JRadioButton("Necessário");
        rbRecomendado = new JRadioButton("Recomendado");
        TemaUI.aplicarRadio(rbNecessario);
        TemaUI.aplicarRadio(rbRecomendado);

        ButtonGroup grupoOpcoes = new ButtonGroup();
        grupoOpcoes.add(rbNecessario);
        grupoOpcoes.add(rbRecomendado);
        rbNecessario.setSelected(true);

        txtCodigo = TemaUI.campoTexto();
        txtCodigo.setColumns(6);
        txtCodigo.setPreferredSize(new Dimension(90, 38));

        painelOpcoes.add(TemaUI.label("L->"));
        painelOpcoes.add(rbNecessario);
        painelOpcoes.add(rbRecomendado);
        painelOpcoes.add(TemaUI.label("Cód:"));
        painelOpcoes.add(txtCodigo);
        painelOpcoes.setVisible(false);

        btnOk.addActionListener(e -> setEstado(true));
        btnDefeito.addActionListener(e -> setEstado(false));
        
        rbNecessario.addActionListener(e -> notificarAlteracao());
        rbRecomendado.addActionListener(e -> notificarAlteracao());
        txtCodigo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { notificarAlteracao(); }
            @Override public void removeUpdate(DocumentEvent e) { notificarAlteracao(); }
            @Override public void changedUpdate(DocumentEvent e) { notificarAlteracao(); }
        });

        add(painelHeader);
        add(painelOpcoes);
    }

    private void configurarBotaoCheck(JButton btn, Color corAtivo) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setBackground(Color.white);
        btn.setForeground(TemaUI.TEXTO_SUAVE);
    }

    private void setEstado(boolean ok) {
        this.isOk = ok;
        this.foiVerificado = true;
        
        if (ok) {
            btnOk.setBackground(TemaUI.VERDE);
            btnOk.setForeground(Color.green);
            btnDefeito.setBackground(Color.white);
            btnDefeito.setForeground(TemaUI.TEXTO_SUAVE);
            painelOpcoes.setVisible(false);
        } else {
            btnDefeito.setBackground(TemaUI.ALERTA);
            btnDefeito.setForeground(Color.red);
            btnOk.setBackground(Color.WHITE);
            btnOk.setForeground(TemaUI.TEXTO_SUAVE);
            painelOpcoes.setVisible(true);
        }
        
        revalidate();
        repaint();
        notificarAlteracao();
    }

    public String getNome() { return nomeItem; }
    public boolean isOk() { return isOk; }
    public boolean foiVerificado() { return foiVerificado; }

    public String getGravidade() {
        return rbNecessario.isSelected() ? "necessário a troca" : "recomendamos a troca";
    }

    public String getCodigo() { return txtCodigo.getText(); }
    public String getTipoGravidade() { return rbNecessario.isSelected() ? "NECESSÁRIO" : "RECOMENDADO"; }

    public void aplicarEstado(boolean ok, String tipoGravidade, String codigo) {
        setEstado(ok);
        rbNecessario.setSelected(!"RECOMENDADO".equalsIgnoreCase(tipoGravidade));
        rbRecomendado.setSelected("RECOMENDADO".equalsIgnoreCase(tipoGravidade));
        txtCodigo.setText(codigo == null ? "" : codigo);
    }

    private void notificarAlteracao() {
        if (aoAlterar != null) {
            aoAlterar.run();
        }
    }
}
