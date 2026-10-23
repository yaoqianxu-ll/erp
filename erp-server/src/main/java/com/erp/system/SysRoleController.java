package com.erp.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.system.entity.SysRole;
import com.erp.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    public Result<IPage<SysRole>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword) {
        return Result.ok(roleService.pageRoles(page, size, keyword));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysRole> detail(@PathVariable Long id) {
        return Result.ok(roleService.detail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysRole role) {
        roleService.createRole(role, role.getMenuIds());
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        roleService.updateRole(id, role, role.getMenuIds());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }
}
