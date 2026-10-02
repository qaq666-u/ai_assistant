package com.liys.ai_assistant.controller;

import com.liys.ai_assistant.common.ApiResponse;
import com.liys.ai_assistant.common.UploadResponse;
import com.liys.ai_assistant.service.DocumentParserService;
import com.liys.ai_assistant.service.DocumentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    // 文档解析服务
    private final DocumentService documentService;
    // 构造函数注入
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

/*
*   code     返回 json  key value   200
*   message
*   data
*
*  用户提问前 流程
*  1  数据准备  数据解析
*  2  分块
*  3  向量
*  4  存入向量数据库
*  用户提问 后
*
*
*
*
*
*
* */

    // 上传文档
    @PostMapping("/upload")
    public ApiResponse<UploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        // 解析文档    分片
        return ApiResponse.ok(documentService.upload(file));
    }



}
