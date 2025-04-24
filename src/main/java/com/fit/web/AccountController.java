package com.fit.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fit.base.BaseController;
import com.fit.entity.OsfUsers;
import com.fit.service.OsfUsersService;
import com.fit.util.CipherUtil;
import com.fit.util.OSFUtils;
import com.fit.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName: AccountController
 * @Description: 帐户控制器
 */
@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

    @Autowired
    private OsfUsersService userService;

    /**
     * 用户登录初始化
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/index";
        }
        if (request.getAttribute("bounce") != null) {
            request.setAttribute("bounce", request.getAttribute("bounce"));
            request.setAttribute("status", Property.ERROR_ACCOUNT_NOTLOGIN);
        }
        return "account/login";
    }

    /**
     * 用户登录
     *
     * @param email
     * @param password
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session) {
        Map<String, Object> ret = new HashMap<String, Object>();
        // 1 empty check
        if (email == null || email.length() <= 0) {
            ret.put("status", Property.ERROR_EMAIL_EMPTY);
        }
        if (password == null || password.length() <= 0) {
            ret.put("status", Property.ERROR_PWD_EMPTY);
        }
        OsfUsers user = new OsfUsers();
        // 2 ValidateEmail
        if (OSFUtils.validateEmail(email)) {
            // 3 email exist?
            user.setUserEmail(email);
        } else {
            user.setUserName(email);
        }
        user = this.userService.get(user);
        if (user == null) {
            ret.put("status", Property.ERROR_EMAIL_NOT_REG);
        } else {
            // 4 check user status
            if (Property.STATUS_USER_NORMAL != user.getUserStatus()) {
                ret.put("status", user.getUserStatus());
            }
        }
        // 5 password validate
        if (!CipherUtil.validatePassword(user.getUserPwd(), password)) {
            ret.put("status", Property.ERROR_PWD_DIFF);
        } else {
            ret.put("status", Property.SUCCESS_ACCOUNT_LOGIN);
            ret.put("path", "account/setting/avatar");
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUserName());
        }
        return ret;
    }

    /**
     * 设置内容信息页
     */
    @RequestMapping(value = "/setting/info", method = RequestMethod.GET)
    public String settingInfoPage(HttpSession session) {
        return "account/setting/info";
    }

    /**
     * 设置内容信息
     *
     * @param user_name
     * @param user_desc
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/setting/info", method = RequestMethod.POST)
    public Map<String, Object> settingInfo(@RequestParam("user_name") String user_name, @RequestParam("user_desc") String user_desc, HttpSession session) {
        OsfUsers me = (OsfUsers) session.getAttribute("user");
        Map<String, Object> map = new HashMap<String, Object>();
        if (user_name == null || user_name.length() == 0) {
            user_name = me.getUserName();
        } else {
            map.put("userName", user_name);
            int count = this.userService.findCount(map);
            if (count > 0) {
                map.put("status", Property.ERROR_USERNAME_EXIST);
                return map;
            }
        }
        // username is ok, but return a error status
        OsfUsers user = this.userService.get(me.getId());
        user.setUserDesc(user_desc);
        user.setUserName(user_name);
        this.userService.update(user);
        // update session
        me.setUserName(user_name);
        me.setUserDesc(user_desc);

        map.put("status", Property.ERROR_USERNAME_NOTEXIST);
        return map;
    }

    /**
     * 头像设置
     */
    @RequestMapping(value = "/setting/avatar")
    public ModelAndView settingAvatar(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("account/setting/avatar");
        return mav;
    }

    /**
     * 安全设置页
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/setting/security")
    public String settingSecurity(HttpSession session) {
        return "account/setting/security";
    }

    /**
     * 修改密码页
     *
     * @param key
     * @param email
     * @param session
     * @return
     */
    @RequestMapping(value = "/resetpwd", method = RequestMethod.GET)
    public ModelAndView resetpwdPage(@RequestParam("key") String key, @RequestParam("email") String email, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        boolean isAllowedResetPwd = true;
        String status = Property.ERROR_PWD_RESET_NOTALLOWED;
        if ((email == null) || (key == null)) {
            isAllowedResetPwd = false;
        }
        // set user login
        OsfUsers user = new OsfUsers();
        user.setUserEmail(email);
        user = this.userService.get(user);
        session.setAttribute("user", user);
        String resetpwd_key = user.getResetpwdKey();
        if (resetpwd_key == null || resetpwd_key.length() == 0) {
            isAllowedResetPwd = false;
        } else {
            isAllowedResetPwd = resetpwd_key.equals(key) ? true : false;
        }
        if (isAllowedResetPwd) {
            status = Property.SUCCESS_PWD_RESET_ALLOWED;
        }
        mav.addObject("status", status);
        mav.addObject("SUCCESS_PWD_RESET_ALLOWED", Property.SUCCESS_PWD_RESET_ALLOWED);
        mav.addObject("ERROR_PWD_RESET_NOTALLOWED", Property.ERROR_PWD_RESET_NOTALLOWED);
        mav.setViewName("account/resetpwd");
        return mav;
    }

    /**
     * 修改密码
     *
     * @param password
     * @param cfm_pwd
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resetpwd", method = RequestMethod.POST)
    public Map<String, Object> resetpwd(@RequestParam("password") String password, @RequestParam("cfm_pwd") String cfm_pwd, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (password == null || password.length() == 0) {
            map.put("status", Property.ERROR_PWD_EMPTY);
        }
        if (cfm_pwd == null || cfm_pwd.length() == 0) {
            map.put("status", Property.ERROR_CFMPWD_EMPTY);
        }
        if (!password.equals(cfm_pwd)) {
            map.put("status", Property.ERROR_CFMPWD_NOTAGREE);
        } else {
            String vpf_rs = CipherUtil.validatePasswordFormat(password);
            if (vpf_rs != Property.SUCCESS_PWD_FORMAT) {
                map.put("status", vpf_rs);
            } else {
                user = this.userService.get(user);
                user.setUserPwd(CipherUtil.generatePassword(password));
                user.setResetpwdKey(null);
                this.userService.update(user);
                map.put("status", Property.SUCCESS_PWD_RESET);
            }
        }

        return map;
    }

    /**
     * 更改密码
     *
     * @param old_pwd
     * @param new_pwd
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/changepwd", method = RequestMethod.POST)
    public Map<String, Object> changepwd(@RequestParam("old_pwd") String old_pwd, @RequestParam("new_pwd") String new_pwd, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        if (old_pwd == null || old_pwd.length() == 0) {
            map.put("status", Property.ERROR_PWD_EMPTY);
        }
        if (new_pwd == null || new_pwd.length() == 0) {
            map.put("status", Property.ERROR_CFMPWD_EMPTY);
        }
        if (!old_pwd.equals(new_pwd)) {
            map.put("status", Property.ERROR_CFMPWD_NOTAGREE);
        } else {
            String vpf_rs = CipherUtil.generatePassword(old_pwd);
            if (vpf_rs != Property.SUCCESS_PWD_FORMAT) {
                map.put("status", vpf_rs);
            } else {
                user = this.userService.get(user);
                user.setUserPwd(CipherUtil.generatePassword(new_pwd));
                this.userService.update(user);
                map.put("status", Property.SUCCESS_PWD_RESET);
            }
        }
        return map;
    }

    /**
     * 发送重置密码邮件
     */
    @ResponseBody
    @RequestMapping(value = "/send_resetpwd_email")
    public Map<String, Object> sendResetPwdEmail(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        OsfUsers user = (OsfUsers) session.getAttribute("user");
        OsfUsers users = this.userService.get(user.getId());
        user.setResetpwdKey(CipherUtil.generateRandomLinkUseEmail(user.getUserEmail()));
        this.userService.update(users);
        map.put("status", Property.SUCCESS_EMAIL_RESETPWD_SEND);
        return map;
    }

    /**
     * 用户注册初始化
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register() {
        return "account/register";
    }

    /**
     * 用户注册
     *
     * @param username
     * @param email
     * @param password
     * @param cfmPwd
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Map<String, String> register(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("cfmPwd") String cfmPwd) {
        Map<String, String> map = new HashMap<String, String>();
        String status = null;
        // 1 empty check
        if (email == null || email.length() <= 0) {
            status = Property.ERROR_EMAIL_EMPTY;
        } else {
            // 4 ValidateEmail
            if (!OSFUtils.validateEmail(email)) {
                status = Property.ERROR_EMAIL_FORMAT;
            }
            // 5 email exist?
            OsfUsers userEmail = new OsfUsers();
            userEmail.setUserEmail(email);
            userEmail = this.userService.get(userEmail);
            if (email != null) {
                // 6 user status check
                if (Property.STATUS_USER_NORMAL == userEmail.getUserStatus()) {
                    status = Property.ERROR_ACCOUNT_EXIST;
                } else if (Property.STATUS_USER_INACTIVE == userEmail.getUserStatus()) {
                    map.put("activationKey", URLEncoder.encode(userEmail.getUserActivationkey()));
                    status = Property.ERROR_ACCOUNT_INACTIVE;
                } else if (Property.STATUS_USER_LOCK == userEmail.getUserStatus()) {
                    status = Property.ERROR_ACCOUNT_LOCK;
                } else if (Property.STATUS_USER_CANCELLED == userEmail.getUserStatus()) {
                    status = Property.ERROR_ACCOUNT_CANCELLED;
                }
            } else {
                if (username == null || username.length() == 0) {
                    status = Property.ERROR_USERNAME_EMPTY;
                } else {
                    OsfUsers user = new OsfUsers();
                    user.setUserName(username);
                    user = this.userService.get(user);
                    // username exist check
                    if (user != null) {
                        status = Property.ERROR_USERNAME_EXIST;
                    } else {
                        if (password == null || password.length() <= 0) {
                            status = Property.ERROR_PWD_EMPTY;
                        } else {
                            // 3 password format validate
                            String vpf_rs = CipherUtil.validatePasswordFormat(password);
                            if (vpf_rs != Property.SUCCESS_PWD_FORMAT) {
                                status = vpf_rs;
                            }
                        }
                        if (cfmPwd == null || cfmPwd.length() <= 0) {
                            status = Property.ERROR_CFMPWD_EMPTY;
                        }

                        // 2 pwd == conformPwd ?
                        if (!password.equals(cfmPwd)) {
                            status = Property.ERROR_CFMPWD_NOTAGREE;
                        }

                        user = new OsfUsers();
                        user.setUserName(username);
                        user.setUserPwd(CipherUtil.generatePassword(password));
                        user.setUserEmail(email);
                        user.setUserStatus(Property.STATUS_USER_INACTIVE);
                        user.setUserAvatar(Property.DEFAULT_USER_AVATAR);
                        String activationKey = CipherUtil.generateActivationUrl(email, password);
                        user.setUserActivationkey(activationKey);
                        int id = this.userService.save(user);

                        map.put("id", String.valueOf(id));
                        map.put("activationKey", URLEncoder.encode(activationKey));
                        status = Property.SUCCESS_ACCOUNT_REG;
                    }
                }
            }
        }

        if (Property.SUCCESS_ACCOUNT_REG.equals(status)) {
            this.sendAccountActivationEmail(email, map.get("activationKey"));
        }
        map.put("status", status);
        return map;
    }

    /**
     * 初始化状态
     */
    private void initStatus(ModelAndView mav) {
        mav.addObject("ERROR_ACCOUNT_ACTIVATION_NOTEXIST", Property.ERROR_ACCOUNT_ACTIVATION_NOTEXIST);
        mav.addObject("ERROR_ACCOUNT_ACTIVATION_EXPIRED", Property.ERROR_ACCOUNT_ACTIVATION_EXPIRED);
        mav.addObject("ERROR_ACCOUNT_ACTIVATION", Property.ERROR_ACCOUNT_ACTIVATION);
    }

    /**
     * actication ->n. 激活；
     *
     * @param email
     * @return
     */
    @RequestMapping("/activation/mail/send")
    public ModelAndView actication(@RequestParam("email") String email) {
        ModelAndView mav = new ModelAndView("account/activation");
        initStatus(mav);
        mav.addObject("email", email);
        return mav;
    }

    /**
     * 重新发送激活邮件
     *
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping("/activation/mail/resend")
    public Map<String, String> acticationMailResend(@RequestParam("email") String email) {
        Map<String, String> ret = new HashMap<String, String>();
        String status = null;
        OsfUsers user = new OsfUsers();
        user.setUserEmail(email);
        user = this.userService.get(user);
        if (user == null) {
            status = Property.ERROR_EMAIL_NOT_REG;
        }
        if (Property.STATUS_USER_INACTIVE == user.getUserStatus()) {
            // 2 gen activation key
            String activationKey = CipherUtil.generateActivationUrl(email, new Date().toString());
            user.setUserActivationkey(activationKey);
            this.userService.update(user);
            status = Property.SUCCESS_ACCOUNT_ACTIVATION_KEY_UPD;
            ret.put("activationKey", activationKey);
        } else {
            if (Property.STATUS_USER_NORMAL == user.getUserStatus()) status = Property.ERROR_ACCOUNT_EXIST; // 已激活
            else if (Property.STATUS_USER_CANCELLED == user.getUserStatus()) status = Property.ERROR_ACCOUNT_CANCELLED;
        }
        if (Property.SUCCESS_ACCOUNT_ACTIVATION_KEY_UPD.equals(status)) {
            this.sendAccountActivationEmail(email, ret.get("activationKey"));
            ret.remove("activationKey");
            ret.put("status", Property.SUCCESS_ACCOUNT_ACTIVATION_EMAIL_RESEND);
        }
        return ret;
    }

    /**
     * 用户激活
     *
     * @param key
     * @param email
     * @param session
     * @return
     */
    @RequestMapping("/activation/{key}")
    public ModelAndView activation(@PathVariable("key") String key, @RequestParam("email") String email, HttpSession session) {
        ModelAndView mav = new ModelAndView("account/activation");
        String status = null;
        try {
            OsfUsers user = new OsfUsers();
            user.setUserEmail(email);
            user = this.userService.get(user);
            if (user == null) {
                status = Property.ERROR_ACCOUNT_ACTIVATION_NOTEXIST;
            } else {
                if (user.getUserStatus() == Property.STATUS_USER_INACTIVE) {
                    if (user.getUserActivationkey().equals(URLDecoder.decode(key, "utf-8"))) {
                        user.setUserActivationkey(null);
                        user.setUserStatus(Property.STATUS_USER_NORMAL);
                        this.userService.update(user);
                    } else {
                        status = Property.ERROR_ACCOUNT_ACTIVATION_EXPIRED;
                    }
                } else {
                    if (user.getUserStatus() == Property.STATUS_USER_NORMAL) {
                        status = Property.ERROR_ACCOUNT_EXIST;
                    } else {
                        status = Property.ERROR_ACCOUNT_ACTIVATION;
                    }

                }
            }
            status = Property.SUCCESS_ACCOUNT_ACTIVATION;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (Property.SUCCESS_ACCOUNT_ACTIVATION.equals(status) || Property.ERROR_ACCOUNT_EXIST.equals(status)) {
            mav.setViewName("redirect:/guide");
            OsfUsers user = new OsfUsers();
            user.setUserEmail(email);
            user = this.userService.get(user);
            session.setAttribute("user", user);
        } else {
            mav.addObject("status", status);
            mav.addObject("email", email);
            mav.addObject("ERROR_ACCOUNT_ACTIVATION_NOTEXIST", Property.ERROR_ACCOUNT_ACTIVATION_NOTEXIST);
            mav.addObject("ERROR_ACCOUNT_ACTIVATION_EXPIRED", Property.ERROR_ACCOUNT_ACTIVATION_EXPIRED);
            mav.addObject("ERROR_ACCOUNT_ACTIVATION", Property.ERROR_ACCOUNT_ACTIVATION);
        }
        return mav;
    }

    /**
     * 完整的用户信息
     */
    @RequestMapping("/completeinfo")
    public void completeUserInfo(HttpSession session) {
    }

    /**
     * 用户退出
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "account/login";
    }
}