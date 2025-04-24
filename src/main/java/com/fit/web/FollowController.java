package com.fit.web;

import java.util.Map;

import javax.servlet.http.HttpSession;

import com.fit.entity.OsfNotifications;
import com.fit.entity.OsfUsers;
import com.fit.redis.FollowerService;
import com.fit.service.OsfNotificationsService;
import com.fit.service.OsfUsersService;
import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowerService followerService;
    @Autowired
    private OsfUsersService userService;

    @Autowired
    private OsfNotificationsService notificationService;

    @ResponseBody
    @RequestMapping("/{following_user_id}")
    public Map<String, Object> follow(@PathVariable("following_user_id") int following_user_id, HttpSession session) {
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        OsfUsers followingUser = userService.get(following_user_id);
        Map<String, Object> map = followerService.newFollowing(user.getId(), user.getUserName(), following_user_id, followingUser.getUserName());
        OsfNotifications notification = new OsfNotifications();
        notification.setNotifyType(Dic.NOTIFY_TYPE_FOLLOW);
        notification.setNotifyId(0);
        notification.setObjectType(Dic.OBJECT_TYPE_USER);
        notification.setObjectId(following_user_id);
        notification.setNotifiedUser(following_user_id);
        notification.setNotifier(user.getId());
        notificationService.save(notification);
        return map;
    }

    @ResponseBody
    @RequestMapping("/undo/{following_user_id}")
    public Map<String, Object> undoFollow(@PathVariable("following_user_id") int following_user_id, HttpSession session) {
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        Map<String, Object> map = followerService.undoFollow(user.getId(), following_user_id);
        return map;
    }
}