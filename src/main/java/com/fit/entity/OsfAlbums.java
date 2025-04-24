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
public class OsfAlbums extends BaseEntity<OsfAlbums> {
    /** 用户ID (无默认值) */
    private Integer userId;

    /**   (默认值为: CURRENT_TIMESTAMP) */
    private Date createTs;

    /**  (无默认值) */
    private String albumTitle;

    /** 描述 (无默认值) */
    private String albumDesc;

    /**   (默认值为: 0000-00-00 00:00:00) */
    private Date lastAddTs;

    /**   (默认值为: 0) */
    private Integer photosCount;

    /** 状态  (默认值为: 0) */
    private Integer status;

    /**  (无默认值) */
    private String cover;

    /**  (无默认值) */
    private String albumTags;
}