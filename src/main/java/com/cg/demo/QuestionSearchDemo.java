//package com.cg.embeddingdemo;
//
//import com.cg.tools.ModelUtil;
//import dev.ai4j.openai4j.embedding.EmbeddingModel;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.model.output.Response;
//import dev.langchain4j.store.embedding.EmbeddingMatch;
//import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
//
//import java.util.List;
//
//public class QuestionSearchDemo {
//
//    public static void main(String[] args) {
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
//        // 预设几个指示，生成向量
//        TextSegment textSegment1 = TextSegment.textSegment("客服电话是123456");
//        TextSegment textSegment2 = TextSegment.textSegment("客服工作时间是24小时");
//        TextSegment textSegment3 = TextSegment.textSegment("客服投诉电话是123456");
//        Response<Embedding> embed1 = embeddingModel.embed(textSegment1);
//        Response<Embedding> embed2 = embeddingModel.embed(textSegment2);
//        Response<Embedding> embed3 = embeddingModel.embed(textSegment3);
//        // 存储向量
//        embeddingStore.add(embed1.content(), textSegment1);
//        embeddingStore.add(embed2.content(), textSegment2);
//        embeddingStore.add(embed3.content(), textSegment3);
//
//        // 预设一个问题，生成向量
//        Response<Embedding> embed = embeddingModel.embed("客服电话是多少");
//        // 查询
//        List<EmbeddingMatch<TextSegment>> result = embeddingStore.findRelevant(embed.content(),3,-1);
//        for (EmbeddingMatch<TextSegment> embeddingMatch : result) {
//            System.out.println(embeddingMatch.embedded().text() + ",分数为：" + embeddingMatch.score());
//        }
//    }
//}
