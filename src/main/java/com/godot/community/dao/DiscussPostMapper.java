package com.godot.community.dao;

import com.godot.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // @Param using for take an alias for parameter.
    // If only one para, and use in <if>, must add alias.
    int selectDiscussPostRows(@Param("userId") int userId);
}
