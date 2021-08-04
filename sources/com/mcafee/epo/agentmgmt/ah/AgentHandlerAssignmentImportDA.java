package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.orion.core.ui.DisplayAdapterImpl;
import com.mcafee.orion.core.ui.DisplayContext;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerAssignmentImportDA extends DisplayAdapterImpl {
    public static final String[] PROPERTIES = {"name", "conflict"};

    public AgentHandlerAssignmentImportDA(String str, Resource resource) {
        super(str, resource);
    }

    public String[] getPropertyNames() {
        return PROPERTIES;
    }

    public String formatPropertyValue(String str, Object obj, DisplayContext displayContext) {
        if (!"conflict".equals(str)) {
            return AgentHandlerAssignmentImportDA.super.formatPropertyValue(str, obj, displayContext);
        }
        if (!(obj instanceof Boolean) || !((Boolean) obj).booleanValue()) {
            return getResource().getString("no", displayContext.getLocale());
        }
        return getResource().getString("yes", displayContext.getLocale());
    }
}
