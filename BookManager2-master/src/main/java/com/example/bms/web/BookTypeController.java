package com.example.bms.web;

import com.example.bms.model.BookType;
import com.example.bms.model.OperationLog;
import com.example.bms.model.User;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import com.example.bms.service.BookTypeService;
import com.example.bms.service.OperationLogService;
import com.example.bms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/bookType")
public class BookTypeController {

    @Autowired
    BookTypeService bookTypeService;
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

    // 获得数量
    @GetMapping(value = "/getCount")
    public Integer getCount(){
        return bookTypeService.getCount();
    }

    // 查询所有类型
    @GetMapping(value = {"/queryBookTypes", "/reader/queryBookTypes"})
    public List<BookType> queryBookTypes(){
        return bookTypeService.queryBookTypes();
    }

    // 分页查询图书类型 params: {page, limit, booktypename}
    @GetMapping(value = "/queryBookTypesByPage")
    public Map<String, Object> queryBookTypesByPage(@RequestParam Map<String, Object> params){
        MyUtils.parsePageParams(params);
        int count = bookTypeService.getSearchCount(params);
        List<BookType> bookTypes = bookTypeService.searchBookTypesByPage(params);
        return MyResult.getListResultMap(0, "success", count, bookTypes);
    }

    // 添加类型
    @PostMapping(value = "/addBookType")
    public Integer addBookType(@RequestBody BookType bookType, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookTypeService.addBookType(bookType);
        if (result > 0) {
            recordLog(token, "添加图书类型", bookType.getBooktypename());
        }
        return result;
    }

    // 删除类型
    @DeleteMapping(value = "/deleteBookType")
    public Integer deleteBookType(@RequestBody BookType bookType, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookTypeService.deleteBookType(bookType);
        if (result > 0) {
            recordLog(token, "删除图书类型", bookType.getBooktypename());
        }
        return result;
    }

    // 删除一些类型
    @DeleteMapping(value = "/deleteBookTypes")
    public Integer deleteBookTypes(@RequestBody List<BookType> bookTypes, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookTypeService.deleteBookTypes(bookTypes);
        if (result > 0 && bookTypes != null) {
            StringBuilder names = new StringBuilder();
            for (BookType bt : bookTypes) {
                if (bt.getBooktypename() != null) names.append(bt.getBooktypename()).append(",");
            }
            recordLog(token, "批量删除图书类型", names.length() > 0 ? names.substring(0, names.length()-1) : "多个类型");
        }
        return result;
    }

    // 更新类型
    @PutMapping(value = "/updateBookType")
    public Integer updateBookType(@RequestBody BookType bookType, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = bookTypeService.updateBookType(bookType);
        if (result > 0) {
            recordLog(token, "编辑图书类型", bookType.getBooktypename());
        }
        return result;
    }
}
