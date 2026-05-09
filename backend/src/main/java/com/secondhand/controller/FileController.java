package com.secondhand.controller;

import com.secondhand.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 确保使用绝对路径
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 相对路径转为用户目录下的绝对路径
            uploadDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File dest = new File(uploadDir, filename);

        try {
            file.transferTo(dest);
            return Result.success("上传成功", urlPrefix + filename);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}