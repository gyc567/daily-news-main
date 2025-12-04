package com.ll.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ll.news.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好Mapper接口
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
    // MyBatis-Plus 会自动提供基本的CRUD方法
    // 这里可以添加自定义的复杂查询方法
}