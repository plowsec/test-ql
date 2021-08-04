package com.mcafee.epo.agentmgmt;

import com.mcafee.orion.core.plugin.DefaultPlugin;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentMgmtPlugin extends DefaultPlugin {
    private Resource m_oResourceException = null;

    public Resource getResourceException() {
        return this.m_oResourceException;
    }

    public void setResourceException(Resource resource) {
        this.m_oResourceException = resource;
    }
}
