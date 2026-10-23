package com.erp.product.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.product.entity.Category;
import com.erp.product.entity.Product;
import com.erp.product.mapper.CategoryMapper;
import com.erp.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private final ProductMapper productMapper;

    public List<Category> tree() {
        List<Category> list = list(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort).orderByAsc(Category::getId));
        Map<Long, Category> map = new LinkedHashMap<>();
        list.forEach(c -> {
            c.setChildren(new ArrayList<>());
            map.put(c.getId(), c);
        });
        List<Category> roots = new ArrayList<>();
        for (Category category : list) {
            Category parent = map.get(category.getParentId());
            if (parent == null || category.getParentId() == null || category.getParentId() == 0) {
                roots.add(category);
            } else {
                parent.getChildren().add(category);
            }
        }
        return roots;
    }

    public void createCategory(Category category) {
        category.setId(null);
        category.setParentId(category.getParentId() == null ? 0L : category.getParentId());
        category.setChildren(null);
        save(category);
    }

    public void updateCategory(Long id, Category category) {
        category.setId(id);
        category.setChildren(null);
        updateById(category);
    }

    public void deleteCategory(Long id) {
        Long children = count(Wrappers.<Category>lambdaQuery().eq(Category::getParentId, id));
        if (children != null && children > 0) {
            throw new BizException("请先删除子分类");
        }
        Long products = productMapper.selectCount(
                Wrappers.<Product>lambdaQuery().eq(Product::getCategoryId, id));
        if (products != null && products > 0) {
            throw new BizException("该分类下存在商品，无法删除");
        }
        removeById(id);
    }
}
