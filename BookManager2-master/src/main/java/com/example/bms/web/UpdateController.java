package com.example.bms.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/update")
public class UpdateController {

    /** 图片存储目录（相对于 user.dir） */
    @Value("${upload.img-dir:uploads/pictures}")
    private String imgDir;

    /** 图片访问前缀 */
    private static final String IMG_URL_PREFIX = "http://localhost:8092/BookManager/";

    private Path imgPath;

    @PostConstruct
    public void init() {
        this.imgPath = Paths.get(System.getProperty("user.dir"), imgDir);
        try {
            Files.createDirectories(imgPath);
        } catch (IOException e) {
            throw new RuntimeException("创建上传目录失败: " + imgPath, e);
        }
    }

    /**
     * 上传图书封面图片
     */
    @RequestMapping("/updateImg")
    @ResponseBody
    public Map<String, Object> updateImg(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                res.put("code", 1);
                res.put("data", null);
                return res;
            }

            // 生成唯一文件名
            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            File destFile = imgPath.resolve(newFileName).toFile();
            file.transferTo(destFile);

            // 返回访问 URL
            String url = IMG_URL_PREFIX + imgDir.replace("\\", "/") + "/" + newFileName;
            res.put("code", 0);
            res.put("data", url);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 1);
            res.put("data", null);
        }
        return res;
    }
}
