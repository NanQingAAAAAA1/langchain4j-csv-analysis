//package com.cg.embeddingdemo;
//
//import com.cg.tools.ModelUtil;
//import dev.ai4j.openai4j.embedding.EmbeddingModel;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.model.output.Response;
//import dev.langchain4j.store.embedding.EmbeddingMatch;
//import dev.langchain4j.store.embedding.EmbeddingStore;
//import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
//
//import java.util.List;
//
//public class VectorDemo {
//    public static void main(String[] args) {
//
//        // 不同的大模型有不同的向量化
//        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .build();
//
////        Response<Embedding> embed = embeddingModel.embed("你好，我是南清");
////        System.out.println(embed.content().toString());
////        System.out.println(embed.content().vector().length);
//
//// "我的名字叫楼兰"
//        RedisEmbeddingStore embeddingStore = RedisEmbeddingStore.builder()
//                .host("192.168.124.129")
//                .port(6380) // 这里的端口需要注意是 RedisSearch 的 而不是 redis 的
//                .dimension(1536)
//                .build();
////        embeddingStore.add(embed.content());
//
//        List<EmbeddingMatch<TextSegment>> maches = embeddingStore.findRelevant(embeddingModel.embed("我的名字叫南清").content(),3,-1);
//        for (EmbeddingMatch<TextSegment> match : maches) {
//            System.out.println(match.score());
//        }
//    }
//}
