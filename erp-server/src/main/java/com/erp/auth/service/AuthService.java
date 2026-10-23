package com.erp.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.auth.dto.LoginRequest;
import com.erp.common.BizException;
import com.erp.security.JwtUtil;
import com.erp.security.LoginUser;
import com.erp.system.entity.SysRole;
import com.erp.system.entity.SysUser;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysUserMapper;
import com.erp.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuService menuService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("该账号已被停用");
        }
        List<String> roleCodes = roleCodes(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(), roleCodes);
        String token = jwtUtil.generate(loginUser);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("roles", roleCodes);
        result.put("menus", menuService.menusOf(user.getId(), roleCodes));
        return result;
    }

    public Map<String, Object> me(LoginUser loginUser) {
        SysUser user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        List<String> roleCodes = roleCodes(user.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("roles", roleCodes);
        result.put("menus", menuService.menusOf(user.getId(), roleCodes));
        return result;
    }

    private List<String> roleCodes(Long userId) {
        return roleMapper.selectByUserId(userId).stream().map(SysRole::getCode).toList();
    }
}
