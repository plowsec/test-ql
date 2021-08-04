package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentsDS;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;
import com.mcafee.orion.core.util.resource.Resource;

public class AgentHandlerAssignmentsCR extends TableCellRendererImpl {
    Resource m_oResource = null;
    OrionUser m_oUser = null;

    public void setResource(Resource resource) {
        this.m_oResource = resource;
    }

    public void setUser(OrionUser orionUser) {
        this.m_oUser = orionUser;
    }

    public String formatCell(DisplayAdapter displayAdapter, String str, Object obj) throws PropertyAccessException {
        StringBuilder sb = new StringBuilder();
        String formatCell = AgentHandlerAssignmentsCR.super.formatCell(displayAdapter, str, obj);
        AgentHandlerAssignmentsDS.AgentHandlerAssignment agentHandlerAssignment = (AgentHandlerAssignmentsDS.AgentHandlerAssignment) obj;
        String htmlString = this.m_oResource.getHtmlString("delete.action", getLocale());
        if (agentHandlerAssignment.getHandlerAssignmentID() == -1 || !this.m_oUser.isAllowedForPermission("perm:ahRole.addEdit") || OrionCore.isCloudHosted()) {
            sb.append(formatCell);
        } else if ("actions".equals(str)) {
            sb.append("<a href=\"javascript:fnDelete(" + agentHandlerAssignment.getHandlerAssignmentID() + ")\">" + htmlString + "</a>");
        } else if ("assignment".equals(str)) {
            if (agentHandlerAssignment.getHasValidHandlers()) {
                sb.append("<a title='" + formatCell + "' href=\"javascript:fnEdit(" + agentHandlerAssignment.getHandlerAssignmentID() + ")\">");
                sb.append(formatCell);
                sb.append("</a>");
            } else {
                String formatString = this.m_oResource.formatString("ah.rule.invalid", getLocale(), new Object[0]);
                sb.append("<a href=\"javascript:fnEdit(" + agentHandlerAssignment.getHandlerAssignmentID() + ")\">");
                sb.append("<span class='errorText' title='" + formatString + "'>");
                sb.append(formatCell);
                sb.append("</span>");
                sb.append("</a>");
            }
        }
        return sb.toString();
    }
}
