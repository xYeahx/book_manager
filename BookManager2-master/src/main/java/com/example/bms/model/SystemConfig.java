package com.example.bms.model;

public class SystemConfig {
    private Integer configId;
    private String configKey;
    private String configValue;
    private String configDesc;

    public Integer getConfigId() { return configId; }
    public void setConfigId(Integer configId) { this.configId = configId; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getConfigDesc() { return configDesc; }
    public void setConfigDesc(String configDesc) { this.configDesc = configDesc; }
}
