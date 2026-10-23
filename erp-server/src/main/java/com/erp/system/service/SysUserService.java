package com.erp.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.system.entity.SysRole;
import com.erp.system.entity.SysUser;
import com.erp.system.entity.SysUserRole;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysUserMapper;
import com.erp.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<SysUser> pageUsers(long page, long size, String keyword, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getId);
        IPage<SysUser> result = page(new Page<>(page, Math.min(size, 100)), wrapper);
        result.getRecords().forEach(this::fillRoles);
        return result;
    }

    public SysUser detail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        fillRoles(user);
        return user;
    }

    @Transactional
    public void createUser(SysUser user, List<Long> roleIds) {
        long count = count(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BizException("登录账号已存在");
        }
        user.setId(null);
        user.setDeleted(null);
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        String rawPassword = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPassword));
        save(user);
        saveRoles(user.getId(), roleIds);
    }

    @Transactional
    public void updateUser(Long id, SysUser user, List<Long> roleIds) {
        SysUser existing = getById(id);
        if (existing == null) {
            throw new BizException("用户不存在");
        }
        long count = count(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, user.getUsername())
                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BizException("登录账号已存在");
        }
        user.setId(id);
        user.setPassword(StringUtils.hasText(user.getPassword())
                ? passwordEncoder.encode(user.getPassword()) : null);
        updateById(user);
        if (roleIds != null) {
            saveRoles(id, roleIds);
        }
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BizException("不能删除当前登录用户");
        }
        if (id == 1L) {
            throw new BizException("内置管理员账号不可删除");
        }
        removeById(id);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }

    public void resetPassword(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode("123456"));
        updateById(update);
    }

    public void toggleStatus(Long id, Integer status) {
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    private void fillRoles(SysUser user) {
        List<Long> roleIds = roleMapper.selectByUserId(user.getId()).stream()
                .map(SysRole::getId).toList();
        user.setRoleIds(roleIds);
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            roleIds.stream().distinct()
                    .forEach(roleId -> userRoleMapper.insert(new SysUserRole(userId, roleId)));
        }
    }
}
