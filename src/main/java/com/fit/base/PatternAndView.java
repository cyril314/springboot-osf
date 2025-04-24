package com.fit.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @AUTO 页面构造模型
 * @FILE PatternAndView.java
 * @DATE 2017-9-14 下午10:05:40
 * @Author AIM
 */
@Slf4j
public class PatternAndView extends ModelAndView {

    public PatternAndView(String viewName) {
        String webPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        super.setViewName(viewName);
        super.addObject("webpath", webPath);
    }

    /**
     * 字符串首位去空
     */
    protected String trimSpaces(String str) {
        while (str.startsWith(" ")) {
            str = str.substring(1, str.length()).trim();
        }
        while (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1).trim();
        }
        return str;
    }

    /**
     * 判断是不是IP地址
     */
    protected boolean isIp(String IP) {
        boolean b = false;
        IP = trimSpaces(IP);
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] s = IP.split("\\.");
            if ((Integer.parseInt(s[0]) < 255) && (Integer.parseInt(s[1]) < 255) && (Integer.parseInt(s[2]) < 255) && (Integer.parseInt(s[3]) < 255))
                b = true;
        }
        return b;
    }

    /**
     * URL解码
     */
    protected String decode(String s) {
        return decode(s, "UTF-8");
    }

    protected String decode(String s, String coding) {
        String ret = s;
        try {
            ret = URLDecoder.decode(s.trim(), coding);
        } catch (Exception localException) {
            log.info("URL解码异常");
        }
        return ret;
    }

    /**
     * URL编码
     */
    protected String encode(String s) {
        return encode(s, "UTF-8");
    }

    protected String encode(String s, String coding) {
        String ret = s;
        try {
            ret = URLEncoder.encode(s.trim(), coding);
        } catch (Exception localException) {
            log.info("URL编码异常");
        }
        return ret;
    }

    /**
     * 请求参数URL转MAP对象
     */
    protected Map<String, String> reqStr2Map(String s) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            String[] splitByte = s.split("&");
            for (String str : splitByte) {
                map.put(str.substring(0, str.indexOf("=")), str.substring(str.indexOf("=") + 1));
            }
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }
        return map;
    }
}
