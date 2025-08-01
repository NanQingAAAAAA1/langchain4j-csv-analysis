//package com.cg.aiservicedemo;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.agent.tool.Tool;
//import dev.langchain4j.agent.tool.ToolSpecification;
//import dev.langchain4j.agent.tool.ToolSpecifications;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.service.MemoryId;
//import dev.langchain4j.service.UserMessage;
//
//import java.time.LocalDate;
//
//public class UserAiService {
//
//    interface Assistant {
//        String chat(@MemoryId Long userId, @UserMessage String message);
//    }
//
//    @Tool("获取当前日期")
//    public static String dataUtil() {
//        return LocalDate.now().toString();
//    }
//
//    public static void main(String[] args) throws NoSuchMethodException {
//
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//
//        ChatLanguageModel model = ModelUtil.getOpenAIModel();
//
//        ToolSpecification toolSpecification =ToolSpecifications.toolSpecificationFrom(UserAiService.class.getMethod("dataUtil"));
//
//        Assistant assistant = AiServices.builder(Assistant.class)
//                .chatLanguageModel(model)
////                .chatMemory(chatMemory)
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
//                .tools(toolSpecification)
//                .build();
//
//        System.out.println(assistant.chat(1L, "你好，我是南清一号"));
//        System.out.println(assistant.chat(1L, "我的名字是什么？"));
//
//        System.out.println(assistant.chat(1L, "你好，我是南清二号"));
//        System.out.println(assistant.chat(1L, "我的名字是什么？"));
//    }
//}
