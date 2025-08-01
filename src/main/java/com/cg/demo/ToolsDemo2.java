//package com.cg.tools;
//
//import dev.langchain4j.agent.tool.*;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.ChatMessage;
//import dev.langchain4j.data.message.ToolExecutionResultMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.model.output.Response;
//import dev.langchain4j.service.tool.DefaultToolExecutor;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//public class ToolsDemo2 {
//
//    public static void main(String[] args) throws NoSuchMethodException {
//        // 1. 使用工具类统一管理模型配置
//        ChatLanguageModel model = OpenAiChatModel.builder()
//                .modelName("gpt-4o-mini")
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .build();
//
//        // 2. 获取工具方法规格
//        Method weatherMethod = WeatherUtil.class.getMethod("getWeather", String.class);
//        ToolSpecification toolSpec = ToolSpecifications.toolSpecificationFrom(weatherMethod);
//
//        List<ChatMessage> chatMessages = new ArrayList<>();
//        chatMessages.add(UserMessage.from("北京市的天气怎么样"));
//
//        // 3. 注册工具规格到模型
//        Response<AiMessage> aiResponse = model.generate(
//                chatMessages,
//                Collections.singletonList(toolSpec) // 关键修改：传递工具规格
//        );
//
//        AiMessage aiMessage = aiResponse.content();
//        System.out.println("[AI初始响应] " + aiMessage);
//        chatMessages.add(aiMessage);
//
//        // 4. 安全处理工具执行请求
//        List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests() != null ?
//                aiMessage.toolExecutionRequests() : Collections.emptyList();
//
//        if (!toolRequests.isEmpty()) {
//            System.out.println("\n=== 工具调用信息 ===");
//            toolRequests.forEach(request -> {
//                System.out.printf("方法: %s\n参数: %s\n", request.name(), request.arguments());
//            });
//
//            WeatherUtil weatherUtil = new WeatherUtil();
//            toolRequests.forEach(request -> {
//                // 5. 执行工具调用
//                DefaultToolExecutor executor = new DefaultToolExecutor(weatherUtil, request);
//                String result = executor.execute(request, UUID.randomUUID().toString());
//
//                System.out.printf("\n工具执行结果: %s", result);
//
//                // 6. 添加执行结果到消息链
//                chatMessages.add(ToolExecutionResultMessage.from(request, result));
//            });
//        } else {
//            System.out.println("\n未触发工具调用");
//        }
//
//        // 7. 获取最终响应
//        AiMessage finalResponse = model.generate(chatMessages).content();
//        System.out.println("\n\n=== 最终响应 ===");
//        System.out.println(finalResponse.text());
//    }
//
//    static class WeatherUtil {
//        @Tool("获取指定城市的天气信息")
//        public String getWeather(@P("需要查询的城市名称") String city) {
//            return String.format("今天%s天气晴", city);
//        }
//    }
//}
