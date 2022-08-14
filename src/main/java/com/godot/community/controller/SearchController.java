package com.godot.community.controller;

import com.godot.community.entity.DiscussPost;
import com.godot.community.entity.Page;
import com.godot.community.service.ElasticsearchService;
import com.godot.community.service.LikeService;
import com.godot.community.service.UserService;
import com.godot.community.util.CommunityConstant;
import org.apache.kafka.common.network.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // Search post
        org.springframework.data.domain.Page<DiscussPost> searchResult = null;
        try {
            searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // Aggregate data
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // Post
                map.put("post", post);
                // Author
                map.put("user", userService.findUserById(post.getUserId()));
                // Like counts
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);

            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // Paging information
        page.setPath("/site/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());


        return "/site/search";
    }
}
