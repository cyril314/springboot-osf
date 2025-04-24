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
public class OsfUsers extends BaseEntity<OsfUsers> {
    /** 用户名 (无默认值) */
    private String userName;

    /** 登录名，邮箱 (无默认值) */
    private String userEmail;

    /** 用户密码 (无默认值) */
    private String userPwd;

    /** 注册时间  (默认值为: CURRENT_TIMESTAMP) */
    private Date userRegisteredDate;

    /** 用户状态 (无默认值) */
    private Integer userStatus;

    /** 关键字 (无默认值) */
    private String userActivationkey;

    /** 用户头像 (无默认值) */
    private String userAvatar;

    /** 排序 (无默认值) */
    private String userDesc;

    /** 保留的 (无默认值) */
    private String resetpwdKey;
}