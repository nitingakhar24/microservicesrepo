package com.myecommerce.orderservice.controller;

import com.myecommerce.orderservice.dto.OrderRequestDTO;
import com.myecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private static String ORDER_PLACED_MESSAGE = "Order Placed Successfully";

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        orderService.placeOrder(orderRequestDTO);
        return ORDER_PLACED_MESSAGE;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderRequestDTO> getListOfAllOrders() {
        return orderService.findAll();
    }
}
