package com.subhash.service;

import com.subhash.model.Coin;
import com.subhash.model.User;
import com.subhash.model.WatchList;
import com.subhash.repository.WatchListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class WatchListService {

    @Autowired
    private WatchListRepository watchListRepository;

    public WatchList findUserWatchList(Long userId){
        WatchList watchList=watchListRepository.findByUserId(userId);
        if(watchList == null){
            throw new RuntimeException("WatchList not found with this user");
        }
        return watchList;
    }

    public WatchList createWatchList(User user){
        WatchList watchList=new WatchList();
        watchList.setUser(user);
        return watchListRepository.save(watchList);
    }

    public WatchList findById(Long id){
        Optional<WatchList> optionalWatchList=watchListRepository.findById(id);
        if(optionalWatchList.isEmpty()){
            throw new RuntimeException("WatchList not found");
        }
        return optionalWatchList.get();
    }

    public Coin addItemToWatchList(Coin coin, User user){
        WatchList watchList=findUserWatchList(user.getId());
        if(watchList.getCoins().contains(coin)){
            watchList.getCoins().remove(coin);
        }else{
            watchList.getCoins().add(coin);
        }
        watchListRepository.save(watchList);
        return coin;
    }
}

