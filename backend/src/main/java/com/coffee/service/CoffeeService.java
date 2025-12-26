package com.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.entity.Coffee;
import com.coffee.mapper.CoffeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoffeeService {

    @Autowired
    private CoffeeMapper coffeeMapper;

    public List<Coffee> list() {
        return coffeeMapper.selectList(new QueryWrapper<Coffee>().eq("status", 1));
    }

    public Page<Coffee> page(int pageNum, int pageSize, String keyword) {
        Page<Coffee> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Coffee> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("category", keyword);
        }
        return coffeeMapper.selectPage(page, wrapper);
    }

    public Coffee getById(Long id) {
        return coffeeMapper.selectById(id);
    }

    public boolean save(Coffee coffee) {
        coffee.setStatus(1);
        return coffeeMapper.insert(coffee) > 0;
    }

    public boolean update(Coffee coffee) {
        return coffeeMapper.updateById(coffee) > 0;
    }

    public boolean delete(Long id) {
        Coffee coffee = coffeeMapper.selectById(id);
        if (coffee != null) {
            coffee.setStatus(0);
            return coffeeMapper.updateById(coffee) > 0;
        }
        return false;
    }

    public boolean updateStock(Long id, Integer stock) {
        Coffee coffee = coffeeMapper.selectById(id);
        if (coffee != null) {
            coffee.setStock(stock);
            return coffeeMapper.updateById(coffee) > 0;
        }
        return false;
    }

    public boolean decreaseStock(Long id, Integer quantity) {
        Coffee coffee = coffeeMapper.selectById(id);
        if (coffee != null && coffee.getStock() >= quantity) {
            coffee.setStock(coffee.getStock() - quantity);
            return coffeeMapper.updateById(coffee) > 0;
        }
        return false;
    }
}
