package cn.iocoder.boot.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AgenticRag MCP工具服务
 * 基于ref_mcp_code的简化实现模式
 * 
 * @author backend
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgenticRagMcpTools {
    
    private final WebClient webClient;
    
    @Value("${mcp.agenticrag.base-url:http://localhost:8080}")
    private String agenticRagBaseUrl;
    
    @Value("${mcp.agenticrag.timeout:30s}")
    private Duration timeout;

    /**
     * 智能查询工具 - 调用AgenticRag服务
     */
    @Tool(description = "使用AgenticRag服务进行智能查询")
    public String queryWithAgenticRag(String query) {
        log.info("MCP工具调用: queryWithAgenticRag，查询内容: {}", query);
        
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(agenticRagBaseUrl + "/query")
                    .queryParam("q", query)
                    .toUriString();
            
            String response = webClient.get()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .onErrorReturn("AgenticRag服务调用失败")
                    .block();
            
            log.info("AgenticRag服务响应: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("调用AgenticRag服务出错: {}", e.getMessage());
            return "查询失败: " + e.getMessage();
        }
    }

    /**
     * 批量查询工具
     */
    @Tool(description = "批量查询AgenticRag服务")
    public String batchQueryWithAgenticRag(List<String> queries) {
        log.info("MCP工具调用: batchQueryWithAgenticRag，查询数量: {}", queries.size());
        
        StringBuilder results = new StringBuilder();
        
        for (int i = 0; i < queries.size(); i++) {
            String query = queries.get(i);
            String result = queryWithAgenticRag(query);
            results.append(String.format("查询%d: %s\n结果: %s\n\n", i + 1, query, result));
        }
        
        return results.toString();
    }

    /**
     * 服务状态检查工具
     */
    @Tool(description = "检查AgenticRag服务状态")
    public String getAgenticRagStatus() {
        log.info("MCP工具调用: getAgenticRagStatus");
        
        try {
            String status = webClient.get()
                    .uri(agenticRagBaseUrl + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .onErrorReturn("服务不可用")
                    .block();
            
            return String.format("AgenticRag服务状态: %s", status);
            
        } catch (Exception e) {
            log.error("检查AgenticRag服务状态出错: {}", e.getMessage());
            return "服务状态检查失败: " + e.getMessage();
        }
    }
} 