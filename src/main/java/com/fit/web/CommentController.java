package com.fit.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.OsfComments;
import com.fit.entity.OsfNotifications;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfCommentsService;
import com.fit.service.OsfNotificationsService;
import com.fit.service.OsfUsersService;
import com.fit.util.BeanUtils;
import com.fit.util.Dic;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/comment")
public class CommentController extends BaseController {

    @Autowired
    private OsfCommentsService commentService;
    @Autowired
    private OsfUsersService userService;
    @Autowired
    private OsfNotificationsService notificationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ResponseBody
    @RequestMapping("/{id}")
    public Map<String, Object> comment(@PathVariable("id") int id) {
        OsfComments comment = commentService.get(id);
        Map<String, Object> ret = new HashMap<String, Object>();
        if (comment == null) {
            ret.put("status", Property.ERROR);
        } else {
            ret.put("status", Property.SUCCESS);
            ret.put("comment", comment);
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> createComment(HttpServletRequest request, HttpSession session) {
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        Map<String, Object> map = getRequestParamsMap(request);
        Object commentParent = map.get("comment_parent");
        Map<String, Object> ret = new HashMap<String, Object>();
        OsfNotifications notification = new OsfNotifications();
        notification.setNotifyType(Dic.NOTIFY_TYPE_COMMENT);
        notification.setNotifyId(0);
        notification.setObjectType(Integer.valueOf(map.get("comment_object_type").toString()));
        notification.setObjectId(Integer.valueOf(map.get("comment_object_id").toString()));
        notification.setNotifiedUser(this.getAuthor(Integer.valueOf(map.get("comment_object_type").toString()), Integer.valueOf(map.get("comment_object_id").toString())).getId());
        notification.setNotifier(user.getId());
        if (commentParent != null && Integer.valueOf(commentParent.toString()) != 0) {
            String comment_content = map.get("comment_content").toString();
            if (comment_content == null || comment_content.length() == 0 || Dic.checkType(Integer.valueOf(map.get("comment_object_type").toString())) == null) {
                ret.put("status", Property.ERROR_COMMENT_EMPTY);
            } else {
                OsfUsers comment_parent_author = userService.get(Integer.valueOf(commentParent.toString()));
                ret.put("reply_to_author", comment_parent_author.getId());
                ret.put("reply_to_authorname", comment_parent_author.getUserName());
                OsfComments osfComments = BeanUtils.map2Bean(OsfComments.class, map);
                int save = commentService.save(osfComments);
                ret.put("status", Property.SUCCESS_COMMENT_CREATE);
                ret.put("id", save);
                notification.setNotifyId(save);
                notification.setNotifyType(Dic.NOTIFY_TYPE_COMMENT_REPLY);
                notification.setNotifiedUser(comment_parent_author.getId());
            }
        }
        notificationService.save(notification);

        ret.put("avatar", userService.get(user.getId()).getUserAvatar());
        ret.put("author_id", String.valueOf(user.getId()));
        ret.put("author_name", user.getUserName());

        return ret;
    }

    @RequestMapping(value = "/{type}/{id}")
    public ModelAndView getComments(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("comment/index");
        Map<String, Object> map = getRequestParamsMap(request);
        mav.addObject("comments", commentService.findList(map));
        return mav;
    }

    /**
     * feed附属的comments
     */
    @RequestMapping(value = "/attach/{type}/{id}")
    public ModelAndView getAttachComments(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("comment/attach_comments");
        Map<String, Object> map = getRequestParamsMap(request);
        map.put("offset", 0);
        map.put("limit", 5);
        mav.addObject("comments", commentService.findList(map));
        return mav;
    }

    public OsfUsers getAuthor(int object_type, int object_id) {
        OsfUsers users = new OsfUsers();
        boolean goSql = true;
        StringBuffer sb = new StringBuffer("SELECT u.* FROM `osf_users` u WHERE u.`id`=(");
        if (object_type == Dic.OBJECT_TYPE_POST || object_type == Dic.OBJECT_TYPE_SHORTPOST) {
            sb.append("SELECT a.`post_author` FROM `osf_posts` p WHERE p.`id`=?");
        } else if (object_type == Dic.OBJECT_TYPE_ALBUM) {
            sb.append("select a.`user_id` from `osf_albums` a where a.`id`=?");
        } else if (object_type == Dic.OBJECT_TYPE_PHOTO) {
            sb.append("select t2.`user_id` from `osf_photos` t1,`osf_albums` t2 where t1.id=? and t1.album_id=t2.id");
        } else {
            goSql = false;
        }
        sb.append(")");
        if (goSql) {
            users = jdbcTemplate.queryForObject(sb.toString(), new BeanPropertyRowMapper<>(OsfUsers.class), object_id);
        }
        return users;
    }
}