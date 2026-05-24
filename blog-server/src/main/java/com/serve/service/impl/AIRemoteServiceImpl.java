package com.serve.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.serve.client.AIRemoteClient;
import com.serve.config.JsonTemplateConfig;
import com.serve.context.BaseContext;
import com.serve.dto.RequestAIDTO;
import com.serve.po.AIChatMessage;
import com.serve.service.AIRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIRemoteServiceImpl implements AIRemoteService {

    private final AIRemoteClient aiRemoteClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JsonTemplateConfig jsonTemplateConfig;

    private static final String CHAT_KEY_PREFIX = "chat:history:";
    private static final int MAX_HISTORY_ROUND = 5;

    private boolean reasoningEnded = false;

    @Override
    public void remoteRequestAIStream(RequestAIDTO requestAIDTO, Consumer<String> consumer)
            throws Exception {
        String key = CHAT_KEY_PREFIX + BaseContext.getThreadLocal();

        //获取历史消息
        List<AIChatMessage> userHistoryChat = getUserHistoryChat(key);
        List<AIChatMessage> AIChatMessages = buildAllMessages(userHistoryChat, requestAIDTO);

        // 1. 读取模板
        JsonNode jsonNode = jsonTemplateConfig.getJsonNode();
        // 2. 解析并修改 message 下的 content & role
        buildRequestBody(jsonNode, AIChatMessages, requestAIDTO.getDeepThinking());

        //3. 转成最终要发送的 JSON
        String value = objectMapper.writeValueAsString(jsonNode);

        MediaType mediaType = MediaType.Companion.parse("application/json");
        RequestBody requestBody = RequestBody.Companion.create(value, mediaType);
        StringBuilder aiReply = new StringBuilder();
        try {
            InputStream in = aiRemoteClient.getAIMsgStreamForJson(requestBody);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank() || line.startsWith(":")) {
                    continue;
                }
                if (!line.startsWith("data:")) continue;
                String data = line.substring(6).trim();

                if ("[DONE]".equals(data)) {break;}

                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    String reasoningContent = objectMapper.readTree(data)
                            .at("/choices/0/delta/reasoning_content")
                            .asText();

                    if (!reasoningContent.isEmpty() && !reasoningContent.equals("null")) {
                        aiReply.append(reasoningContent);
                        consumer.accept(reasoningContent);
                    }
                }catch (Exception e) {
                    log.error("解析事件失败: {}", e.getMessage());
                    consumer.accept("文本返回失败!");
                }

                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    String content = objectMapper.readTree(data)
                            .at("/choices/0/delta/content")
                            .asText();
                    if (!content.isEmpty() && !content.equals("null")) {
                        if (!reasoningEnded) {
                            consumer.accept("[PART_DONE]");
                            reasoningEnded = true;
                        }
                        aiReply.append(content);
                        consumer.accept(content);
                    }
                } catch (Exception e) {
                    log.error("解析事件失败: {}", e.getMessage());
                    consumer.accept("文本返回失败!");
                }
            }
            reasoningEnded = false;
            
            //更新用户的对话历史（追加当前对话）
            updateHistoryMessage(AIChatMessages, aiReply.toString(), key);
        } catch (Exception e) {
            log.error("读取流失败: {}", e.getMessage());
            consumer.accept("服务暂时繁忙，请稍后再试~");
        }
    }

    private List<AIChatMessage> getUserHistoryChat(String key) {
        Object history = redisTemplate.opsForValue().get(key);
        if (history instanceof List<?>) {
            return (List<AIChatMessage>) history;
        }
        return new ArrayList<>();
    }

    /**
     *
     * @param history 历史消息
     * @param requestAIDTO 用户当前提问
     * @return
     */
    private List<AIChatMessage> buildAllMessages(List<AIChatMessage> history, RequestAIDTO requestAIDTO) {
        AIChatMessage AIChatMessage = new AIChatMessage();
        AIChatMessage.setMessage(requestAIDTO.getMessage());
        AIChatMessage.setRole("user");
        history.add(AIChatMessage);

        if (history.size() > MAX_HISTORY_ROUND * 2 + 1) {
            int deleteSize = history.size() - (MAX_HISTORY_ROUND * 2 + 1);
            //保留从开始删除的序列号开始直到末尾的消息
            history.subList(deleteSize, history.size());
        }

        return history;
    }

    private void buildRequestBody(JsonNode jsonNode, List<AIChatMessage> AIChatMessages, Boolean deepThinking) {
        ObjectNode messageNode = (ObjectNode) jsonNode;
        if (messageNode == null || messageNode.isArray() || messageNode.size() == 0) {
            throw new RuntimeException("JSON 模板格式错误，实际类型为数组或为空");
        }

        if (deepThinking) {
            ObjectNode thinking = (ObjectNode) messageNode.get("thinking");
            thinking.put("type", "enabled");
        }

        JsonNode oldMessage = messageNode.get("messages");
        ArrayNode newMessages = objectMapper.createArrayNode();
        if (oldMessage == null) {
            messageNode.set("messages", newMessages);
        } else if (!oldMessage.isArray()) {
            throw new RuntimeException("JSON模板格式错误,messages必须是数组类型");
        }else {
            newMessages = (ArrayNode) oldMessage;
        }

        for (AIChatMessage AIChatMessage : AIChatMessages) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("content", AIChatMessage.getMessage());
            objectNode.put("role", AIChatMessage.getRole());

            newMessages.add(objectNode);
        }
    }

    //更新redis的历史消息
    private void updateHistoryMessage(List<AIChatMessage> AIChatMessages, String newMessage, String key) {
        AIChatMessages.add(new AIChatMessage("assistant", newMessage));

        redisTemplate.opsForValue().set(key, AIChatMessages, 10, TimeUnit.MINUTES);
    }
}
