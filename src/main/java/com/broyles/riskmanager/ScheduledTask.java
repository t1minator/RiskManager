package com.broyles.riskmanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.broyles.riskmanager.data.TradeInPlay;
import com.broyles.riskmanager.data.TradeInPlayRepository;
import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.Account;
import com.oanda.v20.account.AccountChanges;
import com.oanda.v20.account.AccountChangesRequest;
import com.oanda.v20.account.AccountChangesResponse;
import com.oanda.v20.account.AccountChangesState;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountListResponse;
import com.oanda.v20.account.AccountProperties;
import com.oanda.v20.order.DynamicOrderState;
import com.oanda.v20.order.Order;
import com.oanda.v20.order.OrderID;
import com.oanda.v20.order.TrailingStopLossOrder;
import com.oanda.v20.position.CalculatedPositionState;
import com.oanda.v20.position.Position;
import com.oanda.v20.primitives.AccountUnits;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.CalculatedTradeState;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;
import com.oanda.v20.trade.TradeID;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.trade.TradeSummary;
import com.oanda.v20.transaction.OrderFillTransaction;
import com.oanda.v20.transaction.TradeReduce;
import com.oanda.v20.transaction.TransactionID;

@Component
public class ScheduledTask {

	@Autowired
	TradeInPlayRepository tipRepository;
	final static Logger logger = Logger.getLogger(ScheduledTask.class);
	Context ctx = null;
	List<Context> ctxList = null;
	Hashtable<String, TradeInPlay> profitStops = null;

	private Account getAccount(ListIterator<AccountProperties> iterator) throws RequestException, ExecuteException {

		AccountProperties ap = iterator.next();
		AccountID id = ap.getId();

		AccountGetResponse accountStateResponse = ctx.account.get(id);
		Account account = accountStateResponse.getAccount();

		return account;
	}

	private void updateProfitStops() {

		if (profitStops.isEmpty() || profitStops == null) {
			List<TradeInPlay> restoreList = tipRepository.findByActive(true);
			for (TradeInPlay t : restoreList) {
				t.setActive(false);
				profitStops.put(t.getId(), t);
			}
		}
	}

	@Scheduled(fixedRate = 1000 * 90)
	public void runScheduledTask() {
		Config accountConfig = Config.getInstance();
		Iterator<AccountConstants> configIterator = accountConfig.getAccounts().iterator();
		int i = 0;
		while (configIterator.hasNext()) {

			AccountConstants config = configIterator.next();
			if (ctxList == null || ctxList.size() < accountConfig.getAccounts().size()) {
				ctxList = (ctxList == null) ? new ArrayList<Context>() : ctxList;
				ctx = new ContextBuilder(config.getURL()).setToken(config.getTOKEN())
						.setApplication("AccountUpdateLoop").build();
				ctxList.add(ctx);
			} else {
				ctx = ctxList.get(i++);
			}
			
			// AccountID accountId = Config.ACCOUNTID;

			profitStops = profitStops != null ? profitStops : new Hashtable<String, TradeInPlay>();

			// Get initial account state
			try {

				AccountListResponse accountListResponse = ctx.account.list();
				ListIterator<AccountProperties> iterator = accountListResponse.getAccounts().listIterator();
				logger.info("-----------------");
				while (iterator.hasNext()) {

					Account account = getAccount(iterator);
					updateProfitStops();

					AccountChanges changes;
					AccountChangesState updatedstate;
					TransactionID lastTransactionId = account.getLastTransactionID();
					AccountID accountId = account.getId();
					logger.info("Polling from " + lastTransactionId + " Account: " + accountId.toString() + " "
							+ Calendar.getInstance().getTime().toString());
					AccountChangesResponse resp = ctx.account
							.changes(new AccountChangesRequest(accountId).setSinceTransactionID(lastTransactionId));

					lastTransactionId = resp.getLastTransactionID();
					changes = resp.getChanges();
					AccountUnits nav = resp.getState().getMarginCloseoutNAV();
					logger.info("NAV: " + nav.toString());
					applyAccountChanges(account, changes);

					List<TradeSummary> trades = account.getTrades();
					updatedstate = resp.getState();

					double balance = account.getBalance().doubleValue();
					double rValue = balance * 0.01;
					// iterate through open trades
					for (TradeSummary t : trades) {
						TradeInPlay tradeInPlay = new TradeInPlay();
						String key = account.getId().toString() + t.getId().toString();
						tradeInPlay = initializeTradeInPlay(account, balance, t, tradeInPlay, key);

						// tipRepository.deleteAll();
						double unrealizedPL = t.getUnrealizedPL().doubleValue();
						double firstThresholdInDollars = tradeInPlay.getFirstThreshold()
								* tradeInPlay.getrValueInDollars();
						double secondThresholdInDollars = tradeInPlay.getSecondThreshold()
								* tradeInPlay.getrValueInDollars();
						double thirdThresholdInDollars = tradeInPlay.getThirdThreshold()
								* tradeInPlay.getrValueInDollars();
						tradeInPlay.setLastUpdate(Calendar.getInstance().getTime());
						tradeInPlay.setNegCount(
								unrealizedPL < 0 ? tradeInPlay.getNegCount() + 1 : tradeInPlay.getNegCount());
						tradeInPlay.setPosCount(
								unrealizedPL > 0 ? tradeInPlay.getPosCount() + 1 : tradeInPlay.getPosCount());
						tradeInPlay.setMostNegative(unrealizedPL < tradeInPlay.getMostNegative() ? unrealizedPL
								: tradeInPlay.getMostNegative());
						tradeInPlay.setMostPositive(unrealizedPL > tradeInPlay.getMostPositive() ? unrealizedPL
								: tradeInPlay.getMostPositive());
						tradeInPlay.setOpenPL(unrealizedPL);
						//tradeInPlay.setProfitStop(0);
						if (tradeInPlay.getMostPositive() <= unrealizedPL
								|| (unrealizedPL > 0 && tradeInPlay.getProfitStop() == 0)) {
							tradeInPlay.setMostPositive(unrealizedPL > tradeInPlay.getMostPositive() ? unrealizedPL
									: tradeInPlay.getMostPositive());

							if (tradeInPlay.getMostPositive() > thirdThresholdInDollars) {
								tradeInPlay.setProfitStop(unrealizedPL
										- (tradeInPlay.getThirdThresholdDistance() * tradeInPlay.getrValueInDollars()));
							} else if (tradeInPlay.getMostPositive() > secondThresholdInDollars) {
								tradeInPlay.setProfitStop(unrealizedPL - (tradeInPlay.getSecondThresholdDistance()
										* tradeInPlay.getrValueInDollars()));
							} else if (unrealizedPL > firstThresholdInDollars) {
								tradeInPlay.setProfitStop(unrealizedPL
										- (tradeInPlay.getFirstThresholdDistance() * tradeInPlay.getrValueInDollars()));
							}
						}
						logger.info(tradeInPlay.getTradeId() + " Symbol " + tradeInPlay.getInstrument()
								+ " Profit Stop : " + tradeInPlay.getProfitStop() + " OpenPL: "
								+ tradeInPlay.getOpenPL() + " Unrealised PL: " + unrealizedPL + " Most Negative: "
								+ tradeInPlay.getMostNegative() + " :" + tradeInPlay.getNegCount() + " Most Positive: "
								+ tradeInPlay.getMostPositive() + " :" + tradeInPlay.getPosCount());
						evaluateForTradeClosure(accountId, tradeInPlay, unrealizedPL);

					}

					applyAccountChangesState(account, updatedstate);

					updateTradesInProgress();
				}
			} catch (Exception e) {
				logger.error(e.getMessage() + "  " + e.getStackTrace());
				// throw new RuntimeException(e);
			}
		}
	}

	private TradeInPlay initializeTradeInPlay(Account account, double balance, TradeSummary t, TradeInPlay tradeInPlay,
			String key) {
		double rValue;
		if (profitStops.containsKey(key))
			tradeInPlay = profitStops.get(key);
		else {

			rValue = balance * tradeInPlay.getrPercent();
			tradeInPlay.setrValueInDollars(rValue);
			tradeInPlay.setId(key);
			tradeInPlay.setInstrument(t.getInstrument().toString());
			tradeInPlay.setInitialDate(Calendar.getInstance().getTime());
			tradeInPlay.setLastUpdate(Calendar.getInstance().getTime());
			tradeInPlay.setAccountId(account.getId().toString());
			tradeInPlay.setTradeId(t.getId().toString());
			tradeInPlay.setProfitStop(0.0);
			tradeInPlay.setActive(true);
			profitStops.put(key, tradeInPlay);
		}
		return tradeInPlay;
	}

	private void evaluateForTradeClosure(AccountID accountId, TradeInPlay tradeInPlay, double unrealizedPL) {
		if (tradeInPlay.getProfitStop() != 0)
			if (unrealizedPL < tradeInPlay.getProfitStop()) {
				logger.info("Close a Trade");
				try {
					// Execute the request and retrieve the response object
					TradeCloseResponse response = ctx.trade
							.close(new TradeCloseRequest(accountId, new TradeSpecifier(tradeInPlay.getTradeId())));
					// Extract the order fill transaction describing the trade close action
					OrderFillTransaction transaction = response.getOrderFillTransaction();
					// Extract the list of trades that were closed by the request
					List<TradeReduce> myTrades = transaction.getTradesClosed();
					// Check if single trade closed
					if (myTrades.size() != 1)
						logger.error("Only 1 trade was expected to be closed");
					// Extract the single closed trade
					TradeReduce trade = myTrades.get(0);
					tradeInPlay.setActive(false);
					// Check if trade closed was the one we asked to be closed
					if (!trade.getTradeID().equals(tradeInPlay.getTradeId()))
						logger.error("The wrong trade was closed");
				} catch (Exception e) {
					logger.error("" + e.getMessage());
				}
			}
	}

	private void updateTradesInProgress() {
		Set<String> tipKeys = profitStops.keySet();
		for (String key : tipKeys) {
			tipRepository.save(profitStops.get(key));
		}
	}

	private static void applyAccountChanges(Account account, AccountChanges changes) {
		/*
		 * System.out.println("Account Changes:"); System.out.println(changes);
		 */
		Map<OrderID, Order> ordermap = new HashMap<>();

		for (Order order : account.getOrders())
			ordermap.put(order.getId(), order);

		for (Order created : changes.getOrdersCreated())
			ordermap.put(created.getId(), created);
		for (Order cancelled : changes.getOrdersCancelled())
			ordermap.remove(cancelled.getId());
		for (Order filled : changes.getOrdersFilled())
			ordermap.remove(filled.getId());
		for (Order triggered : changes.getOrdersTriggered())
			ordermap.remove(triggered.getId());

		account.setOrders(ordermap.values());

		Map<TradeID, TradeSummary> trademap = new HashMap<>();

		for (TradeSummary trade : account.getTrades())
			trademap.put(trade.getId(), trade);

		for (TradeSummary opened : changes.getTradesOpened())
			trademap.put(opened.getId(), opened);
		for (TradeSummary reduced : changes.getTradesReduced())
			trademap.put(reduced.getId(), reduced);
		for (TradeSummary closed : changes.getTradesClosed())
			trademap.remove(closed.getId());

		account.setTrades(trademap.values());

		Map<InstrumentName, Position> positionMap = new HashMap<>();

		for (Position position : account.getPositions())
			positionMap.put(position.getInstrument(), position);

		for (Position position : changes.getPositions())
			positionMap.put(position.getInstrument(), position);

		account.setPositions(positionMap.values());
	}

	private static void applyAccountChangesState(Account account, AccountChangesState updatedstate) {
		/*
		 * System.out.println("Account Changes State:");
		 * System.out.println(updatedstate);
		 */

		if (updatedstate.getUnrealizedPL() != null)
			account.setUnrealizedPL(updatedstate.getUnrealizedPL());
		if (updatedstate.getNAV() != null)
			account.setNAV(updatedstate.getNAV());
		if (updatedstate.getMarginUsed() != null)
			account.setMarginUsed(updatedstate.getMarginUsed());
		if (updatedstate.getMarginAvailable() != null)
			account.setMarginAvailable(updatedstate.getMarginAvailable());
		if (updatedstate.getPositionValue() != null)
			account.setPositionValue(updatedstate.getPositionValue());
		if (updatedstate.getMarginCloseoutUnrealizedPL() != null)
			account.setMarginCloseoutUnrealizedPL(updatedstate.getMarginCloseoutUnrealizedPL());
		if (updatedstate.getMarginCloseoutNAV() != null)
			account.setMarginCloseoutNAV(updatedstate.getMarginCloseoutNAV());
		if (updatedstate.getMarginCloseoutMarginUsed() != null)
			account.setMarginCloseoutMarginUsed(updatedstate.getMarginCloseoutMarginUsed());
		if (updatedstate.getMarginCloseoutPercent() != null)
			account.setMarginCloseoutPercent(updatedstate.getMarginCloseoutPercent());
		if (updatedstate.getMarginCloseoutPositionValue() != null)
			account.setMarginCloseoutPositionValue(updatedstate.getMarginCloseoutPositionValue());
		if (updatedstate.getWithdrawalLimit() != null)
			account.setWithdrawalLimit(updatedstate.getWithdrawalLimit());
		if (updatedstate.getMarginCallMarginUsed() != null)
			account.setMarginCallMarginUsed(updatedstate.getMarginCallMarginUsed());
		if (updatedstate.getMarginCallPercent() != null)
			account.setMarginCallPercent(updatedstate.getMarginCallPercent());

		Map<OrderID, Order> ordermap = new HashMap<>();

		for (Order order : account.getOrders())
			ordermap.put(order.getId(), order);
		for (DynamicOrderState orderstate : updatedstate.getOrders()) {
			TrailingStopLossOrder order = (TrailingStopLossOrder) ordermap.get(orderstate.getId());
			order.setTrailingStopValue(orderstate.getTrailingStopValue());
		}

		Map<TradeID, TradeSummary> trademap = new HashMap<>();

		for (TradeSummary trade : account.getTrades())
			trademap.put(trade.getId(), trade);
		for (CalculatedTradeState tradestate : updatedstate.getTrades()) {
			TradeSummary trade = trademap.get(tradestate.getId());
			trade.setUnrealizedPL(tradestate.getUnrealizedPL());
		}

		Map<InstrumentName, Position> posmap = new HashMap<>();

		for (Position pos : account.getPositions())
			posmap.put(pos.getInstrument(), pos);
		for (CalculatedPositionState posstate : updatedstate.getPositions()) {
			Position pos = posmap.get(posstate.getInstrument());
			pos.setInstrument(posstate.getInstrument());
			pos.setUnrealizedPL(posstate.getNetUnrealizedPL());
			pos.getLong().setUnrealizedPL(posstate.getLongUnrealizedPL());
			pos.getShort().setUnrealizedPL(posstate.getShortUnrealizedPL());
		}
	}

}