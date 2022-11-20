package com.bloxbean.cardano.yaci.core.protocol;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Agent<T extends AgentListener> {
    protected State currenState;
    private Instant instant;
    private Channel channel;
    private final List<T> agentListeners = new ArrayList<>();

    public void setChannel(Channel channel) {
        if (this.channel != null && this.channel.isActive())
            log.warn("An active channel is already attached to this agent");

        this.channel = channel;
    }

    public void sendRequest(Message message) {
        if (currenState.hasAgency()) {
            currenState = currenState.nextState(message);
        } else {
            //TODO
            log.info("Agency = false-----------");
        }
    }

    public Message deserializeResponse(byte[] bytes) {
        return this.currenState.handleInbound(bytes);
    }

    public synchronized final void receiveResponse(Message message) {
        State oldState = currenState;
        currenState = currenState.nextState(message);

        processResponse(message);
        //Notify
        getAgentListeners().forEach(agentListener -> agentListener.onStateUpdate(oldState, currenState));
    }

    public final void sendNextMessage() {
        if (this.hasAgency()) {
            Message message = this.buildNextMessage();
            if (message == null)
                return;

            if (instant == null)
                instant = Instant.now();

            int elapseTime = Duration.between(instant, Instant.now()).getNano() / 1000;
            instant = Instant.now();
            Segment segment = Segment.builder()
                    .timestamp(elapseTime)
                    .protocol((short) this.getProtocolId())
                    .payload(message.serialize())
                    .build();

            channel.writeAndFlush(segment);
            this.sendRequest(message);
        }
    }

    public final boolean hasAgency() {
        return currenState.hasAgency();
    }

    public final synchronized void addListener(T agentListener) {
        agentListeners.add(agentListener);
    }

    public final synchronized void removeListener(T agentListener) {
        agentListeners.remove(agentListener);
    }

    protected List<T> getAgentListeners() {
        return agentListeners;
    }

    public void reset() {

    }

    public void shutdown() {

    }

    public abstract int getProtocolId();
    public abstract boolean isDone();
    protected abstract Message buildNextMessage();
    protected abstract void processResponse(Message message);
}