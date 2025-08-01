//package com.cg.ragdemo;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.model.output.Response;
//import dev.langchain4j.rag.content.Content;
//import dev.langchain4j.rag.content.injector.ContentInjector;
//import dev.langchain4j.rag.content.injector.DefaultContentInjector;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.rag.query.Query;
//import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
//
//import java.util.List;
//
//public class MeituanRagDemo {
//
//    public static void main(String[] args) {
//
//        String question = "在线支付取消订单后怎么返还？"; // 用户提出的问题
//
//        // 1.将问题向量化，并去Redis中查询相似度比较高的条目
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
//
//        Query query = Query.from(question);
//        List<Content> contentList = contentRetriever.retrieve(query);
//
////        for (Content content : contentList) {
////            System.out.println(content);
////        }
//
//        // 2.将问题和相关知识条目整合成一个完整的信息，发给大模型
//        ContentInjector contentInjector = new DefaultContentInjector();
//        UserMessage promptMessage = contentInjector.inject(contentList, UserMessage.from(question));
//        System.out.println(promptMessage.singleText());
//
//        System.out.println("=========================>");
//        ChatLanguageModel model = ModelUtil.getOpenAIModel();
//        Response<AiMessage> response = model.generate(promptMessage);
//        System.out.println(response.content().text());
//    }
//}
