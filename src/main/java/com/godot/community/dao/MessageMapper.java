package com.godot.community.dao;

import com.godot.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // Current user's chat list, every session return 1 newest message
    List<Message> selectConversations(int userId, int offset, int limit);

    // Current session amount
    int selectConversationCount(int userId);

    // One session includes messages
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // One session's messages amount
    int selectLetterCount(String conversationId);

    // Unread message count
    int selectLetterUnreadCount(int userId, String conversationId);

    // Add messaage
    int insertMessage(Message message);

    // update status
    int updateStatus(List<Integer> ids, int status);

    // Query the newest message of One Topic
    Message selectLatestNotice(int userId, String topic);

    // Query amount of one Topic
    int selectNoticeCount(int userId, String topic);

    // Query unread message
    int selectNoticeUnreadCount(int userId, String topic);

    // Query message list of one topic of an uer
    List<Message> selectNotices(int userId, String topic, int offset, int limit);


}
