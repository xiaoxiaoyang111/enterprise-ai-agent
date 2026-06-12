package com.example.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agent.entity.User;
import org.apache.ibatis.annotations.Mapper;
//继承 MyBatis-Plus 的 BaseMapper，增删改查不用自己写了
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
