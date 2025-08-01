//package com.cg.tools;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//
//public class Demo1 {
//
//    public static void main(String[] args) {
//
//        ChatLanguageModel model = OpenAiChatModel.builder()
//                .apiKey("demo")
//                .modelName("gpt-4o-mini")
//                .build();
//
//        String answer = model.generate("今天是几月几号？"); // 会出现幻觉 -> 需要工具机制
//
//        System.out.println(answer);
//    }
//}
