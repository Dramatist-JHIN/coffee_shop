package com.coffee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.Result;
import com.coffee.entity.Coffee;
import com.coffee.service.CoffeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coffee")
public class CoffeeController {

    @Autowired
    private CoffeeService coffeeService;

    @GetMapping
    public Result<List<Coffee>> list() {
        return Result.success(coffeeService.list());
    }

    @GetMapping("/page")
    public Result<Page<Coffee>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(coffeeService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<Coffee> getById(@PathVariable Long id) {
        Coffee coffee = coffeeService.getById(id);
        if (coffee == null) {
            return Result.error("咖啡不存在");
        }
        return Result.success(coffee);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Coffee coffee) {
        if (coffeeService.save(coffee)) {
            return Result.success(null);
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Coffee coffee) {
        coffee.setId(id);
        if (coffeeService.update(coffee)) {
            return Result.success(null);
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (coffeeService.delete(id)) {
            return Result.success(null);
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{id}/stock")
    public Result<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        if (coffeeService.updateStock(id, stock)) {
            return Result.success(null);
        }
        return Result.error("更新库存失败");
    }
}
