package top.javahai.chatroom.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import top.javahai.chatroom.entity.User;

/**
 * @author Hai
 * @date 2020/6/16 - 22:56
 * 用户工具类
 */
public class UserUtil {
    /**
     * 获取当前登录用户实体
     *
     * @return
     */
    public static User getCurrentUser() {
        //用于获取当前用户的认证信息，其中包含了用户的身份信息
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}