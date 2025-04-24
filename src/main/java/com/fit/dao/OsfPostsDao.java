package com.fit.dao;

import com.fit.base.BaseCrudDao;
import com.fit.entity.OsfPosts;
import org.apache.ibatis.annotations.Mapper;

/**
 * @AUTO 接口
 * @Author AIM
 * @DATE 2025-04-23 14:14:41
 */
@Mapper
public interface OsfPostsDao extends BaseCrudDao<OsfPosts> {
}