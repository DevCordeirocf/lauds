package lauds.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.gson.Gson;

import lauds.Dao.LaudoDAO;
import lauds.Model.EstadoLaudo;
import lauds.Model.EstadoLaudo.EstadoComponente;
import lauds.Model.EstadoLaudo.EstadoDisco;

@SuppressWarnings({"serial", "this-escape"})
public class PainelLaudoOS extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();

    private final String numeroOS;
    private final JanelaPrincipal janelaPrincipal;
    private final JTextArea areaTextoLaudo;
    private final JTextField txtLocalizacao;
    private final JTextField txtAssinatura;
    private final JTextField txtProblema;
    private final JCheckBox chkPlacaMae;
    private final JTextField txtValorPlaca;
    private final List<PainelComponente> listaComponentes;
    private final List<PainelDisco> listaDiscos;
    private final JPanel painelListaDiscos;
    private final JPanel painelEsquerdo;
    private final JPanel painelValorPlaca;
    
    private final JPanel painelProcessadorPlaca;
    private final JTextField txtProcessador;


    public PainelLaudoOS(String numeroOS, EstadoLaudo estadoInicial, JanelaPrincipal janelaPrincipal) {
        this.numeroOS = numeroOS;
        this.janelaPrincipal = janelaPrincipal;
        this.listaComponentes = new ArrayList<>();
        this.listaDiscos = new ArrayList<>();

        setLayout(new BorderLayout());
        TemaUI.aplicarFundo(this);
        setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        TemaUI.aplicarSplit(splitPane);
        splitPane.setDividerLocation(560);

        painelEsquerdo = new JPanel();
        painelEsquerdo.setLayout(new BoxLayout(painelEsquerdo, BoxLayout.Y_AXIS));
        TemaUI.aplicarCard(painelEsquerdo);

        painelEsquerdo.add(TemaUI.titulo("OS " + numeroOS));
        painelEsquerdo.add(Box.createVerticalStrut(18));

        painelEsquerdo.add(TemaUI.label("Localização"));
        txtLocalizacao = criarCampoTexto();
        painelEsquerdo.add(txtLocalizacao);
        painelEsquerdo.add(Box.createVerticalStrut(14));

        painelEsquerdo.add(TemaUI.label("Assinatura"));
        txtAssinatura = criarCampoTexto();
        painelEsquerdo.add(txtAssinatura);
        painelEsquerdo.add(Box.createVerticalStrut(14));

        painelEsquerdo.add(TemaUI.label("Problema encontrado"));
        txtProblema = criarCampoTexto();
        painelEsquerdo.add(txtProblema);
        painelEsquerdo.add(Box.createVerticalStrut(20));

        painelEsquerdo.add(separador());
        chkPlacaMae = new JCheckBox("PLACA-MÃE DANIFICADA");
        TemaUI.aplicarCheck(chkPlacaMae);
        chkPlacaMae.setForeground(TemaUI.ALERTA);

        painelValorPlaca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        painelValorPlaca.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelValorPlaca.setOpaque(false);
        painelValorPlaca.add(TemaUI.label("Valor do reparo:"));
        txtValorPlaca = TemaUI.campoTexto();
        txtValorPlaca.setColumns(12);
        txtValorPlaca.setPreferredSize(new Dimension(180, 38));
        painelValorPlaca.add(txtValorPlaca);
        painelValorPlaca.setVisible(false);


        painelProcessadorPlaca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        painelProcessadorPlaca.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelProcessadorPlaca.setOpaque(false);
        painelProcessadorPlaca.add(TemaUI.label("Processador:"));
        txtProcessador = TemaUI.campoTexto();
        txtProcessador.setColumns(12);
        txtProcessador.setPreferredSize(new Dimension(180, 38));
        painelProcessadorPlaca.add(txtProcessador);
        painelProcessadorPlaca.setVisible(false); 
        


        chkPlacaMae.addActionListener(e -> {
            boolean selecionado = chkPlacaMae.isSelected();

            painelValorPlaca.setVisible(selecionado);
            painelProcessadorPlaca.setVisible(selecionado);

            painelEsquerdo.revalidate();
            painelEsquerdo.repaint();
            gerarTextoLaudo();
        });

        painelEsquerdo.add(chkPlacaMae);
        painelEsquerdo.add(painelValorPlaca);
        painelEsquerdo.add(painelProcessadorPlaca);
        painelEsquerdo.add(Box.createVerticalStrut(10));
        painelEsquerdo.add(separador());

        painelEsquerdo.add(Box.createVerticalStrut(12));
        JPanel painelHeaderArmazenamento = new JPanel(new BorderLayout(12, 0));
        painelHeaderArmazenamento.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelHeaderArmazenamento.setOpaque(false);
        painelHeaderArmazenamento.add(TemaUI.subtitulo("Armazenamento"), BorderLayout.WEST);

        JButton btnAddDisco = TemaUI.botaoSecundario("+ Adicionar HD/SSD");
        btnAddDisco.addActionListener(e -> adicionarDiscoPorDialogo());
        painelHeaderArmazenamento.add(btnAddDisco, BorderLayout.EAST);
        painelEsquerdo.add(painelHeaderArmazenamento);
        painelEsquerdo.add(Box.createVerticalStrut(8));

        painelListaDiscos = new JPanel();
        painelListaDiscos.setLayout(new BoxLayout(painelListaDiscos, BoxLayout.Y_AXIS));
        painelListaDiscos.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelListaDiscos.setOpaque(false);
        painelEsquerdo.add(painelListaDiscos);

        painelEsquerdo.add(separador());

        painelEsquerdo.add(TemaUI.subtitulo("Checklist de Componentes"));
        painelEsquerdo.add(TemaUI.label("Marque o estado de cada item:"));
        painelEsquerdo.add(Box.createVerticalStrut(10));
        adicionarComponente("Tela");
        adicionarComponente("Teclado");
        adicionarComponente("Touchpad");
        adicionarComponente("Webcam");
        adicionarComponente("Alto falante");
        adicionarComponente("Bateria");
        adicionarComponente("Memória RAM");

        splitPane.setLeftComponent(TemaUI.scroll(painelEsquerdo));

        JPanel painelDireito = new JPanel(new BorderLayout(0, 12));
        TemaUI.aplicarCard(painelDireito);
        painelDireito.add(TemaUI.subtitulo("Laudo gerado automaticamente"), BorderLayout.NORTH);

        areaTextoLaudo = TemaUI.areaTexto(true);
        painelDireito.add(TemaUI.scroll(areaTextoLaudo), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelBotoes.setOpaque(false);
        JButton btnCopiar = TemaUI.botaoSecundario("Copiar texto");
        JButton btnRascunho = TemaUI.botaoSecundario("Salvar rascunho");
        JButton btnSalvar = TemaUI.botaoPrimario("Finalizar e salvar");

        btnCopiar.addActionListener(e -> copiarTexto(areaTextoLaudo.getText(), "Laudo copiado com sucesso!"));
        btnRascunho.addActionListener(e -> salvarRascunho());
        btnSalvar.addActionListener(e -> finalizarLaudo());

        painelBotoes.add(btnCopiar);
        painelBotoes.add(btnRascunho);
        painelBotoes.add(btnSalvar);
        painelDireito.add(painelBotoes, BorderLayout.SOUTH);

        splitPane.setRightComponent(painelDireito);
        add(splitPane, BorderLayout.CENTER);

        configurarAtualizacaoEmTempoReal();

        if (estadoInicial != null) {
            aplicarEstado(estadoInicial);
        }
        gerarTextoLaudo();
    }

    private JTextField criarCampoTexto() {
        JTextField campo = TemaUI.campoTexto();
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return campo;
    }

    private JSeparator separador() {
        JSeparator separador = new JSeparator();
        separador.setForeground(TemaUI.BORDA);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return separador;
    }

    private void adicionarComponente(String nome) {
        PainelComponente comp = new PainelComponente(nome, this::gerarTextoLaudo);
        listaComponentes.add(comp);
        painelEsquerdo.add(comp);
    }

    private void adicionarDiscoPorDialogo() {
        String nomeDisco = JOptionPane.showInputDialog(this, "Nome do Disco (Ex: SSD 256GB ou HD 1TB):");
        if (nomeDisco != null && !nomeDisco.trim().isEmpty()) {
            adicionarDisco(nomeDisco.trim(), null);
            gerarTextoLaudo();
        }
    }

    private void adicionarDisco(String nomeDisco, EstadoDisco estadoDisco) {
        PainelDisco novoDisco = new PainelDisco(nomeDisco, this::gerarTextoLaudo);
        if (estadoDisco != null) {
            novoDisco.aplicarEstado(estadoDisco.ok, estadoDisco.saude, estadoDisco.gravidade, estadoDisco.comBackup);
        }
        listaDiscos.add(novoDisco);
        painelListaDiscos.add(novoDisco);
        painelEsquerdo.revalidate();
        painelEsquerdo.repaint();
    }

    private void configurarAtualizacaoEmTempoReal() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                gerarTextoLaudo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                gerarTextoLaudo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                gerarTextoLaudo();
            }
        };

        txtLocalizacao.getDocument().addDocumentListener(listener);
        txtAssinatura.getDocument().addDocumentListener(listener);
        txtProblema.getDocument().addDocumentListener(listener);
        txtValorPlaca.getDocument().addDocumentListener(listener);
        txtProcessador.getDocument().addDocumentListener(listener);
    }

    private void gerarTextoLaudo() {
        StringBuilder texto = new StringBuilder();
        texto.append("Após os testes em bancada, foram detectadas as seguintes informações:\n\n");

        String problema = txtProblema.getText().trim();
        if (!problema.isEmpty()) {
            texto.append(problema).append("\n\n");
        }

        List<String> itensFuncionando = new ArrayList<>();
        List<String> testadosDepois = new ArrayList<>();
        List<String> discosOkFora = new ArrayList<>();
        boolean isPlacaMorta = chkPlacaMae.isSelected();

        if (isPlacaMorta) {
            texto.append("placa mãe danificada, necessário reparo da placa");
            String valor = txtValorPlaca.getText().trim();
            String Processador = txtProcessador.getText().trim();
            if (!valor.isEmpty()) {
                texto.append(", ").append(valor);
            }
            if (!Processador.isEmpty()) {
                texto.append( ", ").append(Processador);
            }
            
            texto.append("\n\n");
        }

        for (PainelComponente comp : listaComponentes) {
            if (!comp.foiVerificado()) continue;

            if (isPlacaMorta) {
                if (comp.getNome().equalsIgnoreCase("Memória RAM")) {
                    if (comp.isOk()) {
                        itensFuncionando.add(comp.getNome());
                    }
                } else {
                    testadosDepois.add(comp.getNome());
                }
            } else if (comp.isOk()) {
                itensFuncionando.add(comp.getNome());
            } else {
                texto.append(comp.getNome()).append(" danificado/com avaria, ")
                        .append(comp.getGravidade());
                String codigo = comp.getCodigo().trim();
                if (!codigo.isEmpty()) {
                    texto.append(", cod: ").append(codigo);
                }
                texto.append("\n");
            }
        }

        for (PainelDisco disco : listaDiscos) {
            if (disco.isOk()) {
                String nomeFormatado = disco.getNome();
                if (!disco.getSaude().trim().isEmpty()) {
                    nomeFormatado += " saúde em " + disco.getSaude().trim();
                }

                if (isPlacaMorta) {
                    itensFuncionando.add(nomeFormatado);
                } else {
                    discosOkFora.add(nomeFormatado);
                }
            } else {
                texto.append(disco.getNome()).append(" danificado/com avaria, ")
                        .append(disco.getGravidade()).append(", cod:\n")
                        .append(disco.getBackupTexto()).append("\n\n");
            }
        }

        if (!isPlacaMorta) {
            texto.append("\n");
        }

        if (isPlacaMorta) {
            if (!itensFuncionando.isEmpty()) {
                texto.append("(").append(String.join(" | ", itensFuncionando)).append(") estão funcionando.\n");
            }
            if (!testadosDepois.isEmpty()) {
                texto.append("(").append(String.join(" | ", testadosDepois)).append(") serão testados após o serviço.\n");
            }
        } else {
            for (String discoTexto : discosOkFora) {
                texto.append(discoTexto).append(".\n");
            }
            if (!itensFuncionando.isEmpty()) {
                texto.append("(").append(String.join(" | ", itensFuncionando)).append(") estão funcionando.\n");
            }
        }

        texto.append("ASS: ").append(txtAssinatura.getText().trim()).append("\n");
        texto.append("loc: ").append(txtLocalizacao.getText().trim());
        areaTextoLaudo.setText(texto.toString());
    }

    private void salvarRascunho() {
        gerarTextoLaudo();
        boolean sucesso = LaudoDAO.salvarRascunho(
                numeroOS,
                txtAssinatura.getText().trim(),
                txtLocalizacao.getText().trim(),
                areaTextoLaudo.getText(),
                exportarEstadoJson()
        );

        if (sucesso) {
            janelaPrincipal.atualizarHistorico();
            JOptionPane.showMessageDialog(this, "Rascunho salvo com sucesso!\nOS: " + numeroOS, "Salvo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao salvar rascunho.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarLaudo() {
        gerarTextoLaudo();
        boolean sucesso = LaudoDAO.salvarLaudoFinalizado(
                numeroOS,
                txtAssinatura.getText().trim(),
                txtLocalizacao.getText().trim(),
                areaTextoLaudo.getText(),
                exportarEstadoJson()
        );

        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Auditoria salva com sucesso!\nOS: " + numeroOS, "Salvo", JOptionPane.INFORMATION_MESSAGE);
            janelaPrincipal.fecharAba(this);
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copiarTexto(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            return;
        }
        StringSelection selecao = new StringSelection(texto);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selecao, null);
        JOptionPane.showMessageDialog(this, mensagem);
    }

    private String exportarEstadoJson() {
        EstadoLaudo estado = new EstadoLaudo();
        estado.numeroOS = numeroOS;
        estado.localizacao = txtLocalizacao.getText().trim();
        estado.assinatura = txtAssinatura.getText().trim();
        estado.problema = txtProblema.getText().trim();
        estado.placaMaeDanificada = chkPlacaMae.isSelected();
        estado.valorPlaca = txtValorPlaca.getText().trim();

        for (PainelComponente comp : listaComponentes) {
            EstadoComponente item = new EstadoComponente();
            item.nome = comp.getNome();
            item.ok = comp.isOk();
            item.verificado = comp.foiVerificado();
            item.gravidade = comp.getTipoGravidade();
            item.codigo = comp.getCodigo().trim();
            estado.componentes.add(item);
        }

        for (PainelDisco disco : listaDiscos) {
            EstadoDisco item = new EstadoDisco();
            item.nome = disco.getNome();
            item.ok = disco.isOk();
            item.saude = disco.getSaude().trim();
            item.gravidade = disco.getTipoGravidade();
            item.comBackup = disco.isComBackup();
            estado.discos.add(item);
        }

        return GSON.toJson(estado);
    }

    public static EstadoLaudo estadoDeJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return GSON.fromJson(json, EstadoLaudo.class);
    }

    private void aplicarEstado(EstadoLaudo estado) {
        txtLocalizacao.setText(valorOuVazio(estado.localizacao));
        txtAssinatura.setText(valorOuVazio(estado.assinatura));
        txtProblema.setText(valorOuVazio(estado.problema));
        chkPlacaMae.setSelected(estado.placaMaeDanificada);
        txtValorPlaca.setText(valorOuVazio(estado.valorPlaca));
        painelValorPlaca.setVisible(estado.placaMaeDanificada);

        if (estado.componentes != null) {
            for (EstadoComponente estadoComponente : estado.componentes) {
                for (PainelComponente componente : listaComponentes) {
                    if (componente.getNome().equalsIgnoreCase(estadoComponente.nome)) {
                        if (estadoComponente.verificado) {
                            componente.aplicarEstado(estadoComponente.ok, estadoComponente.gravidade, estadoComponente.codigo);
                        }
                        break;
                    }
                }
            }
        }

        if (estado.discos != null) {
            for (EstadoDisco disco : estado.discos) {
                if (disco.nome != null && !disco.nome.isBlank()) {
                    adicionarDisco(disco.nome, disco);
                }
            }
        }
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
