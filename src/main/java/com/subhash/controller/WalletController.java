package com.subhash.controller;

import com.razorpay.RazorpayException;
import com.subhash.model.*;
import com.subhash.service.OrderService;
import com.subhash.service.PaymentService;
import com.subhash.service.UserService;
import com.subhash.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/api/wallet")
    public ResponseEntity<Wallet> getUserWallet(@RequestHeader("Authorization") String jwt){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Wallet wallet=walletService.getUserWallet(user);
        return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
    }

    // This will work after payment controller
    // This order_id is the paymentOrder id and payment_id
    // is the razorpay/stripe payment id that is generated in the link
    @PutMapping("/api/wallet/deposit")
    public ResponseEntity<Wallet>addMoneyToWallet(@RequestHeader("Authorization") String jwt,
                                                  @RequestParam(name = "order_id") Long orderId,
                                                  @RequestParam(name = "payment_id")String paymentId) throws RazorpayException {
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Wallet wallet=walletService.getUserWallet(user);
        PaymentOrder order=paymentService.getPaymentOrderById(orderId);
        Boolean status=paymentService.proceedPaymentOrder(order,paymentId);
        if(wallet.getBalance()==null){
            wallet.setBalance(BigDecimal.valueOf(0));
        }
        if(status){
            wallet=walletService.addBalance(wallet,order.getAmount());
        }
        return new ResponseEntity<>(wallet,HttpStatus.ACCEPTED);
    }

    @PutMapping("/api/wallet/{walletId}/transfer")
    public ResponseEntity<Wallet>walletToWalletTransfer(@RequestHeader("Authorization") String jwt,
                                                        @PathVariable Long walletId,
                                                        @RequestBody WalletTransaction request){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User senderUser=userService.findUserProfileByJwt(jwt.trim());
        Wallet receiverWallet=walletService.findWalletById(walletId);
        Wallet wallet=walletService.walletToWalletTransfer(senderUser,receiverWallet,request.getAmount());

        return new ResponseEntity<>(wallet,HttpStatus.ACCEPTED);
    }

    @PutMapping("/api/wallet/order/{orderId}/pay")
    public ResponseEntity<Wallet>payOrderPayment(@RequestHeader("Authorization") String jwt,
                                                 @PathVariable Long orderId){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Order order=orderService.getOrderById(orderId);
        Wallet wallet=walletService.payOrderPayment(order,user);
        return new ResponseEntity<>(wallet,HttpStatus.ACCEPTED);
    }


}

