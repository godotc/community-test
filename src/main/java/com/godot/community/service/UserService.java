package com.godot.community.service;

import com.godot.community.dao.UserMapper;
import com.godot.community.entity.LoginTicket;
import com.godot.community.entity.User;
import com.godot.community.util.CommunityConstant;
import com.godot.community.util.CommunityUtil;
import com.godot.community.util.MailClient;
import com.godot.community.util.RedisKeyUtil;
import com.mysql.cj.log.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    //    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
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
            clearCache(userId);
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
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {

//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {

//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }


    // TODO : send verify codee
    public boolean getVerifyCode(String email) {

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
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "邮箱验证码", content);
        return true;
    }

    // TODO : handle forget password request
    public Map<String, Object> resetPassword(String email, String code, String newPassWord) {
        Map<String, Object> map = new HashMap<>();
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(CommunityUtil.md5(email).substring(0, 5));
        String redisKey = RedisKeyUtil.getTicketKey(code);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        if (loginTicket == null || loginTicket.getTicket() != code || StringUtils.isBlank(code)) {
            map.put("codeMsg", "Verify code error!");
            return map;
        }
        if (loginTicket.getExpired().before(new Date())) {
            map.put("codeMsg", "Verify code has been expired!");
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

    // TODO: get user has posts
    //

    // 1. First get from cache
    private User getCache(int userId) {
        String rediskey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(rediskey);
    }

    // 2. No cache then Init cache
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. If Data change then eliminate cache
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }
}