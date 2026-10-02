package com.liys.ai_assistant.common;

public record UploadResponse(
        // 文件名
        String fileName,
        // 分片数量
        int chunkCount
) {
}
