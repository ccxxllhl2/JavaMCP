package cn.iocoder.boot.mcp.config;

import cn.iocoder.boot.mcp.service.AgenticRagMcpTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * MCP配置类
 * 基于ref_mcp_code的简化实现模式
 * 
 * @author backend
 */
@Configuration
@Slf4j
public class McpConfig {

    /**
     * WebClient配置 - 用于调用外部API
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * 注册MCP工具回调提供器
     * 基于ref_mcp_code的MethodToolCallbackProvider模式
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(ApplicationContext applicationContext) {
        log.info("正在注册MCP工具: AgenticRagMcpTools");
        AgenticRagMcpTools agenticRagMcpTools = applicationContext.getBean(AgenticRagMcpTools.class);
        return MethodToolCallbackProvider.builder()
                .toolObjects(agenticRagMcpTools)
                .build();
    }

    /**
     * ChatClient.Builder配置
     * 基于ref_mcp_code的模式，为控制器提供ChatClient构建器
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        log.info("配置ChatClient.Builder，使用ChatModel: {}", chatModel.getClass().getSimpleName());
        return ChatClient.builder(chatModel)
                .defaultTools(toolCallbackProvider);
    }
} 