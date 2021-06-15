package com.lyd.mall.order.interceptor;

import com.lyd.common.constant.AuthServerConstant;
import com.lyd.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 20:32
 * @Email man021436@163.com
 * @Description: TODO
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean match = new AntPathMatcher().match("/order/order/status/**", request.getRequestURI());
        if (match){
            return true;
        }
        MemberResponseVo attribute = (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            loginUser.set(attribute);
            return true;
        }else {
            // 没登录请登录
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.mall.com/login.html");
            return false;
        }
    }
}
