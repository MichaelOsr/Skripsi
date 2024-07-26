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
import core.SimScenario;
import core.Tuple;
import java.util.HashSet;
import java.util.Set;
import routing.*;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P>
 * <strong>Note:</strong> if some statistics could not be created (e.g. overhead
 * ratio if no messages were delivered) "NaN" is reported for double values and
 * zero for integer median(s).
 */
public class MessageStatsReportMod extends Report implements MessageListener {

    private Map<String, Double> creationTimes;
    private List<Double> latencies;
    private List<Integer> hopCounts;
    private List<Double> msgBufferTime;
    private List<Double> rtt; // round trip times
    private List<Double> delivery;

    private Map<String, Integer> nrofNodeInterest; //menyimpan jumlah node dengan interest tertentu
    private Map<String, Set<DTNHost>> nrofMsgInterest; //menyimpan masing-masing jumlah pesan yg terforward
    private Map<String, Message> messages; //menyimpan pesan yang dibuat

    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;
	public static final String MESSAGE_TOPICS_S = "topic";


    /**
     * Constructor.
     */
    public MessageStatsReportMod() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.msgBufferTime = new ArrayList<Double>();
        this.hopCounts = new ArrayList<Integer>();
        this.rtt = new ArrayList<Double>();
        this.delivery = new ArrayList<Double>();

        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;

        this.nrofMsgInterest = new HashMap<String, Set<DTNHost>>();
        this.messages = new HashMap<String, Message>();
        this.nrofNodeInterest = new HashMap<String, Integer>();
        this.nrofNodeInterest.put("Sport", 0);
        this.nrofNodeInterest.put("Cooking", 0);
        this.nrofNodeInterest.put("Film", 0);
        this.nrofNodeInterest.put("Traveling", 0);
        this.nrofNodeInterest.put("Music", 0);
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

        List<Boolean> topicMsg = (ArrayList) m.getProperty(MESSAGE_TOPICS_S);
        int i = 0;
        for (Boolean topic : topicMsg){
            if (topic && to.getSocialProfile().get(i) != 0){
                if (nrofMsgInterest.containsKey(m.getId())) {
                    Set<DTNHost> host = nrofMsgInterest.get(m.getId());
                    host.add(to);
                    nrofMsgInterest.put(m.getId(), host);
                } else {
                    Set<DTNHost> host = new HashSet<>();
                    host.add(to);
                    nrofMsgInterest.put(m.getId(), host);
                }
            }
            i++;
        }

        if (finalTarget) {
            this.latencies.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCounts.add(m.getHops().size() - 1);

            if (m.isResponse()) {
                this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
                this.nrofResponseDelivered++;
            }
        }
    }

    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }

        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        this.messages.put(m.getId(), m);
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
        write("Message stats for scenario " + getScenarioName()
                + "\nsim_time: " + format(getSimTime()));
        double deliveryProb = 0; // delivery probability
        double deliveryProbC = 0; // delivery probability interest
        double responseProb = 0; // request-response success probability
        double overHead = Double.NaN;	// overhead ratio

        if (this.nrofCreated > 0) {
            deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
        }
        if (this.nrofDelivered > 0) {
            overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered))
                    / this.nrofDelivered;
        }
        if (this.nrofResponseReqCreated > 0) {
            responseProb = (1.0 * this.nrofResponseDelivered)
                    / this.nrofResponseReqCreated;
        }

        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

        int nrofDeliveredC = 0;
        for (DTNHost h : nodes) {
            for (String interest : nrofNodeInterest.keySet()) {
                if (h.getSocialProfile().contains(interest)) {
                    int n = nrofNodeInterest.get(interest) + 1;
                    nrofNodeInterest.put(interest, n);
                }
            }
        }
        if (this.nrofCreated > 0) {
            deliveryProbC = (1.0 * nrofDeliveredC) / this.nrofCreated;
        }
        for (Map.Entry<String, Integer> entry : nrofNodeInterest.entrySet()) {
            write(entry.getKey() + " : " + entry.getValue() + "\n");
        }
        for (Map.Entry<String, Set<DTNHost>> entry : nrofMsgInterest.entrySet()) {
            Message m = this.messages.get(entry.getKey());
            Tuple<String, String> topic = (Tuple<String, String>) m.getProperty(M_TOPIC);

            write(entry.getKey() + " (" + topic + ") : " + entry.getValue().size());

            double nrofEachDelivered = entry.getValue().size();
            double nrofNode;

            if (entry.getKey().contains("S")) {
                nrofNode = 499;
            } else {
                nrofNode = this.nrofNodeInterest.get(topic.getKey());
            }

            double deliveryEachMsg = nrofEachDelivered / nrofNode;

            write("Delivery Prob: " + deliveryEachMsg + "\n");

            this.delivery.add(deliveryEachMsg);
        }

        write("Delivery Prob total: " + getAverage(this.delivery) + "\n");
        
        String statsText = "created: " + this.nrofCreated
                + "\nstarted: " + this.nrofStarted
                + "\nrelayed: " + this.nrofRelayed
                + "\naborted: " + this.nrofAborted
                + "\ndropped: " + this.nrofDropped
                + "\nremoved: " + this.nrofRemoved
                + "\ndelivered: " + this.nrofDelivered
                + "\ndelivery_prob: " + format(deliveryProb)
                + "\ndelivered interest: " + nrofDeliveredC
                + "\ndelivery_prob interest: " + format(deliveryProbC)
                + "\nresponse_prob: " + format(responseProb)
                + "\noverhead_ratio: " + format(overHead)
                + "\nlatency_avg: " + getAverage(this.latencies)
                + "\nlatency_med: " + getMedian(this.latencies)
                + "\nhopcount_avg: " + getIntAverage(this.hopCounts)
                + "\nhopcount_med: " + getIntMedian(this.hopCounts)
                + "\nbuffertime_avg: " + getAverage(this.msgBufferTime)
                + "\nbuffertime_med: " + getMedian(this.msgBufferTime)
                + "\nrtt_avg: " + getAverage(this.rtt)
                + "\nrtt_med: " + getMedian(this.rtt);
        write(statsText);
        super.done();
    }

}
