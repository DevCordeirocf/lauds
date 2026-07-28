package lauds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonObject;

public class Updater {
    private static final String REPO = "DevCordeirocf/lauds";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases";
    private static final String CURRENT_VERSION = "v1.0.0"; // Versão base
    // TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE// TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE// TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE// TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE// TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE// TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
    // O valor abaixo será substituído pelo GitHub Actions durante o build
    private static String githubToken = "GITHUB_TOKEN_PLACEHOLDER"; 
    private static final Logger LOGGER = Logger.getLogger(Updater.class.getName());
    private static JLabel statusBar;

    public static void setStatusBar(JLabel label) {
        statusBar = label;
    }

    private static void setStatus(String text, int delayMs) {
        if (statusBar == null) return;
        SwingUtilities.invokeLater(() -> statusBar.setText(text));
        if (delayMs > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                    SwingUtilities.invokeLater(() -> {
                        if (statusBar.getText().equals(text)) statusBar.setText("");
                    });
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    private static void loadConfig() {
        // Se o token injetado for o placeholder, tenta carregar do arquivo local (para desenvolvimento)
        if ("GITHUB_TOKEN_PLACEHOLDER".equals(githubToken)) {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(configFile)) {
                    java.util.Properties prop = new java.util.Properties();
                    prop.load(input);
                    githubToken = prop.getProperty("github.token");
                    String masked = (githubToken == null) ? "(null)" : (githubToken.length() > 8 ? githubToken.substring(0,4) + "..." + githubToken.substring(githubToken.length()-4) : "(set)");
                    LOGGER.info("config.properties carregado. Token presente: " + (githubToken != null) + ", valor: " + masked);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Erro ao carregar config.properties", ex);
                }
            } else {
                LOGGER.fine("Arquivo config.properties não encontrado no diretório de trabalho");
            }
        } else {
            LOGGER.fine("Token injetado pelo build presente (placeholder substituído durante build)");
        }
    }

    public static void checkForUpdates() {
        loadConfig();
        
        LOGGER.info("Verificando atualizações para " + API_URL + ", token presente: " + (githubToken != null && !githubToken.isEmpty()));
        setStatus("Verificando atualizações...", 0);
        
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "Java-Updater");
                
                if (githubToken != null && !githubToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", "token " + githubToken);
                }

                int responseCode = conn.getResponseCode();
                LOGGER.info("GitHub API response code: " + responseCode);
                
                // Se falhar com 'token ', tenta novamente com 'Bearer ' (para tokens fine-grained)
                if ((responseCode == 401 || responseCode == 403 || responseCode == 404) && githubToken != null) {
                    LOGGER.info("Primeira tentativa com 'token ' falhou (" + responseCode + "), tentando com 'Bearer '");
                    conn = (HttpURLConnection) new URL(API_URL).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    conn.setRequestProperty("User-Agent", "Java-Updater");
                    conn.setRequestProperty("Authorization", "Bearer " + githubToken);
                    responseCode = conn.getResponseCode();
                    LOGGER.info("GitHub API response code (Bearer retry): " + responseCode);
                }

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    var releases = com.google.gson.JsonParser.parseReader(reader).getAsJsonArray();
                    LOGGER.info("Releases obtidos: " + releases.size());
                    
                    if (releases.size() > 0) {
                        JsonObject release = releases.get(0).getAsJsonObject();
                        String latestVersion = release.has("tag_name") ? release.get("tag_name").getAsString() : null;
                        LOGGER.info("Release selecionado: tag_name=" + latestVersion);

                        // Tentar detectar o JAR atual (caso a aplicação esteja sendo executada de um JAR)
                        String currentJarPath = null;
                        long currentJarSize = -1L;
                        try {
                            currentJarPath = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                            if (currentJarPath.endsWith(".jar")) {
                                File currentJarFile = new File(currentJarPath);
                                if (currentJarFile.exists()) currentJarSize = currentJarFile.length();
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Falha ao obter caminho/size do JAR atual", ex);
                        }
                        LOGGER.info("JAR atual: " + currentJarPath + ", tamanho=" + currentJarSize);

                        // Procurar o asset gerador-laudos.jar e comparar tamanho como verificação primária
                        long assetSize = -1L;
                        for (var asset : release.getAsJsonArray("assets")) {
                            JsonObject assetObj = asset.getAsJsonObject();
                            String name = assetObj.get("name").getAsString();
                            LOGGER.fine("Asset encontrado no release: " + name + ", size present: " + assetObj.has("size"));
                            if (name.equals("gerador-laudos.jar") && assetObj.has("size")) {
                                assetSize = assetObj.get("size").getAsLong();
                                LOGGER.info("Asset gerador-laudos.jar size=" + assetSize + ", browser_download_url=" + (assetObj.has("browser_download_url") ? assetObj.get("browser_download_url").getAsString() : "(nenhum)"));
                                break;
                            }
                        }
                        LOGGER.info("AssetSize=" + assetSize + ", currentJarSize=" + currentJarSize);

                        if (assetSize > 0 && currentJarSize > 0 && assetSize == currentJarSize) {
                            // Mesmo tamanho: muito provável que já estamos com a mesma build instalada
                            setStatus("O sistema está atualizado", 5000);
                        } else {
                            // Se não for possível comparar por tamanho, cair de volta para comparação por tag
                            boolean differentTag = latestVersion != null && !latestVersion.trim().equalsIgnoreCase(CURRENT_VERSION.trim());
                            if (assetSize > 0 && currentJarSize > 0) {
                                // Se tamanhos diferentes, forçar atualização (mesmo que tags sejam iguais)
                                differentTag = true;
                            }

                            if (differentTag) {
                                String displayVersion = latestVersion != null ? latestVersion : "(versão desconhecida)";
                                setStatus("Atualização disponível: " + displayVersion, 10000);
                                int response = JOptionPane.showConfirmDialog(null,
                                        "Uma nova versão (" + displayVersion + ") está disponível. Deseja atualizar agora?",
                                        "Atualização Disponível",
                                        JOptionPane.YES_NO_OPTION);

                                if (response == JOptionPane.YES_OPTION) {
                                    setStatus("Baixando atualização...", 0);
                                    downloadAndInstall(release);
                                }
                            } else {
                                setStatus("O sistema está atualizado", 5000);
                            }
                        }
                    } else {
                        setStatus("Nenhum release encontrado", 5000);
                    }
                } else if (responseCode == 404) {
                    setStatus("Erro 404: Verifique o Token", 7000);
                } else if (responseCode == 401) {
                    setStatus("Erro 401: Token Inválido", 7000);
                } else {
                    setStatus("Erro HTTP: " + responseCode, 7000);
                }
            } catch (Exception e) {
                setStatus("Erro: " + e.getMessage(), 7000);
                System.err.println("Erro ao verificar atualizações: " + e.getMessage());
            }
        }).start();
    }

    private static void downloadAndInstall(JsonObject release) throws Exception {
        // Preferir a URL da API do asset para permitir downloads autenticados (`/repos/:owner/:repo/releases/assets/:id`)
        String assetApiUrl = null;
        String fallbackBrowserUrl = null;
        for (var asset : release.getAsJsonArray("assets")) {
            JsonObject assetObj = asset.getAsJsonObject();
            String name = assetObj.get("name").getAsString();
            if (name.equals("gerador-laudos.jar")) {
                if (assetObj.has("url")) assetApiUrl = assetObj.get("url").getAsString();
                if (assetObj.has("browser_download_url")) fallbackBrowserUrl = assetObj.get("browser_download_url").getAsString();
                break;
            }
        }

        String downloadUrl = assetApiUrl != null ? assetApiUrl : fallbackBrowserUrl;
        if (downloadUrl == null) {
            setStatus("Arquivo de atualização não encontrado", 7000);
            return;
        }

        File tempFile = File.createTempFile("update-", ".jar");

        LOGGER.info("Iniciando download do asset. assetApiUrl present: " + downloadUrl.contains("/repos/") + ", browser fallback used: " + !downloadUrl.contains("/repos/"));
        LOGGER.fine("downloadUrl=" + downloadUrl + ", tokenPresent=" + (githubToken != null && !githubToken.isEmpty()));
        HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Java-Updater");
        // Para a URL da API de assets, solicitar o conteúdo binário
        if (downloadUrl.contains("/repos/")) {
            conn.setRequestProperty("Accept", "application/octet-stream");
        }
        if (githubToken != null && !githubToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + githubToken);
        }
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        LOGGER.info("Resposta do servidor de download: HTTP " + responseCode);
        // Se houver problemas de autorização, tentar o prefixo Bearer (tokens fine-grained)
        if ((responseCode == 401 || responseCode == 403) && githubToken != null && !githubToken.isEmpty()) {
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java-Updater");
            if (downloadUrl.contains("/repos/")) {
                conn.setRequestProperty("Accept", "application/octet-stream");
            }
            conn.setRequestProperty("Authorization", "Bearer " + githubToken);
            conn.setInstanceFollowRedirects(true);
            responseCode = conn.getResponseCode();
        }

        if (responseCode >= 200 && responseCode < 300) {
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            long downloaded = tempFile.length();
            LOGGER.info("Download concluído. Arquivo temporário: " + tempFile.getAbsolutePath() + ", bytes=" + downloaded);
        } else {
            String msg = "Falha ao baixar atualização: HTTP " + responseCode;
            LOGGER.warning(msg);
            setStatus(msg, 7000);
            throw new IOException(msg);
        }

        String currentJarPath = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        File currentJar = new File(currentJarPath);
        
        File exeFile = null;
        File appDir = currentJar.getParentFile();
        if (appDir != null && appDir.getName().equals("app")) {
            File rootDir = appDir.getParentFile();
            if (rootDir != null) {
                File exe = new File(rootDir, "GeradorLaudos.exe");
                if (exe.exists()) exeFile = exe;
            }
        }

        if (currentJarPath.endsWith(".jar")) {
            File updateScript = createUpdateScript(tempFile, currentJar, exeFile);
            LOGGER.info("Executando script de atualização: " + updateScript.getAbsolutePath());
            Runtime.getRuntime().exec("cmd /c start /min \"\" \"" + updateScript.getAbsolutePath() + "\"");
            System.exit(0);
        }
    }

    private static File createUpdateScript(File tempJar, File targetJar, File exeToStart) throws IOException {
        File script = File.createTempFile("update-script", ".bat");
        try (PrintWriter writer = new PrintWriter(new FileWriter(script))) {
            writer.println("@echo off");
            writer.println("chcp 65001 > nul");
            writer.println("timeout /t 2 /nobreak > nul");
            writer.println("move /y \"" + tempJar.getAbsolutePath() + "\" \"" + targetJar.getAbsolutePath() + "\"");
            if (exeToStart != null) {
                writer.println("start \"\" \"" + exeToStart.getAbsolutePath() + "\"");
            } else {
                writer.println("start \"\" java -jar \"" + targetJar.getAbsolutePath() + "\"");
            }
            writer.println("del \"%~f0\"");
        }
        LOGGER.info("Script de atualização criado em: " + script.getAbsolutePath());
        return script;
    }
}
