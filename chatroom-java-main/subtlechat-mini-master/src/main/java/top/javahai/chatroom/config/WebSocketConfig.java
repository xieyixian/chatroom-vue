package top.javahai.chatroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Hai
 * @date 2020/6/16 - 23:31
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * 注册stomp站点
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //这个方法用于注册WebSocket端点。在这里，注册了一个名为"/ws/ep"的端点，并允许所有来源的跨域请求访问该端点
        //同时，还启用了SockJS，以便在不支持WebSocket的浏览器中使用WebSocket
        registry.addEndpoint("/ws/ep").setAllowedOrigins("*").withSockJS();
    }

    /**
     * 注册拦截"/topic","/queue"的消息
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //这个方法用于配置消息代理。在这里，启用了一个简单的消息代理，并配置了"/topic"和"/queue"作为消息目标前缀。
        //这意味着所有以"/topic"和"/queue"开头的消息将会被路由到消息代理，并转发给连接到这些目标的客户端
        registry.enableSimpleBroker("/topic", "/queue");
    }
}