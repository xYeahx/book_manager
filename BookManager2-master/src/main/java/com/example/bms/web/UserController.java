package com.example.bms.web;

import com.example.bms.model.User;
import com.example.bms.model.SystemConfig;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import com.example.bms.utils.TokenProcessor;
import com.example.bms.service.UserService;
import com.example.bms.service.SystemConfigService;
import com.example.bms.service.TransactionService;
import com.example.bms.model.Transaction;
import com.example.bms.exception.BusinessException;
import com.example.bms.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    TransactionService transactionService;

    // 根据用户名查询用户身份（用于登录时自动识别）
    @GetMapping(value = "/checkRole")
    public Map<String, Object> checkRole(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.USERNAME_EMPTY);
        }
        User user = userService.getUserByUsername(username.trim());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return MyResult.success(user);
    }

    // 登录
    @RequestMapping(value = "/login")
    public Map<String, Object> login(@RequestBody User user) {
        User userObj = userService.login(user);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = TokenProcessor.getInstance().makeToken();
        userService.saveUser(token, userObj);
        return MyResult.getResultMap(200, "登录成功",
                new HashMap<String, String>() {{ put("token", token); }});
    }

    // 查看用户信息
    @RequestMapping(value = "/info")
    public Map<String, Object> info(String token) {
        User user = userService.getUser(token);
        if (user == null) {
            throw new BusinessException(ErrorCode.GET_USER_INFO_FAILED);
        }
        return MyResult.success(user);
    }

    // 退出登录
    @RequestMapping(value = "/logout")
    public Map<String, Object> logout(String token) {
        userService.removeUser(token);
        return MyResult.success();
    }

    // 注册
    @RequestMapping(value = "/register")
    public Integer register(String username, String password, Byte isadmin, String inviteCode) {
        if (isadmin != null && isadmin >= 1) {
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.MISSING_INVITE_CODE);
            }
            List<SystemConfig> configs = systemConfigService.getAll();
            String validCode = "BMS_ADMIN_2026";
            for (SystemConfig config : configs) {
                if ("invite_code".equals(config.getConfigKey())) {
                    validCode = config.getConfigValue();
                    break;
                }
            }
            if (!validCode.equals(inviteCode.trim())) {
                throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
            }
        }
        return userService.register(username, password, isadmin != null ? isadmin : (byte) 0);
    }

    // 修改密码
    @RequestMapping(value = {"/alterPassword", "reader/alterPassword"})
    public Integer alterPassword(Integer userid, String username, Byte isadmin, String oldPassword, String newPassword) {
        User userObj = new User();
        userObj.setUserid(userid);
        userObj.setUsername(username);
        userObj.setUserpassword(oldPassword);
        userObj.setIsadmin(isadmin);

        User user = userService.login(userObj);
        if (user == null) {
            throw new BusinessException(ErrorCode.PASSWORD_WRONG);
        }
        userService.setPassword(userObj.getUserid(), newPassword);
        return 1;
    }

    // 获得数量
    @GetMapping(value = "/getCount")
    public Integer getCount() {
        return userService.getCount();
    }

    // 查询所有用户
    @GetMapping(value = "/queryUsers")
    public List<User> queryUsers() {
        return userService.queryUsers();
    }

    // 分页查询用户
    @GetMapping(value = "/queryUsersByPage")
    public Map<String, Object> queryUsersByPage(@RequestParam Map<String, Object> params) {
        MyUtils.parsePageParams(params);
        int count = userService.getSearchCount(params);
        List<User> users = userService.searchUsersByPage(params);
        return MyResult.getListResultMap(0, "success", count, users);
    }

    // 根据当前登录用户角色过滤的分页查询（权限控制）
    @GetMapping(value = "/queryUsersByPageWithAuth")
    public Map<String, Object> queryUsersByPageWithAuth(@RequestParam Map<String, Object> params,
                                                         @RequestHeader(value = "X-Token", required = false) String token) {
        MyUtils.parsePageParams(params);
        User currentUser = userService.getUser(token);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        }
        Byte currentRole = currentUser.getIsadmin();
        if (currentRole == 2) {
            // super_admin: 不限制
        } else if (currentRole == 1) {
            params.put("maxRole", 0);
        } else {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        int count = userService.getSearchCountWithRole(params);
        List<User> users = userService.searchUsersByPageWithRole(params);
        return MyResult.getListResultMap(0, "success", count, users);
    }

    // 添加用户
    @PostMapping(value = "/addUser")
    public Integer addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    // 删除用户
    @DeleteMapping(value = "/deleteUser")
    public Integer deleteUser(@RequestBody User user, @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        if (user.getUserid().equals(currentUser.getUserid())) throw new BusinessException(ErrorCode.CANNOT_OPERATE_SELF);
        User target = userService.getUserById(user.getUserid());
        if (target == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (currentUser.getIsadmin() == 1 && target.getIsadmin() >= 1) throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        if (currentUser.getIsadmin() == 2 && target.getIsadmin() == 2) {
            int superAdminCount = userService.countByRole((byte) 2);
            if (superAdminCount <= 1) throw new BusinessException(ErrorCode.SUPER_ADMIN_MINIMUM);
        }
        return userService.deleteUser(user);
    }

    // 删除一些用户
    @DeleteMapping(value = "/deleteUsers")
    public Integer deleteUsers(@RequestBody List<User> users, @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        for (User user : users) {
            if (user.getUserid().equals(currentUser.getUserid())) throw new BusinessException(ErrorCode.CANNOT_OPERATE_SELF);
            User target = userService.getUserById(user.getUserid());
            if (target == null) continue;
            if (currentUser.getIsadmin() == 1 && target.getIsadmin() >= 1) throw new BusinessException(ErrorCode.PERMISSION_DENIED);
            if (currentUser.getIsadmin() == 2 && target.getIsadmin() == 2) {
                int superAdminCount = userService.countByRole((byte) 2);
                int targetCount = 0;
                for (User u : users) {
                    User t = userService.getUserById(u.getUserid());
                    if (t != null && t.getIsadmin() == 2) targetCount++;
                }
                if (superAdminCount <= targetCount) throw new BusinessException(ErrorCode.SUPER_ADMIN_MINIMUM);
            }
        }
        return userService.deleteUsers(users);
    }

    // 更新用户
    @RequestMapping(value = "/updateUser")
    public Integer updateUser(@RequestBody User user, @RequestHeader(value = "X-Token", required = false) String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        if (user.getUserid().equals(currentUser.getUserid())) {
            User original = userService.getUserById(user.getUserid());
            if (original != null && !original.getIsadmin().equals(user.getIsadmin())) throw new BusinessException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }
        User target = userService.getUserById(user.getUserid());
        if (target == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (currentUser.getIsadmin() == 1 && user.getIsadmin() != null && user.getIsadmin() >= 1) throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        if (currentUser.getIsadmin() == 2 && target.getIsadmin() == 2 && user.getIsadmin() != null && user.getIsadmin() < 2) {
            int superAdminCount = userService.countByRole((byte) 2);
            if (superAdminCount <= 1) throw new BusinessException(ErrorCode.SUPER_ADMIN_MINIMUM);
        }
        return userService.updateUser(user);
    }

    // get user balance
    @GetMapping(value = "/balance")
    public Map<String, Object> getBalance(@RequestHeader("X-Token") String token) {
        User user = userService.getUser(token);
        if (user == null) return MyResult.getResultMap(420, "Not logged in");
        User fullUser = userService.getUserById(user.getUserid());
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("balance", fullUser.getBalance());
        return MyResult.getResultMap(200, "success", data);
    }

        // recharge balance (admin can recharge anyone, reader can recharge self)
    @PostMapping(value = "/recharge")
    public Integer recharge(@RequestBody java.util.Map<String, Object> params, @RequestHeader("X-Token") String token) {
        User currentUser = userService.getUser(token);
        if (currentUser == null) return -1;
        Number userIdNum = (Number) params.get("userId");
        Integer userId = userIdNum != null ? userIdNum.intValue() : null;
        Number amountNum = (Number) params.get("amount");
        Double amountDouble = amountNum != null ? amountNum.doubleValue() : null;
        if (userId == null || amountDouble == null || amountDouble <= 0) return 0;
        // reader can only recharge self, admin can recharge anyone
        if (currentUser.getIsadmin() < 1) {
            if (!userId.equals(currentUser.getUserid())) return -3;
        }
        java.math.BigDecimal amount = java.math.BigDecimal.valueOf(amountDouble);
        return userService.recharge(userId, amount);
    }
// get transaction history
    @GetMapping(value = "/transactions")
    public java.util.List<Transaction> getTransactions(@RequestHeader("X-Token") String token) {
        User user = userService.getUser(token);
        if (user == null) return new java.util.ArrayList<>();
        return transactionService.getTransactionsByUserId(user.getUserid());
    }
}

