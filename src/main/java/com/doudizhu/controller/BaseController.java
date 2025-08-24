package com.doudizhu.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础API控制器
 */
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class BaseController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "斗地主服务运行正常");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "斗地主在线游戏");
        response.put("version", "1.0.0");
        response.put("description", "基于Spring Boot的斗地主后端服务");
        return response;
    }
}