package com.broyles.riskmanager.data;

import java.util.Date;

import org.springframework.data.annotation.Id;

public class TradeInPlay {

	@Id
	String Id;

	String instrument;
	Date initialDate;
	Date lastUpdate;
	double rValueInDollars;
	String accountId;
	String tradeId;
	double profitStop;
	double openPL;
	boolean active;
	double mostPositive;
	double mostNegative;
	int posCount;
	int negCount;
	
	// How much of balance to risk in R
	double rPercent = 0.01;
	// what distance (in percent of R) 0.2 -> 0.2R
	double firstThreshold = 0.5;
	// when to start profitStop (when to start profitStop)
	double firstThresholdDistance = 0.5;
	// after what level to loosen/tighten profit stop
	double secondThresholdDistance = 0.5;
	// Distance in R from "most profit" to take profit stop
	double secondThreshold = 1.0;
	// after what level to loosen/tighten profit stop
	double thirdThresholdDistance = 0.03;
	// Distance in R from "most profit" to take profit stop
	double thirdThreshold = 3.0;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public double getrValueInDollars() {
		return rValueInDollars;
	}

	public void setrValueInDollars(double rValueInDollars) {
		this.rValueInDollars = rValueInDollars;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getTradeId() {
		return tradeId;
	}

	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}

	public double getProfitStop() {
		return profitStop;
	}

	public void setProfitStop(double profitStop) {
		this.profitStop = profitStop;
	}

	public double getOpenPL() {
		return openPL;
	}

	public void setOpenPL(double openPL) {
		this.openPL = openPL;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public double getrPercent() {
		return rPercent;
	}

	public void setrPercent(double rPercent) {
		this.rPercent = rPercent;
	}

	public Date getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}

	public double getMostPositive() {
		return mostPositive;
	}

	public void setMostPositive(double mostPositive) {
		this.mostPositive = mostPositive;
	}

	public double getMostNegative() {
		return mostNegative;
	}

	public void setMostNegative(double mostNegative) {
		this.mostNegative = mostNegative;
	}

	public int getPosCount() {
		return posCount;
	}

	public void setPosCount(int posCount) {
		this.posCount = posCount;
	}

	public int getNegCount() {
		return negCount;
	}

	public void setNegCount(int negCount) {
		this.negCount = negCount;
	}

	public double getFirstThreshold() {
		return firstThreshold;
	}

	public void setFirstThreshold(double firstThreshold) {
		this.firstThreshold = firstThreshold;
	}

	public double getFirstThresholdDistance() {
		return firstThresholdDistance;
	}

	public void setFirstThresholdDistance(double firstThresholdDistance) {
		this.firstThresholdDistance = firstThresholdDistance;
	}

	public double getSecondThresholdDistance() {
		return secondThresholdDistance;
	}

	public void setSecondThresholdDistance(double secondThresholdDistance) {
		this.secondThresholdDistance = secondThresholdDistance;
	}

	public double getSecondThreshold() {
		return secondThreshold;
	}

	public void setSecondThreshold(double secondThreshold) {
		this.secondThreshold = secondThreshold;
	}

	public double getThirdThresholdDistance() {
		return thirdThresholdDistance;
	}

	public void setThirdThresholdDistance(double thirdThresholdDistance) {
		this.thirdThresholdDistance = thirdThresholdDistance;
	}

	public double getThirdThreshold() {
		return thirdThreshold;
	}

	public void setThirdThreshold(double thirdThreshold) {
		this.thirdThreshold = thirdThreshold;
	}

	@Override
	public String toString() {
		return "TradeInPlay [Id=" + Id + ", instrument=" + instrument + ", initialDate=" + initialDate + ", lastUpdate="
				+ lastUpdate + ", rValueInDollars=" + rValueInDollars + ", accountId=" + accountId + ", tradeId="
				+ tradeId + ", profitStop=" + profitStop + ", openPL=" + openPL + ", active=" + active
				+ ", mostPositive=" + mostPositive + ", mostNegative=" + mostNegative + ", posCount=" + posCount
				+ ", negCount=" + negCount + ", rPercent=" + rPercent + ", firstThreshold=" + firstThreshold
				+ ", firstThresholdDistance=" + firstThresholdDistance + ", secondThresholdDistance="
				+ secondThresholdDistance + ", secondThreshold=" + secondThreshold + ", thirdThresholdDistance="
				+ thirdThresholdDistance + ", thirdThreshold=" + thirdThreshold + "]";
	}

}
