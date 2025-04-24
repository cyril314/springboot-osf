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
public class OsfRelations extends BaseEntity<OsfRelations> {
    /**  (无默认值) */
    private Integer objectType;

    /**  (无默认值) */
    private Integer objectId;

    /** 标签ID (无默认值) */
    private Integer tagId;

    /** 添加时间  (默认值为: CURRENT_TIMESTAMP) */
    private Date addTs;
}