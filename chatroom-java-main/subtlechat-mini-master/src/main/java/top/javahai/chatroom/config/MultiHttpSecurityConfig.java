package top.javahai.chatroom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import top.javahai.chatroom.entity.Admin;
import top.javahai.chatroom.entity.RespBean;
import top.javahai.chatroom.entity.User;
import top.javahai.chatroom.service.impl.AdminServiceImpl;
import top.javahai.chatroom.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@EnableWebSecurity
public class MultiHttpSecurityConfig {
    //密码加密方案
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    @Order(1)
    public static class AdminSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        AdminServiceImpl adminService;
        @Autowired
        VerificationCodeFilter verificationCodeFilter;
        @Autowired
        SimpMessagingTemplate simpMessagingTemplate;
        @Autowired
        MyAuthenticationFailureHandler myAuthenticationFailureHandler;
        @Autowired
        MyLogoutSuccessHandler myLogoutSuccessHandler;

        //用户名和密码验证服务
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(adminService);
        }

        //忽略"/login","/verifyCode"请求,该请求不需要进入Security的拦截器
        @Override
        public void configure(WebSecurity web) throws Exception {
            //web.ignoring().antMatchers("/api/**");
            web.ignoring().antMatchers("groupMsgContent/deleteMessage", "/css/**", "/fonts/**", "/img/**", "/js/**", "/favicon.ico", "/index.html", "/admin/login", "/admin/mailVerifyCode");
        }

        //http请求验证和处理规则,响应处理的配置
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //将验证码过滤器添加在用户名密码过滤器的前面,以便先进行验证码验证
            http.addFilterBefore(verificationCodeFilter, UsernamePasswordAuthenticationFilter.class);
            //指定只有匹配"/admin/**"路径的请求才会进行后续的授权配置
            http.antMatcher("/admin/**").authorizeRequests()
                    .anyRequest().authenticated()//表示所有请求都需要进行认证,即用户必须登录才能访问
                    .and()
                    .formLogin()//配置基于表单的登录
                    .usernameParameter("username")//指定登录表单中用户名的参数名
                    .passwordParameter("password")//指定登录表单中密码的参数名
                    .loginPage("/admin/login")//指定登录页面的URL
                    .loginProcessingUrl("/admin/doLogin")//指定处理登录请求的URL
                    //配置登录成功的处理器,以便返回自定义的JSON响应
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException {
                            resp.setContentType("application/json;charset=utf-8");//设置响应的内容类型为JSON格式,并指定字符集为UTF-8
                            PrintWriter out = resp.getWriter();
                            Admin admin = (Admin) authentication.getPrincipal();//从Authentication对象中获取已认证的用户信息,这里假设用户信息是Admin类型的
                            admin.setPassword(null);//为了安全起见,将管理员的密码设为null,避免密码泄露
                            RespBean ok = RespBean.ok("登录成功", admin);//创建一个成功的响应对象,其中包含了登录成功的消息和管理员信息
                            String s = new ObjectMapper().writeValueAsString(ok);//将响应对象转换为JSON格式的字符串
                            out.write(s);//将JSON格式的字符串写入响应输出流
                            out.flush();//刷新输出流,确保数据被写入到响应中
                            out.close();
                        }
                    })
                    //失败处理
                    .failureHandler(myAuthenticationFailureHandler)//配置登录失败的处理器
                    .permitAll()//返回值直接返回//指定登录页面和处理登录请求的URL不需要身份验证,允许所有用户访问
                    .and()
                    //登出处理
                    .logout()//配置登出操作
                    .logoutUrl("/admin/logout")//指定登出的URL
                    .logoutSuccessHandler(myLogoutSuccessHandler)//配置登出成功后的处理器
                    .permitAll()
                    .and()
                    .csrf().disable()//禁用CSRF防护,以便于调试
                    //没有认证时,在这里处理结果,不进行重定向到login页
                    //配置异常处理
                    //指定当请求未经身份验证时的处理方式，这里直接返回401状态码。
                    .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
                        @Override
                        public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) {
                            httpServletResponse.setStatus(401);
                        }
                    });
        }
    }

    @Configuration
    @Order(2)
    public static class UserSecurityConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        UserServiceImpl userService;
        @Autowired
        VerificationCodeFilter verificationCodeFilter;
        @Autowired
        SimpMessagingTemplate simpMessagingTemplate;
        @Autowired
        MyAuthenticationFailureHandler myAuthenticationFailureHandler;

        //验证服务
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userService);
        }

//    //密码加密
//    @Bean
//    PasswordEncoder passwordEncoder(){
//      return new BCryptPasswordEncoder();
//    }


        //忽略"/login","/verifyCode"请求,该请求不需要进入Security的拦截器
        @Override
        public void configure(WebSecurity web) throws Exception {
            //web.ignoring().antMatchers("/api/**");
            web.ignoring().antMatchers("/login", "/verifyCode", "/file", "/ossFileUpload", "/user/register", "/user/checkUsername", "/user/checkNickname");
        }

        //登录验证
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //将验证码过滤器添加在用户名密码过滤器的前面,以便先进行验证码验证
            http.addFilterBefore(verificationCodeFilter, UsernamePasswordAuthenticationFilter.class);
            http.authorizeRequests()
                    .anyRequest().authenticated()//表示所有请求都需要进行认证,即用户必须登录才能访问
                    .and()
                    .formLogin()//配置基于表单的登录
                    .usernameParameter("username")//指定登录表单中用户名的参数名
                    .passwordParameter("password")//指定登录表单中密码的参数名
                    .loginPage("/login")//指定登录页面的URL
                    .loginProcessingUrl("/doLogin")//指定处理登录请求的URL
                    //成功处理
                    //配置登录成功的处理器.在这个处理器中,设置了响应的内容类型为JSON,并向客户端返回登录成功的消息以及用户的信息;另外,更新了用户的在线状态,并通过WebSocket广播系统通知消息
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException {
                            resp.setContentType("application/json;charset=utf-8");//设置响应的内容类型为JSON格式,并指定字符集为UTF-8
                            PrintWriter out = resp.getWriter();
                            User user = (User) authentication.getPrincipal();//从Authentication对象中获取已认证的用户信息,这里假设用户信息是User类型的
                            user.setPassword(null);//为了安全起见,将用户的密码设为null,避免密码泄露
                            req.getSession().setAttribute("loggedInUser", user);
                            //更新用户状态为在线
                            userService.setUserStateToOn(user.getId());//调用userService中的方法将用户状态设置为在线
                            user.setUserStateId(1);//设置用户状态为在线状态
                            //广播系统通知消息
                            //使用WebSocket向指定的主题发送系统通知消息,通知其他用户有用户进入了聊天室
                            simpMessagingTemplate.convertAndSend("/topic/notification", "系统消息：用户【" + user.getNickname() + "】进入了聊天室");
                            RespBean ok = RespBean.ok("登录成功", user);//创建一个成功的响应对象,其中包含了登录成功的消息和用户信息
                            String s = new ObjectMapper().writeValueAsString(ok);//将响应对象转换为JSON格式的字符串
                            out.write(s);//将JSON格式的字符串写入响应输出流
                            out.flush();//刷新输出流,确保数据被写入到响应中
                            out.close();
                        }
                    })
                    //失败处理
                    .failureHandler(myAuthenticationFailureHandler)//配置登录失败的处理器
                    .permitAll()//返回值直接返回//指定登录页面和处理登录请求的URL不需要身份验证,允许所有用户访问
                    .and()
                    //登出处理
                    .logout()//配置登出操作
                    .logoutUrl("/logout")//指定登出的URL
                    //配置登出成功后的处理器。在这个处理器中，更新了用户的离线状态，并通过WebSocket广播系统通知消息
                    .logoutSuccessHandler(new LogoutSuccessHandler() {
                        @Override
                        public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException, ServletException {
                            //更新用户状态为离线
                            User user = (User) authentication.getPrincipal();
                            userService.setUserStateToLeave(user.getId());
                            //广播系统消息
                            simpMessagingTemplate.convertAndSend("/topic/notification", "系统消息：用户【" + user.getNickname() + "】退出了聊天室");
                            resp.setContentType("application/json;charset=utf-8");
                            PrintWriter out = resp.getWriter();
                            out.write(new ObjectMapper().writeValueAsString(RespBean.ok("成功退出！")));
                            out.flush();
                            out.close();
                        }
                    })
                    .permitAll()
                    .and()
                    .csrf().disable()//禁用CSRF防护,以便于调试
                    //没有认证时,在这里处理结果,不进行重定向到login页
                    //配置异常处理
                    //指定当请求未经身份验证时的处理方式，这里直接返回401状态码。
                    .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
                        @Override
                        public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                            httpServletResponse.setStatus(401);
                        }
                    });
        }
    }
}