package com.subhash.controller;

import com.subhash.enums.OrderType;
import com.subhash.model.Coin;
import com.subhash.model.Order;
import com.subhash.model.User;
import com.subhash.request.CreateOrderRequest;
import com.subhash.service.CoinService;
import com.subhash.service.OrderService;
import com.subhash.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    //This controller is for purchasing the coins

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @PostMapping("/pay")
    public ResponseEntity<Order> payOrderPayment(@RequestHeader("Authorization") String jwt,
                                                 @RequestBody CreateOrderRequest request){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Coin coin=coinService.findById(request.getCoinId());
        Order order=orderService.processOrder(coin,request.getQuantity(),request.getOrderType(),user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order>getOrderById(@RequestHeader("Authorization") String jwt,
                                             @PathVariable Long orderId){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Order order=orderService.getOrderById(orderId);
        if(order.getUser().getId().equals(user.getId())){
            return ResponseEntity.ok(order);
        }
        else {
            throw new RuntimeException("you dont have access ");
        }
    }

    @GetMapping()
    public ResponseEntity<List<Order>>getAllOrdersForUser(@RequestHeader("Authorization") String jwt,
                                                          @RequestParam(required = false) OrderType order_type,
                                                          @RequestParam(required = false)String asset_symbol){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        Long userId=userService.findUserProfileByJwt(jwt.trim()).getId();
        List<Order>userOrders=orderService.getAllOrdersOfUser(userId,order_type,asset_symbol);
        return ResponseEntity.ok(userOrders);
    }
}
