package lauds.view;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings({"serial", "this-escape"})
public class PainelComponente extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JCheckBox chkItem;
    private final JPanel painelOpcoes;
    private final JRadioButton rbNecessario;
    private final JRadioButton rbRecomendado;
    private final JTextField txtCodigo;
    private final Runnable aoAlterar;

    public PainelComponente(String nomeItem, Runnable aoAlterar) {
        this.aoAlterar = aoAlterar;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 4, 0));

        chkItem = new JCheckBox(nomeItem, true);
        TemaUI.aplicarCheck(chkItem);

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
        txtCodigo.setPreferredSize(new java.awt.Dimension(90, 38));
        txtCodigo.setMaximumSize(new java.awt.Dimension(90, 38));

        JLabel seta = TemaUI.label("L->");
        JLabel codigo = TemaUI.label("Cód:");
        painelOpcoes.add(seta);
        painelOpcoes.add(rbNecessario);
        painelOpcoes.add(rbRecomendado);
        painelOpcoes.add(codigo);
        painelOpcoes.add(txtCodigo);
        painelOpcoes.setVisible(false);

        chkItem.addActionListener(e -> {
            painelOpcoes.setVisible(!chkItem.isSelected());
            revalidate();
            repaint();
            notificarAlteracao();
        });
        rbNecessario.addActionListener(e -> notificarAlteracao());
        rbRecomendado.addActionListener(e -> notificarAlteracao());
        txtCodigo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notificarAlteracao();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notificarAlteracao();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notificarAlteracao();
            }
        });

        add(chkItem);
        add(painelOpcoes);
    }

    public String getNome() {
        return chkItem.getText();
    }

    public boolean isOk() {
        return chkItem.isSelected();
    }

    public String getGravidade() {
        return rbNecessario.isSelected() ? "necessário a troca" : "recomendamos a troca";
    }

    public String getCodigo() {
        return txtCodigo.getText();
    }

    public String getTipoGravidade() {
        return rbNecessario.isSelected() ? "NECESSÁRIO" : "RECOMENDADO";
    }

    public void aplicarEstado(boolean ok, String tipoGravidade, String codigo) {
        chkItem.setSelected(ok);
        rbNecessario.setSelected(!"RECOMENDADO".equalsIgnoreCase(tipoGravidade));
        rbRecomendado.setSelected("RECOMENDADO".equalsIgnoreCase(tipoGravidade));
        txtCodigo.setText(codigo == null ? "" : codigo);
        painelOpcoes.setVisible(!ok);
        revalidate();
        repaint();
    }

    private void notificarAlteracao() {
        if (aoAlterar != null) {
            aoAlterar.run();
        }
    }
}
