package com.example.bms.service;

import com.example.bms.model.SystemConfig;
import java.util.List;

public interface SystemConfigService {
    List<SystemConfig> getAll();
    int update(SystemConfig config);
    int batchUpdate(List<SystemConfig> configs);
}
