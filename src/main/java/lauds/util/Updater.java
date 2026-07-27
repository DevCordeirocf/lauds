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
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String CURRENT_VERSION = "v1.0.0"; // Versão base

    public static void checkForUpdates() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "Java-Updater");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JsonObject release = JsonParser.parseReader(reader).getAsJsonObject();
                    String latestVersion = release.get("tag_name").getAsString();
                    System.out.println("Versão local: " + CURRENT_VERSION);
                    System.out.println("Versão GitHub: " + latestVersion);

                    // Se a versão do GitHub começar com 'v', comparamos de forma simples
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
            } catch (Exception e) {
                System.err.println("Erro ao verificar atualizações: " + e.getMessage());
            }
        }).start();
    }

    private static void downloadAndInstall(JsonObject release) throws Exception {
        String downloadUrl = null;

        // Procura pelo JAR nos assets do release
        for (var asset : release.getAsJsonArray("assets")) {
            JsonObject assetObj = asset.getAsJsonObject();
            String name = assetObj.get("name").getAsString();
            // Procura especificamente pelo nosso JAR
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
        try (InputStream in = new URL(downloadUrl).openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Script para substituir o arquivo e reiniciar
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
