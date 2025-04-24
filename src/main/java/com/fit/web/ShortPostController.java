package com.fit.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.*;
import com.fit.redis.FeedService;
import com.fit.service.OsfEventsService;
import com.fit.service.OsfPostsService;
import com.fit.service.OsfUsersService;
import com.fit.util.Dic;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/spost")
public class ShortPostController extends BaseController {

    @Autowired
    private OsfPostsService postService;

    @Autowired
    private OsfEventsService eventService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private OsfUsersService userService;

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> createPost(@RequestParam("content") String content, HttpSession session) {
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        Map<String, Object> map = new HashMap<String, Object>();
        OsfPosts posts = new OsfPosts();
        posts.setPostAuthor(user.getId());
        posts.setPostContent(content);
        this.postService.save(posts);
        map.put("spost", posts);
        map.put("status", Property.SUCCESS_POST_CREATE);
        int event_id = eventService.save(toEvent(Dic.OBJECT_TYPE_SHORTPOST, posts));
        feedService.push(user.getId(), event_id);

        map.put("avatar", user.getUserAvatar());
        map.put("author_name", user.getUserName());

        return map;
    }
}
