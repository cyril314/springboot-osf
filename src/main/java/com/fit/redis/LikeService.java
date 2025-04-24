package com.fit.redis;

import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @AUTO
 * @Author AIM
 * @DATE 2025/4/23
 */
@Service
public class LikeService {

    private static final String LIKE_KEY = "like:";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "redisTemplate")
    private SetOperations<String, Integer> setOps;

    public boolean isLike(int user_id, int object_type, int object_id) {
        cacheLikers(object_type, object_id);
        return setOps.isMember(LIKE_KEY + Dic.checkType(object_type) + ":" + object_id, user_id);
    }

    public long likersCount(int object_type, int object_id) {
        cacheLikers(object_type, object_id);
        return setOps.size(LIKE_KEY + Dic.checkType(object_type) + ":" + object_id);
    }

    private void cacheLikers(int object_type, int object_id) {
        getLikers(object_type, object_id);
    }

    public List<Integer> getLikers(int object_type, int object_id) {
        //key e.g like:post:1
        final String key = LIKE_KEY + Dic.checkType(object_type) + ":" + object_id;
        List<Integer> likers = null;
        if (!redisTemplate.hasKey(key)) {
            final String sql = "select user_id from osf_likes where object_type=? and object_id=?";
            likers = jdbcTemplate.query(sql, new Object[]{object_type, object_id}, new RowMapper<Integer>() {
                public Integer mapRow(ResultSet rs, int row) throws SQLException {
                    setOps.add(key, rs.getInt("user_id"));
                    return rs.getInt("user_id");
                }
            });
        }
        return likers;
    }
}
