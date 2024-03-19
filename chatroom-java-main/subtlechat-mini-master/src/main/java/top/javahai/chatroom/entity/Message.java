package top.javahai.chatroom.entity;

import java.util.Date;

/**
 * 单聊的消息实体
 *
 * @author Hai
 * @date 2020/6/25 - 19:32
 */
public class Message {
    private String from;
    private String to;
    private String content;
    private Date createTime;
    private Date expireTime;
    private String fromNickname;
    private String fromUserProfile;
    private Integer messageTypeId;
    private boolean deleted;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public String getFromUserProfile() {
        return fromUserProfile;
    }

    public void setFromUserProfile(String fromUserProfile) {
        this.fromUserProfile = fromUserProfile;
    }

    public Integer getMessageTypeId() {
        return messageTypeId;
    }

    public void setMessageTypeId(Integer messageTypeId) {
        this.messageTypeId = messageTypeId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", fromNickname='" + fromNickname + '\'' +
                ", fromUserProfile='" + fromUserProfile + '\'' +
                ", messageTypeId=" + messageTypeId +
                '}';
    }
}