package com.example.bms.service.impl;

import com.example.bms.mapper.SystemConfigMapper;
import com.example.bms.model.SystemConfig;
import com.example.bms.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> getAll() {
        return systemConfigMapper.selectAll();
    }

    @Override
    public int update(SystemConfig config) {
        return systemConfigMapper.updateByKey(config);
    }

    @Override
    @Transactional
    public int batchUpdate(List<SystemConfig> configs) {
        int count = 0;
        for (SystemConfig config : configs) {
            SystemConfig existing = systemConfigMapper.selectByKey(config.getConfigKey());
            if (existing != null) {
                count += systemConfigMapper.updateByKey(config);
            } else {
                count += systemConfigMapper.insertConfig(config);
            }
        }
        return count;
    }
}
