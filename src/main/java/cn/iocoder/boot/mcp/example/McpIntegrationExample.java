package cn.iocoder.boot.mcp.example;

import cn.iocoder.boot.mcp.dto.McpServerConfigRequest;
import cn.iocoder.boot.mcp.service.McpClientConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MCP集成使用示例
 * 
 * @author backend-team
 */
@Slf4j
//@Component  // 暂时注释掉，避免在没有ChatModel配置时启动失败
public class McpIntegrationExample implements CommandLineRunner {

    private final McpClientConfigService mcpClientConfigService;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ChatModel chatModel;

    public McpIntegrationExample(McpClientConfigService mcpClientConfigService,
                                ToolCallbackProvider toolCallbackProvider,
                                ChatModel chatModel) {
        this.mcpClientConfigService = mcpClientConfigService;
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatModel = chatModel;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== MCP集成示例启动 ===");
        
        // 1. 配置MCP服务器连接（连接到自己的agenticRagMcp服务器）
        configureSelfMcpServer();
        
        // 2. 演示使用MCP工具进行AI对话
        demonstrateMcpToolUsage();
        
        log.info("=== MCP集成示例完成 ===");
    }

    /**
     * 配置连接到外部MCP服务器示例
     */
    private void configureSelfMcpServer() {
        try {
            // 这里应该配置连接到实际的外部MCP服务器
            // 示例：连接到另一个MCP服务器
            McpServerConfigRequest config = new McpServerConfigRequest()
                    .setServerName("external-mcp-server")
                    .setTransportType(McpServerConfigRequest.TransportType.SSE)
                    .setSseConfig(new McpServerConfigRequest.SseConfig()
                            .setUrl("http://external-mcp-server:8080")
                            .setTimeoutSeconds(30));
            
            var response = mcpClientConfigService.configureMcpServer(config);
            log.info("MCP服务器配置结果: {}", response.getMessage());
            
        } catch (Exception e) {
            log.warn("配置MCP服务器时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 演示使用MCP工具进行AI对话
     */
    private void demonstrateMcpToolUsage() {
        try {
            // 注意：这里需要配置实际的ChatModel，比如OpenAI
            if (chatModel == null) {
                log.info("未配置ChatModel，跳过AI对话演示");
                return;
            }
            
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultTools(toolCallbackProvider)
                    .build();

            // 示例对话1：使用AgenticRag查询
            String query1 = "请使用AgenticRag查询Spring Boot的相关信息";
            log.info("发送查询: {}", query1);
            
            String response1 = chatClient.prompt(query1)
                    .call()
                    .content();
            
            log.info("AI响应: {}", response1);

            // 示例对话2：检查服务状态
            String query2 = "检查AgenticRag服务的运行状态";
            log.info("发送查询: {}", query2);
            
            String response2 = chatClient.prompt(query2)
                    .call()
                    .content();
            
            log.info("AI响应: {}", response2);
            
        } catch (Exception e) {
            log.warn("演示MCP工具使用时发生异常: {}", e.getMessage());
        }
    }
} 