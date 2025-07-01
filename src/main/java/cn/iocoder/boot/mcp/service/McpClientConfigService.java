package cn.iocoder.boot.mcp.service;

import cn.iocoder.boot.mcp.dto.McpConfigResponse;
import cn.iocoder.boot.mcp.dto.McpServerConfigRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP客户端配置服务
 * 简化版本 - 专注于配置管理
 * 
 * @author backend-team
 */
@Slf4j
@Service
public class McpClientConfigService {

    /**
     * 存储MCP服务器配置
     */
    private final Map<String, McpServerConfigRequest> serverConfigs = new ConcurrentHashMap<>();

    /**
     * 配置MCP服务器连接
     * 
     * @param configRequest 配置请求
     * @return 配置响应
     */
    public McpConfigResponse configureMcpServer(McpServerConfigRequest configRequest) {
        try {
            log.info("配置MCP服务器: {}", configRequest.getServerName());
            
            // 验证配置
            if (configRequest.getServerName() == null || configRequest.getServerName().trim().isEmpty()) {
                return McpConfigResponse.failure(
                    configRequest.getServerName(),
                    "配置失败",
                    "服务器名称不能为空"
                );
            }
            
            if (configRequest.getTransportType() == null) {
                return McpConfigResponse.failure(
                    configRequest.getServerName(),
                    "配置失败",
                    "传输类型不能为空"
                );
            }
            
            // 存储配置
            serverConfigs.put(configRequest.getServerName(), configRequest);
            
            log.info("MCP服务器配置成功: {}", configRequest.getServerName());
            return McpConfigResponse.success(
                configRequest.getServerName(),
                "MCP服务器配置成功"
            );
            
        } catch (Exception e) {
            log.error("配置MCP服务器失败: {}", configRequest.getServerName(), e);
            return McpConfigResponse.failure(
                configRequest.getServerName(),
                "配置失败",
                e.getMessage()
            );
        }
    }

    /**
     * 获取MCP服务器配置
     * 
     * @param serverName 服务器名称
     * @return 服务器配置
     */
    public McpServerConfigRequest getServerConfig(String serverName) {
        return serverConfigs.get(serverName);
    }

    /**
     * 删除MCP服务器配置
     * 
     * @param serverName 服务器名称
     * @return 是否删除成功
     */
    public boolean removeServerConfig(String serverName) {
        McpServerConfigRequest removed = serverConfigs.remove(serverName);
        if (removed != null) {
            log.info("删除MCP服务器配置: {}", serverName);
            return true;
        }
        return false;
    }

    /**
     * 获取所有已配置的服务器
     */
    public Map<String, String> getConfiguredServers() {
        Map<String, String> servers = new ConcurrentHashMap<>();
        serverConfigs.forEach((name, config) -> {
            String status = "CONFIGURED";
            servers.put(name, status);
        });
        return servers;
    }

    /**
     * 检查服务器是否已配置
     */
    public boolean isServerConfigured(String serverName) {
        return serverConfigs.containsKey(serverName);
    }

    /**
     * 获取配置数量
     */
    public int getConfiguredServerCount() {
        return serverConfigs.size();
    }
} 