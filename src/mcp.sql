-- 添加MCP相关字段
ALTER TABLE chat_agents_info 
ADD COLUMN IF NOT EXISTS mcp_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用MCP工具';

ALTER TABLE chat_agents_info 
ADD COLUMN IF NOT EXISTS mcp_server_url VARCHAR(500) COMMENT 'MCP服务器地址';

ALTER TABLE chat_agents_info 
ADD COLUMN IF NOT EXISTS mcp_server_name VARCHAR(100) COMMENT 'MCP服务器配置名称';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_chat_agents_mcp_enabled ON chat_agents_info(mcp_enabled);
CREATE INDEX IF NOT EXISTS idx_chat_agents_mcp_server_name ON chat_agents_info(mcp_server_name);

-- 更新表注释
COMMENT ON COLUMN chat_agents_info.mcp_enabled IS '是否启用MCP工具';
COMMENT ON COLUMN chat_agents_info.mcp_server_url IS 'MCP服务器地址';
COMMENT ON COLUMN chat_agents_info.mcp_server_name IS 'MCP服务器配置名称';
