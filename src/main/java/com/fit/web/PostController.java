package com.fit.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.OsfPosts;
import com.fit.entity.OsfTags;
import com.fit.entity.OsfUsers;
import com.fit.redis.FeedService;
import com.fit.redis.FollowerService;
import com.fit.redis.LikeService;
import com.fit.service.*;
import com.fit.util.BeanUtils;
import com.fit.util.Dic;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/post")
public class PostController extends BaseController {

    @Autowired
    private OsfPostsService postService;
    @Autowired
    private OsfUsersService userService;
    @Autowired
    private OsfEventsService eventService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private FollowerService followService;

    @RequestMapping("/{id}")
    public ModelAndView post(@PathVariable("id") int id, HttpSession session) {
        ModelAndView mav = new ModelAndView("post/index");
        OsfUsers me = (OsfUsers) session.getAttribute("OsfUsers");
        OsfPosts osfPosts = postService.get(id);
        OsfUsers author = userService.get(osfPosts.getPostAuthor());
        mav.addObject("u", author);
        mav.addObject("follow", followService.isFollowing(me == null ? 0 : me.getId(), author.getId()));
        mav.addObject("is_like", likeService.isLike(me == null ? 0 : me.getId(), Dic.OBJECT_TYPE_POST, id));
        mav.addObject("post", postService.get(id));
        return mav;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createPost() {
        return "post/create";
    }

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> createPost(HttpServletRequest request, HttpSession session) {
        OsfUsers OsfUsers = (OsfUsers) session.getAttribute("OsfUsers");
        String post_cover = (String) session.getAttribute("post_cover");
        session.removeAttribute("post_cover");
        Map<String, Object> params = getRequestParamsMap(request);
        OsfPosts osfPosts = BeanUtils.map2Bean(OsfPosts.class, params);
        params.clear();
        if (osfPosts.getPostStatus() == null) osfPosts.setPostStatus(0);
        if (osfPosts.getPostStatus() < 0 || osfPosts.getPostStatus() > 3) {
            params.put("status", Property.ERROR_POST_STATUS);
        }

        if (osfPosts.getCommentStatus() == null) osfPosts.setPostStatus(0);
        if (osfPosts.getCommentStatus() != 0 && osfPosts.getCommentStatus() != 1) {
            params.put("status", Property.ERROR_COMMENT_STATUS);
        }
        int save = this.postService.save(osfPosts);
        if (save > 0) {
            params.put("status", Property.SUCCESS_POST_CREATE);
        }
        //1 save post
        String status = (String) params.get("status");
        OsfPosts post = this.postService.get(save);
        //2 add event
        if (Property.SUCCESS_POST_CREATE.equals(status)) {
            int event_id = eventService.save(toEvent(Dic.OBJECT_TYPE_POST, post));
            //3 push to followers
            if (event_id != 0) {
                feedService.push(OsfUsers.getId(), event_id);
            }
            //4 push to OsfUsers who follow the tags in the post
            String[] tags = post.getPostTags().split(":");
            feedService.post2CacheFeed2Tag(tags, event_id);
        }
        return params;
    }

    @ResponseBody
    @RequestMapping("/delete/{id}")
    public Map<String, Object> deletePost(@PathVariable("id") int id) {
        Map<String, Object> map = new HashMap<String, Object>();
        postService.delete(id);
        map.put("status", Property.SUCCESS_POST_DELETE);
        return map;
    }
}