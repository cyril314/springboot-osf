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
public class OsfComments extends BaseEntity<OsfComments> {
    /** post, album,... (无默认值) */
    private Integer commentObjectType;

    /**  (无默认值) */
    private Integer commentObjectId;

    /**  (无默认值) */
    private Integer commentAuthor;

    /**  (无默认值) */
    private String commentAuthorName;

    /**   (默认值为: CURRENT_TIMESTAMP) */
    private Date commentTs;

    /**  (无默认值) */
    private String commentContent;

    /**   (默认值为: 0) */
    private Integer commentParent;

    /**  (无默认值) */
    private String commentParentAuthorName;

    /**   (默认值为: 0) */
    private Integer commentParentAuthor;
}