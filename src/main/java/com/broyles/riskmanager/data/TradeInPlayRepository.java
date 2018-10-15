package com.broyles.riskmanager.data;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeInPlayRepository extends MongoRepository<TradeInPlay, String> {

    //public Optional<TradeInPlay> findById(String id);
    public List<TradeInPlay> findByActive(boolean active);

}
