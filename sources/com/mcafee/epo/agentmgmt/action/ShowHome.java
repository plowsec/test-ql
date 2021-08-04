package com.mcafee.epo.agentmgmt.action;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentsCR;
import com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentsDS;
import com.mcafee.epo.agentmgmt.ah.AgentHandlerGroupsCR;
import com.mcafee.epo.agentmgmt.ah.AgentHandlerGroupsDS;
import com.mcafee.epo.agentmgmt.ah.AgentHandlerListCR;
import com.mcafee.epo.computermgmt.agentmgmttemp.AgentHandlerListDS;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.epo.core.servlet.XMLResponse;
import com.mcafee.epo.core.util.EPODisplayUtil;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.DataSourceUtil;
import com.mcafee.orion.core.data.QueryDataSourceFactory;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.query.OrionQuery;
import com.mcafee.orion.core.query.OrionQueryAggregation;
import com.mcafee.orion.core.query.OrionQueryGrouping;
import com.mcafee.orion.core.query.QueryChartDataSource;
import com.mcafee.orion.core.query.table.TableService;
import com.mcafee.orion.core.servlet.ActionResponse;
import com.mcafee.orion.core.servlet.Forward;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.servlet.util.UserUtil;
import com.mcafee.orion.core.util.OrionURI;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowHome implements UserAware, ConnectionBean, LocaleAware {
    private EPOAgentHandlerServiceInternal agentHandlerService = null;
    private Connection connection = null;
    private QueryDataSourceFactory factory = null;
    private Locale locale;
    private Resource resource = null;
    private TableService tableService = null;
    private OrionUser user = null;

    public Response showHome(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Object arrayList;
        Locale locale2 = getLocale();
        AgentHandlerAssignmentsDS listDataSource = DataSourceUtil.getListDataSource(new OrionURI("datasource:Agent.Handler.Assignments.DS"));
        AgentHandlerAssignmentsCR agentHandlerAssignmentsCR = new AgentHandlerAssignmentsCR();
        listDataSource.setLocale(this.locale);
        listDataSource.ClearCachedData();
        agentHandlerAssignmentsCR.setResource(this.resource);
        agentHandlerAssignmentsCR.setUser(this.user);
        listDataSource.setUser(this.user);
        listDataSource.setShowDefaultRule(true);
        httpServletRequest.setAttribute("cellRendererAttr", "agentHandlerAssignmentsCR");
        httpServletRequest.setAttribute("agentmgmt.handler.assignments.datasource", listDataSource);
        httpServletRequest.setAttribute("agentHandlerAssignmentsCR", agentHandlerAssignmentsCR);
        int size = this.agentHandlerService.getHandlerAssignments(this.user).size();
        if (size > 1) {
            httpServletRequest.setAttribute("statusEditPriorityButton", "false");
        } else {
            httpServletRequest.setAttribute("statusEditPriorityButton", "true");
        }
        if (size > 0) {
            httpServletRequest.setAttribute("statusExportButton", "false");
        } else {
            httpServletRequest.setAttribute("statusExportButton", "true");
        }
        if (isEditAllowed()) {
            httpServletRequest.setAttribute("tableProperties", "assignment,actions");
        } else {
            httpServletRequest.setAttribute("tableProperties", "assignment");
        }
        if (this.user.getAttribute("agentHandler.home.expandedNodes") != null) {
            arrayList = (List) this.user.getAttribute("agentHandler.home.expandedNodes");
        } else {
            arrayList = new ArrayList();
        }
        httpServletRequest.setAttribute("expandedNodes", arrayList);
        httpServletRequest.setAttribute("chartHeight", "160");
        int contentWidth = EPODisplayUtil.getContentWidth(httpServletRequest);
        Double valueOf = Double.valueOf(((double) contentWidth) * 0.45d);
        httpServletRequest.setAttribute("chartWidth", Integer.toString(valueOf.intValue()));
        httpServletRequest.setAttribute("boxWidth", Integer.toString(((contentWidth - valueOf.intValue()) - 20) / 2));
        httpServletRequest.setAttribute("chartTitle", this.resource.formatString("ah.chart.title", locale2, new Object[0]));
        if (this.tableService.isAuthorized(this.user, "EPOComputerProperties")) {
            OrionQuery orionQuery = new OrionQuery();
            orionQuery.setName("Handler Assignments");
            orionQuery.setTarget("EPOLeafNode");
            orionQuery.setQueryType("pie.pie");
            orionQuery.setUniqueKey("EPOAgentHandlers.AutoID");
            OrionQueryAggregation orionQueryAggregation = new OrionQueryAggregation();
            orionQueryAggregation.setType("count");
            orionQuery.addAggreation(orionQueryAggregation);
            orionQuery.addGrouping(new OrionQueryGrouping("EPOComputerProperties.LastAgentHandler", (String) null, "desc", "6", "true"));
            orionQuery.setCondition("(where (isNotNull EPOLeafNode.AgentGUID ))");
            orionQuery.getTableData().setTableColumns("count AS count, EPOAgentHandlers.ComputerName");
            QueryChartDataSource createQueryChartDataSource = this.factory.createQueryChartDataSource(this.user, orionQuery);
            httpServletRequest.setAttribute("datasourceAttr", "ahChartDS");
            this.user.setAttribute("ahChartDS", createQueryChartDataSource);
        } else {
            httpServletRequest.setAttribute("noChartPerm", "true");
        }
        return ActionResponse.forward("/AgentMgmt", "/home/home.jsp");
    }

    public Response loadHandlerList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        List<EPORegisteredApacheServer> handlers = this.agentHandlerService.getHandlers(this.user);
        long time = new Date().getTime();
        int i = 0;
        int i2 = 0;
        for (EPORegisteredApacheServer ePORegisteredApacheServer : handlers) {
            i2++;
            if (ePORegisteredApacheServer.getEnabled() && (ePORegisteredApacheServer.getLastUpdate().getTime() <= 0 || time - ePORegisteredApacheServer.getLastUpdate().getTime() >= 300000)) {
                i++;
            }
            i = i;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        stringBuffer.append("<results>");
        stringBuffer.append("<serverCount>" + i2 + "</serverCount>");
        stringBuffer.append("<serverCountOutdated>" + i + "</serverCountOutdated>");
        stringBuffer.append("</results>");
        return new XMLResponse(stringBuffer.toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        r0 = 0;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0057, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0058, code lost:
        com.mcafee.orion.core.util.IOUtil.close(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005b, code lost:
        throw r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0057 A[ExcHandler: all (r0v0 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r2 
      PHI: (r2v1 java.sql.PreparedStatement) = (r2v0 java.sql.PreparedStatement), (r2v5 java.sql.PreparedStatement), (r2v5 java.sql.PreparedStatement) binds: [B:1:0x0002, B:2:?, B:4:0x0010] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0002] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.mcafee.orion.core.servlet.Response loadGroupsList(javax.servlet.http.HttpServletRequest r6, javax.servlet.http.HttpServletResponse r7) {
        /*
            r5 = this;
            r3 = 0
            r2 = 0
            java.sql.Connection r0 = r5.connection     // Catch:{ Exception -> 0x0050, all -> 0x0057 }
            java.lang.String r1 = "select Enabled, count(*) FROM EPOAgentHandlerGroup group by Enabled"
            java.sql.PreparedStatement r2 = r0.prepareStatement(r1)     // Catch:{ Exception -> 0x0050, all -> 0x0057 }
            java.sql.ResultSet r4 = r2.executeQuery()     // Catch:{ Exception -> 0x0050, all -> 0x0057 }
            r0 = r3
            r1 = r3
        L_0x0010:
            boolean r3 = r4.next()     // Catch:{ Exception -> 0x005c, all -> 0x0057 }
            if (r3 == 0) goto L_0x0029
            r3 = 1
            boolean r3 = r4.getBoolean(r3)     // Catch:{ Exception -> 0x005c, all -> 0x0057 }
            if (r3 == 0) goto L_0x0023
            r3 = 2
            int r1 = r4.getInt(r3)     // Catch:{ Exception -> 0x005c, all -> 0x0057 }
            goto L_0x0010
        L_0x0023:
            r3 = 2
            int r0 = r4.getInt(r3)     // Catch:{ Exception -> 0x005c, all -> 0x0057 }
            goto L_0x0010
        L_0x0029:
            com.mcafee.orion.core.util.IOUtil.close(r2)
        L_0x002c:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<results><numGroups>"
            r2.<init>(r3)
            int r1 = r1 + r0
            java.lang.StringBuilder r1 = r2.append(r1)
            java.lang.String r3 = "</numGroups><numDisabledGroups>"
            r1.append(r3)
            java.lang.StringBuilder r0 = r2.append(r0)
            java.lang.String r1 = "</numDisabledGroups></results>"
            r0.append(r1)
            com.mcafee.epo.core.servlet.XMLResponse r0 = new com.mcafee.epo.core.servlet.XMLResponse
            java.lang.String r1 = r2.toString()
            r0.<init>(r1)
            return r0
        L_0x0050:
            r0 = move-exception
            r0 = r3
            r1 = r3
        L_0x0053:
            com.mcafee.orion.core.util.IOUtil.close(r2)
            goto L_0x002c
        L_0x0057:
            r0 = move-exception
            com.mcafee.orion.core.util.IOUtil.close(r2)
            throw r0
        L_0x005c:
            r3 = move-exception
            goto L_0x0053
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.action.ShowHome.loadGroupsList(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):com.mcafee.orion.core.servlet.Response");
    }

    public Response showAgentHandlers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        OrionUser orionUser = UserUtil.getOrionUser(httpServletRequest);
        Locale locale2 = getLocale();
        AgentHandlerListDS listDataSource = DataSourceUtil.getListDataSource(new OrionURI("datasource:Agent.Handler.List.DS"));
        if ("true".equals(httpServletRequest.getParameter("inactiveOnly"))) {
            listDataSource.setFilterID("ah.list.filter.inactive");
            httpServletRequest.setAttribute("title", this.resource.getString("ah.list.inactive.titlebar", locale2));
        } else {
            httpServletRequest.setAttribute("title", this.resource.getString("ah.list.titlebar", locale2));
        }
        AgentHandlerListCR agentHandlerListCR = new AgentHandlerListCR();
        listDataSource.setUser(orionUser);
        agentHandlerListCR.setResource(this.resource);
        httpServletRequest.setAttribute("cellRendererAttr", "agentHandlerListCR");
        orionUser.setAttribute("agentmgmt.handler.list.datasource", listDataSource);
        orionUser.setAttribute("agentHandlerListCR", agentHandlerListCR);
        if (isEditAllowed()) {
            httpServletRequest.setAttribute("tableProperties", "lastknowndnsname,lastknownipaddress,publisheddnsname,publishedipaddress,lastcommunication,version,actions");
        } else {
            httpServletRequest.setAttribute("tableProperties", "lastknowndnsname,lastknownipaddress,publisheddnsname,publishedipaddress,lastcommunication,version");
        }
        return new Forward("/AgentMgmt", "/home/showHandlers.jsp");
    }

    public ActionResponse showAgentHandlerGroups(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        OrionUser orionUser = UserUtil.getOrionUser(httpServletRequest);
        if (((AgentHandlerGroupsDS) this.user.getAttribute("agentmgmt.handler.groups.datasource")) == null) {
            AgentHandlerGroupsDS listDataSource = DataSourceUtil.getListDataSource(new OrionURI("datasource:Agent.Handler.Groups.DS"));
            listDataSource.setUser(orionUser);
            orionUser.setAttribute("agentmgmt.handler.groups.datasource", listDataSource);
        }
        AgentHandlerGroupsCR agentHandlerGroupsCR = new AgentHandlerGroupsCR();
        agentHandlerGroupsCR.setResource(this.resource);
        agentHandlerGroupsCR.setUser(orionUser);
        httpServletRequest.setAttribute("cellRendererAttr", "agentHandlerGroupsCR");
        httpServletRequest.setAttribute("datasourceAttr", "agentmgmt.handler.groups.datasource");
        orionUser.setAttribute("agentHandlerGroupsCR", agentHandlerGroupsCR);
        if (this.agentHandlerService.getHandlers(this.user).size() > 0) {
            httpServletRequest.setAttribute("statusNewGroupButton", "false");
        } else {
            httpServletRequest.setAttribute("statusNewGroupButton", "true");
        }
        if (isEditAllowed()) {
            httpServletRequest.setAttribute("tableProperties", "groupname,virtualdnsname,virtualipaddress,handlercount,actions");
        } else {
            httpServletRequest.setAttribute("tableProperties", "groupname,virtualdnsname,virtualipaddress,handlercount");
        }
        return ActionResponse.forward("/AgentMgmt", "/home/showGroups.jsp");
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
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

    public QueryDataSourceFactory getFactory() {
        return this.factory;
    }

    public void setFactory(QueryDataSourceFactory queryDataSourceFactory) {
        this.factory = queryDataSourceFactory;
    }

    public EPOAgentHandlerServiceInternal getAgentHandlerService() {
        return this.agentHandlerService;
    }

    public void setAgentHandlerService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.agentHandlerService = ePOAgentHandlerServiceInternal;
    }

    public TableService getTableService() {
        return this.tableService;
    }

    public void setTableService(TableService tableService2) {
        this.tableService = tableService2;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    private boolean isEditAllowed() {
        return getUser().isAllowedForPermission("perm:ahRole.addEdit") && !OrionCore.isCloudHosted();
    }
}
