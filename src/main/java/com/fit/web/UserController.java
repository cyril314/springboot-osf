package com.fit.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import com.fit.entity.OsfAlbums;
import com.fit.entity.OsfPosts;
import com.fit.entity.OsfUsers;
import com.fit.redis.FollowerService;
import com.fit.service.OsfAlbumsService;
import com.fit.service.OsfPostsService;
import com.fit.service.OsfUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName: UserController
 * @Description: 用户控制器
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private OsfUsersService userService;
    @Autowired
    private OsfPostsService postService;
    @Autowired
    private OsfAlbumsService albumService;
    @Autowired
    private FollowerService followService;

    /**
     * @param id
     * @param session
     * @return
     */
    @RequestMapping("/{id}")
    public ModelAndView collection(@PathVariable("id") int id, HttpSession session) {
        OsfUsers me = (OsfUsers) session.getAttribute("user");

        ModelAndView mav = new ModelAndView("user/index");
        OsfUsers user = userService.get(id);
        mav.addObject("u", user);
        mav.addObject("follow", followService.isFollowing(me == null ? 0 : me.getId(), id));
        Map<String, Long> counter = new HashMap<String, Long>();
        counter.put("follower", followService.getFollowersCount(id));
        counter.put("following", followService.getFollowingsCount(id));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("counter", id);
        counter.put("spost", (long) postService.findCount(map));
        mav.addObject("counter", counter);
        map.clear();
        map.put("postAuthor", id);
        List<OsfPosts> posts = postService.findList(map);
        mav.addObject("posts", posts);
        map.clear();
        map.put("userId", id);
        List<OsfAlbums> albums = albumService.findList(map);
        mav.addObject("albums", albums);

        return mav;
    }
}