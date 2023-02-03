package com.app.scrumble.model.scrapbook;

import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Comment {

    private final long id;
    private final long timeStamp;
    private final String content;
    private final User author;

    private List<Comment> children;

    public Comment(long id, long timeStamp, String content, User author){
        this(id, timeStamp, content, author, null);
    }

    public Comment(long id, long timeStamp, String content, User author, List<Comment> children){
        this.id = id;
        this.timeStamp = timeStamp;
        this.content = content;
        this.author = author;
        this.children = children;
    }

    public void addChildren(List<Comment> children){
        this.children = children;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public long getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getContent(){
        return this.content;
    }

    public User getAuthor() {
        return author;
    }

    public boolean hasChildren(){
        return children != null && children.size() > 0;
    }

    public int getChildCount(){
        return children == null ? 0 : children.size();
    }

    public long getID() {
        return id;
    }

    public int getDepth(Comment comment) {
        return getDepth(this, comment, 0);
    }

    private int getDepth(Comment root, Comment comment, int depth) {
        if (root == comment) {
            return depth;
        }
        if(root.getChildren() != null){
            for (Comment reply : root.getChildren()) {
                int replyDepth = getDepth(reply, comment, depth + 1);
                if (replyDepth != -1) {
                    return replyDepth;
                }
            }
        }
        return -1;
    }

    public List<Comment> toList() {
        List<Comment> list = new ArrayList<>();
        toList(this, list);
        return list;
    }

    private void toList(Comment comment, List<Comment> list) {
        list.add(comment);
        if(comment.getChildren() != null){
            for (Comment reply : comment.getChildren()) {
                toList(reply, list);
            }
        }
    }

    public List<Comment> toList(int maxDepth) {
        List<Comment> list = new ArrayList<>();
        toList(this, list, 0, maxDepth);
        return list;
    }

    private void toList(Comment comment, List<Comment> list, int depth, int maxDepth) {
        if (depth <= maxDepth) {
            list.add(comment);
        }
        if(comment.getChildren() != null){
            for (Comment reply : comment.getChildren()) {
                toList(reply, list, depth + 1, maxDepth);
            }
        }
    }

    public Comment findCommentById(long id) {
        return findCommentById(this, id);
    }

    private Comment findCommentById(Comment root, long id) {
        if (root.getId() == id) {
            return root;
        }
        if(root.getChildren() != null){
            for (Comment reply : root.getChildren()) {
                Comment foundComment = findCommentById(reply, id);
                if (foundComment != null) {
                    return foundComment;
                }
            }
        }
        return null;
    }
}
