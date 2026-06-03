package com.example.bms.web;

import com.example.bms.model.User;
import com.example.bms.model.SystemConfig;
import com.example.bms.utils.MyResult;
import com.example.bms.utils.MyUtils;
import com.example.bms.utils.TokenProcessor;
import com.example.bms.service.UserService;
import com.example.bms.service.SystemConfigService;
import com.example.bms.utils.*;
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

    // 根据用户名查询用户身份（用于登录时自动识别）
    @GetMapping(value = "/checkRole")
    public Map<String, Object> checkRole(String username) {
        if (username == null || username.trim().isEmpty()) {
            return MyResult.getResultMap(420, "用户名不能为空");
        }
        User user = userService.getUserByUsername(username.trim());
        if (user == null) {
            return MyResult.getResultMap(404, "该用户不存在");
        }
        return MyResult.getResultMap(200, "查询成功", user);
    }

    // 登录
    @RequestMapping(value = "/login")
    public Map<String, Object> login(@RequestBody User user) {
        // 登录
        User userObj = userService.login(user);
        if(userObj == null) {   // 账号或密码错误
            // 返回结果对象
            return MyResult.getResultMap(420, "账号或密码错误");
        } else {    // 账号密码正确
            // 创建token
            String token = TokenProcessor.getInstance().makeToken();
            // 保存到Redis
            userService.saveUser(token, userObj);
            // 返回结果对象
            return MyResult.getResultMap(200, "登录成功",
                    new HashMap<String, String>(){{ put("token", token); }});
        }
    }

    // 查看用户信息
    @RequestMapping(value = "/info")
    public Map<String, Object> info(String token) {
        // 从redis中取用户
        User user = userService.getUser(token);
        if(user == null) {  // 获取失败
            return MyResult.getResultMap(420, "获取用户信息失败");
        } else {    // 获取成功
            return MyResult.getResultMap(200, "获取用户信息成功", user);
        }
    }

    // 退出登录
    @RequestMapping(value = "/logout")
    public Map<String, Object> logout(String token) {
        // 从redis中移除用户
        userService.removeUser(token);
        return MyResult.getResultMap(200, "退出登录成功" );
    }

    // 注册
    @RequestMapping(value = "/register")
    public Integer register(String username, String password, Byte isadmin, String inviteCode){
        // 如果请求的是管理员或超级管理员角色，校验邀请码
        if(isadmin != null && isadmin >= 1){
            if(inviteCode == null || inviteCode.trim().isEmpty()){
                return -1; // 缺少邀请码
            }
            // 从数据库读取配置的邀请码
            List<SystemConfig> configs = systemConfigService.getAll();
            String validCode = "BMS_ADMIN_2026"; // 默认值（兜底）
            for (SystemConfig config : configs) {
                if ("invite_code".equals(config.getConfigKey())) {
                    validCode = config.getConfigValue();
                    break;
                }
            }
            if(!validCode.equals(inviteCode.trim())){
                return -1; // 邀请码无效
            }
        }
        return userService.register(username, password, isadmin != null ? isadmin : (byte)0);
    }

    // 修改密码
    @RequestMapping(value = {"/alterPassword", "reader/alterPassword"})
    public Integer alterPassword(Integer userid, String username, Byte isadmin, String oldPassword, String newPassword){
        //检查旧密码是否正确
        User userObj = new User();
        userObj.setUserid(userid);
        userObj.setUsername(username);
        userObj.setUserpassword(oldPassword);
        userObj.setIsadmin(isadmin);

        User user = userService.login(userObj);
        if(user == null) {  //旧密码不正确
            return 0;
        } else {    //旧密码正确，设置新密码
            userService.setPassword(userObj.getUserid(), newPassword);
            return 1;
        }
    }

    // 获得数量
    @GetMapping(value = "/getCount")
    public Integer getCount(){
        return userService.getCount();
    }

    // 查询所有用户
    @GetMapping(value = "/queryUsers")
    public List<User> queryUsers(){
        return userService.queryUsers();
    }

    // 分页查询用户 params: {page, limit, username}
    @GetMapping(value = "/queryUsersByPage")
    public Map<String, Object> queryUsersByPage(@RequestParam Map<String, Object> params){
        MyUtils.parsePageParams(params);
        int count = userService.getSearchCount(params);
        List<User> users = userService.searchUsersByPage(params);
        return MyResult.getListResultMap(0, "success", count, users);
    }

    // 根据当前登录用户角色过滤的分页查询（权限控制）
    @GetMapping(value = "/queryUsersByPageWithAuth")
    public Map<String, Object> queryUsersByPageWithAuth(@RequestParam Map<String, Object> params, @RequestHeader(value = "X-Token", required = false) String token){
        MyUtils.parsePageParams(params);
        // 获取当前登录用户信息
        User currentUser = userService.getUser(token);
        if(currentUser == null){
            return MyResult.getResultMap(420, "未登录或会话已过期");
        }
        // 根据当前用户角色决定可管理的最大权限级别
        Byte currentRole = currentUser.getIsadmin();
        if(currentRole == 2){ // super_admin: 可以管理所有用户，不限制
            // 不加 maxRole 过滤
        } else if(currentRole == 1){ // admin: 只能管理读者(isAdmin <= 0)
            params.put("maxRole", 0);
        } else { // reader: 无权管理任何用户
            return MyResult.getListResultMap(403, "无权限访问", 0, new java.util.ArrayList<>());
        }
        int count = userService.getSearchCountWithRole(params);
        List<User> users = userService.searchUsersByPageWithRole(params);
        return MyResult.getListResultMap(0, "success", count, users);
    }

    // 添加用户
    @PostMapping(value = "/addUser")
    public Integer addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    // 删除用户
    @DeleteMapping(value = "/deleteUser")
    public Integer deleteUser(@RequestBody User user, @RequestHeader(value = "X-Token", required = false) String token){
        User currentUser = userService.getUser(token);
        if(currentUser == null) return -1;

        // 不能删除自己
        if(user.getUserid().equals(currentUser.getUserid())) return -2;

        // 获取目标用户信息
        User target = userService.getUserById(user.getUserid());
        if(target == null) return -1;

        // 普通管理员不能删除其他管理员和超级管理员
        if(currentUser.getIsadmin() == 1){
            if(target.getIsadmin() >= 1) return -3;
        }

        // 超级管理员可以删除其他超级管理员，但不能删除最后一个超管
        if(currentUser.getIsadmin() == 2 && target.getIsadmin() == 2){
            int superAdminCount = userService.countByRole((byte)2);
            if(superAdminCount <= 1) return -5; // 至少保留一个超管
        }

        return userService.deleteUser(user);
    }

    // 删除一些用户
    @DeleteMapping(value = "/deleteUsers")
    public Integer deleteUsers(@RequestBody List<User> users, @RequestHeader(value = "X-Token", required = false) String token){
        User currentUser = userService.getUser(token);
        if(currentUser == null) return -1;

        for(User user : users){
            // 不能批量删除自己
            if(user.getUserid().equals(currentUser.getUserid())) return -2;

            User target = userService.getUserById(user.getUserid());
            if(target == null) continue;

            // 普通管理员不能删除管理员和超管
            if(currentUser.getIsadmin() == 1 && target.getIsadmin() >= 1) return -3;

            // 超管不能删除最后一个超管（检查批量操作后是否还有超管）
            if(currentUser.getIsadmin() == 2 && target.getIsadmin() == 2){
                int superAdminCount = userService.countByRole((byte)2);
                int targetCount = 0;
                for(User u : users){
                    User t = userService.getUserById(u.getUserid());
                    if(t != null && t.getIsadmin() == 2) targetCount++;
                }
                if(superAdminCount <= targetCount) return -5; // 批量删除后会没有超管了
            }
        }
        return userService.deleteUsers(users);
    }

    // 更新用户
    @RequestMapping(value = "/updateUser")
    public Integer updateUser(@RequestBody User user, @RequestHeader(value = "X-Token", required = false) String token){
        User currentUser = userService.getUser(token);
        if(currentUser == null) return -1;

        // 不能修改自己的角色
        if(user.getUserid().equals(currentUser.getUserid())){
            User original = userService.getUserById(user.getUserid());
            if(original != null && !original.getIsadmin().equals(user.getIsadmin())) return -6;
        }

        // 获取目标用户原始信息
        User target = userService.getUserById(user.getUserid());
        if(target == null) return -1;

        // 普通管理员不能将用户升级为管理员或超管
        if(currentUser.getIsadmin() == 1){
            if(user.getIsadmin() != null && user.getIsadmin() >= 1) return -3;
        }

        // 超管可以将其他超管降级，但不能降级最后一个超管
        if(currentUser.getIsadmin() == 2 && target.getIsadmin() == 2){
            if(user.getIsadmin() != null && user.getIsadmin() < 2){
                int superAdminCount = userService.countByRole((byte)2);
                if(superAdminCount <= 1) return -5; // 至少保留一个超管
            }
        }

        return userService.updateUser(user);
    }
}
