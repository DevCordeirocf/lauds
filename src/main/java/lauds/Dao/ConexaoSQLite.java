package lauds.Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoSQLite {

    private static final String URL = "jdbc:sqlite:laudos.db";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar com o banco: " + e.getMessage());
            return null;
        }
    }

    public static void inicializarBanco() {
        String sql = """
            CREATE TABLE IF NOT EXISTS laudos_auditoria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_os TEXT NOT NULL,
                assinatura TEXT,
                localizacao TEXT,
                texto_laudo TEXT NOT NULL,
                status TEXT NOT NULL,
                dados_interface TEXT,
                data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sql);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_laudos_os ON laudos_auditoria(numero_os)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_laudos_data ON laudos_auditoria(data_atualizacao)");
                System.out.println("Banco de dados e tabela inicializados com sucesso!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela: " + e.getMessage());
        }
    }
}
