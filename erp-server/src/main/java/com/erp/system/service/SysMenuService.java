package com.erp.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.system.entity.SysMenu;
import com.erp.system.entity.SysRoleMenu;
import com.erp.system.mapper.SysMenuMapper;
import com.erp.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    private final SysRoleMenuMapper roleMenuMapper;

    public List<SysMenu> tree() {
        List<SysMenu> menus = list(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    public List<SysMenu> menusOf(Long userId, List<String> roleCodes) {
        List<SysMenu> menus;
        if (roleCodes != null && roleCodes.contains("admin")) {
            menus = list(Wrappers.<SysMenu>lambdaQuery()
                    .eq(SysMenu::getVisible, 1)
                    .orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId));
        } else {
            menus = baseMapper.selectByUserId(userId);
        }
        return buildTree(menus);
    }

    public void createMenu(SysMenu menu) {
        menu.setId(null);
        menu.setParentId(menu.getParentId() == null ? 0L : menu.getParentId());
        menu.setChildren(null);
        save(menu);
    }

    public void updateMenu(Long id, SysMenu menu) {
        menu.setId(id);
        menu.setChildren(null);
        updateById(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        Long children = count(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("请先删除子菜单");
        }
        removeById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> map = new LinkedHashMap<>();
        menus.forEach(menu -> {
            menu.setChildren(new ArrayList<>());
            map.put(menu.getId(), menu);
        });
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            SysMenu parent = map.get(menu.getParentId());
            if (parent == null || menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        return roots;
    }
}
