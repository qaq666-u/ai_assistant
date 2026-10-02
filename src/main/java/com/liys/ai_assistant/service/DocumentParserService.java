package com.liys.ai_assistant.service;

import com.liys.ai_assistant.common.PageText;

import java.util.List;
/**
 * 文档解析：PDF 用 PDFBox 保留页码，DOCX 用 Tika，TXT 直接读取。
 */
public interface DocumentParserService {

    // 文档解析方法
    List<PageText> parse(byte[] content,String filePath);

    /** PDF 逐页抽取，保证引用可定位页码。 */
    List<PageText> parsePDF(byte[] content,String filePath);

    //  统一作为第一页  非分格式
    List<PageText> parseByTika(byte[] content);
}
