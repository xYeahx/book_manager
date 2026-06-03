package com.example.bms.service;

import com.example.bms.model.OperationLog;
import java.util.List;
import java.util.Map;

public interface OperationLogService {
    Integer addLog(OperationLog log);

    List<OperationLog> queryLogsByPage(Map<String, Object> params);

    Integer getCountBySearch(Map<String, Object> params);
}
