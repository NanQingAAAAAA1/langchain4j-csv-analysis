//package com.cg.ragdemo;
//
//import com.cg.tools.ModelUtil;
//import dev.ai4j.openai4j.embedding.EmbeddingModel;
//import dev.langchain4j.data.document.Document;
//import dev.langchain4j.data.document.DocumentParser;
//import dev.langchain4j.data.document.DocumentSplitter;
//import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
//import dev.langchain4j.data.document.parser.TextDocumentParser;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
//import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//public class MeituanRagLoader {
//
//    public static void main(String[] args) throws URISyntaxException {
//        // 1.读取本地知识库文件
//        Path documentPath = Paths.get(MeituanRagLoader.class.getClassLoader().getResource("meituan-question.txt").toURI());
//        DocumentParser documentParser = new TextDocumentParser();
//        Document document = FileSystemDocumentLoader.loadDocument(documentPath);
//
//        // 2.把知识文件分解为一个一个的知识条目
//        DocumentSplitter splitter = new MyDocumentSplitter();
//        List<TextSegment> segments = splitter.split(document);
//
//        // 3.对每个条目进行文本向量化，并且保存到Redis中
//        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .build();
//
//        RedisEmbeddingStore embeddingStore = RedisEmbeddingStore.builder()
//                .host("192.168.124.129")
//                .port(6380) // 这里的端口需要注意是 RedisSearch 的 而不是 redis 的
//                .dimension(1536)
//                .indexName("meituan-rag")
//                .build();
//
//        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
//
//        embeddingStore.addAll(embeddings,segments);
//    }
//}
