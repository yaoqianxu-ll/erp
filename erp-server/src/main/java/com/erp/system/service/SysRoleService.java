package com.erp.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.system.entity.SysRole;
import com.erp.system.entity.SysRoleMenu;
import com.erp.system.entity.SysUserRole;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysRoleMenuMapper;
import com.erp.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public IPage<SysRole> pageRoles(long page, long size, String keyword) {
        var wrapper = Wrappers.<SysRole>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getName, keyword).or().like(SysRole::getCode, keyword));
        }
        wrapper.orderByAsc(SysRole::getId);
        IPage<SysRole> result = page(new Page<>(page, Math.min(size, 100)), wrapper);
        result.getRecords().forEach(this::fillMenus);
        return result;
    }

    public List<SysRole> listAll() {
        return list(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId));
    }

    public SysRole detail(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        fillMenus(role);
        return role;
    }

    @Transactional
    public void createRole(SysRole role, List<Long> menuIds) {
        checkCodeUnique(role.getCode(), null);
        role.setId(null);
        role.setDeleted(null);
        role.setStatus(role.getStatus() == null ? 1 : role.getStatus());
        save(role);
        saveMenus(role.getId(), menuIds);
    }

    @Transactional
    public void updateRole(Long id, SysRole role, List<Long> menuIds) {
        if (getById(id) == null) {
            throw new BizException("角色不存在");
        }
        checkCodeUnique(role.getCode(), id);
        role.setId(id);
        updateById(role);
        if (menuIds != null) {
            saveMenus(id, menuIds);
        }
    }

    @Transactional
    public void deleteRole(Long id) {
        if (id == 1L) {
            throw new BizException("内置管理员角色不可删除");
        }
        Long users = userRoleMapper.selectCount(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id));
        if (users != null && users > 0) {
            throw new BizException("该角色已分配给用户，无法删除");
        }
        removeById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
    }

    private void checkCodeUnique(String code, Long excludeId) {
        var wrapper = Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BizException("角色编码已存在");
        }
    }

    private void fillMenus(SysRole role) {
        List<Long> menuIds = roleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, role.getId()))
                .stream().map(SysRoleMenu::getMenuId).toList();
        role.setMenuIds(menuIds);
    }

    private void saveMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            menuIds.stream().distinct()
                    .forEach(menuId -> roleMenuMapper.insert(new SysRoleMenu(roleId, menuId)));
        }
    }
}
