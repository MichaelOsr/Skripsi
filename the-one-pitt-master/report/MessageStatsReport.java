/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P>
 * <strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class MessageStatsReport extends Report implements MessageListener, UpdateListener {
	public static final String MESSAGE_TOPICS_S = "topic";

	private Map<String, Double> creationTimes;
	private List<Double> latencies;
	private List<Integer> hopCounts;
	private List<Double> msgBufferTime;
	private List<Double> rtt; // round trip times

	private int nrofDropped;
	private int nrofRemoved;
	private int nrofStarted;
	private int nrofAborted;
	private int nrofRelayed;
	private int nrofCreated;
	private int nrofResponseReqCreated;
	private int nrofResponseDelivered;
	private int nrofDelivered;
	private Map<Integer, List<Double>> interestWeightNodep1;
	private Map<Integer, List<Double>> interestWeightNodep86;
	private Map<Integer, List<Double>> interestWeightNodep474;
	private List<Boolean> OIP1;
	private List<Boolean> OIP86;
	private List<Boolean> OIP474;
	private double lastUpdateTime = 0.0;
	private double interval = 8640;
	private double start = 0;
	
	/**
	 * Constructor.
	 */
	public MessageStatsReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		this.interestWeightNodep1 = new HashMap<Integer, List<Double>>();
		this.interestWeightNodep86 = new HashMap<Integer, List<Double>>();
		this.interestWeightNodep474 = new HashMap<Integer, List<Double>>();

		this.OIP1 = new ArrayList<>();
		this.OIP86 = new ArrayList<>();
		this.OIP474 = new ArrayList<>();

		this.creationTimes = new HashMap<String, Double>();
		this.latencies = new ArrayList<Double>();
		this.msgBufferTime = new ArrayList<Double>();
		this.hopCounts = new ArrayList<Integer>();
		this.rtt = new ArrayList<Double>();

		this.nrofDropped = 0;
		this.nrofRemoved = 0;
		this.nrofStarted = 0;
		this.nrofAborted = 0;
		this.nrofRelayed = 0;
		this.nrofCreated = 0;
		this.nrofResponseReqCreated = 0;
		this.nrofResponseDelivered = 0;
		this.nrofDelivered = 0;

		this.interestWeightNodep1.put(0, new ArrayList<>());
		this.interestWeightNodep1.put(1, new ArrayList<>());
		this.interestWeightNodep1.put(2, new ArrayList<>());
		this.interestWeightNodep1.put(3, new ArrayList<>());
		this.interestWeightNodep1.put(4, new ArrayList<>());

		this.interestWeightNodep86.put(0, new ArrayList<>());
		this.interestWeightNodep86.put(1, new ArrayList<>());
		this.interestWeightNodep86.put(2, new ArrayList<>());
		this.interestWeightNodep86.put(3, new ArrayList<>());
		this.interestWeightNodep86.put(4, new ArrayList<>());

		this.interestWeightNodep474.put(0, new ArrayList<>());
		this.interestWeightNodep474.put(1, new ArrayList<>());
		this.interestWeightNodep474.put(2, new ArrayList<>());
		this.interestWeightNodep474.put(3, new ArrayList<>());
		this.interestWeightNodep474.put(4, new ArrayList<>());
	}

	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId())) {
			return;
		}

		if (dropped) {
			this.nrofDropped++;
		} else {
			this.nrofRemoved++;
		}

		this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
	}

	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofAborted++;
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofRelayed++;

		// if (finalTarget) {
		// this.latencies.add(getSimTime() -
		// this.creationTimes.get(m.getId()) );
		// this.nrofDelivered++;
		// this.hopCounts.add(m.getHops().size() - 1);

		// if (m.isResponse()) {
		// this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
		// this.nrofResponseDelivered++;
		// }
		// }
	}

	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}

		this.creationTimes.put(m.getId(), getSimTime());
		this.nrofCreated++;
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
		}
	}

	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofStarted++;

	}

	@Override
	public void done() {
		write("Interest Weight Node Interval 15 menit");
		// double deliveryProb = 0; // delivery probability
		// double responseProb = 0; // request-response success probability
		// double overHead = Double.NaN; // overhead ratio

		// if (this.nrofCreated > 0) {
		// deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
		// }
		// if (this.nrofDelivered > 0) {
		// overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) /
		// this.nrofDelivered;
		// }
		// if (this.nrofResponseReqCreated > 0) {
		// responseProb = (1.0 * this.nrofResponseDelivered) /
		// this.nrofResponseReqCreated;
		// }

		// String statsText = "created: " + this.nrofCreated +
		// "\nstarted: " + this.nrofStarted +
		// "\nrelayed: " + this.nrofRelayed +
		// "\naborted: " + this.nrofAborted +
		// "\ndropped: " + this.nrofDropped +
		// "\nremoved: " + this.nrofRemoved +
		// "\ndelivered: " + this.nrofDelivered +
		// "\ndelivery_prob: " + format(deliveryProb) +
		// "\nresponse_prob: " + format(responseProb) +
		// "\noverhead_ratio: " + format(overHead) +
		// "\nlatency_avg: " + getAverage(this.latencies) +
		// "\nlatency_med: " + getMedian(this.latencies) +
		// "\nhopcount_avg: " + getIntAverage(this.hopCounts) +
		// "\nhopcount_med: " + getIntMedian(this.hopCounts) +
		// "\nbuffertime_avg: " + getAverage(this.msgBufferTime) +
		// "\nbuffertime_med: " + getMedian(this.msgBufferTime) +
		// "\nrtt_avg: " + getAverage(this.rtt) +
		// "\nrtt_med: " + getMedian(this.rtt);

		String statsText = "Node P1 \n";
		for (Map.Entry<Integer, List<Double>> entry : interestWeightNodep1.entrySet()) {

			// Printing all elements of a Map
			statsText += "Topik " + (entry.getKey()+1) + " : " + entry.getValue() + "\n";
		}
		statsText += "OI : " + this.OIP1 + "\n";
		statsText += "Node P86 \n";
		for (Map.Entry<Integer, List<Double>> entry : interestWeightNodep86.entrySet()) {

			// Printing all elements of a Map
			statsText += "Topik " + (entry.getKey()+1) + " : " + entry.getValue() + "\n";
		}
		statsText += "OI : " + this.OIP86 + "\n";
		statsText += "Node P474 \n";
		for (Map.Entry<Integer, List<Double>> entry : interestWeightNodep474.entrySet()) {

			// Printing all elements of a Map
			statsText += "Topik " + (entry.getKey()+1) + " : " + entry.getValue() + "\n";
		}
		statsText += "OI : " + this.OIP474 + "\n";

		write(statsText);
		super.done();
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		if(isWarmup()){
			start = SimClock.getTime();
		}
		if ((SimClock.getTime() - lastUpdateTime) >= interval || SimClock.getTime() - start >= 43200) {
			for (DTNHost host : hosts) {
				// List<Boolean> topicMsg = (ArrayList) m.getProperty(MESSAGE_TOPICS_S);
				if (host.toString().equals("p1")) {
					this.OIP1 = host.getSocialProfileOI();
					// System.out.println("Test");
					for (int i = 0; i < host.getSocialProfile().size(); i++) {
						// this.interestTemp.add();
					 	// System.out.println(	host.getSocialProfile().size());
						this.interestWeightNodep1.get(i).add(host.getSocialProfile().get(i));
					}

					// int i = 0;
					// while (i < host.getSocialProfile().size()) {
					// 	// interestTemp.add(host.getSocialProfile().get(i));
					// 	this.interestWeightNodep1.get(i).add(host.getSocialProfile().get(i));

					// 	i++;
					// }
				}

				if (host.toString().equals("p86")) {
					// System.out.println("Test");
					this.OIP86 = host.getSocialProfileOI();
					for (int i = 0; i < host.getSocialProfile().size(); i++) {
						// this.interestTemp.add();
						this.interestWeightNodep86.get(i).add(host.getSocialProfile().get(i));
					}

					// int i = 0;
					// while (i < host.getSocialProfile().size()) {
					// 	// interestTemp.add(host.getSocialProfile().get(i));
					// 	this.interestWeightNodep1.get(i).add(host.getSocialProfile().get(i));

					// 	i++;
					// }
				}
				if (host.toString().equals("p474")) {
					// System.out.println("Test");
					this.OIP474 = host.getSocialProfileOI();
					for (int i = 0; i < host.getSocialProfile().size(); i++) {
						// this.interestTemp.add();
						this.interestWeightNodep474.get(i).add(host.getSocialProfile().get(i));
					}

					// int i = 0;
					// while (i < host.getSocialProfile().size()) {
					// 	// interestTemp.add(host.getSocialProfile().get(i));
					// 	this.interestWeightNodep1.get(i).add(host.getSocialProfile().get(i));

					// 	i++;
					// }
				}
				
			}
			lastUpdateTime = SimClock.getTime();
		}
	}

}
