//package com.cg.ragdemo;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.agent.tool.Tool;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.rag.DefaultRetrievalAugmentor;
//import dev.langchain4j.rag.content.injector.ContentInjector;
//import dev.langchain4j.rag.content.injector.DefaultContentInjector;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
//
//
//import java.time.LocalDateTime;
//
//public class MeituanRagService {
//
//    interface AiCustomer {
//        String answer(String question);
//    }
//
//    public static AiCustomer create() {
//
//        ChatLanguageModel chatLanguageModel = ModelUtil.getOpenAIModel();
//
//        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .build();
//
//        RedisEmbeddingStore embeddingStore = RedisEmbeddingStore.builder()
//                .host("192.168.124.129")
//                .port(6380) // 这里的端口需要注意是 RedisSearch 的 而不是 redis 的
//                .dimension(1536)
//                .indexName("question")
//                .build();
//
//        ContentRetriever contentRetriever = (ContentRetriever) EmbeddingStoreContentRetriever.builder()
//                .embeddingModel(embeddingModel)
//                .embeddingStore(embeddingStore)
//                .maxResults(5)
//                .minScore(0.8)
//                .build();
//
//        ContentInjector contentInjector = new DefaultContentInjector();
//
//        DefaultRetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
//                .contentRetriever(contentRetriever)
//                .contentInjector(contentInjector)
//                .build();
//
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//
//        return AiServices.builder(AiCustomer.class)
//                .chatLanguageModel(chatLanguageModel)
//                .retrievalAugmentor(retrievalAugmentor)
//                .tools(new DataCaulator())
//                .chatMemory(chatMemory)
//                .build();
//    }
//
//    // 工具类
//    static class DataCaulator {
//            @Tool
//            String date(Integer days) {
//                return LocalDateTime.now().plusDays(days).toString();
//            }
//    }
//
//    public static void main(String[] args) {
//        // 获取代理的服务对象
//        AiCustomer aiCustomer = MeituanRagService.create();
//        String result = aiCustomer.answer("今天的余额提取最快哪天能到账？给我具体日期");
//        System.out.println(result);
//    }
//}
