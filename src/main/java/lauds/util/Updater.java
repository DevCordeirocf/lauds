package lauds.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Updater {
    private static final String REPO = "DevCordeirocf/lauds";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases";
    private static final String CURRENT_VERSION = "v1.0.0"; // Versão base
    
    // O valor abaixo será substituído pelo GitHub Actions durante o build
    private static String githubToken = "GITHUB_TOKEN_PLACEHOLDER"; 
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
                } catch (IOException ex) {
                    System.err.println("Erro ao carregar config.properties: " + ex.getMessage());
                }
            }
        }
    }

    public static void checkForUpdates() {
        loadConfig();
        
        if ("GITHUB_TOKEN_PLACEHOLDER".equals(githubToken)) {
            githubToken = null;
        }
        
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
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    var releases = com.google.gson.JsonParser.parseReader(reader).getAsJsonArray();
                    
                    if (releases.size() > 0) {
                        JsonObject release = releases.get(0).getAsJsonObject();
                        String latestVersion = release.get("tag_name").getAsString();
                        
                        if (latestVersion != null && !latestVersion.trim().equalsIgnoreCase(CURRENT_VERSION.trim())) {
                            setStatus("Atualização disponível: " + latestVersion, 10000);
                            int response = JOptionPane.showConfirmDialog(null,
                                    "Uma nova versão (" + latestVersion + ") está disponível. Deseja atualizar agora?",
                                    "Atualização Disponível",
                                    JOptionPane.YES_NO_OPTION);

                            if (response == JOptionPane.YES_OPTION) {
                                setStatus("Baixando atualização...", 0);
                                downloadAndInstall(release);
                            }
                        } else {
                            setStatus("O sistema está atualizado", 5000);
                        }
                    } else {
                        setStatus("Nenhum release encontrado", 5000);
                    }
                } else {
                    setStatus("Erro ao verificar atualizações", 5000);
                }
            } catch (Exception e) {
                setStatus("Erro de conexão com GitHub", 5000);
                System.err.println("Erro ao verificar atualizações: " + e.getMessage());
            }
        }).start();
    }

    private static void downloadAndInstall(JsonObject release) throws Exception {
        String downloadUrl = null;
        for (var asset : release.getAsJsonArray("assets")) {
            JsonObject assetObj = asset.getAsJsonObject();
            String name = assetObj.get("name").getAsString();
            if (name.equals("gerador-laudos.jar")) {
                downloadUrl = assetObj.get("browser_download_url").getAsString();
                break;
            }
        }

        if (downloadUrl == null) return;

        File tempFile = File.createTempFile("update-", ".jar");
        HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
        if (githubToken != null && !githubToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + githubToken);
        }
        
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        return script;
    }
}
