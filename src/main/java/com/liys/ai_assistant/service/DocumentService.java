package com.liys.ai_assistant.service;

import com.liys.ai_assistant.common.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

// 上传  存向量库
@Service
public class DocumentService {

    // 解析对象
    private final DocumentParserService documentParserService;
    // 向量的对象
    private final VectorStore vectorStore;
    // 配置
    private final AppProperties appProperties;

    //  rag 整个的流程  数据准备   分块  向量化   保存向量库  检索   回溯 回答

    // 文件的名称
    public static final String METADATA_FILE_NAME = "fileName";
    public static final String META_PAGE = "page";
    private  static  final  Set<String> SUPPORTED = Set.of("pdf", "doc","docx", "txt");
    //  构造方法注入
    public DocumentService(DocumentParserService documentParserService, VectorStore vectorStore, AppProperties appProperties) {
        this.documentParserService = documentParserService;
        this.vectorStore = vectorStore;
        this.appProperties = appProperties;
    }


    // 上传功能
    public UploadResponse upload(MultipartFile fileName) {

        if (fileName == null || fileName.isEmpty()){
            throw new ApiException("fileName is required");
        }
      // 文件类型
        String originalFilename = fileName.getOriginalFilename();
        if (!SUPPORTED.contains(extension(originalFilename))) {
            throw new ApiException("仅支持 PDF/DOC/DOCX/TXT，当前文件: " + originalFilename);
        }
        // 读取文件内容
        byte[] content;
        try {
            content = fileName.getBytes();
        } catch (Exception e) {
            throw new ApiException("读取文件失败", e);
        }
        // 1. 解析文件
        var pages = documentParserService.parse(content, originalFilename);
        // 2. 分块
        AppProperties.Rag rag = appProperties.getRag();

        // List
        ArrayList<Document> documents = new ArrayList<>();
        // 遍历
        for (PageText page: pages) {
           for(String chunk : TextSplitter.split(page.text(), rag.getChunkSize(), rag.getChunkOverlap())) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put(METADATA_FILE_NAME, originalFilename);
                metadata.put(META_PAGE, page.page());
                documents.add(new Document(chunk, metadata));
            }
        }
        if (documents.isEmpty()) {
            throw new ApiException("文档无有效文本内容");
        }
        // 3. 向量化    // 4. 保存向量库
        vectorStore.add(documents);

        return new UploadResponse(originalFilename, documents.size());

    }




    private String extension(String fileName) {
        int idx = fileName == null ? -1 : fileName.lastIndexOf('.');
        return idx < 0 ? "" : fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

}
