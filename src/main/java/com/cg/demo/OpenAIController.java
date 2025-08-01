//package com.cg.springboot;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import jakarta.annotation.Resource;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * langchain4j 整合 springboot 示例
// */
//@RestController
//public class OpenAIController {
//
//    @Resource
//    private ChatLanguageModel chatLanguageModel;
//
//    @GetMapping("/openai/hello")
//    public String hello() {
//        return chatLanguageModel.generate("Hello");
//    }
//}
