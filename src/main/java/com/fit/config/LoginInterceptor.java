package com.fit.config;

import com.fit.entity.OsfUsers;
import com.fit.util.Dic;
import com.fit.util.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @AUTO
 * @Author AIM
 * @DATE 2025/4/24
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String NOTIFY_KEY = "notification:";
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Integer> hashOps;

    public boolean preHandle(HttpServletRequest req, HttpServletResponse rpo, Object arg2) throws Exception {
        HttpSession session = req.getSession();
        String requestURI = req.getRequestURI();
        if (requestURI.contains("index") || requestURI.contains("login") || requestURI.contains("static") || requestURI.equals(req.getContextPath() + "/")) {
            return true;
        }
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (user == null) {
            String redirectUrl = URLEncoder.encode(requestURI, "UTF-8");
            // 跳转到登录页，携带消息和原始访问地址
            rpo.sendRedirect(String.format("%s/account/login?bounce=%s", req.getContextPath(), redirectUrl));
        } else {
            final HashMap<String, Integer> notifications = new HashMap<String, Integer>();
            String user_id = user.getId().toString();
            try {
                if (!redisTemplate.hasKey(NOTIFY_KEY + user_id)) {
                    notifications.put("comment", 0);
                    notifications.put("comment_reply", 0);
                    notifications.put("follow", 0);
                    notifications.put("like", 0);
                    notifications.put("system", 0);
                    String sql = "select notified_user,notify_type,count(*) count from `osf_notifications` where notified_user=? group by notified_user,notify_type";
                    jdbcTemplate.query(sql, new Object[]{user_id}, new ResultSetExtractor<Integer>() {
                        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                            while (rs.next()) {
                                notifications.put(Dic.toNotifyTypeDesc(rs.getInt("notify_type")), rs.getInt("count"));
                            }
                            return null;
                        }
                    });
                    hashOps.putAll(NOTIFY_KEY + user_id, notifications);
                } else {
                    for (String key : hashOps.keys(NOTIFY_KEY + user_id)) {
                        notifications.put(key, hashOps.get(NOTIFY_KEY + user_id, key));
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            session.setAttribute("notifications", notifications);
            return true;
        }
        return false;
    }
}
