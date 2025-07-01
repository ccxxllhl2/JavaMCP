package cn.iocoder.boot.mcp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * MCP配置响应DTO
 * 
 * @author backend-team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class McpConfigResponse {

    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 连接状态
     */
    private String status;

    /**
     * 错误详情（如果有）
     */
    private String errorDetail;

    /**
     * 创建成功响应
     */
    public static McpConfigResponse success(String serverName, String message) {
        return new McpConfigResponse()
                .setSuccess(true)
                .setServerName(serverName)
                .setMessage(message)
                .setStatus("CONNECTED");
    }

    /**
     * 创建失败响应
     */
    public static McpConfigResponse failure(String serverName, String message, String errorDetail) {
        return new McpConfigResponse()
                .setSuccess(false)
                .setServerName(serverName)
                .setMessage(message)
                .setStatus("FAILED")
                .setErrorDetail(errorDetail);
    }
} 