//package com.cg.tools;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModelName;
//
//public class ModelUtil {
//
//    public static final String API_KEY_OPENAI = "sk-Qxt1e040220a75c18e3a2193f6b5cb0d8fb718b7ff3uxxI1";
//    public static final String BASE_URL_OPENAI = "https://api.gptsapi.net/v1";
//
//    public static ChatLanguageModel getOpenAIModel() {
//        return OpenAiChatModel.builder()
//                .apiKey(API_KEY_OPENAI)       // 使用预设API密钥
//                .baseUrl(BASE_URL_OPENAI)     // 配置自定义API端点
//                .modelName(OpenAiChatModelName.GPT_4_O) // 指定模型版本
//                .temperature(0.3)             // 控制生成随机性（0-1）
//                .maxRetries(3)                // 网络请求重试次数
//                .build();
//    }
//}
