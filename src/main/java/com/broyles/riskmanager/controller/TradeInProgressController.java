package com.broyles.riskmanager.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.broyles.riskmanager.data.TradeInPlay;
import com.broyles.riskmanager.data.TradeInPlayRepository;

@RestController
public class TradeInProgressController {
	@Autowired
	TradeInPlayRepository tipRepository;
	
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/trade_in_progress")
    public List<TradeInPlay> greeting(@RequestParam(value="active", defaultValue="true") boolean active) {
        return tipRepository.findByActive(active);
    }
}