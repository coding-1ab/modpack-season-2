package dev.codinglabs.updater;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Installer {
    private static final String VERSION_PREFIX = "21.1.";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }

    private static boolean isValidMinecraftPath(Path minecraft) {
        var profilesPath = minecraft.resolve("launcher_profiles.json");
        return profilesPath.toFile().exists();
    }

    public static class ErrorReport extends JFrame {
        public final String errorMessage;
        protected final JPanel panel;

        public ErrorReport(String errorMessage) {
            super("Error Report");
            this.errorMessage = errorMessage;
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            var label = new JLabel("<html>" + errorMessage.replace("\n", "<br>") + "</html>");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.RED);
            panel.add(label);

            var exitButton = new JButton("확인");
            exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            exitButton.addActionListener(event -> dispose());
            panel.add(exitButton);

            add(panel);
            pack();
            setMinimumSize(getSize());
        }
    }

    public static class ExceptionReport extends Installer.ErrorReport {
        public final Exception exception;

        public ExceptionReport(Exception exception) {
            super(exception.getMessage());
            this.exception = exception;
            JTextArea stackTrace = new JTextArea(16, 48);
            stackTrace.setEditable(false);
            stackTrace.setLineWrap(true);
            stackTrace.setWrapStyleWord(true);

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter);
            stackTrace.setText(stringWriter.toString());

            JScrollPane scrollPane = new JScrollPane(stackTrace);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollPane.setPreferredSize(new Dimension(480, 220));
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

            panel.add(scrollPane);
            pack();
            setLocationRelativeTo(null);
            setMinimumSize(getSize());
        }
    }

    public static class MainUI extends JFrame {
        JTextArea log;

        public MainUI() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel head = new JLabel("<html><h1><strong>코딩랩 모드팩 자동 설치기</strong></h1><hr></html>", SwingConstants.CENTER);
            head.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(head);

            JLabel description = new JLabel(
                    "<html><div style='text-align:center;'>"
                            + "이 프로그램은 포지를 자동으로 감지하고 모드를 설치할 것입니다.<br>"
                            + "주의: 이 설치기는 다른 모드가 설치되어 있는 경우 잘 대응하지 못합니다.<br>"
                            + "이 설치 프로그램은 동시에 모드로써도 동작하니, 네오포지와 모드 설치 방법을 이미 안다면<br>"
                            + "직접 mods 폴더에 이 설치기를 복사하셔도 됩니다."
                            + "</div></html>",
                    SwingConstants.CENTER
            );
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(description);

            JLabel pathHint = new JLabel("마인크래프트 설치 경로:");
            pathHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(pathHint);

            JTextField pathInput = new JTextField(defaultMinecraftPath());
            pathInput.setAlignmentX(Component.LEFT_ALIGNMENT);
            pathInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathInput.getPreferredSize().height));
            panel.add(pathInput);

            log = new JTextArea(16, 48);
            log.setEditable(false);
            log.setLineWrap(true);
            log.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(log);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollPane.setPreferredSize(new Dimension(480, 220));
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
            panel.add(scrollPane);

            JButton installButton = installButton(pathInput);
            installButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(installButton);

            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            add(panel);
            pack();
            setLocationRelativeTo(null);
            setMinimumSize(getSize());
        }

        private @NotNull JButton installButton(JTextField pathInput) {
            JButton installButton = new JButton("시작");
            installButton.addActionListener(event -> {
                if (Objects.equals(installButton.getText(), "설치 완료")) {
                    dispose();
                    return;
                }

                installButton.setEnabled(false);
                pathInput.setEnabled(false);
                log.setText("");
                new InstallWorker(pathInput, installButton).execute();
            });
            return installButton;
        }


        private class InstallWorker extends SwingWorker<Void, String> {
            private final JTextField pathInput;
            private final JButton installButton;

            private InstallWorker(JTextField pathInput, JButton installButton) {
                this.pathInput = pathInput;
                this.installButton = installButton;
            }


            @Override
            protected Void doInBackground() throws Exception {
                Path path = Path.of(pathInput.getText());
                if (!isValidMinecraftPath(path)) {
                    throw new IllegalArgumentException("선택하신 경로가 제대로 된 마인크래프트 경로가 아닌 것 같습니다");
                }

                Map<String, Object> profile = readProfiles(path);
                String latest = getLatestNeoForgeVersion(log);

                if (!isForgeInstalled(profile, latest)) {
                    installNeoForge(path, log);
                } else {
                    publish("포지 설치를 건너뜁니다.\n");
                }

                addProfile(profile, latest, log, path);
                installSelf(path);
                publish("코딩랩 모드팩 설치 완료. 이제 마인크래프트 1.21.1 NeoForge를 실행하시면 됩니다\n");
                pack();
                setMinimumSize(getSize());

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    log.append(chunk);
                }
                log.setCaretPosition(log.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    get();
                    installButton.setText("설치 완료");
                    installButton.setEnabled(true);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception ex) {
                        new ExceptionReport(ex).setVisible(true);
                    } else {
                        new ErrorReport(cause.toString()).setVisible(true);
                    }
                    installButton.setText("재시도");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    new ExceptionReport(e).setVisible(true);
                    installButton.setText("재시도");
                } finally {
                    installButton.setEnabled(true);
                    pathInput.setEnabled(true);
                }
            }
        }
    }

    private static void installNeoForge(Path minecraftPath, JTextArea log) throws IOException, InterruptedException {
        Path latest = downloadLatestNeoForge(log);
        log.append("NeoForge 설치 프로그램 실행중...\n");

        var builder = new ProcessBuilder(
                "java",
                "-jar",
                latest.toAbsolutePath().toString(),
                "--install-client",
                minecraftPath.toAbsolutePath().toString()
        );
        Process process = builder.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line + "\n");
            }
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("NeoForge 설치 프로세스가 비정상 종료되었습니다. exitCode=" + exit);
        }
    }

    private static Path downloadLatestNeoForge(JTextArea log) throws IOException, InterruptedException {
        var version = getLatestNeoForgeVersion(log);
        log.append("최신 NeoForge 설치중\n");
        var url = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-installer.jar"
                .formatted(version, version);
        log.append("다운받을 파일: " + url + "\n");

        var uri = URI.create(url);
        var fileName = "neoforge-%s-installer.jar".formatted(version);
        Path downloaded;
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            var request = HttpRequest.newBuilder(uri).GET().build();
            downloaded = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(
                            Path.of(fileName),
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE
                    )
            ).body();
            log.append("다운로드 성공\n");
        }

        return downloaded;
    }

    private static String getLatestNeoForgeVersion(JTextArea log) throws IOException, InterruptedException {
        log.append("최신 NeoForge 버전 확인중...\n");
        var url = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
        log.append("다운받을 URL: %s\n".formatted(url));

        String xml;
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            xml = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }

        return xml.lines().map(line -> {
            String trimmed = line.trim();
            if (trimmed.startsWith("<version>") && trimmed.endsWith("</version>")) {
                return trimmed.replace("<version>", "").replace("</version>", "");
            } else {
                return "";
            }
        }).filter(version -> version.startsWith(VERSION_PREFIX)).max((a, b) -> {
            String[] aTokens = a.split("\\.");
            String[] bTokens = b.split("\\.");

            int aVersion = Integer.parseInt(aTokens[aTokens.length - 1]);
            int bVersion = Integer.parseInt(bTokens[bTokens.length - 1]);

            return Integer.compare(aVersion, bVersion);
        }).orElseThrow(() -> new IllegalStateException("적절한 NeoForge 버전을 찾지 못했습니다."));
    }

    private static String defaultMinecraftPath() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            return appData + "\\.minecraft";
        } else if (osName.contains("mac")) {
            String home = System.getProperty("user.home");
            return Path.of(home, "Library/Application Support/minecraft").toAbsolutePath().toString();
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            String home = System.getProperty("user.home");
            return Path.of(home, ".minecraft").toAbsolutePath().toString();
        } else {
            return "알 수 없는 운영체제입니다!";
        }
    }

    private static Map<String, Object> readProfiles(Path minecraftPath) throws IOException {
        var profiles = minecraftPath.resolve("launcher_profiles.json");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(profiles.toFile(), new TypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    private static boolean isForgeInstalled(Map<String, Object> launcherProfiles, String targetVersion) {
        Map<String, Object> profiles = (Map<String, Object>) launcherProfiles.get("profiles");

        if (profiles == null) {
            throw new IllegalStateException("마인크래프트가 제대로 설정되지 않았습니다! 게임을 한번은 실행해주세요!");
        }

        return profiles.entrySet().stream().anyMatch(entry -> {
            Map<String, Object> profile = (Map<String, Object>) entry.getValue();
            String name = (String) profile.get("name");

            if (!name.toLowerCase().contains("neoforge")) {
                return false;
            }

            String version = (String) profile.get("lastVersionId");
            String stripped = version.replace("neoforge-", "");
            if (!stripped.startsWith(VERSION_PREFIX)) {
                return false;
            }

            String last = stripped.replaceFirst(VERSION_PREFIX, "");
            int lastInt = Integer.parseInt(last);
            int targetLastInt = Integer.parseInt(targetVersion.replaceFirst(VERSION_PREFIX, ""));

            return lastInt >= targetLastInt;
        });
    }

    private static void installSelf(Path minecraftPath) throws IOException {
        var mods = minecraftPath.resolve("mods");
        Files.createDirectories(mods);

        Path jarPath;
        try {
            jarPath = Path.of(Installer.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new IOException("현재 실행 중인 JAR 경로를 확인할 수 없습니다.", e);
        }

        Files.copy(jarPath, mods.resolve(jarPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    @SuppressWarnings("unchecked")
    private static void addProfile(Map<String, Object> launchProfile, String neoVersion, JTextArea log, Path minecraftDir) throws IOException {
        HashMap<String, String> newProfile = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String now = sdf.format(date);

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        byte[] contents;
        try(InputStream iconStream = Objects.requireNonNull(classloader.getResourceAsStream("icon.png"))) {
            contents = iconStream.readAllBytes();
        }

        String base64 = Base64.getEncoder().encodeToString(contents);

        newProfile.put("created", now);
        newProfile.put("lastUsed", now);
        newProfile.put("type", "custom");
        newProfile.put("name", "코딩랩 모드팩 시즌 2");
        newProfile.put("lastVersionId", "neoforge-" + neoVersion);
        newProfile.put("icon", "data:image/png;base64," + base64);
        newProfile.put("javaArgs", "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:+ZGenerational");

        Map<String, Object> profiles = (Map<String, Object>) launchProfile.get("profiles");
        profiles.put("codinglab-modpack-season2", newProfile);

        File file = minecraftDir.resolve("launcher_profiles.json").toFile();
        if (!file.exists()) {
            throw new IllegalStateException("외부 프로세스가 모드팩 설치 과정에 간섭했습니다!");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, launchProfile);

        log.append("코딩랩 실행 설정 추가 완료\n");
    }
}
