# 智慧库助手

基于 Spring Boot、Spring AI 和 DashScope 的智能文档知识库问答项目，支持 PDF、DOC、DOCX、TXT 文档解析、向量检索、来源引用及 SSE 流式回答。

## 环境要求

- JDK 21
- Maven（仓库提供 Maven Wrapper）
- DashScope API Key

## 配置和运行

在 PowerShell 中设置 DashScope 密钥，然后启动服务：

```powershell
$env:DASHSCOPE_API_KEY = "你的 DashScope API Key"
.\mvnw.cmd spring-boot:run
```

启动后访问 http://localhost:8082/ 。也可以复制 `src/main/resources/application-local.yml.example` 为 `application-local.yml` 并填写本地密钥；该本地文件已加入 Git 忽略规则。

## 配置说明

运行参数位于 `src/main/resources/application.yml`，包括端口、上传大小、模型、分块长度、检索 TopK 和对话历史轮数。默认向量库与会话存储为内存实现，服务重启后数据会清空。
