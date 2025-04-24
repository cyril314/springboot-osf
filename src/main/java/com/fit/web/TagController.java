package com.fit.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.fit.entity.OsfEvents;
import com.fit.entity.OsfInterests;
import com.fit.entity.OsfTags;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfEventsService;
import com.fit.service.OsfInterestsService;
import com.fit.service.OsfTagsService;
import com.fit.util.Dic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fit.util.Property;

@Controller
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private OsfTagsService tagService;

    @Autowired
    private OsfInterestsService interestService;

    @Autowired
    private OsfEventsService feedService;

    @RequestMapping("/{tag_id}")
    public ModelAndView getFeedsByTag(@PathVariable("tag_id") int tag_id, HttpSession session) {
        ModelAndView mav = new ModelAndView("tag/index");
        OsfTags tag = tagService.get(tag_id);
        if (tag == null) {
            mav.setViewName("404");
            return mav;
        }
        mav.addObject("tag", tag.getTag());
        mav.addObject("id", tag.getId());

        Map<String, Object> params = new HashMap<>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (user != null) {
            params.put("userId", user.getId());
            params.put("tagId", tag.getId());
            int count = this.interestService.findCount(params);
            mav.addObject("isInterest", count == 0 ? false : true);
        } else {
            mav.addObject("isInterest", false);
        }
        params.clear();
        params.put("tagId", tag.getId());
        params.put("userId", user != null ? user.getId() : 0);
        List<OsfEvents> feeds = feedService.findList(params);
        mav.addObject("feeds", feeds);
        mav.addObject("dic", new Dic());
        return mav;
    }

    @RequestMapping("/{tag_id}/page/{page}")
    public ModelAndView getFeedsByTagOfPage(@PathVariable("tag_id") int tag_id, @PathVariable("page") int page, HttpSession session) {
        ModelAndView mav = new ModelAndView("nextpage");
        OsfTags tag = tagService.get(tag_id);
        if (tag == null) {
            mav.setViewName("404");
            return mav;
        }
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        Map<String, Object> params = new HashMap<>();
        params.put("tagId", tag.getId());
        params.put("userId", user != null ? user.getId() : 0);
        List<OsfEvents> feeds = feedService.findList(params);
        mav.addObject("feeds", feeds);
        mav.addObject("dic", new Dic());
        return mav;
    }

    /**
     * 对某个标签感兴趣
     */
    @ResponseBody
    @RequestMapping("/{tag_id}/interest")
    public Map<String, Object> interest(@PathVariable("tag_id") int tag_id, HttpSession session) {
        Map<String, Object> ret = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        OsfInterests interest = new OsfInterests();
        interest.setId(tag_id);
        interest.setUserId(user.getId());
        interestService.save(interest);
        ret.put("status", Property.SUCCESS_INTEREST);
        return ret;
    }

    /**
     *
     */
    @ResponseBody
    @RequestMapping("/{tag_id}/undointerest")
    public Map<String, Object> undoInterest(@PathVariable("tag_id") int tag_id, HttpSession session) {
        Map<String, Object> ret = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        OsfInterests interest = new OsfInterests();
        interest.setId(tag_id);
        interest.setUserId(user.getId());
        interestService.delete(interest);
        ret.put("status", Property.SUCCESS_INTEREST_UNDO);
        return ret;
    }
}