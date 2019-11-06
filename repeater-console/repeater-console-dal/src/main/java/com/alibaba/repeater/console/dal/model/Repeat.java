package com.alibaba.repeater.console.dal.model;

import java.io.Serializable;

/**
 * @author : gaozhiwen
 * @date : 2019/11/5
 */
public class Repeat implements Serializable{

    private Long id;

    private String repeatId;

    private boolean finish;

    private String response;

    private Long originResponseId;

    private Long cost;

    private String traceId;

    private String mockInvocations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepeatId() {
        return repeatId;
    }

    public void setRepeatId(String repeatId) {
        this.repeatId = repeatId;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMockInvocations() {
        return mockInvocations;
    }

    public void setMockInvocations(String mockInvocations) {
        this.mockInvocations = mockInvocations;
    }

    public Long getOriginResponseId() {
        return originResponseId;
    }

    public void setOriginResponseId(Long originResponseId) {
        this.originResponseId = originResponseId;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
