package com.java.xinge.dto;

import com.java.xinge.entity.Setmeal;
import com.java.xinge.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
