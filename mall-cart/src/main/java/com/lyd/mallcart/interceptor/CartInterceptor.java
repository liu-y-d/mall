package com.lyd.mallcart.interceptor;

import com.lyd.common.constant.AuthServerConstant;
import com.lyd.common.constant.CartConstant;
import com.lyd.common.vo.MemberResponseVo;
import com.lyd.mallcart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:48
 * @Email man021436@163.com
 * @Description: 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标请求
 */
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * @Description: 在目标方法执行之前
     * @Param: [request, response, handler]
     * @return: boolean
     * @Author: Liuyunda
     * @Date: 2021/5/25
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberResponseVo != null){
            // 用户登录了
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null && cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }
        // 如果没有临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        // 目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * @Description: 业务执行之后,分配临时用户，让浏览器保存
     * @Param: [request, response, handler, modelAndView]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/25
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("mall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
