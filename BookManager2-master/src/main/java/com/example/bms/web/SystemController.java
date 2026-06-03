package com.example.bms.web;

import com.example.bms.model.OperationLog;
import com.example.bms.model.SystemConfig;
import com.example.bms.service.OperationLogService;
import com.example.bms.service.SystemConfigService;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    OperationLogService operationLogService;
    @Autowired
    SystemConfigService systemConfigService;

    // ========== 操作日志接口 ==========

    @GetMapping("/logs/queryByPage")
    public Map<String, Object> queryLogs(@RequestParam Map<String, Object> params){
        MyUtils.parsePageParams(params);
        int count = operationLogService.getCountBySearch(params);
        List<OperationLog> logs = operationLogService.queryLogsByPage(params);
        return MyResult.getListResultMap(0, "success", count, logs);
    }

    @PostMapping("/logs/add")
    public Integer addLog(@RequestBody OperationLog log){
        if(log.getOperateTime() == null) log.setOperateTime(new Date());
        return operationLogService.addLog(log);
    }

    // ========== 系统设置接口 ==========

    @GetMapping("/config/getAll")
    public Map<String, Object> getAllConfig(){
        List<SystemConfig> configs = systemConfigService.getAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", configs);
        return result;
    }

    @PostMapping("/config/update")
    public Integer updateConfig(@RequestBody SystemConfig config){
        return systemConfigService.update(config);
    }

    @PostMapping("/config/batchUpdate")
    public Map<String, Object> batchUpdateConfig(@RequestBody List<SystemConfig> configs){
        int count = systemConfigService.batchUpdate(configs);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", count);
        return result;
    }
}
