package com.mall.item.domain.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class ItemDetailDTO {
    // 主DTO的Getters/Setters
    // 商品信息
    private Long id;            // 商品ID
    private String name;        // 商品名称
    private Integer price;      // 价格（单位：分）



    // 评论信息（列表）
    private List<CommentDetail> comments;

    // 内部类：评论详情（包含用户ID）
    @Setter
    @Getter
    public static class CommentDetail {
        // Getters and Setters
        private String content;      // 评论内容
        // 核心：返回用户ID
        private Long userId;         // 用户ID (核心需求字段)
        private String username;     // 用户名
        private Date createTime;     // 评论时间
        private Integer likes;       // 点赞数

    }

}