package top.niunaijun.blackbox.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * Trie 前缀树：支持插入单词、批量插入与按前缀逐字搜索，遇到单词结束返回匹配词。
 */
public class TrieTree {

    private final TrieNode root = new TrieNode();

    private static class TrieNode {
        char content;
        String word;
        boolean isEnd = false; // 是否一个单词的结尾
        List<TrieNode> children = new LinkedList<>();

        public TrieNode() {}

        public TrieNode(char content, String word) {
            this.content = content;
            this.word    = word;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof TrieNode) {
                return ((TrieNode) object).content == content;
            }
            return false;
        }

        public TrieNode nextNode(char content) {
            for (TrieNode childNode : children) {
                if (childNode.content == content)
                    return childNode;
            }
            return null;
        }
    }

    public void add(String word) {
        TrieNode current = root;
        StringBuilder wordBuilder = new StringBuilder();
        for (int index = 0; index < word.length(); ++index) {
            char content = word.charAt(index);
            wordBuilder.append(content);
            TrieNode node = new TrieNode(content, wordBuilder.toString());
            if (current.children.contains(node)) {
                current = current.nextNode(content);
            } else {
                current.children.add(node);
                current = node;
            }
            if (index == (word.length() - 1))
                current.isEnd = true;
        }
    }

    public void addAll(List<String> words) {
        for (String word : words) {
            add(word);
        }
    }

    /**
     * 从根按给定字符串逐字符查找，若中途遇到 isEnd 则返回该词（否则无匹配返回 null）。
     */
    public String search(String word) {
        TrieNode current = root;
        for (int index = 0; index < word.length(); ++index) {
            char content = word.charAt(index);
            TrieNode node = new TrieNode(content, null);
            if (current.children.contains(node))
                current = current.nextNode(content);
            else
                return null;
            if (current.isEnd)
                return current.word;
        }
        return null;
    }
}
