package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.orion.core.ui.DisplayAdapterImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerAssignmentsDA extends DisplayAdapterImpl {
    public static final String[] PROPERTIES = {"assignment", "actions"};

    public AgentHandlerAssignmentsDA(String str, Resource resource) {
        super(str, resource);
    }

    public String[] getPropertyNames() {
        return PROPERTIES;
    }
}
