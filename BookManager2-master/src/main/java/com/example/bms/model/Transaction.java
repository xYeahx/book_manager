package com.example.bms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Transaction implements Serializable {
    private Integer transactionId;
    private Integer userId;
    private String type;       // recharge / deposit / refund / fine
    private BigDecimal amount;
    private String description;
    private Date createTime;

    // For display
    private String createTimeStr;

    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getCreateTimeStr() { return createTimeStr; }
    public void setCreateTimeStr(String createTimeStr) { this.createTimeStr = createTimeStr; }
}
