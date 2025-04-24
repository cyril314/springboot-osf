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
 * @DATE 2025-04-23 14:14:40
 */
@Data
@Builder
@NoArgsConstructor //无参数的构造方法
@AllArgsConstructor //包含所有变量构造方法
public class OsfEvents extends BaseEntity<OsfEvents> {
    /**  (无默认值) */
    private Integer objectType;

    /**  (无默认值) */
    private Integer objectId;

    /**   (默认值为: CURRENT_TIMESTAMP) */
    private Date ts;

    /**  (无默认值) */
    private Integer userId;

    /**  (无默认值) */
    private String userName;

    /**  (无默认值) */
    private String userAvatar;

    /**  (无默认值) */
    private Integer likeCount;

    /**  (无默认值) */
    private Integer shareCount;

    /**  (无默认值) */
    private Integer commentCount;

    /**  (无默认值) */
    private String title;

    /**  (无默认值) */
    private String summary;

    /**  (无默认值) */
    private String content;

    /**  (无默认值) */
    private String tags;

    /**  (无默认值) */
    private Integer followingUserId;

    /**  (无默认值) */
    private String followingUserName;

    /**  (无默认值) */
    private Integer followerUserId;

    /**  (无默认值) */
    private String followerUserName;
}