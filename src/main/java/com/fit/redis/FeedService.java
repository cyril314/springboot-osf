package com.fit.redis;

import com.fit.entity.OsfEvents;
import com.fit.entity.OsfRelations;
import com.fit.entity.OsfTags;
import com.fit.service.OsfEventsService;
import com.fit.util.Dic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
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
@Slf4j
@Service
public class FeedService {

    public static final int FEED_COUNT_PER_PAGE = 10;
    public static final int FEED_COUNT = 200;    //feed缓存量

    @Autowired
    private FollowerService followerService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParaJdbcTemplate;
    @Resource(name = "redisTemplate")
    private ListOperations<String, Integer> listOps;

    public void push(int user_id, int event_id) {
        List<Integer> followers = followerService.getFollowerIDs(user_id);
        followers.add(user_id);    //add self
        if (followers != null && followers.size() != 0) {
            for (Integer follower : followers) {
                this.save("feed:user:" + follower, event_id);
            }
        }
    }

    /**
     * 缓存feed到对应标签列表序列中
     *
     * @param tag_id
     * @param event_id
     */
    public void cacheFeed2Tag(int tag_id, int event_id) {
        this.save("feed:tag:" + tag_id, event_id);
    }

    private List<Integer> getEventIDs(int user_id, int start, int count) {
        return this.fetch("feed:user:" + user_id, start, count);
    }

    public List<OsfEvents> getFeeds(int user_id) {
        return getFeeds(user_id, FEED_COUNT_PER_PAGE);
    }

    public List<OsfEvents> getFeeds(int user_id, int count) {
        List<Integer> event_ids = getEventIDs(user_id, 0, count - 1);
        return decorateFeeds(user_id, event_ids);
    }

    public List<OsfEvents> getFeedsOfPage(int user_id, int num) {
        List<Integer> event_ids = this.fetch("feed:user:" + user_id, FEED_COUNT_PER_PAGE * (num - 1), FEED_COUNT_PER_PAGE - 1);
        return decorateFeeds(user_id, event_ids);
    }

    private List<OsfEvents> decorateFeeds(int user_id, List<Integer> event_ids) {
        List<OsfEvents> events = new ArrayList<OsfEvents>();
        if (event_ids != null && event_ids.size() != 0) {
            String sql = "select * from osf_events where id in (:ids) order by ts desc";
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("ids", event_ids);
            return namedParaJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(OsfEvents.class));
        }
        return events;
    }

    public void codeStart(int user_id) {
        if (this.count("feed:user:" + user_id) != 0) {
            return;
        }
        StringBuffer sb = new StringBuffer("SELECT e.* FROM `osf_events` e WHERE e.`object_id` IN (");
        sb.append("SELECT r.`object_id` FROM osf_relations r WHERE r.tag_id IN (");
        sb.append("SELECT t2.id FROM osf_interests t1, osf_tags t2 WHERE t1.user_id=? AND t1.tag_id=t2.id)");
        sb.append(");");
        List<OsfEvents> events = this.jdbcTemplate.query(sb.toString(), new BeanPropertyRowMapper<>(OsfEvents.class), user_id);
        List<Integer> events_id = new ArrayList<Integer>();
        for (OsfEvents event : events) {
            events_id.add(event.getId());
        }
        this.saveAll("feed:user:" + user_id, events_id);
    }

    public Map<Integer, Boolean> hasInterestInTags(int user_id, List<Integer> tags_id) {
        final Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
        for (Integer id : tags_id) {
            result.put(id, false);
        }
        String sql = "select tag_id from  where user_id=:user_id and tag_id in (:tags_id)";
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", user_id);
        paramMap.put("tags_id", tags_id);
        namedParaJdbcTemplate.query(sql, paramMap, new ResultSetExtractor<Boolean>() {
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    result.put(rs.getInt("tag_id"), true);
                }
                return true;
            }
        });
        return result;
    }

    public void cacheFeeds2Tag(int tag_id, List<Integer> events_id) {
        this.saveAll("feed:tag:" + tag_id, events_id);
    }

    public void save(String key, int event_id) {
        listOps.leftPush(key, event_id);
    }

    public void delete(String key, int event_id) {
        listOps.remove(key, 0, event_id);

    }

    public Long count(String key) {
        return listOps.size(key);
    }

    public List<Integer> fetch(String key) {
        return listOps.range(key, 0, listOps.size(key) - 1);
    }

    public List<Integer> fetch(String key, long start, long step) {
        return listOps.range(key, start, start + step);
    }

    public void saveAll(String key, List<Integer> events_id) {
        Iterator<Integer> events_it = events_id.iterator();
        int count = 0;
        while (events_it.hasNext() && count < FeedService.FEED_COUNT) {
            save(key, events_it.next());
            count++;
        }
    }

    /**
     * @param tags
     * @param event_id
     */
    public void post2CacheFeed2Tag(String[] tags, int event_id) {
        for (String tag_id : tags) {
            try {
                String sql = "select * from osf_interests where tag_id=?";
                List<Integer> users = jdbcTemplate.query(sql, new Object[]{tag_id}, new RowMapper<Integer>() {
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("user_id");
                    }
                });
                for (int u : users) {
                    this.push(u, event_id);
                }
                //5 cache feeds to tag list
                this.cacheFeed2Tag(Integer.valueOf(tag_id), event_id);
            } catch (Exception e) {
                log.error("post2CacheFeed2Tag push redis fail: tag_id={}, event_id={}; error_msg={}", tag_id, event_id, e.getMessage());
            }
        }
    }

    public List<OsfEvents> getRecommendFeeds(int start, int step) {
        String sql = "select * from `osf_events` where (object_type=? and content is not null) or (object_type=? and title is not null) limit ?,?";
        return jdbcTemplate.query(sql, new Object[]{Dic.OBJECT_TYPE_POST, Dic.OBJECT_TYPE_ALBUM, start, step}, new BeanPropertyRowMapper<>(OsfEvents.class));
    }

    public List<OsfTags> getRecommendTags() {
        String sql = "select * from `osf_tags` where `cover` is not null limit 12";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OsfTags.class));
    }
}
