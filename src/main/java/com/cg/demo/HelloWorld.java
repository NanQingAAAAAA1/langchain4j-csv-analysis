//package com.cg.base;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.model.output.Response;
//
///**
// * 基础会话示例
// */
//public class HelloWorld {
//
//    public static void main(String[] args) {
//        // ChatLanguageModel 是一个统一接口，提供各种实现类
//        ChatLanguageModel model = OpenAiChatModel.builder()
//                .apiKey("demo")
//                .modelName("gpt-4o-mini")
//                .build();
//
//        System.out.println("======================没有记忆的会话======================");
//
//        // generate 方法用于向模型发话
//        String answer = model.generate("你好");
//        System.out.println(answer);
//
//        // 会话的概念：在两次交互过程中openai并没有记住之前的会话内容
//        String answer2 = model.generate("请重复一次");
//        System.out.println(answer2);
//
//        // 想要做到有记忆的交互的代码如下：
//        System.out.println("======================有记忆的会话======================");
//
//        UserMessage userMessage = UserMessage.from("你是谁");
//        Response<AiMessage> response =  model.generate(userMessage);
//        AiMessage aiMessage = response.content();
//        System.out.println(aiMessage.text());
//
//        UserMessage userMessage2 = UserMessage.from("请重复一次");
//        Response<AiMessage> response2 = model.generate(userMessage, aiMessage, userMessage2);
//
//        System.out.println(response2.content().text());
//    }
//}
