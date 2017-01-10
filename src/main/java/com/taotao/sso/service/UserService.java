package com.taotao.sso.service;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.sso.mapper.UserMapper;
import com.taotao.sso.pojo.User;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

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

}
