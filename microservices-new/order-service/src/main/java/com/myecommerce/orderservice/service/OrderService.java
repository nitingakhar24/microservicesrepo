package com.myecommerce.orderservice.service;

import com.myecommerce.orderservice.dto.InventoryResponse;
import com.myecommerce.orderservice.dto.OrderLineItemsDTO;
import com.myecommerce.orderservice.dto.OrderRequestDTO;
import com.myecommerce.orderservice.model.Order;
import com.myecommerce.orderservice.model.OrderLineItems;
import com.myecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(final OrderRequestDTO orderRequestDTO) {
        final Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequestDTO.getOrderLineItemDtos()
                .stream()
                .map(this::mapToOrderLineItem)
                .toList();
        order.setOrderLineItems(orderLineItems);

        //Get the list of SKU's and pass it to
        final List<String> skuCodesFromOrder = order.getOrderLineItems()
                .stream()
                .map(orderLineItems1 -> orderLineItems1.getSkuCode()).toList();

        // Call inventory service and place order if product is in stock.
        final InventoryResponse[] inventoryResponses = webClientBuilder.build().get().
                uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodesFromOrder).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
        log.info("Order {} saved successfully", order.getOrderNumber());
    }

    private OrderLineItems mapToOrderLineItem(final OrderLineItemsDTO orderLineItemsDTO) {
        final OrderLineItems orderLineItems = new OrderLineItems();
        //orderLineItems.setId(orderLineItemsDTO.getId());
        orderLineItems.setQuantity(orderLineItemsDTO.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDTO.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDTO.getPrice());
        return orderLineItems;
    }

    public List<OrderRequestDTO> findAll() {
        final List<Order> orders = orderRepository.findAll();
        return getListOrderLineItems(orders);
    }

    private List<OrderRequestDTO> getListOrderLineItems(final List<Order> orders) {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        List<OrderRequestDTO> orderRequestDTOs = new ArrayList<>();
        OrderLineItemsDTO orderLineItemsDTO = new OrderLineItemsDTO();
        List<OrderLineItemsDTO> orderLineItemsDTOs = new ArrayList<>();
        orderLineItemsDTO.setPrice(new BigDecimal(199.99));
        orderLineItemsDTO.setId(1011L);
        orderLineItemsDTO.setSkuCode("108000000");
        orderLineItemsDTO.setQuantity(2);
        orderLineItemsDTOs.add(orderLineItemsDTO);
        orderRequestDTO.setOrderLineItemDtos(orderLineItemsDTOs);
        orderRequestDTOs.add(orderRequestDTO);
        return  orderRequestDTOs;
    }

/*    private OrderRequestDTO mapToOrderRequestDto(final Order order) {
        final OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        final OrderLineItemsDTO orderLineItemsDTO = new OrderLineItemsDTO();

                orderRequestDTO.setOrderLineItemDtos();

    }*/
}
