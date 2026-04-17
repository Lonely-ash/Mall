package com.lonelyash.item.domain.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class ItemDetailDTO {

    // 商品信息
    private Long id;            // 商品ID
    private String name;        // 商品名称
    private Integer price;      // 价格（单位：分）



    // 评论信息
    private List<CommentDetail> comments;

    // 评论详情
    @Setter
    @Getter
    public static class CommentDetail {

        private String content;      // 评论内容

        private Long userId;         // 用户ID
        private String username;     // 用户名
        private Date createTime;     // 评论时间
        private Integer likes;       // 点赞数

    }

}