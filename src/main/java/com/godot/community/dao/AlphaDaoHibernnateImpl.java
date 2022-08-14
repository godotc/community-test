package com.godot.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaHibernate")
public class AlphaDaoHibernnateImpl implements AlphaDao{

    @Override
    public  String select(){
        return "Hibernate";
    }
}
