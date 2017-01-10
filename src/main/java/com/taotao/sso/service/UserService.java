package com.taotao.sso.service;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.service.RedisService;
import com.taotao.sso.mapper.UserMapper;
import com.taotao.sso.pojo.User;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisService redisService;
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final String TOKENKEY = "TT_TOKEN_";
    
    private static final Integer TIME = 60 * 30;        //生存时间

    public Boolean check(String param, Integer type) {
        User record = new User();
        // 1-验证用户名；2-验证邮箱；3-验证手机号码
        switch (type) {
        case 1:
            record.setUsername(param);
            break;
        case 2:
            record.setPhone(param);
            break;
        case 3:
            record.setEmail(param);
            break;
        default:
            return null;
        }
        return this.userMapper.selectOne(record) == null;
    }

    public Boolean doRegister(User user) {
        // 初始化user
        user.setId(null);
        user.setCreated(new Date());
        user.setUpdated(user.getCreated());
        // 密码进行MD5加密处理
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));
        return this.userMapper.insert(user) == 1;
    }

    public String doLogin(String username, String password) throws Exception {
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record );
        if (user == null) {
            // 用户名不存在
            return null;
        }
        // 用户名存在,核对密码
        if (!StringUtils.equals(user.getPassword(), DigestUtils.md5Hex(password))) {
            // 密码不正确
            return null;
        }
        // 密码正确,将token存入redis
        String token = DigestUtils.md5Hex(username + System.currentTimeMillis());
        this.redisService.set(TOKENKEY + token, MAPPER.writeValueAsString(user), TIME);
        return token;
    }

    public User queryUserByToken(String token) {
        String str = this.redisService.get(TOKENKEY + token);
        if (StringUtils.isEmpty(str)) {
            // 登录超时
            return null;
        }
        // 已登录,重新设置token生存时间(访问时生存时间往后移)
        this.redisService.expire(TOKENKEY + token, TIME);
        // 反序列化字符串
        try {
            return MAPPER.readValue(str, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
