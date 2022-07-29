package com.godot.community.service;

import com.godot.community.CommunityApplication;
import com.godot.community.dao.LoginTicketMapper;
import com.godot.community.dao.UserMapper;
import com.godot.community.entity.LoginTicket;
import com.godot.community.entity.User;
import com.godot.community.util.CommunityConstant;
import com.godot.community.util.CommunityUtil;
import com.godot.community.util.MailClient;
import com.mysql.cj.log.Log;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Session;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // Null val handler
        if (user == null) {
            throw new IllegalArgumentException("Parameter can't be empty!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Account can't be null!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password can't be null!");
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "Email can't be null!");
        }

        // Verify account username
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "Account username has exist!");
            return map;
        }
        // Verify email
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "Email has been registered");
            return map;
        }

        //**************** Register ****************/
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // Send activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILED;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // null val handler
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Account can't be null!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can't be null! ");
            return map;
        }

        // Verification
        User user = userMapper.selectByName(username);
        // account
        if (user == null) {
            map.put("usernameMsg", "Account not exist! ");
            return map;
        }
        // status
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "Account hasn't been activate!");
            return map;
        }
        // password
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Wrong password!");
            return map;
        }

        // Generate login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }


    // TODO : send verify codee
    public void getVerifyCode(String email) {

        // email context
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.md5(email).substring(0, 5);
        context.setVariable("code", code);

        // Generate verify code By login_ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(code);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_SECONDS * 1000));
        loginTicket.setStatus(3);
        loginTicketMapper.insertLoginTicket(loginTicket);


        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "邮箱验证码", content);
    }

    // TODO : handle forget password request
    public Map<String, Object> resetPassword(String email, String code, String newPassWord) {
        Map<String, Object> map = new HashMap<>();
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(CommunityUtil.md5(email).substring(0, 5));
        if (loginTicket == null || loginTicket.getTicket() != code || StringUtils.isBlank(code)) {
            map.put("codeMsg", "Verify code error!");
            return map;
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "Email doesn't exist!");
            return map;
        }

        userMapper.updatePassword(user.getId(), newPassWord);
        map.put("successMsg", "password reset succeed");

        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

}
