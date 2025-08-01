//package com.cg.aiservicedemo;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.service.SystemMessage;
//import dev.langchain4j.service.UserMessage;
//import dev.langchain4j.service.V;
//
//import java.io.Writer;
//
//public class AiWriter {
//
//    interface Writer {
//        @SystemMessage("你是一个专业的吃饭人，根据输入的题目写一篇文章")
//        String write(String content);
//    }
//
////    interface Writer2 {
////        @SystemMessage("你是一个专业的做饭人,题目是{{title}}，字数不超过{{count}}个字")
////        String write(@UserMessage String content, @V("title") String title, @V("count")Long count);
////    }
//
//    public static void main(String[] args) {
//
//        ChatLanguageModel model = ModelUtil.getOpenAIModel();
//
//        Writer writer = AiServices.create(Writer.class, model);
//        String content = writer.write("写一篇关于我要吃饭的文章");
//        System.out.println(content);
//
////        Writer2 writer2 = AiServices.create(Writer2.class, model);
////        String content = writer2.write("写一篇关于我要做饭的文章", "做饭的艺术", 200L);
////        System.out.println(content);
//    }
//}
