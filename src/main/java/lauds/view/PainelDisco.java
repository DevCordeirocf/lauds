package lauds.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings({"serial", "this-escape"})
public class PainelDisco extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String nomeDisco;
    private final JCheckBox chkOk;
    private final JTextField txtSaude;
    private final JPanel painelOpcoes;
    private final JRadioButton rbNecessario;
    private final JRadioButton rbRecomendado;
    private final JRadioButton rbComBackup;
    private final JRadioButton rbSemBackup;
    private final Runnable aoAlterar;

    public PainelDisco(String nomeDisco, Runnable aoAlterar) {
        this.nomeDisco = nomeDisco;
        this.aoAlterar = aoAlterar;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JPanel linhaPrincipal = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        linhaPrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaPrincipal.setOpaque(false);

        linhaPrincipal.add(TemaUI.label("Disco: " + nomeDisco + " | Saúde:"));
        txtSaude = TemaUI.campoTexto();
        txtSaude.setColumns(4);
        txtSaude.setPreferredSize(new Dimension(76, 38));
        txtSaude.setMaximumSize(new Dimension(76, 38));
        linhaPrincipal.add(txtSaude);

        chkOk = new JCheckBox("OK", true);
        TemaUI.aplicarCheck(chkOk);
        linhaPrincipal.add(chkOk);

        painelOpcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        painelOpcoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelOpcoes.setOpaque(false);

        rbNecessario = new JRadioButton("Necessário");
        rbRecomendado = new JRadioButton("Recomendado");
        TemaUI.aplicarRadio(rbNecessario);
        TemaUI.aplicarRadio(rbRecomendado);

        ButtonGroup grupoGravidade = new ButtonGroup();
        grupoGravidade.add(rbNecessario);
        grupoGravidade.add(rbRecomendado);
        rbRecomendado.setSelected(true);

        rbComBackup = new JRadioButton("C/ Backup (21)");
        rbSemBackup = new JRadioButton("S/ Backup (2)");
        TemaUI.aplicarRadio(rbComBackup);
        TemaUI.aplicarRadio(rbSemBackup);

        ButtonGroup grupoBackup = new ButtonGroup();
        grupoBackup.add(rbComBackup);
        grupoBackup.add(rbSemBackup);
        rbComBackup.setSelected(true);

        painelOpcoes.add(TemaUI.label("L->"));
        painelOpcoes.add(rbNecessario);
        painelOpcoes.add(rbRecomendado);
        painelOpcoes.add(TemaUI.label("|"));
        painelOpcoes.add(rbComBackup);
        painelOpcoes.add(rbSemBackup);
        painelOpcoes.setVisible(false);

        chkOk.addActionListener(e -> {
            painelOpcoes.setVisible(!chkOk.isSelected());
            revalidate();
            repaint();
            notificarAlteracao();
        });
        rbNecessario.addActionListener(e -> notificarAlteracao());
        rbRecomendado.addActionListener(e -> notificarAlteracao());
        rbComBackup.addActionListener(e -> notificarAlteracao());
        rbSemBackup.addActionListener(e -> notificarAlteracao());
        txtSaude.getDocument().addDocumentListener(new DocumentListener() {
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

        add(linhaPrincipal);
        add(painelOpcoes);
    }

    public String getNome() {
        return nomeDisco;
    }

    public boolean isOk() {
        return chkOk.isSelected();
    }

    public String getSaude() {
        return txtSaude.getText();
    }

    public String getGravidade() {
        return rbNecessario.isSelected() ? "necessário a troca" : "recomendamos a troca";
    }

    public String getTipoGravidade() {
        return rbNecessario.isSelected() ? "NECESSARIO" : "RECOMENDADO";
    }

    public boolean isComBackup() {
        return rbComBackup.isSelected();
    }

    public String getBackupTexto() {
        if (rbComBackup.isSelected()) {
            return "necessário a instalação do sistema operacional com backup, cod: 21;";
        }
        return "necessário a instalação do sistema operacional sem backup, cod: 2;";
    }

    public void aplicarEstado(boolean ok, String saude, String tipoGravidade, boolean comBackup) {
        chkOk.setSelected(ok);
        txtSaude.setText(saude == null ? "" : saude);
        rbNecessario.setSelected("NECESSARIO".equalsIgnoreCase(tipoGravidade));
        rbRecomendado.setSelected(!"NECESSARIO".equalsIgnoreCase(tipoGravidade));
        rbComBackup.setSelected(comBackup);
        rbSemBackup.setSelected(!comBackup);
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
