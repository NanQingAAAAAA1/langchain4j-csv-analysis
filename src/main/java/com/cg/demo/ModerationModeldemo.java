//package com.cg.base;
//
//import dev.langchain4j.model.moderation.Moderation;
//import dev.langchain4j.model.moderation.ModerationModel;
//import dev.langchain4j.model.openai.OpenAiModerationModel;
//import dev.langchain4j.model.output.Response;
//
///**
// * 温和模式示例
// * 温和模式能够校验输入中是否存在敏感内容
// */
//public class ModerationModeldemo {
//    public static void main(String[] args) {
//
//        System.out.println("======================温和模式输出示例======================");
//
//        ModerationModel model = OpenAiModerationModel.builder()
//                .apiKey("demo")
//                .build();
//
//        Response<Moderation> response = model.moderate("我要吃了你");
//        System.out.println(response.content().flaggedText()); // 模型回应为 null
//    }
//}
