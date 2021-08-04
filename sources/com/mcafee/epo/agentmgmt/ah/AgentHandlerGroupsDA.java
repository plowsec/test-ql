package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.orion.core.ui.DisplayAdapterImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerGroupsDA extends DisplayAdapterImpl {
    public static final String[] PROPERTIES = {"groupname", "virtualdnsname", "virtualipaddress", "loadbalancerset", "handlercount", "agentcount", "actions"};

    public AgentHandlerGroupsDA(String str, Resource resource) {
        super(str, resource);
    }

    public String[] getPropertyNames() {
        return PROPERTIES;
    }
}
