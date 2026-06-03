package com.example.bms.web;

import com.example.bms.model.Message;
import com.example.bms.model.OperationLog;
import com.example.bms.model.User;
import com.example.bms.service.MessageService;
import com.example.bms.service.OperationLogService;
import com.example.bms.service.UserService;
import com.example.bms.utils.MyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/message")
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    OperationLogService operationLogService;

    private void recordLog(String token, String operation, String target) {
        try {
            User user = userService.getUser(token);
            if (user != null) {
                OperationLog log = new OperationLog();
                log.setUserId(user.getUserid());
                log.setOperatorName(user.getUsername());
                log.setOperationType(operation);
                log.setTargetName(target);
                log.setOperateTime(new Date());
                operationLogService.addLog(log);
            }
        } catch (Exception e) {}
    }

    @RequestMapping(value = "/sendMessage")
    public Map<String, Object> sendMessage(@RequestBody Message message, @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null || currentUser.getIsadmin() == 0) {
            return MyResult.getResultMap(403, "无权限发送消息");
        }
        Integer result = messageService.sendMessage(message);
        if (result > 0) {
            recordLog(token, "发送通知", message.getTitle());
            return MyResult.getResultMap(200, "发送成功");
        }
        return MyResult.getResultMap(420, "发送失败");
    }

    @GetMapping(value = "/queryMessagesByPage")
    public Map<String, Object> queryMessagesByPage(@RequestParam Integer userid, @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer limit,
                                                    @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) {
            return MyResult.getResultMap(420, "未登录");
        }
        int count = messageService.getCountByUserid(userid);
        List<Message> messages = messageService.getMessagesByUserid(userid, page, limit);
        return MyResult.getListResultMap(0, "success", count, messages);
    }

    @RequestMapping(value = "/markAsRead")
    public Integer markAsRead(Integer messageid, @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) {
            return -1;
        }
        return messageService.markAsRead(messageid);
    }

    @GetMapping(value = "/getUnreadCount")
    public Map<String, Object> getUnreadCount(@RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) {
            return MyResult.getResultMap(420, "未登录");
        }
        Integer count = messageService.getUnreadCountByUserid(currentUser.getUserid());
        return MyResult.getResultMap(200, "查询成功", count);
    }

    @GetMapping(value = "/sendReminder")
    public Map<String, Object> sendReminder(Integer userid, Integer bookid, String bookname,
                                            @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null || currentUser.getIsadmin() == 0) {
            return MyResult.getResultMap(403, "无权限发送催还信息");
        }
        Message message = new Message();
        message.setUserid(userid);
        message.setTitle("催还通知");
        message.setContent("您好，您借阅的图书《" + bookname + "》已临近或超过归还期限，请尽快归还。如有疑问请联系管理员。");
        Integer result = messageService.sendMessage(message);
        if (result > 0) {
            recordLog(token, "发送催还通知", bookname);
            return MyResult.getResultMap(200, "催还信息发送成功");
        }
        return MyResult.getResultMap(420, "催还信息发送失败");
    }
}
