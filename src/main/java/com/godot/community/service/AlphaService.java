package com.godot.community.service;

import com.godot.community.config.AlphaConfig;
import com.godot.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out .println("Implenting AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("Initialing AlphaService");
    }

    @PreDestroy
    public void destory(){
        System.out.println("Destorying AlphaService");
    }

    public String find()
    {
        return alphaDao.select();
    }
}
