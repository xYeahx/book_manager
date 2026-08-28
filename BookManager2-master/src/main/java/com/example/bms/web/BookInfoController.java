package com.example.bms.web;

import com.example.bms.model.BookInfo;
import com.example.bms.model.OperationLog;
import com.example.bms.model.User;
import com.example.bms.exception.BusinessException;
import com.example.bms.exception.ErrorCode;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import com.example.bms.service.BookInfoService;
import com.example.bms.service.OperationLogService;
import com.example.bms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/bookInfo")
public class BookInfoController {

    @Autowired
    BookInfoService bookInfoService;

    @Autowired
    OperationLogService operationLogService;

    @Autowired
    UserService userService;

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

    // 获取图书数量
    @GetMapping(value = "/getCount")
    public Integer getCount(){
        return bookInfoService.getCount();
    }

    // 查询所有图书信息
    @GetMapping(value = "/queryBookInfos")
    public List<BookInfo> queryBookInfos(){
        return bookInfoService.queryBookInfos();
    }

    // 分页搜索查询图书信息 params: {page, limit, bookname, bookauthor, booktypeid}
    @GetMapping(value = "/queryBookInfosByPage")
    public Map<String, Object> queryBookInfosByPage(@RequestParam Map<String, Object> params){
        MyUtils.parsePageParams(params);
        int count = bookInfoService.getSearchCount(params);
        List<BookInfo> bookInfos = bookInfoService.searchBookInfosByPage(params);
        return MyResult.getListResultMap(0, "success", count, bookInfos);
    }

    // 添加图书信息
    @PostMapping(value = "/addBookInfo")
    public Integer addBookInfo(@RequestBody BookInfo bookInfo, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookInfoService.addBookInfo(bookInfo);
        if (result > 0) {
            recordLog(token, "新增图书", bookInfo.getBookname());
        }
        return result;
    }

    // 删除图书信息
    @DeleteMapping(value = "/deleteBookInfo")
    public Integer deleteBookInfo(@RequestBody BookInfo bookInfo, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookInfoService.deleteBookInfo(bookInfo);
        if (result > 0) {
            recordLog(token, "删除图书", bookInfo.getBookname());
        }
        return result;
    }

    // 删除一些图书信息
    @DeleteMapping(value = "/deleteBookInfos")
    public Integer deleteBookInfos(@RequestBody List<BookInfo> bookInfos, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookInfoService.deleteBookInfos(bookInfos);
        if (result > 0) {
            recordLog(token, "批量删除图书", "共" + bookInfos.size() + "本");
        }
        return result;
    }

    // 更新图书信息（含下架/上架检测）
    @PutMapping(value = "/updateBookInfo")
    public Integer updateBookInfo(@RequestBody BookInfo bookInfo, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookInfoService.updateBookInfo(bookInfo);
        if (result > 0) {
            try {
                User user = userService.getUser(token);
                if (user != null) {
                    OperationLog log = new OperationLog();
                    log.setUserId(user.getUserid());
                    log.setOperatorName(user.getUsername());
                    if (bookInfo.getIsoffshelf() != null && bookInfo.getIsoffshelf() == 1) {
                        log.setOperationType("下架图书");
                    } else if (bookInfo.getIsoffshelf() != null && bookInfo.getIsoffshelf() == 0) {
                        log.setOperationType("上架图书");
                    } else {
                        log.setOperationType("修改图书信息");
                    }
                    log.setTargetName(bookInfo.getBookname());
                    log.setOperateTime(new Date());
                    operationLogService.addLog(log);
                    System.out.println("[操作日志] " + user.getUsername() + " → " + log.getOperationType() + " → " + bookInfo.getBookname());
                } else {
                    System.out.println("[操作日志] 警告：无法获取用户信息，token=" + token);
                }
            } catch (Exception e) {
                System.out.println("[操作日志] 记录失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @GetMapping(value = "/bookCountByType")
    public Map<String, Object> getBookCountByType() {
        List<Map<String, Object>> stats = bookInfoService.getBookCountByType();
        return MyResult.getListResultMap(0, "success", stats.size(), stats);
    }

    // 读者端查询：排除已下架图书
    @GetMapping(value = "/queryBooksForReader")
    public Map<String, Object> queryBooksForReader(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        List<BookInfo> books = bookInfoService.queryBookInfosForReader(page, limit);
        int count = bookInfoService.getCount();
        return MyResult.getListResultMap(0, "success", count, books);
    }
}

