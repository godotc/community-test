package com.godot.community.controller.interceptor;

import com.godot.community.entity.LoginTicket;
import com.godot.community.entity.User;
import com.godot.community.service.UserService;
import com.godot.community.util.CookieUtil;
import com.godot.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get ticket from cookie
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // Query ticket
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // Check ticket validation
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // Query user by ticket
                User user = userService.findUserById(loginTicket.getUserId());
                // Maintain user into this request
                hostHolder.setUser(user);
            }
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
         hostHolder.clear();
    }
}