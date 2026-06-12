package com.example.agent.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    // 自动在项目根目录下创建 uploads 文件夹
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public String uploadDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }

        // 1. 确保上传目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 获取原文件名并提取后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 防呆设计：目前只允许 PDF
        if (!".pdf".equalsIgnoreCase(suffix)) {
            throw new RuntimeException("目前仅支持上传 PDF 格式的知识库文档");
        }

        // 3. 生成 UUID 唯一文件名，防止覆盖
        String newFilename = UUID.randomUUID().toString() + suffix;
        File targetFile = new File(UPLOAD_DIR + newFilename);

        try {
            // 4. 写入本地
            file.transferTo(targetFile);
            // 返回绝对路径，留着 Day 4 给大模型去解析
            return targetFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }
    public String extractTextFromPdf(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            // 排序，保证阅读顺序基本正确
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("PDF文本提取失败: " + e.getMessage());
        }
    }

    /**
     * 智能切片算法：固定大小 + 重叠区间
     * @param text 原始长文本
     * @param chunkSize 每个切片的最大字符数（推荐 300-500）
     * @param overlapSize 两个相邻切片的重叠字符数（防止语义在截断处丢失，推荐 50-100）
     */
    public List<String> splitText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        // 去除多余的空行和空格，让文本连续
        String cleanText = text.replaceAll("\\s+", " ").trim();

        int length = cleanText.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunk = cleanText.substring(start, end);
            chunks.add(chunk);

            // 步进距离 = 切片大小 - 重叠大小
            start += (chunkSize - overlapSize);
            if (start >= length || chunkSize >= length) {
                break;
            }
        }
        return chunks;
    }
}
