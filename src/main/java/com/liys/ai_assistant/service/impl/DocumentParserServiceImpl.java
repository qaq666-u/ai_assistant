package com.liys.ai_assistant.service.impl;

import com.liys.ai_assistant.common.ApiException;
import com.liys.ai_assistant.common.PageText;
import com.liys.ai_assistant.service.DocumentParserService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
*   xxx.pdf
*
*  pdf
*
* */

/**
 * Service implementation for parsing various document types.
 * This class provides functionality to extract text content from different file formats
 * including PDF, DOC/DOCX, and TXT files.
 */
@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    /**
     * Tika instance for document parsing
     */
    private final Tika tika = new Tika();


    /**
     * Main parsing method that dispatches to appropriate parser based on file extension
     *
     * @param content byte array of the file content
     * @param fileName name of the file to parse
     * @return List of PageText objects containing parsed text content
     * @throws ApiException if the file type is not supported
     */
    // 获取后缀名 分发解析方法     找到 是什么文件  我就给什么样的解析
    @Override
    public List<PageText> parse(byte[] content, String fileName) {
        // 获取文件后缀名
        String ext = extension(fileName);
        return switch (ext) {
            case "pdf" -> parsePDF(content, fileName);
            case "docx", "doc" -> parseByTika(content);
            case "txt" -> List.of(new PageText(1, new String(content, StandardCharsets.UTF_8)));
            default -> throw new ApiException("不支持的文件类型: " + ext);
        };
    }

    /**
     * Parses PDF documents and extracts text content from each page
     *
     * @param content byte array of the PDF file
     * @param fileName name of the PDF file
     * @return List of PageText objects, one for each page
     * @throws ApiException if PDF parsing fails
     */
    // 解析pdf文件
    @Override
    public List<PageText> parsePDF(byte[] content,String fileName) {
        List<PageText> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(content)) {
            int totalPages = document.getNumberOfPages();
            for (int page = 1; page <= totalPages; page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document);
                if (text != null && !text.isBlank()) {
                    pages.add(new PageText(page, text.trim()));
                }
            }
        } catch (Exception e) {
            throw new ApiException("PDF 解析失败: " + fileName, e);
        }
        return pages;
    }

    /**
     * Uses Apache Tika to parse document content
     *
     * @param content byte array of the document
     * @return List containing a single PageText object with the parsed content
     * @throws ApiException if Tika parsing fails
     */
    // 使用Tika解析txt文件
    @Override
    public List<PageText> parseByTika(byte[] content) {
        //   流   字节流
        try (InputStream in = new ByteArrayInputStream(content)) {
            // 获取文件内容 使用Tika解析文件内容
            String text = tika.parseToString(in);
            // 如果内容为空 返回空集合
            if (text == null || text.isBlank()) {
                return List.of();
            }
            // 返回一个集合 里面只有一个元素
            return List.of(new PageText(1, text.trim()));
        } catch (Exception e) {
            throw new ApiException("Tika 解析失败", e);
        }
    }

    /**
    * Extracts file extension from a filename
     *
     * Example:
     * Input: "123.txt"
     * Output: "txt"
     *
     * @param fileName the filename to process
     * @return file extension in lowercase, empty string if no extension found
     */
    /*
    *
    *  获取文件后缀名
    *123.txt
    * */

    private String extension(String fileName) {
        int idx = fileName == null ? -1 : fileName.lastIndexOf('.');
        return idx < 0 ? "" : fileName.substring(idx + 1).
                toLowerCase(Locale.ROOT);
    }


}
