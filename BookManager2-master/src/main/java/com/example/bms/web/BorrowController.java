package com.example.bms.web;

import com.example.bms.model.BookInfo;
import com.example.bms.model.Borrow;
import com.example.bms.model.OperationLog;
import com.example.bms.model.SystemConfig;
import com.example.bms.model.User;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import com.example.bms.exception.NotEnoughException;
import com.example.bms.exception.OperationFailureException;
import com.example.bms.service.BookInfoService;
import com.example.bms.service.BorrowService;
import com.example.bms.service.OperationLogService;
import com.example.bms.service.SystemConfigService;
import com.example.bms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/borrow")
public class BorrowController {

    @Autowired
    BorrowService borrowService;
    @Autowired
    BookInfoService bookInfoService;
    @Autowired
    UserService userService;
    @Autowired
    OperationLogService operationLogService;
    @Autowired
    SystemConfigService systemConfigService;

    private int getConfigInt(String key, int defaultValue) {
        try {
            List<SystemConfig> configs = systemConfigService.getAll();
            for (SystemConfig config : configs) {
                if (key.equals(config.getConfigKey())) {
                    int value = Integer.parseInt(config.getConfigValue());
                    System.out.println("[系统配置] " + key + " = " + value);
                    return value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[系统配置] " + key + " 使用默认值: " + defaultValue);
        return defaultValue;
    }

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

    // 分页查询借阅 params: {page, limit, userid, bookid}
    @RequestMapping(value = "/queryBorrowsByPage")
    public Map<String, Object> queryBorrowsByPage(@RequestParam Map<String, Object> params){
        MyUtils.parsePageParams(params);
        int count = borrowService.getSearchCount(params);
        List<Borrow> borrows = borrowService.searchBorrowsByPage(params);
        return MyResult.getListResultMap(0, "success", count, borrows);
    }

    // 添加借阅
    @RequestMapping(value = "/addBorrow")
    public Integer addBorrow(@RequestBody Borrow borrow){
        return borrowService.addBorrow(borrow);
    }

    // 获得数量
    @RequestMapping(value = "/getCount")
    public Integer getCount(){
        return borrowService.getCount();
    }

    // 删除借阅
    @RequestMapping(value = "/deleteBorrow")
    public Integer deleteBorrow(@RequestBody Borrow borrow, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = borrowService.deleteBorrow(borrow);
        if (result > 0) {
            recordLog(token, "删除借阅记录", borrow.getBookname());
        }
        return result;
    }

    // 删除一些借阅
    @RequestMapping(value = "/deleteBorrows")
    public Integer deleteBorrows(@RequestBody List<Borrow> borrows, @RequestHeader(value = "X-Token", required = false) String token){
        Integer result = borrowService.deleteBorrows(borrows);
        if (result > 0 && borrows != null) {
            StringBuilder names = new StringBuilder();
            for ( Borrow b : borrows) {
                if (b.getBookname() != null) names.append(b.getBookname()).append(",");
            }
            recordLog(token, "批量删除借阅记录", names.length() > 0 ? names.substring(0, names.length()-1) : "多条记录");
        }
        return result;
    }

    // 更新借阅
    @RequestMapping(value = "/updateBorrow")
    public Integer updateBorrow(@RequestBody Borrow borrow){
        return borrowService.updateBorrow(borrow);
    }

    // 借书
    @RequestMapping(value = {"/borrowBook", "/reader/borrowBook"})
    @Transactional
    public Integer borrowBook(Integer userid, Integer bookid, @RequestHeader(value = "X-Token", required = false) String token){
        try{
            int borrowDays = getConfigInt("borrow_days", 14);
            int maxBorrowCount = getConfigInt("max_borrow_count", 5);

            BookInfo theBook = bookInfoService.queryBookInfoById(bookid);

            if(theBook == null) {
                throw new NullPointerException("图书" + bookid + "不存在");
            } else if(theBook.getIsborrowed() == 1) {
                throw new NotEnoughException("图书" + bookid + "库存不足（已经被借走）");
            } else if(theBook.getIsoffshelf() != null && theBook.getIsoffshelf() == 1) {
                throw new NotEnoughException("图书" + theBook.getBookname() + "已下架，无法借阅");
            }

            int currentBorrowCount = borrowService.countCurrentBorrows(userid);
            if(currentBorrowCount >= maxBorrowCount) {
                throw new NotEnoughException("您已达到最大借阅数量限制（" + maxBorrowCount + "本），请先归还部分图书");
            }

            BookInfo bookInfo = new BookInfo();
            bookInfo.setBookid(bookid);
            bookInfo.setIsborrowed((byte) 1);
            Integer res2 = bookInfoService.updateBookInfo(bookInfo);
            if(res2 == 0) throw new OperationFailureException("图书" + bookid + "更新被借信息失败");

            Borrow borrow = new Borrow();
            borrow.setUserid(userid);
            borrow.setBookid(bookid);
            borrow.setBorrowtime(new Date(System.currentTimeMillis()));
            borrow.setDuetime(new Date(System.currentTimeMillis() + (long)borrowDays * 24 * 60 * 60 * 1000));
            Integer res1 = borrowService.addBorrow2(borrow);
            if(res1 == 0) throw new OperationFailureException("图书" + bookid + "添加借阅记录失败");

            recordLog(token, "借出图书", theBook.getBookname());

        } catch (Exception e) {
            System.out.println("发生异常，进行手动回滚");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 还书
    @RequestMapping(value = {"/returnBook", "/reader/returnBook"})
    @Transactional
    public Integer returnBook(Integer borrowid, Integer bookid, @RequestHeader(value = "X-Token", required = false) String token){
        try {
            BookInfo theBook = bookInfoService.queryBookInfoById(bookid);
            Borrow theBorrow = borrowService.queryBorrowsById(borrowid);

            if(theBook == null) {
                throw new NullPointerException("图书" + bookid + "不存在");
            } else if(theBorrow == null) {
                throw new NullPointerException("借书记录" + bookid + "不存在");
            } else if(theBorrow.getReturntime() != null) {
                throw new NotEnoughException("图书" + bookid + "已经还过了");
            }

            BookInfo bookInfo = new BookInfo();
            bookInfo.setBookid(bookid);
            bookInfo.setIsborrowed((byte) 0);
            Integer res2 = bookInfoService.updateBookInfo(bookInfo);
            if(res2 == 0) throw new OperationFailureException("图书" + bookid + "更新被借信息失败");

            Borrow borrow = new Borrow();
            borrow.setBorrowid(borrowid);
            borrow.setReturntime(new Date(System.currentTimeMillis()));
            Integer res1 = borrowService.updateBorrow2(borrow);
            if(res1 == 0) throw new OperationFailureException("图书" + bookid + "更新借阅记录失败");

            recordLog(token, "归还图书", theBook.getBookname());

        } catch (Exception e) {
            System.out.println("发生异常，进行手动回滚");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 续借
    @RequestMapping(value = "/renewBorrow")
    public Integer renewBorrow(Integer borrowid, Integer bookid, @RequestHeader(value = "X-Token", required = false) String token){
        try {
            int maxRenewCount = getConfigInt("max_renew_count", 1);
            int renewDays = getConfigInt("renew_days", 14);
            int borrowDays = getConfigInt("borrow_days", 14);

            Borrow theBorrow = borrowService.queryBorrowsById(borrowid);

            if(theBorrow == null) {
                return -2;
            }
            if(theBorrow.getReturntime() != null) {
                return -1;
            }

            if(theBorrow.getRenewcount() != null && theBorrow.getRenewcount() >= maxRenewCount) {
                return -4;
            }

            Date dueDate = theBorrow.getDuetime();
            if (dueDate == null) {
                Date borrowTime = theBorrow.getBorrowtime();
                dueDate = new Date(borrowTime.getTime() + (long)borrowDays * 24 * 60 * 60 * 1000);
            }
            if (new Date().after(dueDate)) {
                return -3;
            }

            long newDueTime = dueDate.getTime() + (long)renewDays * 24 * 60 * 60 * 1000;

            Borrow borrow = new Borrow();
            borrow.setBorrowid(borrowid);
            borrow.setRenewcount((theBorrow.getRenewcount() == null ? 0 : theBorrow.getRenewcount()) + 1);
            borrow.setDuetime(new Date(newDueTime));
            Integer res1 = borrowService.updateBorrow2(borrow);
            if(res1 == 0) throw new OperationFailureException("续借失败");

            recordLog(token, "续借图书", theBorrow.getBookname());

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    @GetMapping(value = "/weeklyStats")
    public Map<String, Object> getWeeklyStats(@RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) {
            return MyResult.getResultMap(420, "未登录");
        }
        if (currentUser.getIsadmin() == 0) {
            return MyResult.getResultMap(403, "无权限查看统计");
        }
        List<Map<String, Object>> stats = borrowService.getWeeklyStats();
        return MyResult.getListResultMap(0, "success", stats.size(), stats);
    }

}
