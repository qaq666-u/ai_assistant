package com.liys.ai_assistant.common;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 向量数据库的配置类
@Configuration
public class VectorStoreConfig {

    // 那个模型  Embedding  ChatGPT    5个
   // 基于内存的 向量模型数据库  已关机 数据就没了
  //  如果上线了  在这里用redis替换
    //  sqlServer mysql 关系型数据库  比较慢
    //  做持久化 内存  下周有时间  Redis  非关系型数据库   向量数据库  速度快
    @Bean   // 放到容器当中  springboot 容器
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 构件一下
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        return store;

    }
}
