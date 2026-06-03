package com.example.bms.model;

import java.util.Date;

public class OperationLog {
    private Integer logid;
    private Integer userId;
    private String operatorName;
    private Byte operatorRole;
    private String operationType;
    private String targetName;
    private String detail;
    private Date operateTime;

    public Integer getLogid() { return logid; }
    public void setLogid(Integer logid) { this.logid = logid; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public Byte getOperatorRole() { return operatorRole; }
    public void setOperatorRole(Byte operatorRole) { this.operatorRole = operatorRole; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Date getOperateTime() { return operateTime; }
    public void setOperateTime(Date operateTime) { this.operateTime = operateTime; }
}
