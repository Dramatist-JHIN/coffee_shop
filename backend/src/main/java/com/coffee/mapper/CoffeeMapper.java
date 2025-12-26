package com.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.entity.Coffee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface CoffeeMapper extends BaseMapper<Coffee> {

    @Select("SELECT * FROM coffee WHERE stock <= stock_warning AND status = 1")
    List<Coffee> findLowStockCoffees();

    @Select("SELECT category, COUNT(*) as count FROM coffee WHERE status = 1 GROUP BY category")
    List<Map<String, Object>> countByCategory();
}
