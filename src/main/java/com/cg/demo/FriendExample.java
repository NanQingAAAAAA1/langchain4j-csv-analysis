//package com.cg.chatmemory;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//
///**
// * chat memory示例
// *
// */
//public class FriendExample {
//
//    public static void main(String[] args) {
//        // ChatLanguageModel 是一个统一接口，提供各种实现类
//        ChatLanguageModel model = OpenAiChatModel.builder()
//                .apiKey("demo")
//                .modelName("gpt-4o-mini")
//                .build();
//
//        ChatMemory chatMemory = (ChatMemory) MessageWindowChatMemory.builder()
//                .maxMessages(10)
//                .build();
//
//        chatMemory.add(UserMessage.from("我要吃饭,你可以推荐给我一种好吃的吗？"));
//        AiMessage answer = model.generate(chatMemory.messages()).content();
//        System.out.println(answer.text());
//        chatMemory.add(answer); // 关键：将AI的回复进行缓存
//
//        chatMemory.add(UserMessage.from("你还记得你推荐给我的好吃的是什么吗？"));
//        AiMessage answer2 = model.generate(chatMemory.messages()).content();
//        System.out.println(answer2.text());
//        chatMemory.add(answer2);
//
//    }
//}
