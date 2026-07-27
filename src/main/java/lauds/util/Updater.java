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
    private static String githubToken = null;

    private static void loadConfig() {
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

    public static void checkForUpdates() {
        loadConfig();
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "Java-Updater");
                
                if (githubToken != null && !githubToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", "token " + githubToken);
                }

                System.out.println("Verificando atualizações em: " + API_URL);
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    var releases = com.google.gson.JsonParser.parseReader(reader).getAsJsonArray();
                    
                    if (releases.size() > 0) {
                        JsonObject release = releases.get(0).getAsJsonObject(); // Pega o mais recente da lista
                        String latestVersion = release.get("tag_name").getAsString();
                        
                        System.out.println("Versão local: " + CURRENT_VERSION);
                        System.out.println("Versão GitHub: " + latestVersion);

                        if (latestVersion != null && !latestVersion.trim().equalsIgnoreCase(CURRENT_VERSION.trim())) {
                            int response = JOptionPane.showConfirmDialog(null,
                                    "Uma nova versão (" + latestVersion + ") está disponível. Deseja atualizar agora?",
                                    "Atualização Disponível",
                                    JOptionPane.YES_NO_OPTION);

                            if (response == JOptionPane.YES_OPTION) {
                                downloadAndInstall(release);
                            }
                        }
                    }
                } else {
                    System.out.println("Resposta da API: " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("Erro ao verificar atualizações: " + e.getMessage());
                e.printStackTrace();
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

        if (downloadUrl == null) {
            JOptionPane.showMessageDialog(null, "Não foi possível encontrar o arquivo de atualização.");
            return;
        }

        File tempFile = File.createTempFile("update-", ".jar");
        HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
        if (githubToken != null && !githubToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + githubToken);
        }
        
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String currentJarPath = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        
        if (currentJarPath.endsWith(".jar")) {
            File currentJar = new File(currentJarPath);
            File updateScript = createUpdateScript(tempFile, currentJar);
            
            Runtime.getRuntime().exec("cmd /c start " + updateScript.getAbsolutePath());
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(null, "Execução em modo desenvolvimento. O auto-update só funciona rodando o JAR.");
        }
    }

    private static File createUpdateScript(File tempJar, File targetJar) throws IOException {
        File script = File.createTempFile("update-script", ".bat");
        try (PrintWriter writer = new PrintWriter(new FileWriter(script))) {
            writer.println("@echo off");
            writer.println("timeout /t 2 /nobreak > nul");
            writer.println("move /y \"" + tempJar.getAbsolutePath() + "\" \"" + targetJar.getAbsolutePath() + "\"");
            writer.println("start \"\" java -jar \"" + targetJar.getAbsolutePath() + "\"");
            writer.println("del \"%~f0\"");
        }
        return script;
    }
}
