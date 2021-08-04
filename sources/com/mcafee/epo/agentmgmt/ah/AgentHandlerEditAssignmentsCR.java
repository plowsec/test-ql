package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentsDS;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerEditAssignmentsCR extends TableCellRendererImpl {
    Resource m_oResource = null;

    public Resource getResource() {
        return this.m_oResource;
    }

    public void setResource(Resource resource) {
        this.m_oResource = resource;
    }

    public String formatCell(DisplayAdapter displayAdapter, String str, Object obj) throws PropertyAccessException {
        StringBuilder sb = new StringBuilder();
        String formatCell = AgentHandlerEditAssignmentsCR.super.formatCell(displayAdapter, str, obj);
        AgentHandlerAssignmentsDS.AgentHandlerAssignment agentHandlerAssignment = (AgentHandlerAssignmentsDS.AgentHandlerAssignment) obj;
        String htmlString = this.m_oResource.getHtmlString("move.to.top", getLocale());
        if ("actions".equals(str)) {
            sb.append("<a href=\"javascript:fnMoveToTop(" + agentHandlerAssignment.getHandlerAssignmentID() + ")\">" + htmlString + "</a>");
        } else {
            sb.append(formatCell);
        }
        return sb.toString();
    }
}
