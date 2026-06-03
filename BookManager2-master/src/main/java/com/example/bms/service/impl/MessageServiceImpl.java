package com.example.bms.service.impl;

import com.example.bms.mapper.MessageMapper;
import com.example.bms.model.Message;
import com.example.bms.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Override
    public Integer sendMessage(Message message) {
        if (message.getCreatetime() == null) {
            message.setCreatetime(new Date());
        }
        if (message.getIsread() == null) {
            message.setIsread(0);
        }
        return messageMapper.insert(message);
    }

    @Override
    public Integer markAsRead(Integer messageid) {
        Message message = new Message();
        message.setMessageid(messageid);
        message.setIsread(1);
        return messageMapper.updateByPrimaryKeySelective(message);
    }

    @Override
    public List<Message> getMessagesByUserid(Integer userid, int page, int limit) {
        int begin = (page - 1) * limit;
        List<Message> messages = messageMapper.selectByUserid(begin, limit, userid);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Message msg : messages) {
            if (msg.getCreatetime() != null) {
                msg.setCreatetimestr(simpleDateFormat.format(msg.getCreatetime()));
            }
        }
        return messages;
    }

    @Override
    public Integer getCountByUserid(Integer userid) {
        return messageMapper.selectCountByUserid(userid);
    }

    @Override
    public Integer getUnreadCountByUserid(Integer userid) {
        return messageMapper.selectUnreadCountByUserid(userid);
    }
}
