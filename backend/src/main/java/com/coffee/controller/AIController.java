package com.coffee.controller;

import com.coffee.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI对话控制器
 * 整合文心一言API，提供咖啡杯AI对话功能
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    // 文心一言API配置
    private static final String API_URL = "http://qianfan.bj.baidubce.com/v2/chat/completions";
    private static final String API_KEY = "bce-v3/ALTAK-8BTj0w9Z1YiVUsrW7wsy1/fdab3e3fc726ffab0117acb6fa4fee5ff1decb50";
    private static final String APP_ID = "app-uMhFYP0m";
    private static final String MODEL = "ernie-4.5-turbo-128k";

    // 用于保存用户会话上下文的Map
    // key: sessionId, value: 对话历史消息列表
    private final Map<String, List<Map<String, String>>> sessions = new HashMap<>();

    // 用于HTTP请求的RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();
    // 用于JSON处理的ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI对话接口
     * @param request 包含消息内容和会话ID的请求体
     * @return AI回复结果
     */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody AIChatRequest request) {
        try {
            String message = request.getMessage();
            String sessionId = request.getSessionId();

            // 验证参数
            if (message == null || message.trim().isEmpty()) {
                return Result.error(400, "消息内容不能为空");
            }
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.error(400, "会话ID不能为空");
            }

            // 初始化会话上下文
            sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());
            List<Map<String, String>> context = sessions.get(sessionId);

            // 将用户消息添加到上下文
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            context.add(userMessage);

            // 构建API请求体
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", MODEL);
            payload.put("messages", context);

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + API_KEY);
            headers.put("appid", APP_ID);

            // 调用文心一言API
            // 创建请求实体
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            httpHeaders.setAll(headers);
            org.springframework.http.HttpEntity<Map<String, Object>> httpEntity = new org.springframework.http.HttpEntity<>(payload, httpHeaders);

            // 发送POST请求
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(API_URL, httpEntity, String.class);
            String responseBody = response.getBody();

            // 解析API响应
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String aiReply = rootNode.path("choices").get(0).path("message").path("content").asText("模型未返回内容");

            // 将AI回复添加到上下文
            Map<String, String> aiMessage = new HashMap<>();
            aiMessage.put("role", "assistant");
            aiMessage.put("content", aiReply);
            context.add(aiMessage);

            // 返回结果
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("reply", aiReply);
            return Result.success(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "AI对话服务异常: " + e.getMessage());
        }
    }

    /**
     * AI对话请求DTO
     */
    private static class AIChatRequest {
        private String message;
        private String sessionId;

        // getter and setter
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}