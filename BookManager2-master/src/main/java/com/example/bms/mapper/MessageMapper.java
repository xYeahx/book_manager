package com.example.bms.mapper;

import com.example.bms.model.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MessageMapper {
    int insert(Message record);

    int updateByPrimaryKeySelective(Message record);

    Message selectByPrimaryKey(Integer messageid);

    List<Message> selectByUserid(@Param("begin") Integer begin, @Param("size") Integer size, @Param("userid") Integer userid);

    Integer selectCountByUserid(Integer userid);

    Integer selectUnreadCountByUserid(Integer userid);
}
