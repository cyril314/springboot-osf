package com.fit.web;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fit.base.BaseController;
import com.fit.dao.OsfAlbumsDao;
import com.fit.entity.*;
import com.fit.redis.FeedService;
import com.fit.redis.FollowerService;
import com.fit.service.*;
import com.fit.util.Dic;
import com.fit.util.OSFUtils;
import com.fit.util.Property;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/album")
public class AlbumController extends BaseController {


    @Autowired
    private OsfAlbumsService albumService;
    @Autowired
    private OsfEventsService eventsService;
    @Autowired
    private OsfInterestsService interestsService;
    @Autowired
    private OsfUsersService userService;
    @Autowired
    private OsfPhotosService photoService;
    @Autowired
    private OsfTagsService tagsService;
    @Autowired
    @Qualifier("feedService")
    private FeedService feedService;
    @Autowired
    private FollowerService followService;


    @RequestMapping("/{id}/photos")
    public ModelAndView album(@PathVariable("id") int id, HttpSession session) {
        OsfUsers me = (OsfUsers) session.getAttribute("user");

        ModelAndView mav = new ModelAndView();
        OsfAlbums album = albumService.get(id);
        mav.addObject("album", album);

        OsfUsers author = userService.get(album.getUserId());
        mav.addObject("u", author);

        mav.addObject("follow", followService.isFollowing(me == null ? 0 : me.getId(), author.getId()));

        mav.setViewName("album/index");
        return mav;
    }

    @ResponseBody
    @RequestMapping("/{id}")
    public OsfAlbums getAlbumInfo(@PathVariable("id") int id) {
        return albumService.get(id);
    }

    /*
     * 相册上传页面
     * 指定album
     */
    @RequestMapping(value = "/{album_id}/upload", method = RequestMethod.GET)
    public String albumUploadPage(@PathVariable("album_id") int id) {
        return "album/upload";
    }


    /*
     * 相册上传页面
     * 未指定album
     */
    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public ModelAndView albumUploadPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        OsfAlbums osfAlbums = new OsfAlbums();
        osfAlbums.setUserId(user.getId());
        osfAlbums.setStatus(1); //待发布
        osfAlbums = albumService.get(osfAlbums);
        int album_id = osfAlbums.getId();
        Map<String, Object> param = new HashMap<>();
        param.put("albumId", album_id);
        List<OsfPhotos> photos = photoService.findList(param);
        session.setAttribute("album_id", album_id);
        mav.addObject("photos", photos);
        mav.setViewName("album/upload");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/delete/photo/{id}", method = RequestMethod.GET)
    public Map<String, Object> deletePhoto(@PathVariable("id") int id) {
        Map<String, Object> map = new HashMap<String, Object>();
        photoService.delete(id);
        map.put("status", Property.SUCCESS_PHOTO_DELETE);
        return map;
    }

    /*
     * 上传图片到相册
     */
    @ResponseBody
    @RequestMapping(value = "/{album_id}/upload/photo", method = RequestMethod.POST)
    public Map<String, Object> albumUpload(@PathVariable("album_id") int album_id, @RequestParam("uploader_input") MultipartFile img, HttpSession session) throws IOException {

        Map<String, Object> map = new HashMap<String, Object>();
        if (img.isEmpty()) {
            map.put("status", Property.ERROR_PHOTO_EMPTY);
            return map;
        }

        OsfUsers user = (OsfUsers) session.getAttribute("user");
        //检查相册是否属于用户
        OsfAlbums osfAlbums = this.albumService.get(album_id);
        if (user.getId() != osfAlbums.getUserId()) {
            map.put("status", Property.ERROR_ALBUM_PERMISSIONDENIED);
        } else {
            //上传图片
            String contentType = img.getContentType();
            String imgType = contentType.substring(contentType.indexOf('/') + 1);
            String key = UUID.randomUUID().toString() + "." + imgType;
            OSFUtils.uploadPhoto(img.getBytes(), key);
            OsfPhotos photos = new OsfPhotos();
            photos.setKey(key);
            photos.setAlbumId(album_id);
            int save = this.photoService.save(photos);
            map.put("status", Property.SUCCESS_PHOTO_CREATE);
            map.put("photo", photos);
        }
        return map;
    }

    private OsfAlbums toAlbum(String params) {
        OsfAlbums album = new OsfAlbums();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(params);
            album.setAlbumDesc(root.path("album_desc").asText());
            JsonNode photos = root.path("photos");
            if (photos.size() > 0) {
                OsfPhotos osfPhotos = this.photoService.get(Integer.parseInt(photos.get(0).path("id").asText()));
                album.setCover(osfPhotos.getKey());
                List<OsfPhotos> photos2upd = new ArrayList<OsfPhotos>();
                album.setPhotosCount(photos.size());
            }

            JsonNode tags = root.path("tags");
            if (tags.size() > 0) {
                album.setAlbumTags(tags.asText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return album;
    }

    /*
     * 创建相册
     *
     */
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> createAlbum(@RequestBody String params, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        OsfAlbums album = toAlbum(params);
        album.setId((Integer) session.getAttribute("album_id"));
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        album.setUserId(user.getId());
        if (album == null || album.getAlbumTags().length() == 0) {
            map.put("status", Property.ERROR_TAG_EMPTY);
        } else {
            List<OsfTags> taglist = new ArrayList<OsfTags>();
            for (String tag : album.getAlbumTags().split(":")) {
                OsfTags tg = new OsfTags();
                tg.setTag(tag);
                tg = tagsService.get(tg);
                if (tg == null) {
                    tg.setTag(tag);
                    int id = tagsService.save(tg);
                    tg.setId(id);
                }
                taglist.add(tg);
            }
            int event_id = this.eventsService.save(toEvent(Dic.OBJECT_TYPE_ALBUM, album));
            //push to users who follow u
            if (event_id != 0) {
                feedService.push(user.getId(), event_id);
            }
            //push to users who follow the tags in the album
            for (OsfTags tag : taglist) {
                OsfInterests interests = new OsfInterests();
                interests.setTagId(tag.getId());
                List<OsfInterests> list = this.interestsService.findList(interests);
                for (OsfInterests osfInterests : list) {
                    feedService.push(osfInterests.getUserId(), event_id);
                }
                //cache feeds to tag list
                feedService.cacheFeed2Tag(tag.getId(), event_id);
            }
            map.put("status", Property.SUCCESS_ALBUM_UPDATE);
        }
        map.put("album", album);
        return map;
    }

    /*
     * 未指定相册
     * 临时创建相册
     */
    @ResponseBody
    @RequestMapping(value = "/upload/photo", method = RequestMethod.POST)
    public Map<String, Object> uploadPhoto(@RequestParam("uploader_input") MultipartFile img, HttpSession session) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        if (img.isEmpty()) {
            map.put("status", Property.ERROR_PHOTO_EMPTY);
            return map;
        }
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        Integer album_id = (Integer) session.getAttribute("album_id");
        //创建临时相册
        if (album_id == null || album_id == 0) {
            OsfAlbums album = new OsfAlbums();
            album.setUserId(user.getId());
            album = this.albumService.get(album);
            if (album == null) {
                album_id = this.albumService.save(album);
                map.put("status", Property.SUCCESS_ALBUM_CREATE);
            }
            session.setAttribute("album_id", album_id);
        }
        //上传图片
        String contentType = img.getContentType();
        String imgType = contentType.substring(contentType.indexOf('/') + 1);
        String key = UUID.randomUUID().toString() + "." + imgType;
        OSFUtils.uploadPhoto(img.getBytes(), key);
        map.put("status", Property.SUCCESS_PHOTO_CREATE);
        OsfPhotos photos = new OsfPhotos();
        photos.setAlbumId(album_id);
        photos.setKey(key);
        int photo_id = this.photoService.save(photos);
        if (photo_id == 0) {
            map.put("status", Property.ERROR_PHOTO_CREATE);
        }
        map.put("id", photo_id);
        map.put("key", key);
        return map;
    }

    /*
     * post 中图片上传
     */
    @ResponseBody
    @RequestMapping(value = "/upload/postphoto", method = RequestMethod.POST)
    public Map<String, Object> postPhotoUpload(@RequestParam("uploader_input") MultipartFile img, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (img.isEmpty()) {
            map.put("status", Property.ERROR_PHOTO_EMPTY);
            return map;
        }

        //upload photo
        map = this.uploadImg(img);
        //set post cover
        session.setAttribute("post_cover", map.get("key"));
        return map;
    }

    /**
     * 上传头像
     *
     * @param img
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upload/avatar", method = RequestMethod.POST)
    public Map<String, Object> avatarUpload(@RequestParam("avatar_file") MultipartFile img, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (img.isEmpty()) {
            map.put("status", Property.ERROR_PHOTO_EMPTY);
            return map;
        }
        map = this.uploadImg(img);

        session.setAttribute("temp_avatar", map.get("key"));

        return map;
    }

    private Map<String, Object> uploadImg(MultipartFile img) {
        Map<String, Object> map = new HashMap<String, Object>();
        String contentType = img.getContentType();
        String imgType = contentType.substring(contentType.indexOf('/') + 1);
        String key = UUID.randomUUID().toString() + "." + imgType;
        try {
            //upload photo
            OSFUtils.uploadPhoto(img.getBytes(), key);
            map.put("key", key);
            map.put("link", Property.IMG_BASE_URL + key);
            map.put("status", Property.SUCCESS_PHOTO_CREATE);
            //save to local
            BufferedImage imgBuf = ImageIO.read(img.getInputStream());
            String classpath = ClassLoader.getSystemResource("").getPath();
            ImageIO.write(imgBuf, imgType, new File(classpath + "/tmp/" + key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/cropavatar", method = RequestMethod.POST)
    public Map<String, Object> cropAvatar(@RequestParam("x") int x, @RequestParam("y") int y, @RequestParam("width") int width, @RequestParam("height") int height, HttpSession session) throws IOException {
        //System.out.println("x:"+x+" y:"+y + " width:"+width+ " height:"+height);
        Map<String, Object> map = new HashMap<String, Object>();
        String key = (String) session.getAttribute("temp_avatar");
        if (key == null || key.length() == 0) {
            map.put("status", Property.ERROR_AVATAR_CHANGE);
            return map;
        }
        String classpath = ClassLoader.getSystemResource("").getPath();
        File ori_img = new File(classpath + "/tmp/" + key);
        BufferedImage croped_img = Thumbnails.of(ImageIO.read(ori_img)).sourceRegion(x, y, width, height).size(200, 200).asBufferedImage();
        String img_type = key.split("\\.")[1];
        // convert bufferedimage to inputstream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(croped_img, img_type, bos);
        OSFUtils.delPhotoInBucket(key);
        String new_key = UUID.randomUUID().toString() + "." + img_type;
        String avatar_img = key;
        if (OSFUtils.uploadPhoto(bos.toByteArray(), new_key) != null) {
            if (ori_img.exists()) {
                ori_img.delete();
            }
            avatar_img = new_key;
        }

        OsfUsers user = (OsfUsers) session.getAttribute("user");
        user = this.userService.get(user.getId());
        user.setUserAvatar(avatar_img);
        this.userService.update(user);
        ((OsfUsers) session.getAttribute("user")).setUserAvatar(avatar_img);

        map.put("status", Property.SUCCESS_AVATAR_CHANGE);
        return map;
    }
}