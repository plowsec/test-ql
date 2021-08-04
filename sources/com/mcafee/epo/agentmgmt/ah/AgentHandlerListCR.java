package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.computermgmt.agentmgmttemp.AgentHandlerListDS;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.PropertyAccessException;
import com.mcafee.orion.core.ui.control.table.TableCellRendererImpl;
import com.mcafee.orion.core.util.resource.Resource;
import org.apache.commons.lang.StringEscapeUtils;

public class AgentHandlerListCR extends TableCellRendererImpl {
    Resource m_oResource = null;

    public void setResource(Resource resource) {
        this.m_oResource = resource;
    }

    public String formatCell(DisplayAdapter displayAdapter, String str, Object obj) throws PropertyAccessException {
        boolean z;
        boolean z2;
        if (!getRequest().getUserPrincipal().isAllowedForPermission("perm:ahRole.addEdit") || OrionCore.isCloudHosted()) {
            z = true;
        } else {
            z = false;
        }
        StringBuilder sb = new StringBuilder();
        String formatCell = AgentHandlerListCR.super.formatCell(displayAdapter, str, obj);
        AgentHandlerListDS.AgentHandler agentHandler = (AgentHandlerListDS.AgentHandler) obj;
        String htmlString = this.m_oResource.getHtmlString("disable.action", getLocale());
        String htmlString2 = this.m_oResource.getHtmlString("enable.action", getLocale());
        String htmlString3 = this.m_oResource.getHtmlString("delete.action", getLocale());
        if (agentHandler.getVersionMatch() == 1) {
            z2 = false;
        } else {
            z2 = true;
        }
        if (!"actions".equals(str) || agentHandler.isMaster()) {
            if (!"actions".equals(str) || !agentHandler.isMaster()) {
                if (str.equals("agentcount")) {
                    int handlerID = agentHandler.getHandlerID();
                    if (Integer.parseInt(StringEscapeUtils.unescapeHtml(formatCell).replaceAll("[^a-zA-Z0-9\\s]", "").replace(",", "").replace(".", "")) > 0) {
                        sb.append("<span title=\"" + formatCell + "\">");
                        sb.append("<a style='align:center' href=\"/AgentMgmt/showAgents.do?ahId=" + handlerID + "\">");
                        sb.append(formatCell);
                        sb.append("</a>");
                        sb.append("</span>");
                    } else if (z2) {
                        sb.append("<span class=\"errorText\" title=\"" + formatCell + "\">" + formatCell + "</span>");
                    } else {
                        sb.append("<span title=\"" + formatCell + "\">");
                        sb.append(formatCell);
                        sb.append("</span>");
                    }
                } else if (str.equals("lastknowndnsname")) {
                    if (!z) {
                        sb.append("<a title='" + formatCell + "' href=\"javascript:fnEdit(" + agentHandler.getHandlerID() + ")\">");
                    }
                    if (!z2) {
                        sb.append("<span title=\"" + formatCell + "\">");
                        sb.append(formatCell);
                        sb.append("</span>");
                    } else {
                        sb.append("<span class=\"errorText\" title='" + this.m_oResource.formatString("AgentHandler.versionMismatch", getLocale(), new Object[]{formatCell}) + "'>" + formatCell + "</span>");
                    }
                    if (!z) {
                        sb.append("</a>");
                    }
                } else if (z2) {
                    sb.append("<span class=\"errorText\" title=\"" + formatCell + "\">" + formatCell + "</span>");
                } else if (agentHandler.getEnabled()) {
                    sb.append("<span title=\"" + formatCell + "\">");
                    sb.append(formatCell);
                    sb.append("</span>");
                } else {
                    sb.append("<span disabled=true title=\"" + formatCell + "\">" + formatCell + "</span>");
                }
            }
        } else if (!z2 && !z) {
            if (agentHandler.getEnabled()) {
                sb.append("<a href=\"javascript:fnToggle(" + agentHandler.getHandlerID() + ", true)\">" + htmlString + "</a>");
            } else {
                sb.append("<a href=\"javascript:fnToggle(" + agentHandler.getHandlerID() + ", false)\">" + htmlString2 + "</a>");
            }
            sb.append("<label>&nbsp;|&nbsp;</label>");
            sb.append("<a href=\"javascript:fnDelete(" + agentHandler.getHandlerID() + ")\">" + htmlString3 + "</a>");
        } else if (z2 && !z) {
            sb.append("<span style='color:#959595'>" + htmlString2 + "</span>");
            sb.append("<label>&nbsp;|&nbsp;</label>");
            sb.append("<a href=\"javascript:fnDelete(" + agentHandler.getHandlerID() + ")\">" + htmlString3 + "</a>");
        } else if (z) {
            sb.append("<span style='color:#959595'>" + htmlString2 + "</span>");
            sb.append("<label>&nbsp;|&nbsp;</label>");
            sb.append("<span style='color:#959595'>" + htmlString3 + "</span>");
        }
        return sb.toString();
    }

    public String[] getDisplayableColumns(ListDataSource listDataSource) {
        OrionUser user = getUser();
        String[] displayableColumns = AgentHandlerListCR.super.getDisplayableColumns(listDataSource);
        for (String equals : displayableColumns) {
            if (equals.equals("actions")) {
                return displayableColumns;
            }
        }
        if (!user.isAllowedForPermission("perm:ahRole.addEdit") || OrionCore.isCloudHosted()) {
            return displayableColumns;
        }
        String[] strArr = new String[(displayableColumns.length + 1)];
        System.arraycopy(displayableColumns, 0, strArr, 0, displayableColumns.length);
        strArr[displayableColumns.length] = "actions";
        return strArr;
    }
}
