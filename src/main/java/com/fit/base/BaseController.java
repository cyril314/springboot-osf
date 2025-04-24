package com.fit.base;

import com.alibaba.fastjson.JSONObject;
import com.fit.entity.OsfAlbums;
import com.fit.entity.OsfEvents;
import com.fit.entity.OsfPosts;
import com.fit.util.Dic;
import lombok.extern.slf4j.Slf4j;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @AUTO 控制层基类
 * @FILE BaseController.java
 * @DATE 2018-3-23 下午2:38:18
 * @Author AIM
 */
@Slf4j
public class BaseController extends BaseCommon {

    private static String MAIL_TPL = ClassLoader.getSystemResource("mailTemplates").getPath();
    @Value("${web_path}")
    private String WEB_PATH;
    @Value("${img_base_url}")
    public String IMG_BASE_URL;
    @Value("${activate_context}")
    private String ACTIVATE_CONTEXT = WEB_PATH + "/account/activation/";
    private String RESET_PWD_CONTEXT = WEB_PATH + "/account/resetpwd";
    @Autowired
    private JavaMailSenderImpl mailSender;
    public static StringTemplateGroup templateGroup = new StringTemplateGroup("mailTemplates", MAIL_TPL);

    private void sendMail(String to, String subject, String body) {
        MimeMessage mail = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "utf-8");
            helper.setFrom(mailSender.getUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send activation mail to
     *
     * @param to
     */
    public void sendAccountActivationEmail(String to, String key) {
        //String body = "<a href='"+ACTIVATE_CONTEXT+key+"?email="+to+"'>激活链接</a>";
        StringTemplate activation_temp = templateGroup.getInstanceOf("activation");
        activation_temp.setAttribute("img_base_url", IMG_BASE_URL);
        activation_temp.setAttribute("email", to);
        activation_temp.setAttribute("href", ACTIVATE_CONTEXT + key + "?email=" + to);
        activation_temp.setAttribute("link", ACTIVATE_CONTEXT + key + "?email=" + to);
        sendMail(to, "OSF账户激活", activation_temp.toString());
    }

    /**
     * send change password link to
     *
     * @param to
     */
    public void sendResetPwdEmail(String to, String key) {
        StringTemplate activation_temp = templateGroup.getInstanceOf("resetpwd");
        activation_temp.setAttribute("img_base_url", IMG_BASE_URL);
        activation_temp.setAttribute("href", RESET_PWD_CONTEXT + "?key=" + key + "&email=" + to);
        activation_temp.setAttribute("link", RESET_PWD_CONTEXT + "?key=" + key + "&email=" + to);

        sendMail(to, "OSF账户密码重置", activation_temp.toString());
    }

    protected String toJSONString(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    /**
     * 将数据保存到session
     *
     * @param key   存入session键名
     * @param value 存入session值
     */
    protected void setValue2Session(HttpServletRequest res, String key, Object value) {
        res.getSession().setAttribute(key, value);
    }

    /**
     * 从session中获取数据
     *
     * @param key 取出session键名
     */
    protected Object getValueFromSession(HttpServletRequest res, String key) {
        return res.getSession().getAttribute(key);
    }

    /**
     * 将数据从session中删除
     *
     * @param key 删除session键名
     */
    protected void removeValueFromSession(HttpServletRequest res, String key) {
        res.getSession().removeAttribute(key);
    }

    /**
     * 获取请求的所有参数,返回值Map
     */
    protected Map<String, Object> getRequestParamsMap(HttpServletRequest request) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            // 参数Map
            Map<String, String[]> properties = request.getParameterMap();
            String value = "";
            for (String strKey : properties.keySet()) {
                Object strObj = properties.get(strKey);
                if (null == strObj) {
                    value = "";
                } else if (strObj instanceof String[]) {
                    String[] values = (String[]) strObj;
                    for (int i = 0; i < values.length; i++) { // 用于请求参数中有多个相同名称
                        value = values[i] + ",";
                    }
                    value = value.substring(0, value.length() - 1);
                } else {
                    value = strObj.toString();// 用于请求参数中请求参数名唯一
                }
                returnMap.put(strKey.toString(), value);
            }
        } catch (Exception e) {
            log.error("getRequestParamsMap错误信息：{}", e);
        }
        return returnMap;
    }

    protected boolean isMobileDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && (userAgent.contains("Mobi") || userAgent.contains("Android") || userAgent.contains("iPhone"));
    }

    public OsfEvents toEvent(int object_type, Object obj) {
        OsfEvents event = new OsfEvents();
        if (Dic.OBJECT_TYPE_POST == object_type || Dic.OBJECT_TYPE_SHORTPOST == object_type) {
            OsfPosts post = (OsfPosts) obj;
            event.setObjectType(Dic.OBJECT_TYPE_POST);
            event.setObjectId(post.getId());
            event.setUserId(post.getPostAuthor());
            event.setTitle(post.getPostTitle());
            event.setSummary(post.getPostExcerpt());
            event.setContent(post.getPostCover());
            event.setLikeCount(post.getLikeCount());
            event.setShareCount(post.getShareCount());
            event.setCommentCount(post.getCommentCount());
            event.setTags(post.getPostTags());
        } else if (Dic.OBJECT_TYPE_ALBUM == object_type) {
            OsfAlbums album = (OsfAlbums) obj;
            event.setObjectType(Dic.OBJECT_TYPE_ALBUM);
            event.setObjectId(album.getId());
            event.setUserId(album.getUserId());
            event.setTitle(album.getCover());
            event.setSummary(album.getAlbumDesc());
            StringBuffer buffer = new StringBuffer();
            event.setContent(buffer.toString());
            event.setLikeCount(0);
            event.setShareCount(0);
            event.setCommentCount(0);
            event.setTags(album.getAlbumTags());

        } else if (Dic.OBJECT_TYPE_PHOTO == object_type) {
            //event_id = eventDao.savePhotoEvent((Photo)obj);
        }
        return event;
    }
}