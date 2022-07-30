package com.godot.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOW = "follow";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_CAPTCHA = "captcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    // some entity's like
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // some User's likes
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // some User's follow.
    // follow:userId:entityType -> zset (entityId, now)
    public static String getFollowKey(int userId, int entityType) {
        return PREFIX_FOLLOW + SPLIT + userId + SPLIT + entityType;
    }

    // some User's Follower.
    // follower:entityType:entityId -> zset(uesrId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // login captcha
    public static String getCaptchaKey(String owner) {
        return PREFIX_CAPTCHA + SPLIT + owner;
    }

    // Login Ticket
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // User key
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
