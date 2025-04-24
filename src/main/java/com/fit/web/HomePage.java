package com.fit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.OsfEvents;
import com.fit.entity.OsfTags;
import com.fit.entity.OsfUsers;
import com.fit.redis.FeedService;
import com.fit.redis.FollowerService;
import com.fit.service.OsfTagsService;
import com.fit.service.OsfUsersService;
import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @ClassName: HomePage
 * @Description: 主页控制器
 */
@Controller
public class HomePage extends BaseController {

    @Autowired
    private FeedService feedService;
    @Autowired
    private FollowerService followerService;
    @Autowired
    private OsfUsersService userService;
    @Autowired
    private OsfTagsService tagService;

    /**
     * 用户首页
     */
    @RequestMapping("/")
    public ModelAndView showHomePage(HttpServletRequest request, HttpSession session) {
        ModelAndView mav = new ModelAndView("index");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (user != null) {
            Map<String, Long> counter = new HashMap<String, Long>();
            counter.put("follower", followerService.getFollowersCount(user.getId()));
            counter.put("following", followerService.getFollowingsCount(user.getId()));
            mav.addObject("counter", counter);
            List<OsfEvents> feeds = feedService.getFeeds(user.getId());
            mav.addObject("feeds", feeds);
            mav.addObject("dic", new Dic());
            mav.addObject("user", user);
            mav.addObject("avatar", IMG_BASE_URL + user.getUserAvatar());
        } else {
            String webPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            mav.addObject("avatar", webPath + "/static/img/avatar.png");
        }
        return mav;
    }

    /**
     * 弹出窗获取用户信息
     *
     * @param user_id
     * @return
     */
    @RequestMapping("/popup_usercard/{user_id}")
    public ModelAndView getPopupUserCard(@PathVariable("user_id") String user_id) {
        ModelAndView mav = new ModelAndView("popup_usercard");
        OsfUsers user = userService.get(Integer.valueOf(user_id));
        if (user != null) {
            mav.addObject("u", user);
            Map<String, Long> counter = new HashMap<String, Long>();
            counter.put("follower", followerService.getFollowersCount(user.getId()));
            counter.put("following", followerService.getFollowingsCount(user.getId()));
            mav.addObject("counter", counter);
        }

        return mav;
    }

    /**
     * @param num_str
     * @param session
     * @return
     */
    @RequestMapping("/page/{num}")
    public ModelAndView nextPage(@PathVariable("num") String num_str, HttpSession session) {
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        ModelAndView mav = new ModelAndView("nextpage");

        int num = Integer.parseInt(num_str);
        List<OsfEvents> feeds = feedService.getFeedsOfPage(user.getId(), num);
        mav.addObject("feeds", feeds);
        mav.addObject("dic", new Dic());

        return mav;
    }

    /**
     * 欢迎首页
     */
    @RequestMapping("/welcome")
    public ModelAndView welcome(HttpSession session) {
        ModelAndView mav = new ModelAndView("welcome");
        if (session.getAttribute("user") != null) {
            mav.setViewName("redirect:/");
        } else {
            List<OsfTags> tags_recommend = tagService.findList();
            if (tags_recommend.size() > 12) {
                tags_recommend.subList(12, tags_recommend.size()).clear();
            }
            mav.addObject("tags", tags_recommend);
            mav.addObject("dic", new Dic());
        }
        return mav;
    }

    /**
     * 工具栏
     */
    @RequestMapping("/sidebar")
    public ModelAndView sideBar(HttpSession session) {
        ModelAndView mav = new ModelAndView("sidebar");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (user == null) {
            return mav;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("userStatus", 0);
        List<OsfUsers> rec_users = userService.findList(params);
        if (rec_users.size() > 4) {
            rec_users.subList(12, rec_users.size()).clear();
        }
        mav.addObject("popusers", rec_users);
        mav.addObject("isFollowings", followerService.isFollowing(user == null ? 0 : user.getId(), rec_users));

        List<OsfTags> tags_recommend = tagService.findList();
        if (tags_recommend.size() > 12) {
            tags_recommend.subList(12, tags_recommend.size()).clear();
        }
        mav.addObject("poptags", tags_recommend);

        return mav;
    }

    /**
     * 指南
     */
    @RequestMapping("/guide")
    public ModelAndView guide(HttpSession session) {
        ModelAndView mav = new ModelAndView("guide");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        List<OsfTags> tags_recommend = tagService.findList();
        if (tags_recommend.size() > 12) {
            tags_recommend.subList(12, tags_recommend.size()).clear();
        }
        mav.addObject("tags", tags_recommend);
        List<Integer> tags_ids = new ArrayList<Integer>();
        for (OsfTags tag : tags_recommend) {
            tags_ids.add(tag.getId());
        }
        mav.addObject("isInterests", feedService.hasInterestInTags(user == null ? 0 : user.getId(), tags_ids));
        mav.addObject("dic", new Dic());
        return mav;
    }

    /**
     * 新用户兴趣选择之后 feed初始化
     *
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/guide/ok")
    public Map<String, Object> guideOk(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        feedService.codeStart(user.getId());

        return map;
    }

    /**
     * 获取粉丝
     */
    @RequestMapping("/followers")
    public ModelAndView getFollowers(HttpSession session) {
        ModelAndView mav = new ModelAndView("follower");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("followers", followerService.getUsersByIDs(followerService.getFollowerIDs(user.getId())));
        return mav;
    }

    /**
     * @param session
     * @return
     */
    @RequestMapping("/followings")
    public ModelAndView getFollowings(HttpSession session) {
        ModelAndView mav = new ModelAndView("following");
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        mav.addObject("followings", followerService.getUsersByIDs(followerService.getFollowingIDs(user.getId())));
        return mav;
    }

    /**
     * 404 ERROR
     */
    @RequestMapping("/404")
    public ModelAndView pageNotFound() {
        return new ModelAndView("404");
    }

    /**
     * 500 ERROR
     */
    @RequestMapping("/500")
    public ModelAndView error500() {
        return new ModelAndView("500");
    }
}