package com.subhash.controller;

import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import com.subhash.enums.PaymentMethod;
import com.subhash.model.PaymentOrder;
import com.subhash.model.User;
import com.subhash.response.PaymentResponse;
import com.subhash.service.PaymentService;
import com.subhash.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/api/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(@PathVariable PaymentMethod paymentMethod,
                                                          @PathVariable Long amount,
                                                          @RequestHeader("Authorization") String jwt) throws RazorpayException, StripeException, RazorpayException, StripeException {
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        PaymentResponse paymentResponse;
        PaymentOrder order=paymentService.createOrder(user,amount,paymentMethod);
        if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
            paymentResponse=paymentService.createRazorPayPaymentLink(user,amount, order.getId());
        }else{
            paymentResponse=paymentService.createStripePaymentLink(user,amount,order.getId());
        }
        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }
}