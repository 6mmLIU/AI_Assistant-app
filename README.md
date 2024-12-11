# &#x20;AI 聊天助手

&#x20;AI 聊天助手是一款桌面应用程序，允许用户与由通义大型语言模型提供支持的聊天机器人进行交互。该应用设计类似于消息应用程序，具有简洁、用户友好的界面，并支持用户和助手消息的自定义聊天气泡。

---

## 功能

- **响应式聊天界面**：视觉上美观的聊天面板，采用圆角消息气泡，并为用户和助手消息提供不同的颜色。
- **流式响应**：支持通义 API 的流式响应，模拟实时交互。
- **行为可定制**：可以通过系统消息定制助手的行为，使其更具互动性和上下文感知能力。
- **跨平台支持**：在安装了 Java 的任何平台上运行。

---

## 系统要求

- Java 开发工具包 (JDK) 17 或更高版本
- 互联网连接
- 有效的通义 API 密钥

---

## 安装

1. **克隆或下载代码库**：

   ```bash
   git clone https://github.com/your-repo/tongyi-ai-chat.git
   cd tongyi-ai-chat
   ```

2. **编译源代码**：

   ```bash
   javac -d out src/**/*.java
   ```

3. **运行应用程序**：

   ```bash
   java -cp out TongYiApiClient
   ```

4. **创建可运行的 JAR 文件（可选）**：

   ```bash
   jar cfe TongYiChat.jar TongYiApiClient -C out .
   ```

   运行 JAR 文件：

   ```bash
   java -jar TongYiChat.jar
   ```

---

## 使用方法

1. 通过运行 JAR 文件或编译后的代码启动应用程序。
2. 在输入框中输入消息，然后点击“发送”按钮或按下 `Enter` 键。
3. 助手会以流式方式实时响应消息。

---

## 配置

- **API 密钥**：将源代码中的占位符 `API_KEY` 替换为您的通义 API 密钥。
- **系统消息**：通过修改 `chatHistory` 初始化中的系统消息，自定义助手的行为。

---

## 示例系统消息

可以通过设置系统消息来配置助手的语气和行为，例如：

```java
Map<String, String> systemMessage = new HashMap<>();
systemMessage.put("role", "system");
systemMessage.put("content", "你是一个友好且知识渊博的助手，旨在帮助用户。");
chatHistory.add(systemMessage);
```

---

## 已知问题

1. **响应缓慢**：网络延迟或服务器端处理可能导致轻微延迟。
2. **错误消息**：如果 API 检测到不适当的内容或其他问题，应用程序可能会显示错误日志。

---

## 未来改进

- 增加对自定义主题和聊天界面自定义的支持。
- 打包为可执行文件，以便于分发。
- 改进错误处理和用户反馈机制。

---

## 许可证

本项目基于 MIT 许可证发布。有关详细信息，请参阅 LICENSE 文件。

---

## 作者

由 [6mm] 开发。如有问题或贡献意向，请通过 [lhm84192395@gmail.com](mailto\:lhm84192395@gmail.com) 联系我。

