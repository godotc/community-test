package com.godot.community.quartz;

import com.godot.community.entity.DiscussPost;
import com.godot.community.service.DiscussPostService;
import com.godot.community.service.ElasticsearchService;
import com.godot.community.service.LikeService;
import com.godot.community.util.CommunityConstant;
import com.godot.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    // Godot Era
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-07-14 22:06:41");
        } catch (ParseException e) {
            throw new RuntimeException("Initialize Godot Era failed !");
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[Task Canceled] No Post need to flush score !");
            return;
        }

        logger.info("[Task Beginning...] Flushing posts score： " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[Task End] Flush post score complete !");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("This post is not EXIST : id = " + postId);
            return;
        }

        // Whether Wonderful
        boolean wonderful = post.getStatus() == 1;
        // Comment count
        int commentCount = post.getCommentCount();
        // Like count
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // Calculate Weight
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // Score = weight + hadPostTime
        double score =
                Math.log10(Math.max(w, 1))
                        + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // Update post score
        discussPostService.updateScore(postId, score);
        // Synchronized Elastic Search data
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
