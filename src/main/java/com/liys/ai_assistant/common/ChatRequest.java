package com.liys.ai_assistant.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 用户的问题   问答请求参数
public record ChatRequest(
        @NotBlank(message = "会话id不能为空") @Size(max = 100, message = "会话id不能超过100个字符") String conversationId,
        @NotBlank(message = "用户问题不能为空") @Size(max = 4000, message = "问题不能超过4000个字符") String question,
        @Size(max = 255, message = "文件名不能超过255个字符")
        String fileName // 可选：仅在指定文件中检索
) {




}
