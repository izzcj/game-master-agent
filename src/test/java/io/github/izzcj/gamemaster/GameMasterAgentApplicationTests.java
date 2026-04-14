package io.github.izzcj.gamemaster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 应用集成测试。
 *
 * <p>覆盖后台配置接口和上传后问答闭环的基础可用性。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class GameMasterAgentApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 验证模型配置接口可以正常返回绑定信息。
     */
    @Test
    void adminModelsEndpointShouldReturnConfiguredBindings() throws Exception {
        String content = mockMvc.perform(get("/api/admin/models"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        assertThat(json.path("success").asBoolean()).isTrue();
        assertThat(json.path("data").path("chatScenes").isArray()).isTrue();
    }

    /**
     * 验证文件上传、异步摄取和内部知识问答链路。
     */
    @Test
    void uploadThenQueryShouldReturnGroundedAnswer() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "guide.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Elden Ring strength builds should level vigor first, then strength, and use heavy affinity weapons."
                .getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/knowledge/files")
                .file(file)
                .param("gameName", "Elden Ring")
                .param("platform", "PC")
                .param("tags", "build,strength"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String jobId = uploadJson.path("data").path("jobId").asText();

        JsonNode jobJson = waitForJob(jobId);
        assertThat(jobJson.path("data").path("status").asText()).isEqualTo("SUCCEEDED");

        String requestBody = """
            {
              "question": "How should I start a strength build in Elden Ring?",
              "gameName": "Elden Ring",
              "platform": "PC",
              "useExternalSearch": false
            }
            """;

        MvcResult queryResult = mockMvc.perform(post("/api/chat/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode queryJson = objectMapper.readTree(queryResult.getResponse().getContentAsString());
        assertThat(queryJson.path("success").asBoolean()).isTrue();
        assertThat(queryJson.path("data").path("answer").asText()).containsIgnoringCase("strength");
        assertThat(queryJson.path("data").path("citations").isArray()).isTrue();
    }

    /**
     * 轮询等待摄取任务完成。
     *
     * @param jobId 任务 ID
     * @return 最终任务响应
     * @throws Exception 轮询请求异常
     */
    private JsonNode waitForJob(String jobId) throws Exception {
        JsonNode json = null;
        for (int i = 0; i < 20; i++) {
            MvcResult result = mockMvc.perform(get("/api/knowledge/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andReturn();
            json = objectMapper.readTree(result.getResponse().getContentAsString());
            String status = json.path("data").path("status").asText();
            if ("SUCCEEDED".equals(status) || "FAILED".equals(status)) {
                return json;
            }
            Thread.sleep(200L);
        }
        return json;
    }
}
