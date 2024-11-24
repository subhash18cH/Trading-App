package com.subhash.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhash.model.Coin;
import com.subhash.repository.CoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

@Service
public class CoinService {

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Coin> getCoinList(int page) throws JsonProcessingException {
        String url="https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=10&page="+page;
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
//            headers.set("x-cg-demo-api-key","api_key");
            HttpEntity<String> entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            List<Coin>coinList=objectMapper.readValue(response.getBody(),new TypeReference<List<Coin>>(){});
            System.out.println(coinList);
            return coinList;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    public String getMarketChart(String coinId,int days){
        String url="https://api.coingecko.com/api/v3/coins/"+coinId+"/market_chart?vs_currency=usd&days="+days;
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<String>entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String>response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            return response.getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getCoinDetails(String coinId) throws JsonProcessingException {
        String url="https://api.coingecko.com/api/v3/coins/"+coinId;
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<String>entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String>response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            JsonNode jsonNode=objectMapper.readTree(response.getBody());

            Coin coin=new Coin();
            coin.setId(jsonNode.get("id").asText());
            coin.setName(jsonNode.get("name").asText());
            coin.setSymbol(jsonNode.get("symbol").asText());
            coin.setImage(jsonNode.get("image").get("large").asText());

            JsonNode marketData=jsonNode.get("market_data");

            coin.setAth(marketData.get("ath").get("usd").asDouble());
            coin.setAthChangePercentage(marketData.get("ath_change_percentage").get("usd").asLong());
            coin.setAtl(marketData.get("atl").get("usd").asDouble());
            coin.setAtlChangePercentage(marketData.get("atl_change_percentage").get("usd").asDouble());

//            coin.setAthDate(marketData.get("ath_date").get("usd"));
//            coin.setAtlDate(marketData.get("atl_date").get("usd"));
//            coin.setLastUpdated(marketData.get("last_updated"));

            coin.setCirculatingSupply(marketData.get("circulating_supply").asLong());
            coin.setFullyDilutedValuation(marketData.get("fully_diluted_valuation").get("usd").asLong());
            coin.setMaxSupply(marketData.get("max_supply").asLong());
            coin.setCurrentPrice(marketData.get("current_price").get("usd").asDouble());
            coin.setMarketCap(marketData.get("market_cap").get("usd").asLong());
            coin.setMarketCapRank(marketData.get("market_cap_rank").asInt());
            coin.setTotalVolume(marketData.get("total_volume").get("usd").asLong());
            coin.setHigh24h(marketData.get("high_24h").get("usd").asDouble());
            coin.setLow24h(marketData.get("low_24h").get("usd").asDouble());
            coin.setPriceChange24h(marketData.get("price_change_24h").asDouble());
            coin.setPriceChangePercentage24h(marketData.get("price_change_percentage_24h").asDouble());
            coin.setMarketCapChange24h(marketData.get("market_cap_change_24h").asLong());
            coin.setMarketCapChangePercentage24h(marketData.get("market_cap_change_percentage_24h").asLong());
            coin.setTotalSupply(marketData.get("total_supply").asLong());
            coinRepository.save(coin);

            return response.getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            System.out.println("Error------"+e);
            throw  new RuntimeException(e.getMessage());
        }
    }

    public Coin findById(String coinId){
        Optional<Coin> optionalCoin=coinRepository.findById(coinId);
        if(optionalCoin.isEmpty())throw new RuntimeException("Coin not found");
        return optionalCoin.get();
    }

    public String searchCoin(String keyword){
        String url="https://api.coingecko.com/api/v3/search?query="+keyword;
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<String>entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String>response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            return response.getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getTop50CoinsByMarketCapRank(){
        String url="https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&page=1";
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<String>entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String>response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            return response.getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getTrendingCoins(){
        String url="https://api.coingecko.com/api/v3/search/trending";
        RestTemplate restTemplate=new RestTemplate();
        try{
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<String>entity=new HttpEntity<String>("parameters",headers);
            ResponseEntity<String>response=restTemplate.exchange(url, HttpMethod.GET,entity, String.class);
            return response.getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
