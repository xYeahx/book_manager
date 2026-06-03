package com.example.bms.service;

import com.example.bms.model.Message;

import java.util.List;

public interface MessageService {
    Integer sendMessage(Message message);

    Integer markAsRead(Integer messageid);

    List<Message> getMessagesByUserid(Integer userid, int page, int limit);

    Integer getCountByUserid(Integer userid);

    Integer getUnreadCountByUserid(Integer userid);
}
