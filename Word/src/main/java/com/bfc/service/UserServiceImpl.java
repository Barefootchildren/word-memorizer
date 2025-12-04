package com.bfc.service;

import com.bfc.entity.User;
import com.bfc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl {

    @Autowired
    private UserRepository userRepository;

    /**
     * 按用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 注册新用户（保存用户信息）
     */
    public User register(User user) {
        // 你可以在这里做用户名查重、密码加密等
        return userRepository.save(user);
    }

    /**
     * 校验用户名和密码（最简单写法，正式项目建议密码加密+安全校验）
     */
    public boolean checkPassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }

    // 你还可以扩展更多方法，比如删除用户、修改用户信息等

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
}
