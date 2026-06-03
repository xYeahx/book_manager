package com.example.bms.service.impl;

import com.example.bms.mapper.OperationLogMapper;
import com.example.bms.model.OperationLog;
import com.example.bms.service.OperationLogService;
import com.example.bms.utils.MyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public Integer addLog(OperationLog log) {
        return operationLogMapper.insert(log);
    }

    @Override
    public List<OperationLog> queryLogsByPage(Map<String, Object> params) {
        int begin = (Integer) params.getOrDefault("begin", 0);
        int size = (Integer) params.getOrDefault("size", 10);
        return operationLogMapper.selectByPage(begin, size);
    }

    @Override
    public Integer getCountBySearch(Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        return operationLogMapper.selectCount(params);
    }
}
