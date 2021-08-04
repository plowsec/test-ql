package com.mcafee.epo.agentmgmt.model;

public class AgentHandlerLoadData {
    private int agentCount;
    private String agentHandlerName;

    public String getAgentHandlerName() {
        return this.agentHandlerName;
    }

    public void setAgentHandlerName(String str) {
        this.agentHandlerName = str;
    }

    public int getAgentCount() {
        return this.agentCount;
    }

    public void setAgentCount(int i) {
        this.agentCount = i;
    }
}
