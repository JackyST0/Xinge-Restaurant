package com.java.reggie.dto;

import com.java.reggie.entity.Setmeal;
import com.java.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
