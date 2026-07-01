package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper
{
    /**
     * 根据OpenId查询用户
     * @param openId
     * @return
     */
    @Select("SELECT * FROM user WHERE openid = #{openId}")
    User getByOpenId(String openId);

    /**
     * 自动注册
     * @param user
     */
    void insert(User user);
}
