package com.godot.community.util;

public interface CommunityConstant {


    // ret val of Activation
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILED = 2;

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;


    // Post
    int ENTITY_TYPE_POST = 1;
    // Comment
    int ENTITY_TYPE_COMMENT = 2;
    // User
    int ENTITY_TYPE_USER = 3;


    // Kafka Topics
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";

    // ID
    int SYSTEM_USER_ID = 1;

}
