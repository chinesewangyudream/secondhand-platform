package com.secondhand.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    private String getAbsoluteUploadPath() {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 相对路径转为用户目录下的绝对路径
            uploadDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        return uploadDir.getAbsolutePath() + File.separator;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传目录为静态资源
        String absolutePath = getAbsoluteUploadPath();
        // 转换为file:///格式的URL
        String resourceLocation = "file:" + absolutePath.replace("\\", "/");
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        System.out.println("Static resource mapping: /api/uploads/** -> " + resourceLocation);
        System.out.println("Absolute upload path: " + absolutePath);
        System.out.println("Resource location: " + resourceLocation);

        // 检查目录是否存在
        File uploadDir = new File(absolutePath);
        System.out.println("Upload directory exists: " + uploadDir.exists());
        System.out.println("Upload directory is readable: " + uploadDir.canRead());

        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            System.out.println("Files in upload directory: " + (files != null ? files.length : 0));
            if (files != null && files.length > 0) {
                System.out.println("First file: " + files[0].getName() + ", canRead: " + files[0].canRead());
            }
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}