package com.mcafee.epo.agentmgmt.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class AgentHandlerLoadDataComparator implements Comparator<AgentHandlerLoadData> {
    private Collator collator = null;
    private String sortColumn = "agentHandlerName";
    private int sortOrder = 0;

    public AgentHandlerLoadDataComparator(String str, int i, Locale locale) {
        setSortColumn(str);
        setSortOrder(i);
        setCollator(Collator.getInstance(locale));
    }

    public int compare(AgentHandlerLoadData agentHandlerLoadData, AgentHandlerLoadData agentHandlerLoadData2) {
        String str;
        String str2;
        if ("agentHandlerName".equals(this.sortColumn)) {
            String agentHandlerName = agentHandlerLoadData.getAgentHandlerName();
            str = agentHandlerLoadData2.getAgentHandlerName();
            str2 = agentHandlerName;
        } else if (!"agentCount".equals(this.sortColumn)) {
            str = null;
            str2 = null;
        } else if (agentHandlerLoadData.getAgentCount() > agentHandlerLoadData2.getAgentCount()) {
            str = "1";
            str2 = "2";
        } else if (agentHandlerLoadData.getAgentCount() < agentHandlerLoadData2.getAgentCount()) {
            str = "2";
            str2 = "1";
        } else {
            str = "1";
            str2 = "1";
        }
        int compare = this.collator.compare(str2, str);
        if (this.sortOrder == 0) {
            return compare * -1;
        }
        return compare;
    }

    public String getSortColumn() {
        return this.sortColumn;
    }

    public void setSortColumn(String str) {
        this.sortColumn = str;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(int i) {
        this.sortOrder = i;
    }

    public Collator getCollator() {
        return this.collator;
    }

    public void setCollator(Collator collator2) {
        this.collator = collator2;
    }
}
