package com.example.bms.mapper;

import com.example.bms.model.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OperationLogMapper {
    int insert(OperationLog record);

    List<OperationLog> selectByPage(@Param("begin") Integer begin, @Param("size") Integer size);

    Integer selectCount(Map<String, Object> params);
}
