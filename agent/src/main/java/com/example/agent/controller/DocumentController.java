package com.example.agent.controller;

import com.example.agent.common.Result;
import com.example.agent.service.DocumentService;
import com.example.agent.service.VectorService; // 1. 别忘了引入你的 VectorService 包
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document") // 保持你原本的统一路由前缀
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired // 2. 注入新写的向量化服务
    private VectorService vectorService;

    /**
     * 原有接口：上传文档
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = documentService.uploadDocument(file);
            return Result.success("上传成功，存储路径：" + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 原有接口：仅解析和切片（方便前端预览效果）
     */
    @PostMapping("/parse")
    public Result<Map<String, Object>> parseAndChunk(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "chunkSize", defaultValue = "300") int chunkSize,
            @RequestParam(value = "overlapSize", defaultValue = "50") int overlapSize) {
        try {
            // 1. 提取文本
            String rawText = documentService.extractTextFromPdf(filePath);

            // 2. 切片
            List<String> chunks = documentService.splitText(rawText, chunkSize, overlapSize);

            // 3. 组装结果返回给前端看效果
            Map<String, Object> result = new HashMap<>();
            result.put("totalLength", rawText.length());
            result.put("chunkCount", chunks.size());
            result.put("chunks", chunks);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文档解析失败：" + e.getMessage());
        }
    }

    /**
     * 新增接口：组装整条流水线（一键提取 + 切片 + 向量化入库）
     */
    @PostMapping("/import")
    public Result<String> importAndVectorize(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "chunkSize", defaultValue = "300") int chunkSize,
            @RequestParam(value = "overlapSize", defaultValue = "50") int overlapSize) {
        try {
            // 验证本地文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return Result.error("文件不存在，请检查路径！");
            }

            // 1. 提取文字 (来自 Day 3)
            String fullText = documentService.extractTextFromPdf(filePath);

            // 2. 切片 (来自 Day 3，这里进化为支持前端动态传入参数)
            List<String> chunks = documentService.splitText(fullText, chunkSize, overlapSize);

            // 3. 向量化并入库 (来自 Day 5)
            // 传入 file.getName() 作为元数据中的 source，便于后续检索追溯
            vectorService.storeDocumentChunks(chunks, file.getName());

            return Result.success("知识库导入成功！共处理并入库分片: " + chunks.size() + " 个。");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("知识库一键导入失败：" + e.getMessage());
        }
    }
}