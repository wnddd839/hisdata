package com.his.server.controller;

import com.his.common.exception.BusinessException;
import com.his.common.result.GlobalResult;
import com.his.server.config.file.FileConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Tag(name = "通用文件服务")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileConfig fileConfig;

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public GlobalResult<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            
            // 确保目录存在
            File destDir = new File(fileConfig.getUploadPath());
            if (!destDir.exists()) {
                if (!destDir.mkdirs()) {
                    throw new BusinessException("创建上传目录失败");
                }
            }

            File destFile = new File(destDir, fileName);
            file.transferTo(destFile);
            
            return GlobalResult.success("上传成功", fileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文件下载/预览")
    @GetMapping("/{fileName}")
    public void download(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        File file = new File(fileConfig.getUploadPath(), fileName);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
    }
}
