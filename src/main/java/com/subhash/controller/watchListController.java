package com.subhash.controller;

import com.subhash.model.Coin;
import com.subhash.model.User;
import com.subhash.model.WatchList;
import com.subhash.service.CoinService;
import com.subhash.service.UserService;
import com.subhash.service.WatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
public class watchListController {

    @Autowired
    private WatchListService watchListService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @GetMapping("/user")
    public ResponseEntity<WatchList> getUserWatchList(@RequestHeader("Authorization") String jwt){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        WatchList watchList=watchListService.findUserWatchList(user.getId());
        return ResponseEntity.ok(watchList);
    }

    @GetMapping("/{watchListId}")
    public ResponseEntity<WatchList>getWatchListById(@PathVariable Long watchListId){
        WatchList watchList=watchListService.findById(watchListId);
        return ResponseEntity.ok(watchList);
    }

    @PatchMapping("/add/coin/{coinId}")
    public ResponseEntity<Coin>addItemToWatchList(@RequestHeader("Authorization") String jwt, @PathVariable String coinId){
        if(jwt.startsWith("Bearer ")){
            jwt=jwt.substring(7);
        }
        User user=userService.findUserProfileByJwt(jwt.trim());
        Coin coin=coinService.findById(coinId);
        Coin addedCoin=watchListService.addItemToWatchList(coin,user);
        return ResponseEntity.ok(addedCoin);
    }
}

