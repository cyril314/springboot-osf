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
public class OsfFollowings extends BaseEntity<OsfFollowings> {
    /**  (无默认值) */
    private Integer userId;

    /**  (无默认值) */
    private String userName;

    /**  (无默认值) */
    private Integer followingUserId;

    /**  (无默认值) */
    private String followingUserName;

    /**   (默认值为: CURRENT_TIMESTAMP) */
    private Date ts;
}