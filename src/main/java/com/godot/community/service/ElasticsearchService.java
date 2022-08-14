package com.godot.community.service;

import com.alibaba.fastjson.JSONObject;
import com.godot.community.dao.elasticsearch.DiscussPostRepository;
import com.godot.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws Exception {
        // 1. 创建searchRequest
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名

        // 2.配置高亮 HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");  //为哪些字段匹配到的内容设置高亮
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>"); //相当于把结果套了一点html标签  然后前端获取到数据就直接用
        highlightBuilder.postTags("</span>");

        // 3. 构建搜索条件 searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(current)// 指定从哪条开始查询
                .size(limit)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//配置高亮

        // 4.将搜索条件参数传入搜索请求
        searchRequest.source(searchSourceBuilder);

        //5.使用客户端发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new LinkedList<>();

        for (org.elasticsearch.search.SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);   // 处理高亮显示的结果

            HighlightField titleField = hit.getHighlightFields().get("title");

            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());  //title=<span style='color:red'>互联网</span>求职暖春计划...  }
                HighlightField contentField = hit.getHighlightFields().get("content");
                if (contentField != null) {
                    discussPost.setContent(contentField.getFragments()[0].toString());  //content=它是最时尚的<span style='color:red'>互联网</span>公司之一...  }
                    list.add(discussPost);
                }
            }
        }

        return new PageImpl<DiscussPost>(list);
    }

}
