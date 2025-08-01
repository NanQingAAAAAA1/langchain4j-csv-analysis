//package com.cg.tools;
//
//import com.google.common.collect.Lists;
//import dev.langchain4j.agent.tool.Tool;
//import dev.langchain4j.agent.tool.ToolExecutionRequest;
//import dev.langchain4j.agent.tool.ToolSpecification;
//import dev.langchain4j.agent.tool.ToolSpecifications;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.ToolExecutionResultMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.output.Response;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.time.LocalDate;
//import java.util.Collections;
//
//public class ToolsDemo1 {
//
//    @Tool("获取当前日期")
//    public static String dataUtil() {
//        return LocalDate.now().toString();
//    }
//
//    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//
//        ChatLanguageModel model = ModelUtil.getOpenAIModel();
//
//        ToolSpecification toolSpecification = ToolSpecifications.
//                toolSpecificationFrom(ToolsDemo1.class.getMethod("dataUtil"));
//
//
//        UserMessage userMessage =UserMessage.from("今天是几月几号？");
//
//        // 第一次生成 AI 响应时携带工具规格
//        Response<AiMessage> aiMessageResponse = model.generate(
//                Collections.singletonList(userMessage),
//                Collections.singletonList(toolSpecification) // 添加工具规格
//        );
//
//        AiMessage aiMessage = aiMessageResponse.content();
//        System.out.println(aiMessage);
//
//        if(aiMessage.hasToolExecutionRequests()) {
//            for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
//                String methodName = toolExecutionRequest.name();
//                Method method =ToolsDemo1.class.getMethod(methodName);
//
//                String result = (String) method.invoke(null);
//                System.out.println(result);
//
//                // 构建执行工具的请求结果
//                ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage
//                        .from(toolExecutionRequest.id(), toolExecutionRequest.name(), result);
//
//                // 将消息打包一起传给大模型
//                // 第二次生成时传递完整消息链路
//                Response<AiMessage> response = model.generate(Lists.newArrayList(userMessage, aiMessage, toolExecutionResultMessage));
//                System.out.println(response.content().text());
//            }
//        }
//    }
//}
