package com.example.bms.service;

import com.example.bms.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    User login(User user);

    User getUserByUsername(String username);

    void saveUser(String token, User user);

    User getUser(String token);

    void removeUser(String token);

    Integer register(String username, String password, Byte isadmin);

    void setPassword(Integer id, String password);

    Integer getCount();

    List<User> queryUsers();

    int getSearchCount(Map<String, Object> searchParam);

    List<User> searchUsersByPage(Map<String, Object> params);

    Integer addUser(User user);

    Integer deleteUser(User user);

    Integer deleteUsers(List<User> users);

    Integer updateUser(User user);

    Integer getSearchCountWithRole(Map<String, Object> params);

    List<User> searchUsersByPageWithRole(Map<String, Object> params);

    User getUserById(Integer userid);

    int countByRole(Byte isadmin);
}
