package lauds.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LaudoDAO {

    public record RegistroHistorico(
            int id,
            String numeroOs,
            String dataAtualizacao,
            String assinatura,
            String status,
            String textoLaudo,
            String dadosInterface
    ) {
    }

    public static boolean salvarLaudoFinalizado(String os, String assinatura, String localizacao, String textoLaudo, String dadosInterface) {
        excluirRascunho(os);
        return inserirLaudo(os, assinatura, localizacao, textoLaudo, "FINALIZADO", dadosInterface);
    }

    public static boolean salvarRascunho(String os, String assinatura, String localizacao, String textoLaudo, String dadosInterface) {
        excluirRascunho(os);
        return inserirLaudo(os, assinatura, localizacao, textoLaudo, "RASCUNHO", dadosInterface);
    }

    private static boolean inserirLaudo(String os, String assinatura, String localizacao, String textoLaudo, String status, String dadosInterface) {
        String sql = """
            INSERT INTO laudos_auditoria
                (numero_os, assinatura, localizacao, texto_laudo, status, dados_interface)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConexaoSQLite.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, os);
            stmt.setString(2, assinatura);
            stmt.setString(3, localizacao);
            stmt.setString(4, textoLaudo);
            stmt.setString(5, status);
            stmt.setString(6, dadosInterface);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Erro ao salvar laudo no banco: " + e.getMessage());
            return false;
        }
    }

    private static void excluirRascunho(String os) {
        String sql = "DELETE FROM laudos_auditoria WHERE numero_os = ? AND status = 'RASCUNHO'";

        try (Connection conn = ConexaoSQLite.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, os);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover rascunho antigo: " + e.getMessage());
        }
    }

    public static List<RegistroHistorico> buscarHistorico(String filtroOs) {
        List<RegistroHistorico> lista = new ArrayList<>();
        boolean temFiltro = filtroOs != null && !filtroOs.trim().isEmpty();
        String sql = """
            SELECT id, numero_os, data_atualizacao, assinatura, status, texto_laudo, dados_interface
            FROM laudos_auditoria
            WHERE (? = 0 OR numero_os LIKE ?)
            ORDER BY data_atualizacao DESC, id DESC
        """;

        try (Connection conn = ConexaoSQLite.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, temFiltro ? 1 : 0);
            stmt.setString(2, "%" + (temFiltro ? filtroOs.trim() : "") + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRegistro(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar histórico: " + e.getMessage());
        }
        return lista;
    }

    public static RegistroHistorico buscarPorId(int id) {
        String sql = """
            SELECT id, numero_os, data_atualizacao, assinatura, status, texto_laudo, dados_interface
            FROM laudos_auditoria
            WHERE id = ?
        """;

        try (Connection conn = ConexaoSQLite.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearRegistro(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar laudo: " + e.getMessage());
        }
        return null;
    }

    private static RegistroHistorico mapearRegistro(ResultSet rs) throws SQLException {
        return new RegistroHistorico(
                rs.getInt("id"),
                rs.getString("numero_os"),
                rs.getString("data_atualizacao"),
                rs.getString("assinatura"),
                rs.getString("status"),
                rs.getString("texto_laudo"),
                rs.getString("dados_interface")
        );
    }
}
