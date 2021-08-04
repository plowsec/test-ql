package com.mcafee.epo.agentmgmt.renderer;

import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerLoadDataCR extends TableCellRendererImpl {
    Resource resource = null;

    public AgentHandlerLoadDataCR(Resource resource2) {
        super(true);
        setResource(resource2);
    }

    public String formatCell(DisplayAdapter displayAdapter, String str, Object obj) throws PropertyAccessException {
        return AgentHandlerLoadDataCR.super.formatCell(displayAdapter, str, obj);
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
    }
}
