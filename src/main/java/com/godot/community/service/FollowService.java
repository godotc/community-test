package com.godot.community.service;

import com.godot.community.entity.User;
import com.godot.community.util.CommunityConstant;
import com.godot.community.util.CommunityUtil;
import com.godot.community.util.RedisKeyUtil;
import io.lettuce.core.models.role.RedisUpstreamInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.awt.print.PrinterGraphics;
import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }


    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    //  one user Followed entity count
    public long findFollowCount(int userId, int entityType) {
        String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followKey);
    }

    // one entity Have Follower count
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // Whether follow some entity user
    public Boolean hasFollowed(int userId, int entityType, int entityId) {
        String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followKey, entityId) != null;
    }

    // Someone Followees
    public List<Map<String, Object>> findFollows(int userId, int offset, int limit) {
        String followKey = RedisKeyUtil.getFollowKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targets = redisTemplate.opsForZSet().reverseRange(followKey, offset, offset + limit - 1);

        if (targets == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targets) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // Follow time == score
            Double score = redisTemplate.opsForZSet().score(followKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //  Someone's Followers
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // TODO: page limit error
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // Follow time == score
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
