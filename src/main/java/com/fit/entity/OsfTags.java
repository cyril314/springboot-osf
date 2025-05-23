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
public class OsfTags extends BaseEntity<OsfTags> {
    /** 标签 (无默认值) */
    private String tag;

    /** 添加时间  (默认值为: CURRENT_TIMESTAMP) */
    private Date addTs;

    /** 封面 (无默认值) */
    private String cover;
}