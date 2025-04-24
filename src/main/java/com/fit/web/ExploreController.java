package com.fit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.fit.entity.OsfEvents;
import com.fit.entity.OsfTags;
import com.fit.entity.OsfUsers;
import com.fit.redis.FeedService;
import com.fit.redis.FollowerService;
import com.fit.service.OsfEventsService;
import com.fit.service.OsfUsersService;
import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/explore")
public class ExploreController {

    @Autowired
    private FeedService feedService;
    @Autowired
    private OsfEventsService eventService;
    @Autowired
    private OsfUsersService userService;
    @Autowired
    private FollowerService followService;

    @RequestMapping("")
    public ModelAndView explore(HttpSession session) {
        ModelAndView mav = new ModelAndView("explore");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("events", feedService.getRecommendFeeds(0, 20));
        List<OsfTags> tags_recommend = feedService.getRecommendTags();
        mav.addObject("tags", tags_recommend);
        List<Integer> tags_ids = new ArrayList<Integer>();
        for (OsfTags tag : tags_recommend) {
            tags_ids.add(tag.getId());
        }
        mav.addObject("isInterests", feedService.hasInterestInTags(user == null ? 0 : user.getId(), tags_ids));
        Map<String, Object> params = new HashMap<>();
        params.put("userStatus", 0);
        List<OsfUsers> rec_users = userService.findList(params);
        rec_users.remove(user);
        if (rec_users.size() > 4) {
            rec_users.subList(4, rec_users.size()).clear();
        }
        mav.addObject("isFollowings", followService.isFollowing(user == null ? 0 : user.getId(), rec_users));
        Map<OsfUsers, List<OsfEvents>> feeds = new HashMap<OsfUsers, List<OsfEvents>>();
        for (OsfUsers rec_user : rec_users) {
            params.clear();
            params.put("userId", rec_user.getId());
            feeds.put(rec_user, eventService.findList(params));
        }
        mav.addObject("feeds", feeds);
        mav.addObject("dic", new Dic());
        return mav;
    }
}