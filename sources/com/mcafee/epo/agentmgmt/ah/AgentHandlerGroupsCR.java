package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerGroupsDS;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;
import com.mcafee.orion.core.util.resource.Resource;
import java.util.Locale;

public class AgentHandlerGroupsCR extends TableCellRendererImpl {
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
        String formatCell = AgentHandlerGroupsCR.super.formatCell(displayAdapter, str, obj);
        AgentHandlerGroupsDS.AgentHandlerGroup agentHandlerGroup = (AgentHandlerGroupsDS.AgentHandlerGroup) obj;
        String htmlString = this.m_oResource.getHtmlString("enable.action", getLocale());
        String htmlString2 = this.m_oResource.getHtmlString("disable.action", getLocale());
        String htmlString3 = this.m_oResource.getHtmlString("delete.action", getLocale());
        Locale locale = getLocale();
        if ("actions".equals(str)) {
            if (this.m_oUser.isAllowedForPermission("perm:ahRole.addEdit") && !OrionCore.isCloudHosted()) {
                if (agentHandlerGroup.getEnabled()) {
                    sb.append("<a href=\"javascript:fnToggle(" + agentHandlerGroup.getHandlerGroupID() + ", true)\">" + htmlString2 + "</a>");
                } else {
                    sb.append("<a href=\"javascript:fnToggle(" + agentHandlerGroup.getHandlerGroupID() + ", false)\">" + htmlString + "</a>");
                }
                sb.append("<label>&nbsp;|&nbsp;</label>");
                sb.append("<a href=\"javascript:fnDelete(" + agentHandlerGroup.getHandlerGroupID() + ")\">" + htmlString3 + "</a>");
            }
        } else if (str.equals("handlercount")) {
            if (agentHandlerGroup.isLoadBalancerSet()) {
                String formatString = this.m_oResource.formatString("not.applicable", locale, new Object[0]);
                sb.append("<span title=\"" + formatString + "\">");
                sb.append(formatString);
                sb.append("</span>");
            } else if (Integer.parseInt(formatCell) > 0) {
                int handlerGroupID = agentHandlerGroup.getHandlerGroupID();
                sb.append("<span title=\"" + formatCell + "\">");
                sb.append("<a style='align:center' href=\"/AgentMgmt/showHandlersForGroup.do?gId=" + handlerGroupID + "\">");
                sb.append(formatCell);
                sb.append("</a>");
                sb.append("</span>");
            } else {
                sb.append("<span title=\"" + formatCell + "\">");
                sb.append(formatCell);
                sb.append("</span>");
            }
        } else if (str.equals("agentcount")) {
            if (agentHandlerGroup.isLoadBalancerSet()) {
                String formatString2 = this.m_oResource.formatString("not.applicable", locale, new Object[0]);
                sb.append("<span title=\"" + formatString2 + "\">");
                sb.append(formatString2);
                sb.append("</span>");
            } else if (Integer.parseInt(formatCell.replace(",", "")) > 0) {
                int handlerGroupID2 = agentHandlerGroup.getHandlerGroupID();
                sb.append("<span title=\"" + formatCell + "\">");
                sb.append("<a style='align:center' href=\"/AgentMgmt/showAgentsForGroup.do?gId=" + handlerGroupID2 + "\">");
                sb.append(formatCell);
                sb.append("</a>");
                sb.append("</span>");
            } else {
                sb.append("<span title=\"" + formatCell + "\">");
                sb.append(formatCell);
                sb.append("</span>");
            }
        } else if ("groupname".equals(str)) {
            int handlerGroupID3 = agentHandlerGroup.getHandlerGroupID();
            String str2 = "fnEditViewOnly";
            if (this.m_oUser.isAllowedForPermission("perm:ahRole.addEdit") && !OrionCore.isCloudHosted()) {
                str2 = "fnEdit";
            }
            sb.append("<a title='" + formatCell + "' href=\"javascript:" + str2 + "(" + handlerGroupID3 + ")\">");
            sb.append(formatCell);
            sb.append("</a>");
        } else if (agentHandlerGroup.getEnabled()) {
            sb.append("<span title=\"" + formatCell + "\">");
            sb.append(formatCell);
            sb.append("</span>");
        } else {
            sb.append("<span disabled=true title=\"" + formatCell + "\">" + formatCell + "</span>");
        }
        return sb.toString();
    }

    public String[] getDisplayableColumns(ListDataSource listDataSource) {
        String[] displayableColumns = AgentHandlerGroupsCR.super.getDisplayableColumns(listDataSource);
        for (String equals : displayableColumns) {
            if (equals.equals("actions")) {
                return displayableColumns;
            }
        }
        if (!this.m_oUser.isAllowedForPermission("perm:ahRole.addEdit") || OrionCore.isCloudHosted()) {
            return displayableColumns;
        }
        String[] strArr = new String[(displayableColumns.length + 1)];
        System.arraycopy(displayableColumns, 0, strArr, 0, displayableColumns.length);
        strArr[displayableColumns.length] = "actions";
        return strArr;
    }
}
