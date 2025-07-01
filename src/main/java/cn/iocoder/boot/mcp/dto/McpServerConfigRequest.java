package cn.iocoder.boot.mcp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * MCP服务器配置请求DTO
 * 
 * @author backend-team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class McpServerConfigRequest {

    /**
     * 服务器名称
     */
    @NotBlank(message = "服务器名称不能为空")
    private String serverName;

    /**
     * 传输类型：STDIO 或 SSE
     */
    @NotNull(message = "传输类型不能为空")
    private TransportType transportType;

    /**
     * STDIO配置
     */
    private StdioConfig stdioConfig;

    /**
     * SSE配置
     */
    private SseConfig sseConfig;

    /**
     * 传输类型枚举
     */
    public enum TransportType {
        STDIO, SSE
    }

    /**
     * STDIO配置
     */
    @Data
    @Accessors(chain = true)
    public static class StdioConfig {
        /**
         * 命令
         */
        @NotBlank(message = "命令不能为空")
        private String command;

        /**
         * 参数列表
         */
        private List<String> args;

        /**
         * 环境变量
         */
        private Map<String, String> env;
    }

    /**
     * SSE配置
     */
    @Data
    @Accessors(chain = true)
    public static class SseConfig {
        /**
         * 服务器URL
         */
        @NotBlank(message = "服务器URL不能为空")
        private String url;

        /**
         * 连接超时时间（秒）
         */
        private Integer timeoutSeconds = 30;

        /**
         * 请求头
         */
        private Map<String, String> headers;
    }
} 