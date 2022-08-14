package com.godot.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    // Root node
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // Add to tire tree
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("Load sensitive words file failed!" + e.getMessage());
        }

    }

    // Add a word into Tire Tree
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); ++i) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // Init sub node
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;

            // Set end flag
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * Filter Sensitive Word
     *
     * @param text Source text
     * @return Text after filter
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // ptr 1 2 3
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {
            char c = text.charAt(position);

            // Skip symbol */.☆
            if (isSymbol(c)) {
                // if tempNode at root node(empty), add symbol to result, begin get a Step.
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // whatever position step
                position++;
                continue;
            }


            // Check next node
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // Not a STW begin with c
                sb.append(text.charAt(begin));
                // step
                position = ++begin;
                // retrieve tempNode to root
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // Is a STW, replace from begin to position
                sb.append(REPLACEMENT);

                begin = ++position;
                tempNode = rootNode;
            } else {
                // not null and not end , continue
                if (position < text.length() - 1) {
                    position++;
                }
            }

        }

        // link left string
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // Judge is or not Symbol
    private boolean isSymbol(Character c) {
        // East char section/scope 0x2e80~0xx9fff
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // Trie Tree
    private class TrieNode {

        // Key word flag
        private boolean isKeywordEnd = false;
        // Child node (key-char, value-node)
        private Map<Character, TrieNode> subNodes = new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // Add sub node
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // Get sub node
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }


}
