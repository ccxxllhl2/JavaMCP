package cn.iocoder.boot.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgenticRag MCP工具测试类
 * 测试MCP服务器是否可以正常查询agenticRAG服务
 *
 * @author backend
 */
@SpringBootTest
@TestPropertySource(properties = {
    "mcp.agenticrag.timeout=10s"
})
class AgenticRagMcpToolsTest {

    private MockWebServer mockWebServer;
    private AgenticRagMcpTools agenticRagMcpTools;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        // 启动Mock Web Server
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // 创建WebClient指向Mock服务器
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        
        // 创建测试对象
        agenticRagMcpTools = new AgenticRagMcpTools(webClient);
        
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(agenticRagMcpTools, "agenticRagBaseUrl", baseUrl);
        ReflectionTestUtils.setField(agenticRagMcpTools, "timeout", Duration.ofSeconds(10));
        
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testQueryWithAgenticRag_成功响应() throws InterruptedException {
        // 准备模拟响应数据
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("data", "这是AgenticRAG的查询结果");
        mockResponse.put("query", "测试查询");
        
        String responseJson = "";
        try {
            responseJson = objectMapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            responseJson = "{\"status\":\"success\",\"data\":\"这是AgenticRAG的查询结果\"}";
        }
        
        // 设置Mock响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // 执行测试
        String result = agenticRagMcpTools.queryWithAgenticRag("测试查询");

        // 验证结果
        assertNotNull(result, "查询结果不应为空");
        assertTrue(result.contains("success"), "响应应包含success状态");
        assertTrue(result.contains("AgenticRAG的查询结果"), "响应应包含查询结果");

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod(), "应该使用GET方法");
        assertTrue(request.getPath().contains("/query"), "请求路径应包含/query");
        assertTrue(request.getPath().contains("q=%E6%B5%8B%E8%AF%95%E6%9F%A5%E8%AF%A2"), "请求应包含查询参数");
    }

    @Test
    void testQueryWithAgenticRag_服务不可用() throws InterruptedException {
        // 设置Mock响应为服务错误
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // 执行测试
        String result = agenticRagMcpTools.queryWithAgenticRag("测试查询");

        // 验证结果
        assertNotNull(result, "查询结果不应为空");
        assertEquals("AgenticRag服务调用失败", result, "应返回服务调用失败信息");

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod(), "应该使用GET方法");
    }

    @Test
    void testGetAgenticRagStatus_服务正常() throws InterruptedException {
        // 准备健康检查响应
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "AgenticRAG");
        
        String responseJson = "";
        try {
            responseJson = objectMapper.writeValueAsString(healthResponse);
        } catch (Exception e) {
            responseJson = "{\"status\":\"UP\",\"service\":\"AgenticRAG\"}";
        }

        // 设置Mock响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // 执行测试
        String result = agenticRagMcpTools.getAgenticRagStatus();

        // 验证结果
        assertNotNull(result, "状态检查结果不应为空");
        assertTrue(result.contains("AgenticRag服务状态"), "应包含服务状态信息");
        assertTrue(result.contains("UP"), "应包含UP状态");

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod(), "应该使用GET方法");
        assertTrue(request.getPath().contains("/health"), "请求路径应包含/health");
    }

    @Test
    void testGetAgenticRagStatus_服务异常() throws InterruptedException {
        // 设置Mock响应为连接超时
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Service Unavailable"));

        // 执行测试
        String result = agenticRagMcpTools.getAgenticRagStatus();

        // 验证结果
        assertNotNull(result, "状态检查结果不应为空");
        assertTrue(result.contains("AgenticRag服务状态"), "应包含服务状态信息");
        assertTrue(result.contains("服务不可用"), "应包含服务不可用信息");

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod(), "应该使用GET方法");
    }

    @Test
    void testBatchQueryWithAgenticRag_多个查询() throws InterruptedException {
        // 为每个查询准备响应
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\":\"success\",\"data\":\"结果1\"}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));
        
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\":\"success\",\"data\":\"结果2\"}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // 执行批量查询
        String result = agenticRagMcpTools.batchQueryWithAgenticRag(Arrays.asList("查询1", "查询2"));

        // 验证结果
        assertNotNull(result, "批量查询结果不应为空");
        assertTrue(result.contains("查询1"), "应包含第一个查询内容");
        assertTrue(result.contains("查询2"), "应包含第二个查询内容");
        assertTrue(result.contains("结果1"), "应包含第一个查询结果");
        assertTrue(result.contains("结果2"), "应包含第二个查询结果");

        // 验证请求数量
        assertEquals(2, mockWebServer.getRequestCount(), "应该发送2个请求");
    }

    @Test
    void testQueryWithAgenticRag_空查询() {
        // 设置正常响应
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\":\"success\",\"data\":\"空查询处理\"}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // 执行空查询测试
        String result = agenticRagMcpTools.queryWithAgenticRag("");

        // 验证结果
        assertNotNull(result, "空查询结果不应为空");
        // 即使是空查询，服务也应该能处理并返回结果
    }

    @Test
    void testQueryWithAgenticRag_中文查询() throws InterruptedException {
        // 设置中文响应
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\":\"success\",\"data\":\"中文查询结果\"}")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .setResponseCode(200));

        // 执行中文查询
        String result = agenticRagMcpTools.queryWithAgenticRag("什么是人工智能？");

        // 验证结果
        assertNotNull(result, "中文查询结果不应为空");
        assertTrue(result.contains("success"), "响应应包含success状态");

        // 验证请求路径包含正确编码的中文
        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(request.getPath().contains("/query"), "请求路径应包含/query");
    }
} 
