package com.fit.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.OsfLikes;
import com.fit.entity.OsfNotifications;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfLikesService;
import com.fit.service.OsfNotificationsService;
import com.fit.util.BeanUtils;
import com.fit.util.Dic;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/like")
public class LikeController extends BaseController {

    @Autowired
    private OsfLikesService likesService;

    @Autowired
    private OsfNotificationsService notificationsService;

    @ResponseBody
    @RequestMapping("/do")
    public Map<String, String> like(@RequestParam("author") int author, HttpServletRequest request, HttpSession session) {
        OsfUsers me = (OsfUsers) session.getAttribute("user");
        Map<String, Object> params = getRequestParamsMap(request);
        OsfLikes osfLikes = BeanUtils.map2Bean(OsfLikes.class, params);
        osfLikes.setUserId(author);
        likesService.save(osfLikes);
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("status", Property.SUCCESS_LIKE);
        OsfNotifications osfNotifications = BeanUtils.map2Bean(OsfNotifications.class, params);
        osfNotifications.setNotifyType(Dic.NOTIFY_TYPE_LIKE);
        osfNotifications.setNotifyId(0);
        osfNotifications.setNotifiedUser(author);
        osfNotifications.setNotifier(me.getId());
        notificationsService.save(osfNotifications);
        return ret;
    }

    @ResponseBody
    @RequestMapping("/undo")
    public Map<String, String> undolike(HttpServletRequest request, HttpSession session) {
        OsfUsers me = (OsfUsers) session.getAttribute("user");
        Map<String, Object> params = getRequestParamsMap(request);
        OsfLikes osfLikes = BeanUtils.map2Bean(OsfLikes.class, params);
        osfLikes.setUserId(me.getId());
        likesService.delete(osfLikes);
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("status", Property.SUCCESS_LIKE_UNDO);
        return ret;
    }
}