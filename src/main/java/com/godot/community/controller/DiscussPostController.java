package com.godot.community.controller;

import com.godot.community.entity.*;
import com.godot.community.event.EventProducer;
import com.godot.community.service.CommentService;
import com.godot.community.service.DiscussPostService;
import com.godot.community.service.LikeService;
import com.godot.community.service.UserService;
import com.godot.community.util.CommunityConstant;
import com.godot.community.util.CommunityUtil;
import com.godot.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "You haven't login!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // Trigger posted event
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);


        // Error will be handle in one in future
        return CommunityUtil.getJSONString(0, "Post Success!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // Post
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // Author
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // Like
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // Status
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(ENTITY_TYPE_POST, hostHolder.getUser().getId(), discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // Comment
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        page.setOffset(page.getOffset());

        // Post's comment
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffSet(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if (commentList != null) {
            for (Comment comment : commentList) {
                // Comment vo
                Map<String, Object> commentVo = new HashMap<>();
                // Comment
                commentVo.put("comment", comment);
                // Author
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // Likes
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
                commentVo.put("likeCount", likeCount);
                // Status
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(ENTITY_TYPE_POST, hostHolder.getUser().getId(), discussPostId);
                commentVo.put("likeStatus", likeStatus);

                // Comment's reply
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // reply vo
                        Map<String, Object> replyVo = new HashMap<>();
                        // reply
                        replyVo.put("reply", reply);
                        // author
                        replyVo.put("user", userService.findUserById(reply.getUserId()));

                        // reply target
                        User target = reply.getTargetId() == 0 ?
                                null : userService.findUserById(reply.getTargetId());

                        replyVo.put("target", target);

                        // Likes
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
                        replyVo.put("likeCount", likeCount);
                        // Status
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(ENTITY_TYPE_POST, hostHolder.getUser().getId(), discussPostId);
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                // Comment counts
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }


}
