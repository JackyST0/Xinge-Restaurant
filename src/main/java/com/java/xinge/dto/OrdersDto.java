package com.java.xinge.dto;

import com.java.xinge.entity.OrderDetail;
import com.java.xinge.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
