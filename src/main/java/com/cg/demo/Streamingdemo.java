//package com.cg.base;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.model.StreamingResponseHandler;
//import dev.langchain4j.model.chat.StreamingChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 流式输出示例
// */
//public class Streamingdemo {
//
//    public static void main(String[] args) {
//
//        // 定义流式语言交互模型
//        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
////                .apiKey("demo")
//                .modelName("gpt-4o-mini")
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .build();
//
//        System.out.println("======================流式输出示例======================");
//
//        model.generate("你好", new StreamingResponseHandler<AiMessage>() {
//            @Override
//            public void onNext(String token) { // 流式方式输出（以token为单位）
//                System.out.println(token);
//                try {
//                    TimeUnit.SECONDS.sleep(1); // 休眠 更好看到流式结果
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public void onError(Throwable error) { // 请求出错打印错误信息
//                System.out.println(error);
//            }
//        });
//    }
//
//}
