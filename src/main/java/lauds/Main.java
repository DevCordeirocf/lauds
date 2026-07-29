package lauds;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lauds.Dao.ConexaoSQLite;
import lauds.util.Updater;
import lauds.view.JanelaPrincipal;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando o Gerador de Laudos Técnicos...");
        ConexaoSQLite.inicializarBanco();
        Updater.checkForUpdates();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                System.out.println("Não foi possível aplicar o visual do sistema: " + e.getMessage());
            }

            JanelaPrincipal janela = new JanelaPrincipal();
            janela.setVisible(true);
        });
    }
}
