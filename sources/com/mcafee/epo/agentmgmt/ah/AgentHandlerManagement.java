package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.core.EpoPermissionException;
import com.mcafee.epo.core.EpoValidateException;
import com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignment;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignmentPriority;
import com.mcafee.epo.core.model.EPOAgentHandlerGroup;
import com.mcafee.epo.core.model.EPOAgentHandlerRule_IPRange;
import com.mcafee.epo.core.model.EPOGroup;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.epo.core.services.EPOApacheNotifyService;
import com.mcafee.epo.core.services.EPOGroupServiceInternal;
import com.mcafee.epo.core.servlet.TextResponse;
import com.mcafee.epo.core.util.EPOIPUtil;
import com.mcafee.epo.repositorymgmt.engine.services.PackagingService;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.DataSourceUtil;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.servlet.Forward;
import com.mcafee.orion.core.servlet.NoResponse;
import com.mcafee.orion.core.servlet.PublicURL;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.servlet.util.ServletUtil;
import com.mcafee.orion.core.servlet.util.UserUtil;
import com.mcafee.orion.core.ui.TabStripAction;
import com.mcafee.orion.core.util.IPUtil;
import com.mcafee.orion.core.util.IPv4Range;
import com.mcafee.orion.core.util.IPv6Range;
import com.mcafee.orion.core.util.OrionURI;
import com.mcafee.orion.core.util.StringUtil;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class AgentHandlerManagement extends TabStripAction implements LocaleAware, UserAware, ConnectionBean {
    private static final String EDIT_HANDLER_CANCEL_URI = "ahEditHandlerCancelUri";
    private static final String GROUP = "GROUP";
    private static final String HANDLER = "HANDLER";
    private static final String HASMASTERHANDLER = "HASMASTERHANDLER";
    private static final String MASTERHANDLER = "MASTERHANDLER";
    private static final String UNDERSCORE = "_";
    static final Logger s_log = Logger.getLogger(AgentHandlerManagement.class);
    private EPOAgentHandlerServiceInternal agentHandlerService = null;
    private Connection connection = null;
    private EPOGroupServiceInternal groupService = null;
    private Locale locale = null;
    private PackagingService packagingService = null;
    private Resource resource = null;
    private OrionUser user = null;

    public Response EditHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException {
        EPORegisteredApacheServer ePORegisteredApacheServer;
        try {
            ePORegisteredApacheServer = this.agentHandlerService.getHandlerById(Integer.parseInt(httpServletRequest.getParameter("iID")), getUser());
        } catch (Exception e) {
            s_log.error(e.toString());
            ePORegisteredApacheServer = null;
        }
        if (ePORegisteredApacheServer == null) {
            return ServletUtil.forwardToFriendlyErrorPage(httpServletRequest, getResource().getString("error.invalidInputOrNoSuchObject", getLocale()));
        }
        setEditHandlerRequestData(httpServletRequest, ePORegisteredApacheServer);
        return new Forward("/AgentMgmt", "/EditHandler.jsp");
    }

    public void NotifyChanges() {
        new EPOApacheNotifyService().ApacheNotify(EPOApacheNotifyService.enumApacheNotifyMessages.AgentHandlerAssignmentsChanged, getConnection());
        getPackagingService().TriggerBackgroundSitelistProcessing(false);
    }

    private void setEditHandlerRequestData(HttpServletRequest httpServletRequest, EPORegisteredApacheServer ePORegisteredApacheServer) {
        httpServletRequest.setAttribute("cancelURI", "/AgentMgmt/showAgentHandlers.do");
        httpServletRequest.setAttribute("valueID", Integer.valueOf(ePORegisteredApacheServer.getAutoId()));
        httpServletRequest.setAttribute("title", getResource().formatString("agent.handler.settings", getLocale(), new Object[]{ePORegisteredApacheServer.getDnsName()}));
        httpServletRequest.setAttribute("HandlerName", ePORegisteredApacheServer.getDnsName());
        httpServletRequest.setAttribute("valueDnsName", ePORegisteredApacheServer.getPublishedDNSName());
        httpServletRequest.setAttribute("valueIpAddress", ePORegisteredApacheServer.getPublishedIP());
    }

    private boolean validateStringValue(String str, int i) {
        if (str == null || str.length() <= 0 || str.length() >= i) {
            return false;
        }
        return true;
    }

    private boolean validateObjectID(String str) {
        if (str != null) {
            return true;
        }
        return false;
    }

    public Response SaveHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        boolean z = false;
        String str = "";
        boolean z2 = true;
        try {
            String parameter = httpServletRequest.getParameter("iID");
            if (!validateObjectID(parameter)) {
                httpServletRequest.setAttribute("friendlyErrorMessage", getResource().formatString("AgentHandler.invalidID", getLocale(), new Object[0]));
                httpServletRequest.setAttribute("okUrl", "/AgentMgmt/home.do");
                return new Forward(PublicURL.ERROR_MESSAGE);
            }
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(parameter), getUser());
            String trim = httpServletRequest.getParameter("otbName_DnsName").trim();
            if (!trim.isEmpty() && !validateStringValue(trim, 255)) {
                str = getResource().getString("AgentHandler.invalidDnsName", getLocale());
                z2 = false;
            }
            String trim2 = httpServletRequest.getParameter("ipv6tName_IpAddress").trim();
            if (trim2.isEmpty() || EPOIPUtil.isValidIPAddress(trim2)) {
                z = z2;
            } else {
                str = getResource().getString("AgentHandler.invalidIPAddress", getLocale());
            }
            if (z) {
                handlerById.setPublishedDNSName(trim);
                handlerById.setPublishedIP(trim2);
                getAgentHandlerService().setResource(getResource());
                getAgentHandlerService().updateHandler(UserUtil.getOrionUser(httpServletRequest), handlerById);
                NotifyChanges();
                return new Forward("/AgentMgmt", "/showAgentHandlers.do");
            }
            setEditHandlerRequestData(httpServletRequest, handlerById);
            httpServletRequest.setAttribute("errorMsg", str);
            return new Forward("/AgentMgmt", "/EditHandler.jsp");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Response ToggleHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException, IOException, EpoValidateException {
        String str = null;
        try {
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(httpServletRequest.getParameter("iID")), getUser());
            if (handlerById == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else if (!handlerById.getMasterHandler()) {
                boolean z = !Boolean.valueOf(httpServletRequest.getParameter("bDisable")).booleanValue();
                getAgentHandlerService().setResource(getResource());
                getAgentHandlerService().enableHandler(UserUtil.getOrionUser(httpServletRequest), handlerById, z);
                NotifyChanges();
            } else {
                str = "error.togglePrimaryHandler";
            }
        } catch (NumberFormatException e) {
            NumberFormatException numberFormatException = e;
            str = "error.invalidInputOrNoSuchObject";
            s_log.error(numberFormatException.toString());
        } catch (EpoPermissionException e2) {
            EpoPermissionException epoPermissionException = e2;
            str = "error.permission";
            s_log.error(epoPermissionException.toString());
        }
        httpServletResponse.getWriter().write(str == null ? "SUCCESS" : getResource().getString(str, getLocale()));
        return new NoResponse();
    }

    public TextResponse getAhRuleViolations(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException {
        String string;
        try {
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(httpServletRequest.getParameter("iID")), getUser());
            if (handlerById == null) {
                string = getResource().getString("error.invalidInputOrNoSuchObject", getLocale());
            } else {
                string = getAhRuleViolationText(handlerById);
                if (!string.isEmpty()) {
                    string = "VIOLATIONS:" + string;
                }
            }
        } catch (Exception e) {
            s_log.error(e.toString());
            string = getResource().getString("error.invalidInputOrNoSuchObject", getLocale());
        }
        return new TextResponse(string);
    }

    private String getAhRuleViolationText(EPORegisteredApacheServer ePORegisteredApacheServer) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (EPOAgentHandlerAssignment ePOAgentHandlerAssignment : getEPOAgentHandlerAssignmentDBDao().getAll()) {
            for (EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority : ePOAgentHandlerAssignment.getAssociatedHandlerPriorities()) {
                if (ePOAgentHandlerAssignmentHandlerPriority.isHandler() && ePORegisteredApacheServer.getAutoId() == ePOAgentHandlerAssignmentHandlerPriority.getHandlerId()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(ePOAgentHandlerAssignment.getName());
                }
            }
        }
        return sb.toString();
    }

    private void commonGroup(HttpServletRequest httpServletRequest) throws SQLException, EpoValidateException, EpoPermissionException {
        String str;
        String str2 = "[";
        Iterator it = getAgentHandlerService().getHandlers(getUser()).iterator();
        while (true) {
            str = str2;
            if (!it.hasNext()) {
                break;
            }
            EPORegisteredApacheServer ePORegisteredApacheServer = (EPORegisteredApacheServer) it.next();
            str2 = str + "{name: '" + StringUtil.escapeJavaScriptString(ePORegisteredApacheServer.getDnsName()) + "', id: " + ePORegisteredApacheServer.getAutoId() + "},";
        }
        String substring = str.substring(0, str.length() - 1);
        if (substring.isEmpty()) {
            substring = "[";
        }
        httpServletRequest.setAttribute("handlersJSON", substring + "]");
        httpServletRequest.setAttribute("textAddHandler", getResource().getHtmlString("add.handlers", getLocale()));
        httpServletRequest.setAttribute("readOnly", "false");
    }

    public Response NewGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int i = 1;
        try {
            String formatString = getResource().formatString("new.group.name", getLocale(), new Object[]{1});
            while (this.agentHandlerService.getHandlerGroupByNameWithOutPermissionCheck(this.user, formatString) != null) {
                i++;
                formatString = getResource().formatString("new.group.name", getLocale(), new Object[]{Integer.valueOf(i)});
            }
            httpServletRequest.setAttribute("valueGroupName", formatString);
            commonGroup(httpServletRequest);
            httpServletRequest.setAttribute("checkedUseLoadBalancer", "true");
            httpServletRequest.setAttribute("checkedUseCustomHandlers", "false");
            httpServletRequest.setAttribute("valueGroupID", 0);
            return new Forward("/AgentMgmt", "/AddOrEditGroup.jsp");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Response ViewGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException, EpoPermissionException, EpoValidateException {
        int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
        commonGroup(httpServletRequest);
        EPOAgentHandlerGroup handlerGroupsById = this.agentHandlerService.getHandlerGroupsById(parseInt, this.user);
        if (handlerGroupsById == null) {
            return ServletUtil.forwardToFriendlyErrorPage(httpServletRequest, getResource().getString("error.invalidInputOrNoSuchObject", getLocale()));
        }
        httpServletRequest.setAttribute("valueGroupID", Integer.valueOf(parseInt));
        httpServletRequest.setAttribute("valueGroupName", handlerGroupsById.getName());
        httpServletRequest.setAttribute("valueVirtualDnsName", handlerGroupsById.getVirtualDNSName());
        httpServletRequest.setAttribute("valueVirtualIpAddress", handlerGroupsById.getVirtualIP());
        ArrayList arrayList = new ArrayList();
        int i = 1;
        Iterator it = handlerGroupsById.getAgentHandlers().iterator();
        while (true) {
            int i2 = i;
            if (!it.hasNext()) {
                break;
            }
            arrayList.add(i2 + ". " + ((EPORegisteredApacheServer) it.next()).getDnsName());
            i = i2 + 1;
        }
        httpServletRequest.setAttribute("handlerList", arrayList);
        if (handlerGroupsById.getLoadBalancerSet()) {
            httpServletRequest.setAttribute("checkedUseLoadBalancer", "true");
            httpServletRequest.setAttribute("checkedUseCustomHandlers", "false");
        } else {
            httpServletRequest.setAttribute("checkedUseLoadBalancer", "false");
            httpServletRequest.setAttribute("checkedUseCustomHandlers", "true");
        }
        if (!UserUtil.getOrionUser(httpServletRequest).isAllowedForPermission("perm:ahRole.addEdit")) {
            httpServletRequest.setAttribute("readOnly", "true");
        }
        return new Forward("/AgentMgmt", "/ViewGroup.jsp");
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00be A[Catch:{ NumberFormatException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00d8 A[Catch:{ NumberFormatException -> 0x0122 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00f4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.mcafee.orion.core.servlet.Response EditGroup(javax.servlet.http.HttpServletRequest r7, javax.servlet.http.HttpServletResponse r8) throws java.sql.SQLException, com.mcafee.epo.core.EpoPermissionException, com.mcafee.epo.core.EpoValidateException {
        /*
            r6 = this;
            r3 = 0
            r1 = 0
            java.lang.String r0 = "iID"
            java.lang.String r0 = r7.getParameter(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r2 = r6.agentHandlerService     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.orion.core.auth.OrionUser r4 = r6.user     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r1 = r2.getHandlerGroupsById(r0, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            if (r1 == 0) goto L_0x00df
            r6.commonGroup(r7)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "valueGroupID"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            r7.setAttribute(r2, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "valueGroupName"
            java.lang.String r4 = r1.getName()     // Catch:{ NumberFormatException -> 0x0122 }
            r7.setAttribute(r2, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "valueVirtualDnsName"
            java.lang.String r4 = r1.getVirtualDNSName()     // Catch:{ NumberFormatException -> 0x0122 }
            r7.setAttribute(r2, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "valueVirtualIpAddress"
            java.lang.String r4 = r1.getVirtualIP()     // Catch:{ NumberFormatException -> 0x0122 }
            r7.setAttribute(r2, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = ""
            com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao r4 = new com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao     // Catch:{ NumberFormatException -> 0x0122 }
            java.sql.Connection r5 = r6.connection     // Catch:{ NumberFormatException -> 0x0122 }
            r4.<init>(r5)     // Catch:{ NumberFormatException -> 0x0122 }
            java.util.List r0 = r4.getByGroupId(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            java.util.Iterator r4 = r0.iterator()     // Catch:{ NumberFormatException -> 0x0122 }
        L_0x004e:
            boolean r0 = r4.hasNext()     // Catch:{ NumberFormatException -> 0x0122 }
            if (r0 == 0) goto L_0x0088
            java.lang.Object r0 = r4.next()     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.epo.core.model.EPOAgentHandlerGroupToHandlers r0 = (com.mcafee.epo.core.model.EPOAgentHandlerGroupToHandlers) r0     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r5 = ""
            boolean r5 = r5.equals(r2)     // Catch:{ NumberFormatException -> 0x0122 }
            if (r5 == 0) goto L_0x006c
            int r0 = r0.getHandlerId()     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = java.lang.Integer.toString(r0)     // Catch:{ NumberFormatException -> 0x0122 }
        L_0x006a:
            r2 = r0
            goto L_0x004e
        L_0x006c:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ NumberFormatException -> 0x0122 }
            r5.<init>()     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.StringBuilder r2 = r5.append(r2)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r5 = ","
            java.lang.StringBuilder r2 = r2.append(r5)     // Catch:{ NumberFormatException -> 0x0122 }
            int r0 = r0.getHandlerId()     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.StringBuilder r0 = r2.append(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = r0.toString()     // Catch:{ NumberFormatException -> 0x0122 }
            goto L_0x006a
        L_0x0088:
            boolean r0 = r2.isEmpty()     // Catch:{ NumberFormatException -> 0x0122 }
            if (r0 == 0) goto L_0x0137
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r6.getAgentHandlerService()     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.orion.core.auth.OrionUser r4 = r6.getUser()     // Catch:{ NumberFormatException -> 0x0122 }
            java.util.List r0 = r0.getHandlers(r4)     // Catch:{ NumberFormatException -> 0x0122 }
            boolean r4 = r0.isEmpty()     // Catch:{ NumberFormatException -> 0x0122 }
            if (r4 != 0) goto L_0x0137
            r4 = 0
            java.lang.Object r0 = r0.get(r4)     // Catch:{ NumberFormatException -> 0x0122 }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r0 = (com.mcafee.epo.core.model.EPORegisteredApacheServer) r0     // Catch:{ NumberFormatException -> 0x0122 }
            if (r0 == 0) goto L_0x0137
            int r0 = r0.getAutoId()     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = java.lang.Integer.toString(r0)     // Catch:{ NumberFormatException -> 0x0122 }
            r2 = 1
            r3 = r2
        L_0x00b3:
            java.lang.String r2 = "csvHandlerIDs"
            r7.setAttribute(r2, r0)     // Catch:{ NumberFormatException -> 0x0122 }
            boolean r0 = r1.getLoadBalancerSet()     // Catch:{ NumberFormatException -> 0x0122 }
            if (r0 == 0) goto L_0x00f4
            java.lang.String r0 = "checkedUseLoadBalancer"
            java.lang.String r2 = "true"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = "checkedUseCustomHandlers"
            java.lang.String r2 = "false"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
        L_0x00cc:
            com.mcafee.orion.core.auth.OrionUser r0 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r7)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "perm:ahRole.addEdit"
            boolean r0 = r0.isAllowedForPermission(r2)     // Catch:{ NumberFormatException -> 0x0122 }
            if (r0 != 0) goto L_0x00df
            java.lang.String r0 = "readOnly"
            java.lang.String r2 = "true"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
        L_0x00df:
            if (r1 != 0) goto L_0x012d
            com.mcafee.orion.core.util.resource.Resource r0 = r6.getResource()
            java.lang.String r1 = "error.invalidInputOrNoSuchObject"
            java.util.Locale r2 = r6.getLocale()
            java.lang.String r0 = r0.getString(r1, r2)
            com.mcafee.orion.core.servlet.Forward r0 = com.mcafee.orion.core.servlet.util.ServletUtil.forwardToFriendlyErrorPage(r7, r0)
        L_0x00f3:
            return r0
        L_0x00f4:
            java.lang.String r0 = "checkedUseLoadBalancer"
            java.lang.String r2 = "false"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = "checkedUseCustomHandlers"
            java.lang.String r2 = "true"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
            if (r3 == 0) goto L_0x00cc
            com.mcafee.orion.core.util.resource.Resource r0 = r6.getResource()     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "error.group.missingHandler"
            java.util.Locale r3 = r6.getLocale()     // Catch:{ NumberFormatException -> 0x0122 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = r0.formatString(r2, r3, r4)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r2 = "handlerMissingMsg"
            r7.setAttribute(r2, r0)     // Catch:{ NumberFormatException -> 0x0122 }
            java.lang.String r0 = "setSaveOnLoad"
            java.lang.String r2 = "true"
            r7.setAttribute(r0, r2)     // Catch:{ NumberFormatException -> 0x0122 }
            goto L_0x00cc
        L_0x0122:
            r0 = move-exception
            org.apache.log4j.Logger r2 = s_log
            java.lang.String r0 = r0.toString()
            r2.error(r0)
            goto L_0x00df
        L_0x012d:
            com.mcafee.orion.core.servlet.Forward r0 = new com.mcafee.orion.core.servlet.Forward
            java.lang.String r1 = "/AgentMgmt"
            java.lang.String r2 = "/AddOrEditGroup.jsp"
            r0.<init>(r1, r2)
            goto L_0x00f3
        L_0x0137:
            r0 = r2
            goto L_0x00b3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.ah.AgentHandlerManagement.EditGroup(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):com.mcafee.orion.core.servlet.Response");
    }

    private String truncate(String str) {
        if (str.length() > 256) {
            return str.substring(0, 255);
        }
        return str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0103, code lost:
        if (r3.isEmpty() != false) goto L_0x0105;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.mcafee.orion.core.servlet.Response SaveGroup(javax.servlet.http.HttpServletRequest r12, javax.servlet.http.HttpServletResponse r13) throws java.sql.SQLException, com.mcafee.epo.core.EpoValidateException {
        /*
            r11 = this;
            r0 = 1
            r2 = 0
            r1 = -1
            java.lang.String r3 = "groupID"
            java.lang.String r3 = r12.getParameter(r3)     // Catch:{ NumberFormatException -> 0x0024 }
            int r1 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x0024 }
            r5 = r1
        L_0x000e:
            if (r5 >= 0) goto L_0x0027
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.invalidInput"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
        L_0x0023:
            return r0
        L_0x0024:
            r3 = move-exception
            r5 = r1
            goto L_0x000e
        L_0x0027:
            java.lang.String r1 = "USE_LOAD_BALANCER"
            java.lang.String r3 = "groupUsage"
            java.lang.String r3 = r12.getParameter(r3)
            boolean r1 = r1.equals(r3)
            if (r1 != 0) goto L_0x0075
            r1 = r0
        L_0x0036:
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.lang.String r3 = "handlerIDs"
            java.lang.String r3 = r12.getParameter(r3)     // Catch:{ Exception -> 0x0060 }
            java.lang.String r4 = ","
            java.lang.String[] r4 = r3.split(r4)     // Catch:{ Exception -> 0x0060 }
            if (r1 == 0) goto L_0x0077
            int r3 = r4.length     // Catch:{ Exception -> 0x0060 }
            if (r3 != 0) goto L_0x0077
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse     // Catch:{ Exception -> 0x0060 }
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()     // Catch:{ Exception -> 0x0060 }
            java.lang.String r2 = "error.invalidAgentHandlers"
            java.util.Locale r3 = r11.getLocale()     // Catch:{ Exception -> 0x0060 }
            java.lang.String r1 = r1.getString(r2, r3)     // Catch:{ Exception -> 0x0060 }
            r0.<init>(r1)     // Catch:{ Exception -> 0x0060 }
            goto L_0x0023
        L_0x0060:
            r0 = move-exception
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.invalidAgentHandlers"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
            goto L_0x0023
        L_0x0075:
            r1 = r2
            goto L_0x0036
        L_0x0077:
            int r7 = r4.length     // Catch:{ Exception -> 0x0060 }
            r3 = r2
        L_0x0079:
            if (r3 >= r7) goto L_0x0091
            r8 = r4[r3]     // Catch:{ Exception -> 0x0060 }
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r9 = r11.agentHandlerService     // Catch:{ Exception -> 0x0060 }
            int r8 = java.lang.Integer.parseInt(r8)     // Catch:{ Exception -> 0x0060 }
            com.mcafee.orion.core.auth.OrionUser r10 = r11.getUser()     // Catch:{ Exception -> 0x0060 }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r8 = r9.getHandlerById(r8, r10)     // Catch:{ Exception -> 0x0060 }
            r6.add(r8)     // Catch:{ Exception -> 0x0060 }
            int r3 = r3 + 1
            goto L_0x0079
        L_0x0091:
            java.lang.String r3 = "otbName_GroupName"
            java.lang.String r3 = r12.getParameter(r3)
            if (r3 == 0) goto L_0x00dc
            java.lang.String r3 = r3.trim()
            boolean r4 = r3.isEmpty()
            if (r4 != 0) goto L_0x00dc
            java.lang.String r4 = r11.truncate(r3)
            java.lang.String r7 = "<>'\""
            r3 = r2
        L_0x00aa:
            int r8 = r7.length()
            if (r3 >= r8) goto L_0x00bd
            char r8 = r7.charAt(r3)
            r9 = 32
            java.lang.String r4 = r4.replace(r8, r9)
            int r3 = r3 + 1
            goto L_0x00aa
        L_0x00bd:
            java.lang.String r7 = r4.trim()
            boolean r3 = r7.isEmpty()
            if (r3 == 0) goto L_0x00f1
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.blankGroupName"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
            goto L_0x0023
        L_0x00dc:
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.blankGroupName"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
            goto L_0x0023
        L_0x00f1:
            java.lang.String r3 = "otbName_VirtualDnsName"
            java.lang.String r3 = r12.getParameter(r3)
            if (r1 != 0) goto L_0x011a
            if (r3 == 0) goto L_0x0105
            java.lang.String r3 = r3.trim()
            boolean r4 = r3.isEmpty()
            if (r4 == 0) goto L_0x011a
        L_0x0105:
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.invalidInput"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
            goto L_0x0023
        L_0x011a:
            java.lang.String r8 = r11.truncate(r3)
            java.lang.String r3 = "ipv6tName_VirtualIpAddress"
            java.lang.String r3 = r12.getParameter(r3)
            java.lang.String r3 = org.apache.commons.lang.StringEscapeUtils.escapeHtml(r3)
            if (r3 != 0) goto L_0x012c
            java.lang.String r3 = ""
        L_0x012c:
            if (r1 != 0) goto L_0x015c
            java.lang.String r3 = r3.trim()
            boolean r4 = r3.isEmpty()
            if (r4 != 0) goto L_0x015c
            java.lang.String r3 = r11.truncate(r3)
            boolean r4 = com.mcafee.epo.core.util.EPOIPUtil.isValidIPAddress(r3)
            if (r4 != 0) goto L_0x015c
            com.mcafee.epo.core.servlet.TextResponse r1 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r4 = r11.getResource()
            java.lang.String r5 = "error.invalidIP"
            java.util.Locale r6 = r11.getLocale()
            java.lang.Object[] r0 = new java.lang.Object[r0]
            r0[r2] = r3
            java.lang.String r0 = r4.formatString(r5, r6, r0)
            r1.<init>(r0)
            r0 = r1
            goto L_0x0023
        L_0x015c:
            if (r5 != 0) goto L_0x015f
            r2 = r0
        L_0x015f:
            if (r2 == 0) goto L_0x019b
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r0 = new com.mcafee.epo.core.model.EPOAgentHandlerGroup     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.<init>()     // Catch:{ EpoPermissionException -> 0x0185 }
            r4 = r0
        L_0x0167:
            java.util.ArrayList r9 = new java.util.ArrayList     // Catch:{ EpoPermissionException -> 0x0185 }
            r9.<init>()     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setName(r7)     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r1 == 0) goto L_0x020f
            java.util.Iterator r3 = r6.iterator()     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x0175:
            boolean r0 = r3.hasNext()     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r0 == 0) goto L_0x01ba
            java.lang.Object r0 = r3.next()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r0 = (com.mcafee.epo.core.model.EPORegisteredApacheServer) r0     // Catch:{ EpoPermissionException -> 0x0185 }
            r9.add(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x0175
        L_0x0185:
            r0 = move-exception
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()
            java.lang.String r2 = "error.permission"
            java.util.Locale r3 = r11.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0.<init>(r1)
            goto L_0x0023
        L_0x019b:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r11.agentHandlerService     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.auth.OrionUser r4 = r11.user     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r0 = r0.getHandlerGroupsById(r5, r4)     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r0 != 0) goto L_0x02b3
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r2 = "error.invalidInputOrNoSuchObject"
            java.util.Locale r3 = r11.getLocale()     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r1 = r1.getString(r2, r3)     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.<init>(r1)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x0023
        L_0x01ba:
            r4.setAgentHandlers(r9)     // Catch:{ EpoPermissionException -> 0x0185 }
            r0 = 0
            r4.setLoadBalancerSet(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r0 = ""
            r4.setVirtualDNSName(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r0 = ""
            r4.setVirtualIP(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x01cb:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r11.getAgentHandlerService()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.util.resource.Resource r3 = r11.getResource()     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.setResource(r3)     // Catch:{ EpoPermissionException -> 0x0185 }
            r3 = 0
            java.sql.Connection r0 = r11.getConnection()     // Catch:{ all -> 0x0263 }
            java.lang.String r8 = "SELECT COUNT(*) FROM EPOAgentHandlerGroup WHERE [Name] = ? AND [AutoID] <> ?"
            java.sql.PreparedStatement r3 = r0.prepareStatement(r8)     // Catch:{ all -> 0x0263 }
            r0 = 1
            r3.setString(r0, r7)     // Catch:{ all -> 0x02b0 }
            r0 = 2
            r3.setInt(r0, r5)     // Catch:{ all -> 0x02b0 }
            java.sql.ResultSet r0 = r3.executeQuery()     // Catch:{ all -> 0x02b0 }
            r0.next()     // Catch:{ all -> 0x02b0 }
            r5 = 1
            int r0 = r0.getInt(r5)     // Catch:{ all -> 0x02b0 }
            if (r0 <= 0) goto L_0x021d
            com.mcafee.epo.core.servlet.TextResponse r0 = new com.mcafee.epo.core.servlet.TextResponse     // Catch:{ all -> 0x02b0 }
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()     // Catch:{ all -> 0x02b0 }
            java.lang.String r2 = "error.dupeGroupName"
            java.util.Locale r4 = r11.getLocale()     // Catch:{ all -> 0x02b0 }
            java.lang.String r1 = r1.getString(r2, r4)     // Catch:{ all -> 0x02b0 }
            r0.<init>(r1)     // Catch:{ all -> 0x02b0 }
            com.mcafee.orion.core.util.IOUtil.close(r3)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x0023
        L_0x020f:
            r0 = 1
            r4.setLoadBalancerSet(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setVirtualDNSName(r8)     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setVirtualIP(r3)     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setAgentHandlers(r9)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x01cb
        L_0x021d:
            com.mcafee.orion.core.util.IOUtil.close(r3)     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r2 == 0) goto L_0x0269
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r11.getAgentHandlerService()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.auth.OrionUser r2 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r12)     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.addHandlerGroup(r2, r4)     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x022d:
            if (r1 == 0) goto L_0x0259
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r11.getAgentHandlerService()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.auth.OrionUser r1 = r11.user     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r1 = r0.getHandlerGroupByNameWithOutPermissionCheck(r1, r7)     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r1 != 0) goto L_0x0275
            org.apache.log4j.Logger r0 = s_log     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ EpoPermissionException -> 0x0185 }
            r1.<init>()     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r2 = "Handler group, "
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.StringBuilder r1 = r1.append(r7)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r2 = ", not found"
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.lang.String r1 = r1.toString()     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.error(r1)     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x0259:
            r11.NotifyChanges()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.servlet.NoResponse r0 = new com.mcafee.orion.core.servlet.NoResponse
            r0.<init>()
            goto L_0x0023
        L_0x0263:
            r0 = move-exception
            r1 = r3
        L_0x0265:
            com.mcafee.orion.core.util.IOUtil.close(r1)     // Catch:{ EpoPermissionException -> 0x0185 }
            throw r0     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x0269:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r0 = r11.getAgentHandlerService()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.orion.core.auth.OrionUser r2 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r12)     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.updateHandlerGroup(r2, r4)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x022d
        L_0x0275:
            com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao r2 = new com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao     // Catch:{ EpoPermissionException -> 0x0185 }
            java.sql.Connection r0 = r11.connection     // Catch:{ EpoPermissionException -> 0x0185 }
            r2.<init>(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            int r0 = r1.getAutoId()     // Catch:{ EpoPermissionException -> 0x0185 }
            r2.deleteByGroupId(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            java.util.Iterator r3 = r6.iterator()     // Catch:{ EpoPermissionException -> 0x0185 }
        L_0x0287:
            boolean r0 = r3.hasNext()     // Catch:{ EpoPermissionException -> 0x0185 }
            if (r0 == 0) goto L_0x02aa
            java.lang.Object r0 = r3.next()     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r0 = (com.mcafee.epo.core.model.EPORegisteredApacheServer) r0     // Catch:{ EpoPermissionException -> 0x0185 }
            com.mcafee.epo.core.model.EPOAgentHandlerGroupToHandlers r4 = new com.mcafee.epo.core.model.EPOAgentHandlerGroupToHandlers     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.<init>()     // Catch:{ EpoPermissionException -> 0x0185 }
            int r5 = r1.getAutoId()     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setGroupId(r5)     // Catch:{ EpoPermissionException -> 0x0185 }
            int r0 = r0.getAutoId()     // Catch:{ EpoPermissionException -> 0x0185 }
            r4.setHandlerId(r0)     // Catch:{ EpoPermissionException -> 0x0185 }
            r2.save(r4)     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x0287
        L_0x02aa:
            java.sql.Connection r0 = r11.connection     // Catch:{ EpoPermissionException -> 0x0185 }
            r0.commit()     // Catch:{ EpoPermissionException -> 0x0185 }
            goto L_0x0259
        L_0x02b0:
            r0 = move-exception
            r1 = r3
            goto L_0x0265
        L_0x02b3:
            r4 = r0
            goto L_0x0167
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.ah.AgentHandlerManagement.SaveGroup(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):com.mcafee.orion.core.servlet.Response");
    }

    public Response EditPriority(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        AgentHandlerAssignmentsDS listDataSource = DataSourceUtil.getListDataSource(new OrionURI("datasource:Agent.Handler.Assignments.DS"));
        AgentHandlerEditAssignmentsCR agentHandlerEditAssignmentsCR = new AgentHandlerEditAssignmentsCR();
        listDataSource.setUser(getUser());
        if (listDataSource.getLength() == 0) {
            httpServletRequest.setAttribute("disableSave", "true");
        } else {
            httpServletRequest.setAttribute("disableSave", "false");
        }
        agentHandlerEditAssignmentsCR.setResource(getResource());
        listDataSource.setUser(getUser());
        if (httpServletRequest.getAttribute("errorMsg") != null) {
            httpServletRequest.setAttribute("errorMsg", httpServletRequest.getAttribute("errorMsg"));
        }
        httpServletRequest.setAttribute("cellRendererAttr", "agentHandlerEditAssignmentsCR");
        getUser().setAttribute("agentmgmt.handler.assignments.datasource", listDataSource);
        getUser().setAttribute("agentHandlerEditAssignmentsCR", agentHandlerEditAssignmentsCR);
        return new Forward("/AgentMgmt", "/EditPriority.jsp");
    }

    /* access modifiers changed from: package-private */
    public void commonAssignment(HttpServletRequest httpServletRequest, List<EPOAgentHandlerGroup> list) throws SQLException, EpoValidateException, EpoPermissionException {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        List handlers = getAgentHandlerService().getHandlers(getUser());
        String str7 = "[";
        Locale locale2 = getLocale();
        Iterator it = handlers.iterator();
        while (true) {
            str = str7;
            if (!it.hasNext()) {
                break;
            }
            EPORegisteredApacheServer ePORegisteredApacheServer = (EPORegisteredApacheServer) it.next();
            String dnsName = ePORegisteredApacheServer.getDnsName();
            if (ePORegisteredApacheServer.getMasterHandler()) {
                dnsName = dnsName.toUpperCase();
                str6 = "MASTERHANDLER_" + Integer.toString(ePORegisteredApacheServer.getAutoId());
            } else if (ePORegisteredApacheServer.getEnabled()) {
                str6 = "HANDLER_" + Integer.toString(ePORegisteredApacheServer.getAutoId());
            } else {
                dnsName = getResource().formatString("assignment.handler.disabled", getLocale(), new Object[]{dnsName});
                str6 = "HANDLER_" + Integer.toString(ePORegisteredApacheServer.getAutoId());
            }
            str7 = str + "{name: '" + StringUtil.escapeJavaScriptString(dnsName) + "', id: '" + str6 + "'},";
        }
        Iterator<EPOAgentHandlerGroup> it2 = list.iterator();
        while (true) {
            str2 = str;
            if (!it2.hasNext()) {
                break;
            }
            EPOAgentHandlerGroup next = it2.next();
            String name = next.getName();
            Iterator it3 = next.getAgentHandlers().iterator();
            while (true) {
                if (it3.hasNext()) {
                    if (((EPORegisteredApacheServer) it3.next()).getMasterHandler()) {
                        str3 = "HASMASTERHANDLER_";
                        break;
                    }
                } else {
                    str3 = "";
                    break;
                }
            }
            if (next.getEnabled()) {
                str4 = "GROUP_" + str3 + Integer.toString(next.getAutoId());
                str5 = name;
            } else {
                String formatString = getResource().formatString("assignment.handler.disabled", getLocale(), new Object[]{next.getName()});
                str4 = "GROUP_" + str3 + Integer.toString(next.getAutoId());
                str5 = formatString;
            }
            str = str2 + "{name: '" + StringUtil.escapeJavaScriptString(str5) + "', id: '" + str4 + "'},";
        }
        String substring = str2.substring(0, str2.length() - 1);
        if (substring.isEmpty()) {
            substring = "[";
        }
        httpServletRequest.setAttribute("handlersJSON", substring + "]");
        httpServletRequest.setAttribute("textAddHandler", getResource().formatString("add.handlers", locale2, new Object[0]));
        httpServletRequest.setAttribute("textAddTreeGroup", getResource().formatString("add.tree.group", locale2, new Object[0]));
        httpServletRequest.setAttribute("readOnly", "false");
    }

    public Response NewAssignment(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int i = 1;
        try {
            EPOAgentHandlerAssignmentDBDao ePOAgentHandlerAssignmentDBDao = getEPOAgentHandlerAssignmentDBDao();
            String parameter = httpServletRequest.getParameter("otbName_AssignmentName");
            if (parameter == null) {
                parameter = getResource().formatString("new.assignment.name", getLocale(), new Object[]{1});
                while (ePOAgentHandlerAssignmentDBDao.getByName(parameter) != null) {
                    i++;
                    parameter = getResource().formatString("new.assignment.name", getLocale(), new Object[]{Integer.valueOf(i)});
                }
            }
            httpServletRequest.setAttribute("valueAssignmentName", parameter);
            commonAssignment(httpServletRequest, getAgentHandlerService().getHandlerGroups(getUser()));
            httpServletRequest.setAttribute("valueAssignmentID", 0);
            httpServletRequest.setAttribute("checkedUseAllHandlers", "true");
            httpServletRequest.setAttribute("checkedUseCustomHandlers", "false");
            return new Forward("/AgentMgmt", "/AddOrEditAssignment.jsp");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Response EditAssignment(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException {
        String str;
        boolean z;
        String str2;
        String str3;
        String str4;
        try {
            int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
            EPOAgentHandlerAssignment byId = getEPOAgentHandlerAssignmentDBDao().getById(parseInt);
            if (byId == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                List handlerGroups = getAgentHandlerService().getHandlerGroups(getUser());
                commonAssignment(httpServletRequest, handlerGroups);
                httpServletRequest.setAttribute("valueAssignmentName", byId.getName());
                httpServletRequest.setAttribute("valueAssignmentID", Integer.valueOf(parseInt));
                List<EPOGroup> groups = byId.getGroups();
                Locale locale2 = getLocale();
                String str5 = "";
                for (EPOGroup ePOGroup : groups) {
                    if ("".equals(str5)) {
                        str4 = ePOGroup.getNodeID() + ";" + ePOGroup.getNodeType() + ";" + getGroupService().getPathToGroup(getUser(), getConnection(), ePOGroup, locale2);
                    } else {
                        str4 = str5 + "," + ePOGroup.getNodeID() + ";" + ePOGroup.getNodeType() + ";" + getGroupService().getPathToGroup(getUser(), getConnection(), ePOGroup, locale2);
                    }
                    str5 = str4;
                }
                httpServletRequest.setAttribute("csvTreeGroups", str5);
                String str6 = "";
                for (EPOAgentHandlerRule_IPRange ePOAgentHandlerRule_IPRange : byId.getIPRanges()) {
                    String str7 = (str6 + (str6.equals("") ? "" : "\n")) + EPOIPUtil.getIPString(ePOAgentHandlerRule_IPRange.getIP6Start());
                    int bitCount = ePOAgentHandlerRule_IPRange.getBitCount();
                    if (bitCount > 0) {
                        str3 = (str7 + "/") + bitCount;
                    } else if (IPUtil.compare(ePOAgentHandlerRule_IPRange.getIP6Start(), ePOAgentHandlerRule_IPRange.getIP6End()) != 0) {
                        str3 = (str7 + "-") + EPOIPUtil.getIPString(ePOAgentHandlerRule_IPRange.getIP6End());
                    } else {
                        str3 = str7;
                    }
                    str6 = str3;
                }
                httpServletRequest.setAttribute("valueAgentSubnets", str6);
                if (byId.getUseAllHandlers()) {
                    httpServletRequest.setAttribute("checkedUseAllHandlers", "true");
                    httpServletRequest.setAttribute("checkedUseCustomHandlers", "false");
                } else {
                    List<EPOAgentHandlerAssignmentHandlerPriority> associatedHandlerPriorities = byId.getAssociatedHandlerPriorities();
                    httpServletRequest.setAttribute("checkedUseAllHandlers", "false");
                    httpServletRequest.setAttribute("checkedUseCustomHandlers", "true");
                    String str8 = "";
                    for (EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority : associatedHandlerPriorities) {
                        int handlerId = ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                        if (handlerId > 0) {
                            z = this.agentHandlerService.getHandlerById(handlerId, getUser()).getMasterHandler();
                        } else {
                            z = false;
                        }
                        if ("".equals(str8)) {
                            if (ePOAgentHandlerAssignmentHandlerPriority.getHandlerId() <= 0) {
                                String str9 = "GROUP_";
                                if (isGroupWithMasterHandler(handlerGroups, ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId())) {
                                    str9 = str9 + HASMASTERHANDLER + UNDERSCORE;
                                }
                                str2 = str9 + ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId();
                            } else if (z) {
                                str2 = "MASTERHANDLER_" + ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                            } else {
                                str2 = "HANDLER_" + ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                            }
                        } else if (ePOAgentHandlerAssignmentHandlerPriority.getHandlerId() <= 0) {
                            String str10 = "GROUP_";
                            if (isGroupWithMasterHandler(handlerGroups, ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId())) {
                                str10 = str10 + HASMASTERHANDLER + UNDERSCORE;
                            }
                            str2 = str8 + "," + str10 + ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId();
                        } else if (z) {
                            str2 = str8 + "," + MASTERHANDLER + UNDERSCORE + ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                        } else {
                            str2 = str8 + "," + HANDLER + UNDERSCORE + ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                        }
                        str8 = str2;
                    }
                    httpServletRequest.setAttribute("csvHandlersOrGroups", str8);
                }
                if (!UserUtil.getOrionUser(httpServletRequest).isAllowedForPermission("perm:ahRole.addEdit")) {
                    httpServletRequest.setAttribute("readOnly", "true");
                }
                str = null;
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoValidateException e2) {
            s_log.error(e2.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoPermissionException e3) {
            s_log.error(e3.toString());
            str = "error.permission";
        }
        if (str == null) {
            return new Forward("/AgentMgmt", "/AddOrEditAssignment.jsp");
        }
        return ServletUtil.forwardToFriendlyErrorPage(httpServletRequest, getResource().getString(str, getLocale()));
    }

    private boolean isGroupWithMasterHandler(List<EPOAgentHandlerGroup> list, int i) {
        boolean z = false;
        if (list == null || list.size() == 0) {
            return false;
        }
        Iterator<EPOAgentHandlerGroup> it = list.iterator();
        while (true) {
            boolean z2 = z;
            if (!it.hasNext()) {
                return z2;
            }
            EPOAgentHandlerGroup next = it.next();
            if (next.getAutoId() == i) {
                for (EPORegisteredApacheServer masterHandler : next.getAgentHandlers()) {
                    if (masterHandler.getMasterHandler()) {
                        z2 = true;
                    }
                }
            }
            z = z2;
        }
    }

    public EPOAgentHandlerRule_IPRange addIPRule(String str) throws Exception {
        byte[] bArr;
        byte[] bArr2;
        int i = 0;
        int indexOf = str.indexOf("-");
        int indexOf2 = str.indexOf("/");
        if (indexOf == -1 && indexOf2 == -1) {
            byte[] address = IPUtil.getInet6Address(str).getAddress();
            bArr = address;
            bArr2 = address;
        } else if (indexOf != -1) {
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1);
            bArr2 = IPUtil.getInet6Address(substring).getAddress();
            byte[] address2 = IPUtil.getInet6Address(substring2).getAddress();
            byte[] address3 = IPUtil.getInet6Address("0").getAddress();
            if ((IPUtil.compare(bArr2, address2) == 1 && IPUtil.compare(address2, address3) == 1) || (IPUtil.compare(bArr2, address2) == -1 && IPUtil.compare(address2, address3) == -1)) {
                bArr = (byte[]) bArr2.clone();
                bArr2 = address2;
            } else {
                bArr = address2;
            }
        } else if (indexOf2 != -1) {
            String substring3 = str.substring(0, indexOf2);
            i = Integer.parseInt(str.substring(indexOf2 + 1));
            if (EPOIPUtil.isIPv4MappedAddress(IPUtil.getInet6Address(substring3).getAddress())) {
                IPv4Range iPv4Range = IPUtil.getIPv4Range(substring3, i);
                byte[] address4 = IPUtil.getInet6Address(iPv4Range.getMin()).getAddress();
                bArr = IPUtil.getInet6Address(iPv4Range.getMax()).getAddress();
                bArr2 = address4;
            } else {
                IPv6Range iPv6Range = IPUtil.getIPv6Range(substring3, i);
                byte[] address5 = iPv6Range.getMin().getAddress();
                bArr = iPv6Range.getMax().getAddress();
                bArr2 = address5;
            }
        } else {
            bArr = null;
            bArr2 = null;
        }
        EPOAgentHandlerRule_IPRange ePOAgentHandlerRule_IPRange = new EPOAgentHandlerRule_IPRange();
        ePOAgentHandlerRule_IPRange.setIP6Start(bArr2);
        ePOAgentHandlerRule_IPRange.setIP6End(bArr);
        ePOAgentHandlerRule_IPRange.setBitCount(i);
        return ePOAgentHandlerRule_IPRange;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:128:0x034d, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x034e, code lost:
        r3 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0393, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0394, code lost:
        r2 = r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.mcafee.orion.core.servlet.Response SaveAssignment(javax.servlet.http.HttpServletRequest r17, javax.servlet.http.HttpServletResponse r18) throws java.lang.Exception {
        /*
            r16 = this;
            r2 = -1
            java.lang.String r1 = "iID"
            r0 = r17
            java.lang.String r1 = r0.getParameter(r1)     // Catch:{ NumberFormatException -> 0x0025 }
            int r1 = java.lang.Integer.parseInt(r1)     // Catch:{ NumberFormatException -> 0x0025 }
            r7 = r1
        L_0x000e:
            if (r7 >= 0) goto L_0x0031
            com.mcafee.orion.core.util.resource.Resource r1 = r16.getResource()
            java.lang.String r2 = "error.invalidInput"
            java.util.Locale r3 = r16.getLocale()
            java.lang.String r1 = r1.getString(r2, r3)
            r0 = r17
            com.mcafee.orion.core.servlet.Forward r1 = com.mcafee.orion.core.servlet.util.ServletUtil.forwardToFriendlyErrorPage(r0, r1)
        L_0x0024:
            return r1
        L_0x0025:
            r1 = move-exception
            org.apache.log4j.Logger r3 = s_log
            java.lang.String r1 = r1.toString()
            r3.error(r1)
            r7 = r2
            goto L_0x000e
        L_0x0031:
            java.util.Locale r8 = r16.getLocale()
            java.lang.String r1 = "otbName_AssignmentName"
            r0 = r17
            java.lang.String r1 = r0.getParameter(r1)
            java.lang.String r9 = com.mcafee.orion.core.util.StringUtil.removeExtraWhitespace(r1)
            java.lang.String r1 = ""
            boolean r1 = r9.equals(r1)
            if (r1 == 0) goto L_0x0069
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()
            java.lang.String r3 = "error.noName"
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]
            java.lang.String r2 = r2.formatString(r3, r8, r4)
            r0 = r17
            r0.setAttribute(r1, r2)
            if (r7 != 0) goto L_0x0064
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)
            goto L_0x0024
        L_0x0064:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)
            goto L_0x0024
        L_0x0069:
            int r1 = r9.length()
            r2 = 256(0x100, float:3.59E-43)
            if (r1 <= r2) goto L_0x0091
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()
            java.lang.String r3 = "error.nameTooLong"
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]
            java.lang.String r2 = r2.formatString(r3, r8, r4)
            r0 = r17
            r0.setAttribute(r1, r2)
            if (r7 != 0) goto L_0x008c
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)
            goto L_0x0024
        L_0x008c:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)
            goto L_0x0024
        L_0x0091:
            r2 = 0
            com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao r1 = r16.getEPOAgentHandlerAssignmentDBDao()     // Catch:{ Exception -> 0x00ca }
            if (r7 != 0) goto L_0x00ee
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r1 = r1.getByName(r9)     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x00b8
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r3 = "assignment.exists"
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = r2.formatString(r3, r8, r4)     // Catch:{ Exception -> 0x00ca }
            r0 = r17
            r0.setAttribute(r1, r2)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x00b8:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r1 = new com.mcafee.epo.core.model.EPOAgentHandlerAssignment     // Catch:{ Exception -> 0x00ca }
            r1.<init>()     // Catch:{ Exception -> 0x00ca }
            r2 = 1
            r5 = r1
            r6 = r2
        L_0x00c0:
            if (r5 != 0) goto L_0x00f5
            java.lang.Exception r1 = new java.lang.Exception     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = "The agent handler assignment appears to no longer exist; the save failed."
            r1.<init>(r2)     // Catch:{ Exception -> 0x00ca }
            throw r1     // Catch:{ Exception -> 0x00ca }
        L_0x00ca:
            r1 = move-exception
            org.apache.log4j.Logger r2 = s_log
            java.lang.String r3 = "Failed to save HandlerAssignment"
            r2.error(r3, r1)
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()
            java.lang.String r3 = "error.generic"
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]
            java.lang.String r2 = r2.formatString(r3, r8, r4)
            r0 = r17
            r0.setAttribute(r1, r2)
            if (r7 != 0) goto L_0x038d
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)
            goto L_0x0024
        L_0x00ee:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r1 = r1.getById(r7)     // Catch:{ Exception -> 0x00ca }
            r5 = r1
            r6 = r2
            goto L_0x00c0
        L_0x00f5:
            java.lang.String r1 = "csvTreeGroups"
            r0 = r17
            java.lang.String r1 = r0.getParameter(r1)     // Catch:{ Exception -> 0x00ca }
            java.util.ArrayList r3 = new java.util.ArrayList     // Catch:{ Exception -> 0x00ca }
            r3.<init>()     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x014a
            int r2 = r1.length()     // Catch:{ Exception -> 0x00ca }
            if (r2 <= 0) goto L_0x014a
            java.lang.String r2 = "_"
            boolean r2 = r1.equals(r2)     // Catch:{ Exception -> 0x00ca }
            if (r2 != 0) goto L_0x014a
            java.lang.String r2 = ","
            java.lang.String[] r2 = r1.split(r2)     // Catch:{ Exception -> 0x00ca }
            int r4 = r2.length     // Catch:{ Exception -> 0x00ca }
            r1 = 0
        L_0x011a:
            if (r1 >= r4) goto L_0x014a
            r10 = r2[r1]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r11 = "_"
            boolean r11 = r10.equals(r11)     // Catch:{ Exception -> 0x00ca }
            if (r11 == 0) goto L_0x0129
        L_0x0126:
            int r1 = r1 + 1
            goto L_0x011a
        L_0x0129:
            java.lang.String r11 = "_"
            java.lang.String[] r10 = r10.split(r11)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.services.EPOGroupServiceInternal r11 = r16.getGroupService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.auth.OrionUser r12 = r16.getUser()     // Catch:{ Exception -> 0x00ca }
            java.sql.Connection r13 = r16.getConnection()     // Catch:{ Exception -> 0x00ca }
            r14 = 0
            r10 = r10[r14]     // Catch:{ Exception -> 0x00ca }
            int r10 = java.lang.Integer.parseInt(r10)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.model.EPOGroup r10 = r11.getGroupById(r12, r13, r10)     // Catch:{ Exception -> 0x00ca }
            r3.add(r10)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0126
        L_0x014a:
            r5.setGroups(r3)     // Catch:{ Exception -> 0x00ca }
            java.util.ArrayList r4 = new java.util.ArrayList     // Catch:{ Exception -> 0x00ca }
            r4.<init>()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r1 = "otaName_Subnets"
            r0 = r17
            java.lang.String r1 = r0.getParameter(r1)     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x01bf
            java.lang.String r2 = ""
            boolean r2 = r1.equals(r2)     // Catch:{ Exception -> 0x00ca }
            if (r2 != 0) goto L_0x01bf
            java.lang.String r1 = r1.trim()     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x01bf
            java.lang.String r2 = ""
            boolean r2 = r1.equals(r2)     // Catch:{ Exception -> 0x00ca }
            if (r2 != 0) goto L_0x01bf
            r2 = 0
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = "(\\s*,\\s*)|(\\s+,*\\s*)"
            java.lang.String[] r1 = r1.split(r2)     // Catch:{ Exception -> 0x00ca }
            java.util.List r1 = java.util.Arrays.asList(r1)     // Catch:{ Exception -> 0x00ca }
            java.util.HashSet r2 = new java.util.HashSet     // Catch:{ Exception -> 0x00ca }
            r2.<init>()     // Catch:{ Exception -> 0x00ca }
            java.util.Iterator r10 = r1.iterator()     // Catch:{ Exception -> 0x00ca }
        L_0x0188:
            boolean r1 = r10.hasNext()     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x0198
            java.lang.Object r1 = r10.next()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x00ca }
            r2.add(r1)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0188
        L_0x0198:
            int r1 = r2.size()     // Catch:{ Exception -> 0x00ca }
            java.lang.String[] r1 = new java.lang.String[r1]     // Catch:{ Exception -> 0x00ca }
            java.lang.Object[] r1 = r2.toArray(r1)     // Catch:{ Exception -> 0x00ca }
            java.lang.String[] r1 = (java.lang.String[]) r1     // Catch:{ Exception -> 0x00ca }
            java.lang.String[] r1 = (java.lang.String[]) r1     // Catch:{ Exception -> 0x00ca }
            java.util.List r2 = java.util.Arrays.asList(r1)     // Catch:{ Exception -> 0x00ca }
            java.util.Collections.sort(r2)     // Catch:{ Exception -> 0x00ca }
            int r10 = r1.length     // Catch:{ Exception -> 0x00ca }
            r2 = 0
        L_0x01af:
            if (r2 >= r10) goto L_0x01bf
            r11 = r1[r2]     // Catch:{ Exception -> 0x00ca }
            r0 = r16
            com.mcafee.epo.core.model.EPOAgentHandlerRule_IPRange r11 = r0.addIPRule(r11)     // Catch:{ Exception -> 0x00ca }
            r4.add(r11)     // Catch:{ Exception -> 0x00ca }
            int r2 = r2 + 1
            goto L_0x01af
        L_0x01bf:
            r5.setIPRanges(r4)     // Catch:{ Exception -> 0x00ca }
            int r1 = r3.size()     // Catch:{ Exception -> 0x00ca }
            if (r1 != 0) goto L_0x01f0
            int r1 = r4.size()     // Catch:{ Exception -> 0x00ca }
            if (r1 != 0) goto L_0x01f0
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r3 = "error.invalidCriteria"
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = r2.formatString(r3, r8, r4)     // Catch:{ Exception -> 0x00ca }
            r0 = r17
            r0.setAttribute(r1, r2)     // Catch:{ Exception -> 0x00ca }
            if (r7 != 0) goto L_0x01ea
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x01ea:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x01f0:
            java.lang.String r1 = "USE_ALL_HANDLERS"
            java.lang.String r2 = "orbName_HandlerUsage"
            r0 = r17
            java.lang.String r2 = r0.getParameter(r2)     // Catch:{ Exception -> 0x00ca }
            boolean r1 = r1.equals(r2)     // Catch:{ Exception -> 0x00ca }
            if (r1 == 0) goto L_0x026c
            r1 = 1
            r5.setUseAllHandlers(r1)     // Catch:{ Exception -> 0x00ca }
        L_0x0204:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r1 = r16.getAgentHandlerService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            r1.setResource(r2)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.util.resource.Resource r1 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = "ah.defaultAssignmentRule"
            r3 = 0
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r1 = r1.formatString(r2, r8, r3)     // Catch:{ Exception -> 0x00ca }
            boolean r1 = r9.equals(r1)     // Catch:{ Exception -> 0x00ca }
            if (r1 != 0) goto L_0x024b
            java.sql.Connection r2 = r16.getConnection()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r3 = "SELECT COUNT(*) FROM EPOAgentHandlerAssignment WHERE [Name] = ? AND [AutoID] <> ?"
            java.sql.PreparedStatement r4 = r2.prepareStatement(r3)     // Catch:{ Exception -> 0x00ca }
            r3 = 0
            r2 = 1
            r4.setString(r2, r9)     // Catch:{ Throwable -> 0x034b, all -> 0x0393 }
            r2 = 2
            r4.setInt(r2, r7)     // Catch:{ Throwable -> 0x034b, all -> 0x0393 }
            java.sql.ResultSet r2 = r4.executeQuery()     // Catch:{ Throwable -> 0x034b, all -> 0x0393 }
            r2.next()     // Catch:{ Throwable -> 0x034b, all -> 0x0393 }
            r10 = 1
            int r2 = r2.getInt(r10)     // Catch:{ Throwable -> 0x034b, all -> 0x0393 }
            if (r2 <= 0) goto L_0x0244
            r1 = 1
        L_0x0244:
            if (r4 == 0) goto L_0x024b
            if (r3 == 0) goto L_0x0346
            r4.close()     // Catch:{ Throwable -> 0x0340 }
        L_0x024b:
            if (r1 == 0) goto L_0x0366
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r3 = "error.dupeName"
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x00ca }
            r5 = 0
            r4[r5] = r9     // Catch:{ Exception -> 0x00ca }
            java.lang.String r2 = r2.formatString(r3, r8, r4)     // Catch:{ Exception -> 0x00ca }
            r0 = r17
            r0.setAttribute(r1, r2)     // Catch:{ Exception -> 0x00ca }
            if (r7 != 0) goto L_0x0360
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x026c:
            java.lang.String r1 = "csvHandlerOrGroups"
            r0 = r17
            java.lang.String r1 = r0.getParameter(r1)     // Catch:{ Exception -> 0x00ca }
            java.util.ArrayList r10 = new java.util.ArrayList     // Catch:{ Exception -> 0x00ca }
            r10.<init>()     // Catch:{ Exception -> 0x00ca }
            r2 = 0
            if (r1 == 0) goto L_0x0316
            int r3 = r1.length()     // Catch:{ Exception -> 0x00ca }
            if (r3 <= 0) goto L_0x0316
            java.lang.String r3 = ","
            java.lang.String[] r11 = r1.split(r3)     // Catch:{ Exception -> 0x00ca }
            r1 = 1
            int r12 = r11.length     // Catch:{ Exception -> 0x00ca }
            r3 = 0
            r4 = r3
        L_0x028c:
            if (r4 >= r12) goto L_0x0316
            r3 = r11[r4]     // Catch:{ Exception -> 0x00ca }
            java.lang.String r13 = "_"
            java.lang.String[] r3 = r3.split(r13)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority r13 = new com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority     // Catch:{ Exception -> 0x00ca }
            r13.<init>()     // Catch:{ Exception -> 0x00ca }
            r13.setAssignmentId(r7)     // Catch:{ Exception -> 0x00ca }
            r13.setPriority(r1)     // Catch:{ Exception -> 0x00ca }
            java.lang.String r14 = "HANDLER"
            r15 = 0
            r15 = r3[r15]     // Catch:{ Exception -> 0x00ca }
            boolean r14 = r14.equals(r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 != 0) goto L_0x02b7
            java.lang.String r14 = "MASTERHANDLER"
            r15 = 0
            r15 = r3[r15]     // Catch:{ Exception -> 0x00ca }
            boolean r14 = r14.equals(r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 == 0) goto L_0x02d9
        L_0x02b7:
            r14 = 1
            r3 = r3[r14]     // Catch:{ Exception -> 0x00ca }
            int r3 = java.lang.Integer.parseInt(r3)     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r14 = r16.getAgentHandlerService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.auth.OrionUser r15 = r16.getUser()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r14 = r14.getHandlerById(r3, r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 == 0) goto L_0x02d5
            r13.setHandlerId(r3)     // Catch:{ Exception -> 0x00ca }
            r2 = 1
            r10.add(r13)     // Catch:{ Exception -> 0x00ca }
            int r1 = r1 + 1
        L_0x02d5:
            int r3 = r4 + 1
            r4 = r3
            goto L_0x028c
        L_0x02d9:
            java.lang.String r14 = "GROUP"
            r15 = 0
            r15 = r3[r15]     // Catch:{ Exception -> 0x00ca }
            boolean r14 = r14.equals(r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 == 0) goto L_0x02d5
            java.lang.String r14 = "HASMASTERHANDLER"
            r15 = 1
            r15 = r3[r15]     // Catch:{ Exception -> 0x00ca }
            boolean r14 = r14.equals(r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 == 0) goto L_0x030e
            r14 = 2
            r3 = r3[r14]     // Catch:{ Exception -> 0x00ca }
            int r3 = java.lang.Integer.parseInt(r3)     // Catch:{ Exception -> 0x00ca }
        L_0x02f6:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r14 = r16.getAgentHandlerService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.auth.OrionUser r15 = r16.getUser()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r14 = r14.getHandlerGroupsById(r3, r15)     // Catch:{ Exception -> 0x00ca }
            if (r14 == 0) goto L_0x02d5
            r13.setHandlerGroupId(r3)     // Catch:{ Exception -> 0x00ca }
            r2 = 1
            r10.add(r13)     // Catch:{ Exception -> 0x00ca }
            int r1 = r1 + 1
            goto L_0x02d5
        L_0x030e:
            r14 = 1
            r3 = r3[r14]     // Catch:{ Exception -> 0x00ca }
            int r3 = java.lang.Integer.parseInt(r3)     // Catch:{ Exception -> 0x00ca }
            goto L_0x02f6
        L_0x0316:
            if (r2 == 0) goto L_0x0321
            r5.setAssociatedHandlerPriorities(r10)     // Catch:{ Exception -> 0x00ca }
            r1 = 0
            r5.setUseAllHandlers(r1)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0204
        L_0x0321:
            java.lang.String r1 = "errorMsg"
            com.mcafee.orion.core.util.resource.Resource r2 = r16.getResource()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r3 = "error.invalidAgentHandlers"
            java.lang.String r2 = r2.getString(r3, r8)     // Catch:{ Exception -> 0x00ca }
            r0 = r17
            r0.setAttribute(r1, r2)     // Catch:{ Exception -> 0x00ca }
            if (r7 != 0) goto L_0x033a
            com.mcafee.orion.core.servlet.Response r1 = r16.NewAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x033a:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x0340:
            r2 = move-exception
            r3.addSuppressed(r2)     // Catch:{ Exception -> 0x00ca }
            goto L_0x024b
        L_0x0346:
            r4.close()     // Catch:{ Exception -> 0x00ca }
            goto L_0x024b
        L_0x034b:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x034d }
        L_0x034d:
            r2 = move-exception
            r3 = r1
        L_0x034f:
            if (r4 == 0) goto L_0x0356
            if (r3 == 0) goto L_0x035c
            r4.close()     // Catch:{ Throwable -> 0x0357 }
        L_0x0356:
            throw r2     // Catch:{ Exception -> 0x00ca }
        L_0x0357:
            r1 = move-exception
            r3.addSuppressed(r1)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0356
        L_0x035c:
            r4.close()     // Catch:{ Exception -> 0x00ca }
            goto L_0x0356
        L_0x0360:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0024
        L_0x0366:
            r5.setName(r9)     // Catch:{ Exception -> 0x00ca }
            if (r6 == 0) goto L_0x0381
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r1 = r16.getAgentHandlerService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.auth.OrionUser r2 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r17)     // Catch:{ Exception -> 0x00ca }
            r1.addHandlerAssignment(r2, r5)     // Catch:{ Exception -> 0x00ca }
        L_0x0376:
            r16.NotifyChanges()     // Catch:{ Exception -> 0x00ca }
            java.lang.String r1 = "/AgentMgmt/home.do"
            com.mcafee.orion.core.servlet.Redirect r1 = com.mcafee.orion.core.servlet.ActionResponse.redirect(r1)
            goto L_0x0024
        L_0x0381:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r1 = r16.getAgentHandlerService()     // Catch:{ Exception -> 0x00ca }
            com.mcafee.orion.core.auth.OrionUser r2 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r17)     // Catch:{ Exception -> 0x00ca }
            r1.updateHandlerAssignment(r2, r5)     // Catch:{ Exception -> 0x00ca }
            goto L_0x0376
        L_0x038d:
            com.mcafee.orion.core.servlet.Response r1 = r16.EditAssignment(r17, r18)
            goto L_0x0024
        L_0x0393:
            r1 = move-exception
            r2 = r1
            goto L_0x034f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.ah.AgentHandlerManagement.SaveAssignment(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):com.mcafee.orion.core.servlet.Response");
    }

    public Response ConfirmDeleteHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException {
        String str = null;
        try {
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(httpServletRequest.getParameter("iID")), getUser());
            if (handlerById == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                httpServletRequest.setAttribute("message", getResource().formatHtmlString("confirm.delete.handler", getLocale(), new Object[]{handlerById.getDnsName()}));
                String ahRuleViolationText = getAhRuleViolationText(handlerById);
                httpServletRequest.setAttribute("violations", ahRuleViolationText);
                if (!ahRuleViolationText.isEmpty()) {
                    httpServletRequest.setAttribute("violationText", getResource().formatString("ah.toggleEnable.warning", getLocale(), new Object[0]));
                } else {
                    httpServletRequest.setAttribute("violationText", "");
                }
            }
        } catch (Exception e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        }
        if (str != null) {
            return ServletUtil.showDialogMessage(httpServletRequest, getResource().getString("error.genDlg.title", getLocale()), getResource().getString(str, getLocale()));
        }
        httpServletRequest.setAttribute("title", getResource().formatString("delete.handler", getLocale(), new Object[0]));
        return new Forward("/AgentMgmt", "/DeletePrompt.jsp");
    }

    public Response DeleteHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String str = null;
        try {
            int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(parseInt, getUser());
            if (handlerById == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else if (handlerById.getMasterHandler()) {
                return new NoResponse();
            } else {
                getAgentHandlerService().deleteHandlerByID(UserUtil.getOrionUser(httpServletRequest), parseInt);
                NotifyChanges();
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoPermissionException e2) {
            s_log.error(e2.toString());
            str = "error.permission";
        }
        httpServletResponse.getWriter().write(str == null ? "SUCCESS" : getResource().getString(str, getLocale()));
        return new NoResponse();
    }

    public Response ToggleGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException, IOException, EpoValidateException {
        String str = null;
        try {
            EPOAgentHandlerGroup handlerGroupsById = this.agentHandlerService.getHandlerGroupsById(Integer.parseInt(httpServletRequest.getParameter("iID")), this.user);
            if (handlerGroupsById == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                boolean z = !Boolean.valueOf(httpServletRequest.getParameter("bDisable")).booleanValue();
                getAgentHandlerService().setResource(getResource());
                getAgentHandlerService().toggleHandlerGroup(UserUtil.getOrionUser(httpServletRequest), handlerGroupsById, z);
                NotifyChanges();
            }
        } catch (NumberFormatException e) {
            NumberFormatException numberFormatException = e;
            str = "error.invalidInputOrNoSuchObject";
            s_log.error(numberFormatException.toString());
        } catch (EpoPermissionException e2) {
            EpoPermissionException epoPermissionException = e2;
            str = "error.permission";
            s_log.error(epoPermissionException.toString());
        }
        httpServletResponse.getWriter().write(str == null ? "SUCCESS" : getResource().getString(str, getLocale()));
        return new NoResponse();
    }

    public TextResponse getGroupRuleViolations(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException, EpoValidateException, EpoPermissionException {
        String string;
        try {
            EPOAgentHandlerGroup handlerGroupsById = this.agentHandlerService.getHandlerGroupsById(Integer.parseInt(httpServletRequest.getParameter("iID")), this.user);
            if (handlerGroupsById == null) {
                string = getResource().getString("error.invalidInputOrNoSuchObject", getLocale());
            } else {
                string = getGroupRuleViolationText(handlerGroupsById);
                if (!string.isEmpty()) {
                    string = "VIOLATIONS:" + string;
                }
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            string = getResource().getString("error.invalidInputOrNoSuchObject", getLocale());
        }
        return new TextResponse(string);
    }

    private String getGroupRuleViolationText(EPOAgentHandlerGroup ePOAgentHandlerGroup) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (EPOAgentHandlerAssignment ePOAgentHandlerAssignment : getEPOAgentHandlerAssignmentDBDao().getAll()) {
            for (EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority : ePOAgentHandlerAssignment.getAssociatedHandlerPriorities()) {
                if (ePOAgentHandlerAssignmentHandlerPriority.isHandlerGroup() && ePOAgentHandlerGroup.getAutoId() == ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId()) {
                    if (sb.length() != 0) {
                        sb.append(", ");
                    }
                    sb.append(ePOAgentHandlerAssignment.getName());
                }
            }
        }
        if (sb.length() != 0) {
            sb.insert(0, "VIOLATIONS:");
        }
        return sb.toString();
    }

    public Response ConfirmDeleteGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException, EpoValidateException {
        String str;
        boolean z;
        try {
            int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
            EPOAgentHandlerGroup handlerGroupsById = this.agentHandlerService.getHandlerGroupsById(parseInt, this.user);
            if (handlerGroupsById == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                boolean z2 = false;
                for (EPOAgentHandlerAssignment associatedHandlerPriorities : getAgentHandlerService().getHandlerAssignments(getUser())) {
                    Iterator it = associatedHandlerPriorities.getAssociatedHandlerPriorities().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            z = z2;
                            break;
                        }
                        EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority = (EPOAgentHandlerAssignmentHandlerPriority) it.next();
                        if (ePOAgentHandlerAssignmentHandlerPriority.isHandlerGroup() && ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId() == parseInt) {
                            z = true;
                            break;
                        }
                    }
                    z2 = z;
                }
                if (z2) {
                    httpServletRequest.setAttribute("disableOk", true);
                    httpServletRequest.setAttribute("message", getResource().formatHtmlString("delete.group.inuse", getLocale(), new Object[]{handlerGroupsById.getName()}));
                } else {
                    httpServletRequest.setAttribute("message", getResource().formatHtmlString("confirm.delete.group", getLocale(), new Object[]{handlerGroupsById.getName()}));
                }
                String groupRuleViolationText = getGroupRuleViolationText(handlerGroupsById);
                httpServletRequest.setAttribute("violations", groupRuleViolationText);
                if (!groupRuleViolationText.isEmpty()) {
                    httpServletRequest.setAttribute("violationText", getResource().formatString("ah.groupDelete.warning", getLocale(), new Object[0]));
                    str = null;
                } else {
                    httpServletRequest.setAttribute("violationText", "");
                    str = null;
                }
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoPermissionException e2) {
            s_log.error(e2.toString());
            str = "error.permission";
        }
        if (str != null) {
            return ServletUtil.showDialogMessage(httpServletRequest, getResource().getString("error.genDlg.title", getLocale()), getResource().getString(str, getLocale()));
        }
        httpServletRequest.setAttribute("title", getResource().formatString("delete.group", getLocale(), new Object[0]));
        return new Forward("/AgentMgmt", "/DeletePrompt.jsp");
    }

    public Response DeleteGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String str = null;
        try {
            getAgentHandlerService().deleteHandlerGroupByID(UserUtil.getOrionUser(httpServletRequest), Integer.parseInt(httpServletRequest.getParameter("iID")));
            NotifyChanges();
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoPermissionException e2) {
            s_log.error(e2.toString());
            str = "error.permission";
        }
        httpServletResponse.getWriter().write(str == null ? "SUCCESS" : getResource().getString(str, getLocale()));
        return new NoResponse();
    }

    public Response ConfirmDeleteAssignment(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SQLException {
        String str = null;
        try {
            EPOAgentHandlerAssignment byId = getEPOAgentHandlerAssignmentDBDao().getById(Integer.parseInt(httpServletRequest.getParameter("iID")));
            if (byId == null) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                httpServletRequest.setAttribute("message", getResource().formatHtmlString("confirm.delete.assignment", getLocale(), new Object[]{byId.getName()}));
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoValidateException e2) {
            s_log.error(e2.toString());
            str = "error.invalidInputOrNoSuchObject";
        }
        if (str != null) {
            httpServletRequest.setAttribute("disableOk", true);
            httpServletRequest.setAttribute("violationText", getResource().getString(str, getLocale()));
        }
        httpServletRequest.setAttribute("title", getResource().formatString("delete.assignment", getLocale(), new Object[0]));
        return new Forward("/AgentMgmt", "/DeletePrompt.jsp");
    }

    public Response DeleteAssignment(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String str = null;
        try {
            int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
            if (parseInt <= 0) {
                str = "error.invalidInputOrNoSuchObject";
            } else {
                getAgentHandlerService().deleteAssignmentByID(UserUtil.getOrionUser(httpServletRequest), parseInt);
                NotifyChanges();
            }
        } catch (NumberFormatException e) {
            s_log.error(e.toString());
            str = "error.invalidInputOrNoSuchObject";
        } catch (EpoPermissionException e2) {
            s_log.error(e2.toString());
            str = "error.permission";
        } catch (EpoValidateException e3) {
            s_log.error(e3.toString());
            str = "error.invalidInputOrNoSuchObject";
        }
        httpServletResponse.getWriter().write(str == null ? "SUCCESS" : getResource().getString(str, getLocale()));
        return new NoResponse();
    }

    public Response UpdatePriority(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        try {
            String[] split = httpServletRequest.getParameter("csvAssignmentIDs").split(",");
            ArrayList arrayList = new ArrayList();
            int length = split.length;
            int i = 0;
            boolean z = false;
            int i2 = 1;
            while (i < length) {
                String str = split[i];
                EPOAgentHandlerAssignmentPriority ePOAgentHandlerAssignmentPriority = new EPOAgentHandlerAssignmentPriority();
                ePOAgentHandlerAssignmentPriority.setAssignmentId(Integer.parseInt(str));
                ePOAgentHandlerAssignmentPriority.setPriority(i2);
                arrayList.add(ePOAgentHandlerAssignmentPriority);
                i++;
                z = true;
                i2++;
            }
            if (z) {
                getAgentHandlerService().setResource(getResource());
                getAgentHandlerService().UpdateAssignmentPriorities(UserUtil.getOrionUser(httpServletRequest), arrayList);
                NotifyChanges();
            }
            return new Forward("/AgentMgmt", "/home.do");
        } catch (Exception e) {
            httpServletRequest.setAttribute("errorMsg", getResource().getString("error.priority.generic", getLocale()));
            return EditPriority(httpServletRequest, httpServletResponse);
        }
    }

    public Response persistTwistyState(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List arrayList;
        int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
        boolean parseBoolean = Boolean.parseBoolean(httpServletRequest.getParameter("twistyState"));
        if (getUser().getAttribute("agentHandler.home.expandedNodes") != null) {
            arrayList = (List) getUser().getAttribute("agentHandler.home.expandedNodes");
        } else {
            arrayList = new ArrayList();
        }
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= arrayList.size()) {
                break;
            }
            if (((Integer) arrayList.get(i2)).intValue() == parseInt) {
                arrayList.remove(i2);
            }
            i = i2 + 1;
        }
        if (parseBoolean) {
            arrayList.add(Integer.valueOf(parseInt));
        }
        getUser().setAttribute("agentHandler.home.expandedNodes", arrayList);
        return new NoResponse();
    }

    public Response GetTwistyContent(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        boolean z;
        String dnsName;
        int parseInt = Integer.parseInt(httpServletRequest.getParameter("iID"));
        if (parseInt != -1) {
            try {
                EPOAgentHandlerAssignment byId = getEPOAgentHandlerAssignmentDBDao().getById(parseInt);
                if (byId == null) {
                    throw new Exception("Failed to retrieve the assignment.");
                }
                List<EPOGroup> groups = byId.getGroups();
                ArrayList arrayList = new ArrayList();
                for (EPOGroup pathToGroup : groups) {
                    arrayList.add(getGroupService().getPathToGroup(getUser(), getConnection(), pathToGroup, getLocale()));
                }
                List<EPOAgentHandlerRule_IPRange> iPRanges = byId.getIPRanges();
                ArrayList arrayList2 = new ArrayList();
                for (EPOAgentHandlerRule_IPRange ePOAgentHandlerRule_IPRange : iPRanges) {
                    arrayList2.add(EPOIPUtil.getIPString(ePOAgentHandlerRule_IPRange.getIP6Start()) + "-" + EPOIPUtil.getIPString(ePOAgentHandlerRule_IPRange.getIP6End()));
                }
                List<EPOAgentHandlerAssignmentHandlerPriority> associatedHandlerPriorities = byId.getAssociatedHandlerPriorities();
                ArrayList arrayList3 = new ArrayList();
                if (byId.getUseAllHandlers()) {
                    httpServletRequest.setAttribute("listHandlers", getResource().formatString("use.all.agent.handlers", getLocale(), new Object[0]));
                } else {
                    for (EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority : associatedHandlerPriorities) {
                        int handlerId = ePOAgentHandlerAssignmentHandlerPriority.getHandlerId();
                        if (handlerId > 0) {
                            z = this.agentHandlerService.getHandlerById(handlerId, getUser()).getMasterHandler();
                        } else {
                            z = false;
                        }
                        if (ePOAgentHandlerAssignmentHandlerPriority.getHandlerId() > 0) {
                            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(ePOAgentHandlerAssignmentHandlerPriority.getHandlerId(), getUser());
                            if (z) {
                                dnsName = handlerById.getDnsName().toUpperCase();
                            } else {
                                dnsName = handlerById.getDnsName();
                            }
                            if (handlerById.getEnabled()) {
                                arrayList3.add(dnsName);
                            } else {
                                arrayList3.add(getResource().formatString("assignment.handler.disabled", getLocale(), new Object[]{dnsName}));
                            }
                        } else {
                            EPOAgentHandlerGroup handlerGroupsById = this.agentHandlerService.getHandlerGroupsById(ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId(), this.user);
                            if (handlerGroupsById == null) {
                                return new TextResponse(getResource().getString("error.invalidInputOrNoSuchObject", getLocale()));
                            }
                            String name = handlerGroupsById.getName();
                            if (handlerGroupsById.getEnabled()) {
                                arrayList3.add(name);
                            } else {
                                arrayList3.add(getResource().formatString("assignment.handler.disabled", getLocale(), new Object[]{name}));
                            }
                        }
                    }
                    httpServletRequest.setAttribute("listHandlers", arrayList3);
                }
                httpServletRequest.setAttribute("listRanges", arrayList2);
                httpServletRequest.setAttribute("listPath", arrayList);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            httpServletRequest.setAttribute("listPath", getGroupService().getGroupName(getGroupService().getGroupById(getUser(), getConnection(), Integer.parseInt(getGroupService().getDirectoryRootNodeIDandType(getUser(), getConnection()).split(UNDERSCORE)[0])), getLocale()));
            httpServletRequest.setAttribute("listRanges", "");
            httpServletRequest.setAttribute("listHandlers", getResource().formatString("use.all.agent.handlers", getLocale(), new Object[0]));
        }
        return new Forward("/AgentMgmt", "/AssignmentTwistyContent.jsp");
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
    }

    public EPOAgentHandlerServiceInternal getAgentHandlerService() {
        return this.agentHandlerService;
    }

    public void setAgentHandlerService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.agentHandlerService = ePOAgentHandlerServiceInternal;
    }

    public EPOGroupServiceInternal getGroupService() {
        return this.groupService;
    }

    public void setGroupService(EPOGroupServiceInternal ePOGroupServiceInternal) {
        this.groupService = ePOGroupServiceInternal;
    }

    public PackagingService getPackagingService() {
        return this.packagingService;
    }

    public void setPackagingService(PackagingService packagingService2) {
        this.packagingService = packagingService2;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    public OrionUser getUser() {
        return this.user;
    }

    public void setUser(OrionUser orionUser) {
        this.user = orionUser;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setConnection(Connection connection2) {
        this.connection = connection2;
    }

    public class SelectItem {
        private String m_Id;
        private String m_Name;
        private boolean m_disabled;

        public SelectItem() {
            this.m_Name = "";
            this.m_Id = "";
        }

        public SelectItem(String str, String str2) {
            this.m_Name = str;
            this.m_Id = str2;
            this.m_disabled = false;
        }

        public SelectItem(String str, String str2, boolean z) {
            this.m_Name = str;
            this.m_Id = str2;
            this.m_disabled = z;
        }

        public String getName() {
            return this.m_Name;
        }

        public String getId() {
            return this.m_Id;
        }

        public boolean isDisabled() {
            return this.m_disabled;
        }

        public boolean getDisabled() {
            return isDisabled();
        }

        public void setDisabled(boolean z) {
            this.m_disabled = z;
        }
    }

    /* access modifiers changed from: protected */
    public EPOAgentHandlerAssignmentDBDao getEPOAgentHandlerAssignmentDBDao() {
        return new EPOAgentHandlerAssignmentDBDao(getConnection());
    }
}
