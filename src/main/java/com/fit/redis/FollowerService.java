package com.fit.redis;

import com.fit.entity.OsfFollowers;
import com.fit.entity.OsfFollowings;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfFollowersService;
import com.fit.service.OsfFollowingsService;
import com.fit.util.OSFUtils;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @AUTO
 * @Author AIM
 * @DATE 2025/4/23
 */
@Service
public class FollowerService {

    @Autowired
    private NamedParameterJdbcTemplate namedParaJdbcTemplate;
    private static final String FOLLOWING_KEY = "following:user:";
    private static final String FOLLOWER_KEY = "follower:user:";

    @Autowired
    private OsfFollowersService osfFollowersService;
    @Autowired
    private OsfFollowingsService osfFollowingsService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "redisTemplate")
    private ListOperations<String, Integer> listOps;
    @Resource(name = "redisTemplate")
    private SetOperations<String, Integer> setOps;

    public long getFollowersCount(int user_id) {
        return setOps.size(FOLLOWER_KEY + user_id);
    }

    public long getFollowingsCount(int user_id) {
        return setOps.size(FOLLOWING_KEY + user_id);
    }

    public boolean isFollowing(int user_a, int user_b) {
        return setOps.isMember(FOLLOWING_KEY + user_a, user_b);
    }

    public Map<Integer, Boolean> isFollowing(int user_id, List<OsfUsers> users) {
        if (users == null || users.size() == 0) {
            return null;
        }

        List<Integer> users_id = new ArrayList<Integer>();
        for (OsfUsers user : users) {
            users_id.add(user.getId());
        }
        return isFollowingUsers(user_id, users_id);
    }

    private void initCheckResult(Map<Integer, Boolean> map, List<Integer> following_ids) {
        for (Integer id : following_ids) {
            map.put(id, false);
        }
    }

    public Map<Integer, Boolean> isFollowingUsers(int user_id, List<Integer> following_ids) {
        final Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
        initCheckResult(result, following_ids);

        String sql = "select following_user_id from `osf_followings` where user_id=:user_id and following_user_id in (:following_ids)";
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", user_id);
        paramMap.put("following_ids", following_ids);
        namedParaJdbcTemplate.query(sql, paramMap, new ResultSetExtractor<Boolean>() {
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    result.put(rs.getInt("following_user_id"), true);
                }
                return true;
            }
        });
        return result;
    }

    public boolean hasFollower(int user_a, int user_b) {
        return setOps.isMember(FOLLOWER_KEY + user_a, user_b);
    }

    public List<Integer> getFollowingIDs(int user_id) {
        // Cursor<Integer> cursor = setOps.scan(FOLLOWING_KEY+user_id,
        // ScanOptions.scanOptions().count(FOLLOW_SCAN_COUNT).build());
        // //List<Integer> following_ids = listOps.range(FOLLOWING_KEY+user_id,
        // 0, listOps.size(FOLLOWING_KEY+user_id)-1);
        //
        //
        // while(cursor.hasNext()) {
        // following_ids.add(cursor.next());
        // }
        Set<Integer> members = setOps.members(FOLLOWING_KEY + user_id);
        return OSFUtils.toList(members);

    }

    public List<Integer> getFollowerIDs(int user_id) {
        // Cursor<Integer> cursor = setOps.scan(FOLLOWER_KEY+user_id,
        // ScanOptions.scanOptions().count(FOLLOW_SCAN_COUNT).build());
        // List<Integer> follower_ids = new ArrayList<Integer>();
        // while(cursor.hasNext()) {
        // follower_ids.add(cursor.next());
        // }
        Set<Integer> members = setOps.members(FOLLOWER_KEY + user_id);
        return OSFUtils.toList(members);
    }

    public List<OsfUsers> getUsersByIDs(List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<OsfUsers>();
        }

        String sql = "select * from `osf_users` where id in (:ids)";
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("ids", ids);
        return namedParaJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(OsfUsers.class));
    }

    public Map<String, Object> newFollowing(int user_id, String user_name, int following_user_id, String following_user_name) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfFollowings following = new OsfFollowings();
        following.setUserId(user_id);
        following.setUserName(user_name);
        following.setFollowingUserId(following_user_id);
        following.setFollowingUserName(following_user_name);
        int id = osfFollowingsService.save(following);
        setOps.add(FOLLOWING_KEY + following.getUserId(), following.getFollowingUserId());
        if (id == 0) {
            map.put("status", Property.ERROR_FOLLOW);
            return map;
        }
        map.put("following", following);

        OsfFollowers follower = new OsfFollowers();
        follower.setUserId(following_user_id);
        follower.setUserName(following_user_name);
        follower.setFollowerUserId(user_id);
        follower.setFollowerUserName(user_name);
        osfFollowersService.save(follower);
        setOps.add(FOLLOWER_KEY + follower.getUserId(), follower.getFollowerUserId());
        if (id == 0) {
            map.put("status", Property.ERROR_FOLLOW);
            return map;
        }
        map.put("follower", follower);
        map.put("status", Property.SUCCESS_FOLLOW);
        return map;
    }

    public Map<String, Object> undoFollow(int user_id, int following_user_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfFollowings following = new OsfFollowings();
        following.setUserId(user_id);
        following.setFollowingUserId(following_user_id);
        if (osfFollowingsService.delete(following) == 0) {
            map.put("status", Property.ERROR_FOLLOW_UNDO);
            return map;
        } else {
            setOps.remove(FOLLOWING_KEY + following.getUserId(), following.getFollowingUserId());
        }
        map.put("following", following);

        OsfFollowers follower = new OsfFollowers();
        follower.setUserId(following_user_id);
        follower.setFollowerUserId(user_id);
        if (osfFollowersService.delete(follower) > 0) {
            setOps.remove(FOLLOWER_KEY + follower.getUserId(), follower.getFollowerUserId());
            map.put("status", Property.SUCCESS_FOLLOW_UNDO);
            map.put("follower", follower);
        } else {
            map.put("status", Property.ERROR_FOLLOW_UNDO);
        }
        return map;
    }
}