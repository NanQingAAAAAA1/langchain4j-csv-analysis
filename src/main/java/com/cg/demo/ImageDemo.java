//package com.cg.base;
//
//import com.cg.tools.ModelUtil;
//import dev.langchain4j.data.image.Image;
//import dev.langchain4j.model.openai.OpenAiImageModel;
//import dev.langchain4j.model.output.Response;
//
///**
// * 文生图示例
// */
//public class ImageDemo {
//
//    public static void main(String[] args) {
//        OpenAiImageModel model = OpenAiImageModel.builder()
//                .baseUrl(ModelUtil.BASE_URL_OPENAI)
//                .apiKey(ModelUtil.API_KEY_OPENAI)
//                .build();
//
//        Response<Image> response = model.generate("一碗大米饭"); // 给模型的图片描述
//
//        System.out.println( response.content().url()); // 查看生成图片的url
//    }
//}
