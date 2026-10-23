package com.erp.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.BizException;
import com.erp.product.entity.Category;
import com.erp.product.entity.Product;
import com.erp.product.mapper.CategoryMapper;
import com.erp.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final CategoryMapper categoryMapper;

    public IPage<Product> pageProducts(long page, long size, String keyword, Long categoryId, Integer status) {
        var wrapper = Wrappers.<Product>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getCode, keyword));
        }
        if (categoryId != null) {
            List<Long> categoryIds = categoryMapper.selectList(
                            Wrappers.<Category>lambdaQuery()
                                    .eq(Category::getParentId, categoryId)
                                    .or().eq(Category::getId, categoryId))
                    .stream().map(Category::getId).toList();
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getId);
        IPage<Product> result = page(new Page<>(page, Math.min(size, 100)), wrapper);
        fillCategoryNames(result.getRecords());
        return result;
    }

    public List<Product> listAll() {
        List<Product> products = list(Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1).orderByAsc(Product::getCode));
        fillCategoryNames(products);
        return products;
    }

    public void createProduct(Product product) {
        checkCodeUnique(product.getCode(), null);
        product.setId(null);
        product.setDeleted(null);
        product.setStatus(product.getStatus() == null ? 1 : product.getStatus());
        save(product);
    }

    public void updateProduct(Long id, Product product) {
        checkCodeUnique(product.getCode(), id);
        product.setId(id);
        updateById(product);
    }

    public void deleteProduct(Long id) {
        removeById(id);
    }

    private void checkCodeUnique(String code, Long excludeId) {
        var wrapper = Wrappers.<Product>lambdaQuery().eq(Product::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Product::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BizException("商品编码已存在");
        }
    }

    private void fillCategoryNames(List<Product> products) {
        List<Long> categoryIds = products.stream().map(Product::getCategoryId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Long, Category> map = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        products.forEach(p -> {
            Category category = map.get(p.getCategoryId());
            if (category != null) {
                p.setCategoryName(category.getName());
            }
        });
    }
}
