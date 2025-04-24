package com.fit.web;

import javax.servlet.http.HttpSession;

import com.fit.entity.OsfNotifications;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfNotificationsService;
import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotifyController {

    @Autowired
    private OsfNotificationsService notificationService;

    @RequestMapping("/comment")
    public ModelAndView comment(HttpSession session) {
        ModelAndView mav = new ModelAndView("notifications/comment");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("dic", new Dic());
        List<OsfNotifications> notifications = getNotifications(user.getId(), Dic.NOTIFY_TYPE_COMMENT);
        notifications.addAll(getNotifications(user.getId(), Dic.NOTIFY_TYPE_COMMENT_REPLY));
        mav.addObject("notis", notifications);
        return mav;
    }

    @RequestMapping("/like")
    public ModelAndView like(HttpSession session) {
        ModelAndView mav = new ModelAndView("notifications/like");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("dic", new Dic());
        mav.addObject("notis", getNotifications(user.getId(), Dic.NOTIFY_TYPE_LIKE));
        return mav;
    }

    @RequestMapping("/follow")
    public ModelAndView follow(HttpSession session) {
        ModelAndView mav = new ModelAndView("notifications/follow");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("dic", new Dic());
        mav.addObject("notis", getNotifications(user.getId(), Dic.NOTIFY_TYPE_FOLLOW));
        return mav;
    }

    @RequestMapping("/system")
    public ModelAndView system(HttpSession session) {
        ModelAndView mav = new ModelAndView("notifications/system");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("dic", new Dic());
        mav.addObject("notis", getNotifications(user.getId(), Dic.NOTIFY_TYPE_SYSTEM));
        return mav;
    }

    private List<OsfNotifications> getNotifications(Integer id, Integer type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("types", type);
        return notificationService.findList(params);
    }
}
