package com.example.bms.service.impl;

import com.example.bms.mapper.UserMapper;
import com.example.bms.model.User;
import com.example.bms.service.UserService;
import com.example.bms.service.TransactionService;
import com.example.bms.model.Transaction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Resource
    private TransactionService transactionService;

    @Override
    public User login(User user) {
        return userMapper.selectByUsernameAndPassword(user.getUsername(), user.getUserpassword());
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void saveUser(String token, User user) {
        // 设置redisTemplate对象key的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // key是token，value是用户保存到redis中，超时时间1小时
        redisTemplate.opsForValue().set(token, user, 1, TimeUnit.HOURS);
    }

    @Override
    public User getUser(String token) {
        // 根据token得到user
        return (User) redisTemplate.opsForValue().get(token);
    }

    @Override
    public void removeUser(String token) {
        // 移除token
        redisTemplate.delete(token);
    }

    @Override
    public Integer register(String username, String password, Byte isadmin) {
        User tmp = userMapper.selectByUsername(username);
        if(tmp != null) return 0;  //账号重复

        User user = new User();
        user.setUsername(username);
        user.setUserpassword(password);
        user.setIsadmin(isadmin != null ? isadmin : (byte)0);
        return userMapper.insertSelective(user);
    }

    @Override
    public void setPassword(Integer id, String password) {
        User user = new User();
        user.setUserid(id);
        user.setUserpassword(password);
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public Integer getCount() {
        return userMapper.selectCount();
    }

    @Override
    public List<User> queryUsers() {
        return userMapper.selectAll();
    }

    @Override
    public int getSearchCount(Map<String, Object> params) {
        return userMapper.selectCountBySearch(params);
    }

    @Override
    public List<User> searchUsersByPage(Map<String, Object> params) {
        return userMapper.selectBySearch(params);
    }

    @Override
    public Integer addUser(User user) {
        return userMapper.insertSelective(user);
    }

    @Override
    public Integer deleteUser(User user) {
        return userMapper.deleteByPrimaryKey(user.getUserid());
    }

    @Override
    public Integer getSearchCountWithRole(Map<String, Object> params) {
        return userMapper.selectCountBySearchWithRole(params);
    }

    @Override
    public List<User> searchUsersByPageWithRole(Map<String, Object> params) {
        return userMapper.selectBySearchWithRole(params);
    }

    @Override
    public User getUserById(Integer userid) {
        return userMapper.selectByPrimaryKey(userid);
    }

    @Override
    public Integer deleteUsers(List<User> users) {
        int count = 0;
        for(User user : users) {
            count += deleteUser(user);
        }
        return count;
    }

    @Override
    public Integer updateUser(User user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public int countByRole(Byte isadmin) {
        return userMapper.countByRole(isadmin);
    }

    @Override
    public int updateBalance(Integer userid, java.math.BigDecimal balance) {
        return userMapper.updateBalance(userid, balance);
    }

    @Override
    public int recharge(Integer userid, java.math.BigDecimal amount) {
        User user = userMapper.selectByPrimaryKey(userid);
        if (user == null) return 0;
        java.math.BigDecimal newBalance = user.getBalance() == null ? java.math.BigDecimal.ZERO : user.getBalance();
        newBalance = newBalance.add(amount);
        userMapper.updateBalance(userid, newBalance);
        // record transaction
        Transaction t = new Transaction();
        t.setUserId(userid);
        t.setType("recharge");
        t.setAmount(amount);
        t.setDescription("Recharge");
        t.setCreateTime(new java.util.Date());
        transactionService.addTransaction(t);
        return 1;
    }
}
