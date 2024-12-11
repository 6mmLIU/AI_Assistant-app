import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.net.ssl.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class TongYiApiClient {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String API_KEY = ""; // 替换为你的通义大模型API密钥
    private static final List<Map<String, String>> chatHistory = new ArrayList<>();

    // 聊天消息显示的面板
    private static JPanel chatPanel;
    private static JScrollPane scrollPane;

    public static void main(String[] args) {
        try {
            disableSslVerification();
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // system 消息预设
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个可爱、温柔的美少女助理，你的对话语气要体现出活泼与温柔的特质。");
        chatHistory.add(systemMessage);

        JFrame frame = new JFrame("WeChat-like AI Chat");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 模拟微信顶部标题栏
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(7, 191, 96)); // 微信绿色
        topPanel.setPreferredSize(new Dimension(frame.getWidth(), 50));
        JLabel titleLabel = new JLabel("AI美少女助理", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        // 聊天显示区域
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        frame.add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JTextField inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JButton sendButton = new JButton("发送");
        sendButton.setBackground(new Color(7, 191, 96));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sendButton.setFocusPainted(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // 添加初始欢迎语
        addMessage("你好！！！有什么可以帮助你的吗？😊", "assistant");

        // 发送消息的公共方法
        Runnable sendMessage = () -> {
            String userInput = inputField.getText().trim();
            if (!userInput.isEmpty()) {
                addMessage(userInput, "user");
                inputField.setText("");

                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", userInput);
                chatHistory.add(userMessage);

                new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        String requestBody = buildRequestBody();
                        callTongYiApiWithStream(requestBody);
                        return null;
                    }
                }.execute();
            }
        };

        sendButton.addActionListener(e -> sendMessage.run());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage.run();
                }
            }
        });

    }

    private static String buildRequestBody() {
        StringBuilder messagesJson = new StringBuilder("[");
        for (Map<String, String> message : chatHistory) {
            messagesJson.append(String.format(
                    "{\"role\":\"%s\",\"content\":\"%s\"},",
                    message.get("role"),
                    message.get("content").replace("\"", "\\\"")
            ));
        }
        if (messagesJson.length() > 1) {
            messagesJson.setLength(messagesJson.length() - 1);
        }
        messagesJson.append("]");

        return String.format(
                "{\"model\":\"qwen-plus\",\"messages\":%s,\"stream\":true,\"temperature\":1.9}",
                messagesJson
        );
    }

    private static void callTongYiApiWithStream(String requestBody) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes());
            os.flush();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder aiResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("data: [DONE]")) {
                    break;
                }
                if (line.startsWith("data: ")) {
                    line = line.substring(6);
                }
                try {
                    JSONObject json = new JSONObject(line);
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("delta")
                            .optString("content", "");
                    if (!content.isEmpty()) {
                        aiResponse.append(content);
                        // 实时更新
                        SwingUtilities.invokeLater(() -> {
                            // 最后一次更新的时候一并添加消息气泡
                            // 这里可以分粒度更新（逐字显示），简化起见一次性显示
                            // 如果要逐字显示，可将内容分片添加，但本示例就一次性显示
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                }
            }

            if (aiResponse.length() > 0) {
                addMessage(aiResponse.toString(), "assistant");
            }
        } catch (Exception e) {
            addMessage("Error during streaming: " + e.getMessage(), "assistant");
        }
    }

    // 添加消息气泡的方法
    // role = "user" 表示用户消息（右侧气泡），role = "assistant" 表示AI回复（左侧气泡）
    private static void addMessage(String text, String role) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messagePanel.setBackground(Color.WHITE);

        // 气泡标签
        JLabel messageLabel = new JLabel("<html><body style='width:200px;'>" + text + "</body></html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setOpaque(true);

        if ("user".equals(role)) {
            // 用户消息：右对齐，使用绿色气泡
            messageLabel.setBackground(new Color(200, 255, 200));
            messageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            // 加个空隙，让消息靠右
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.add(messageLabel, BorderLayout.EAST);
            messagePanel.add(wrapper, BorderLayout.EAST);
        } else {
            // AI消息：左对齐，使用浅灰色气泡
            messageLabel.setBackground(new Color(230, 230, 230));
            messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.add(messageLabel, BorderLayout.WEST);
            messagePanel.add(wrapper, BorderLayout.WEST);
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }

    private static void disableSslVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
