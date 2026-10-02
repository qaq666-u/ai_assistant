package com.liys.ai_assistant.common;

/** 引用到的知识库片段及其来源信息。 */
public record Citation(String fileName, Integer page, String snippet, Double score) {
}
