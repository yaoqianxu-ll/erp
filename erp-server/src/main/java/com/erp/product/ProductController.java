package com.erp.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.common.Result;
import com.erp.product.entity.Product;
import com.erp.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<IPage<Product>> page(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Integer status) {
        return Result.ok(productService.pageProducts(page, size, keyword, categoryId, status));
    }

    @GetMapping("/all")
    public Result<List<Product>> all() {
        return Result.ok(productService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<Void> create(@RequestBody Product product) {
        productService.createProduct(product);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        productService.updateProduct(id, product);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.ok();
    }
}
