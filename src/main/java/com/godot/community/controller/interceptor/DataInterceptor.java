package com.godot.community.controller.interceptor;

import com.godot.community.entity.User;
import com.godot.community.service.DataService;
import com.godot.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    public DataService dataService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //  Count UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // Count DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
