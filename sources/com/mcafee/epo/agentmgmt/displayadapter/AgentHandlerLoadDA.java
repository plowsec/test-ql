package com.mcafee.epo.agentmgmt.displayadapter;

import com.mcafee.orion.core.ui.DisplayAdapterImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerLoadDA extends DisplayAdapterImpl {
    public static final String[] PROPERTIES = {"agentHandlerName", "agentCount"};

    public AgentHandlerLoadDA(String str, Resource resource) {
        super(str, resource);
    }

    public String[] getPropertyNames() {
        return PROPERTIES;
    }
}
