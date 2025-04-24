package com.fit.entity;

import com.fit.base.BaseEntity;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @AUTO 
 * @Author AIM
 * @DATE 2025-04-23 14:14:41
 */
@Data
@Builder
@NoArgsConstructor //无参数的构造方法
@AllArgsConstructor //包含所有变量构造方法
public class OsfPosts extends BaseEntity<OsfPosts> {
    /** 作者ID (无默认值) */
    private Integer postAuthor;

    /** 添加时间  (默认值为: CURRENT_TIMESTAMP) */
    private Date postTs;

    /**  (无默认值) */
    private String postContent;

    /**  (无默认值) */
    private String postTitle;

    /** 摘要 (无默认值) */
    private String postExcerpt;

    /**   (默认值为: 0) */
    private Integer postStatus;

    /**   (默认值为: 0) */
    private Integer commentStatus;

    /**  (无默认值) */
    private String postPwd;

    /**   (默认值为: 0000-00-00 00:00:00) */
    private Date postLasts;

    /**   (默认值为: 0) */
    private Integer commentCount;

    /**   (默认值为: 0) */
    private Integer likeCount;

    /**   (默认值为: 0) */
    private Integer shareCount;

    /**  (无默认值) */
    private String postUrl;

    /**  (无默认值) */
    private String postTags;

    /**   (默认值为: 0) */
    private Integer postAlbum;

    /**  (无默认值) */
    private String postCover;
}