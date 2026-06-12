package com.example.agent.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agent.dto.UserDTO;
import com.example.agent.entity.User;
import com.example.agent.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
// 业务逻辑层
// 注册和登录的核心逻辑,面试官看代码时最关注的地方
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 注册逻辑
    public void register(UserDTO userDTO) {
        // 1. 检查用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码加密 (千万不能明文存数据库，使用 BCrypt)
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt()));

        // 3. 保存到数据库
        userMapper.insert(user);
    }

    // 登录逻辑
    public String login(UserDTO userDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码
        if (!BCrypt.checkpw(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 登录成功，生成一个 Token (这里用 UUID 代替)
        String token = IdUtil.fastSimpleUUID();

        // 4. 将 Token 存入 Redis，设置过期时间为 24 小时 (Token做为Key，UserId做为Value)
        stringRedisTemplate.opsForValue().set("login_token:" + token, user.getId().toString(), 24, TimeUnit.HOURS);

        return token;
    }
}
