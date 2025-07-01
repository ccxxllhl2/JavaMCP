package cn.iocoder.boot.mcp.controller;

import cn.iocoder.boot.mcp.dto.McpConfigResponse;
import cn.iocoder.boot.mcp.dto.McpServerConfigRequest;
import cn.iocoder.boot.mcp.service.McpClientConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * MCP客户端配置控制器
 * 提供MCP服务器配置管理的REST API
 * 
 * @author backend-team
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpClientController {

    private final McpClientConfigService mcpClientConfigService;
    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider toolCallbackProvider;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.fromCallable(() -> {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "MCP Client Service",
                "configuredServers", mcpClientConfigService.getConfiguredServerCount(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(health);
        });
    }

    /**
     * 配置MCP服务器
     * 
     * @param configRequest 配置请求
     * @return 配置响应
     */
    @PostMapping("/servers")
    public Mono<ResponseEntity<McpConfigResponse>> configureServer(
            @RequestBody McpServerConfigRequest configRequest) {
        
        return Mono.fromCallable(() -> {
            log.info("收到MCP服务器配置请求: {}", configRequest.getServerName());
            
            McpConfigResponse response = mcpClientConfigService.configureMcpServer(configRequest);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        });
    }

    /**
     * 获取所有配置的MCP服务器
     * 
     * @return 服务器列表及状态
     */
    @GetMapping("/servers")
    public Mono<ResponseEntity<Map<String, String>>> getServers() {
        return Mono.fromCallable(() -> {
            Map<String, String> servers = mcpClientConfigService.getConfiguredServers();
            return ResponseEntity.ok(servers);
        });
    }

    /**
     * 获取特定MCP服务器配置
     * 
     * @param serverName 服务器名称
     * @return 服务器配置
     */
    @GetMapping("/servers/{serverName}")
    public Mono<ResponseEntity<McpServerConfigRequest>> getServer(
            @PathVariable String serverName) {
        
        return Mono.fromCallable(() -> {
            McpServerConfigRequest config = mcpClientConfigService.getServerConfig(serverName);
            
            if (config != null) {
                return ResponseEntity.ok(config);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    /**
     * 删除MCP服务器配置
     * 
     * @param serverName 服务器名称
     * @return 删除结果
     */
    @DeleteMapping("/servers/{serverName}")
    public Mono<ResponseEntity<Map<String, Object>>> removeServer(
            @PathVariable String serverName) {
        
        return Mono.fromCallable(() -> {
            boolean removed = mcpClientConfigService.removeServerConfig(serverName);
            
            Map<String, Object> result = Map.of(
                "serverName", serverName,
                "removed", removed,
                "message", removed ? "服务器配置已删除" : "服务器配置不存在"
            );
            
            if (removed) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    /**
     * 检查服务器配置状态
     * 
     * @param serverName 服务器名称
     * @return 配置状态
     */
    @GetMapping("/servers/{serverName}/status")
    public Mono<ResponseEntity<Map<String, Object>>> getServerStatus(
            @PathVariable String serverName) {
        
        return Mono.fromCallable(() -> {
            boolean configured = mcpClientConfigService.isServerConfigured(serverName);
            
            Map<String, Object> status = Map.of(
                "serverName", serverName,
                "configured", configured,
                "status", configured ? "CONFIGURED" : "NOT_CONFIGURED"
            );
            
            return ResponseEntity.ok(status);
        });
    }

    /**
     * 测试MCP工具调用
     * 
     * @param query 查询内容
     * @return 查询结果
     */
    @GetMapping("/test/query")
    public Mono<ResponseEntity<Map<String, Object>>> testQuery(@RequestParam String query) {
        log.info("收到测试查询请求: {}", query);
        
        return Mono.fromCallable(() -> {
            try {
                // 创建ChatClient，工具已在配置中注册，无需重复添加
                ChatClient chatClient = chatClientBuilder.build();
                
                String response = chatClient.prompt()
                        .user("请使用AgenticRag工具查询: " + query)
                        .call()
                        .content();
                
                Map<String, Object> result = Map.of(
                    "query", query,
                    "success", true,
                    "response", response,
                    "timestamp", System.currentTimeMillis(),
                    "method", "MCP工具调用"
                );
                
                return ResponseEntity.ok(result);
                
            } catch (Exception e) {
                log.error("MCP工具调用失败: {}", e.getMessage(), e);
                Map<String, Object> result = Map.of(
                    "query", query,
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                );
                
                return ResponseEntity.status(500).body(result);
            }
        });
    }

    /**
     * 流式AI对话
     * 
     * @param prompt 用户提示
     * @return 流式响应
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(@RequestParam String prompt) {
        log.info("收到流式对话请求: {}", prompt);
        
        return Flux.<String>create(sink -> {
            try {
                // 创建ChatClient，工具已在配置中注册，无需重复添加
                ChatClient chatClient = chatClientBuilder.build();
                
                // 发送初始响应
                sink.next("data: 开始处理您的请求...\n\n");
                
                // 调用AI模型
                String response = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
                
                // 模拟流式输出
                String[] words = response.split(" ");
                for (int i = 0; i < words.length; i++) {
                    sink.next("data: " + words[i] + (i < words.length - 1 ? " " : "") + "\n\n");
                    
                    // 添加延迟模拟流式效果
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                sink.next("data: [DONE]\n\n");
                sink.complete();
                
            } catch (Exception e) {
                log.error("流式对话处理失败: {}", e.getMessage(), e);
                sink.next("data: 错误: " + e.getMessage() + "\n\n");
                sink.next("data: [ERROR]\n\n");
                sink.complete();
            }
        })
        .delayElements(Duration.ofMillis(50));
    }
} 