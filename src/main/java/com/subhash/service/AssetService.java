package com.subhash.service;

import com.subhash.model.Asset;
import com.subhash.model.Coin;
import com.subhash.model.User;
import com.subhash.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    public Asset createAsset(User user, Coin coin, double quantity){
        Asset asset=new Asset();
        asset.setUser(user);
        asset.setCoin(coin);
        asset.setQuantity(quantity);
        asset.setBuyPrice(coin.getCurrentPrice());
        return assetRepository.save(asset);
    }

    public Asset getAssetById(Long assetId){
        return assetRepository.findById(assetId).orElseThrow(()-> new RuntimeException("asset not found with this id"));
    }

    public List<Asset> getUsersAssets(Long userId){
        return assetRepository.findByUserId(userId);
    }

    public Asset updateAsset(Long assetId,double quantity){
        Asset oldAsset=getAssetById(assetId);
        oldAsset.setQuantity(quantity+oldAsset.getQuantity());
        return assetRepository.save(oldAsset);
    }

    public Asset findAssetByUserIdAndCoinId(Long userId,String coinId){
        return assetRepository.findByUserIdAndCoinId(userId,coinId);
    }

    public void deleteAsset(Long assetId){
        assetRepository.deleteById(assetId);
    }
}

