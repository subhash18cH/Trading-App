package com.subhash.service;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.subhash.enums.PaymentMethod;
import com.subhash.enums.PaymentOrderStatus;
import com.subhash.model.PaymentOrder;
import com.subhash.model.User;
import com.subhash.repository.PaymentOrderRepository;
import com.subhash.response.PaymentResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecretKey;

    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod){
        PaymentOrder paymentOrder=new PaymentOrder();
        paymentOrder.setUser(user);
        paymentOrder.setAmount(amount);
        paymentOrder.setPaymentMethod(paymentMethod);
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        return paymentOrderRepository.save(paymentOrder);
    }

    public PaymentOrder getPaymentOrderById(Long id){
        return paymentOrderRepository.findById(id).orElseThrow(()-> new RuntimeException("payment order not found"));
    }

    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder,String paymentId) throws RazorpayException, RazorpayException {
        if(paymentOrder.getStatus()==null){
            paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        }
        if(paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)){
            if(paymentOrder.getPaymentMethod().equals(PaymentMethod.RAZORPAY)){
                RazorpayClient razorpay=new RazorpayClient(apiKey,apiSecretKey);
                Payment payment=razorpay.payments.fetch(paymentId);

                Integer amount=payment.get("amount");
                String status=payment.get("status");

                if(status.equals("captured")){
                    paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                    return true;
                }
                paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                paymentOrderRepository.save(paymentOrder);
                return false;
            }
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }
        return false;
    }

    public PaymentResponse createRazorPayPaymentLink(User user, Long amount,Long orderId) throws RazorpayException {
        Long Amount=amount*100;
        try{
            RazorpayClient razorpay=new RazorpayClient(apiKey,apiSecretKey);

            JSONObject paymentLinkRequest=new JSONObject();
            paymentLinkRequest.put("amount",Amount);
            paymentLinkRequest.put("currency","INR");

            JSONObject customer=new JSONObject();
            customer.put("name",user.getUserName());
            customer.put("email",user.getEmail());

            paymentLinkRequest.put("customer",customer);

            JSONObject notify=new JSONObject();
            notify.put("email",true);

            paymentLinkRequest.put("notify",notify);

            paymentLinkRequest.put("reminder_enable",true);

            paymentLinkRequest.put("callback_url","http://localhost:5173/wallet?order_id"+orderId);
            paymentLinkRequest.put("callback_method","get");

            PaymentLink paymentLink=razorpay.paymentLink.create(paymentLinkRequest);

            String paymentLinkId=paymentLink.get("id");
            String paymentLinkUrl=paymentLink.get("short_url");

            PaymentResponse response=new PaymentResponse();
            response.setPayment_url(paymentLinkUrl);
            return response;
        }catch (RazorpayException e){
            System.out.println("error creating payment link"+e);
            throw new RazorpayException(e.getMessage());
        }
    }

    public PaymentResponse createStripePaymentLink(User user,Long amount,Long orderId) throws StripeException, StripeException {
        Stripe.apiKey=stripeSecretKey;

        SessionCreateParams params=SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/wallet?order_id"+orderId)
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount*100)
                                .setProductData(SessionCreateParams
                                        .LineItem.PriceData.ProductData
                                        .builder().setName("top up wallet").build()).build()

                        ).build()
                ).build();

        Session session=Session.create(params);
        System.out.println("Session_____"+session);
        PaymentResponse response=new PaymentResponse();
        response.setPayment_url(session.getUrl());
        return response;
    }
}
