package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.computermgmt.agentmgmttemp.AgentHandlerListDS;
import com.mcafee.epo.computermgmt.ui.datasource.ComputerDS;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.epo.core.servlet.TextResponse;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.DataSourceUtil;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.servlet.Forward;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.util.OrionURI;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class GroupManagement implements LocaleAware, UserAware, ConnectionBean {
    private static final Logger log = Logger.getLogger(GroupManagement.class);
    private EPOAgentHandlerServiceInternal agentHandlerService = null;
    private Connection connection = null;
    private ComputerDS ds = null;
    private Locale locale = null;
    private Resource resource = null;
    private OrionUser user = null;

    public Response showHandlersForGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int parseInt = Integer.parseInt(httpServletRequest.getParameter("gId"));
        AgentHandlerListDS listDataSource = DataSourceUtil.getListDataSource(new OrionURI("datasource:Agent.Handler.List.DS"));
        listDataSource.setUser(this.user);
        listDataSource.setLocale(this.locale);
        listDataSource.setGroupId(parseInt);
        AgentHandlerListCR agentHandlerListCR = new AgentHandlerListCR();
        agentHandlerListCR.setResource(this.resource);
        httpServletRequest.setAttribute("cellRendererAttr", "agentHandlerListCR");
        this.user.setAttribute("agentHandlerListCR", agentHandlerListCR);
        httpServletRequest.setAttribute("datasourceAttr", "agentmgmt.handler.list.datasource");
        this.user.setAttribute("agentmgmt.handler.list.datasource", listDataSource);
        if (!this.user.isAllowedForPermission("perm:ahRole.addEdit") || OrionCore.isCloudHosted()) {
            httpServletRequest.setAttribute("tableProperties", "lastknowndnsname,lastknownipaddress,publisheddnsname,publishedipaddress,lastcommunication,agentcount");
        } else {
            httpServletRequest.setAttribute("tableProperties", "lastknowndnsname,lastknownipaddress,publisheddnsname,publishedipaddress,lastcommunication,agentcount,actions");
        }
        return new Forward("/AgentMgmt", "/home/showHandlers.jsp");
    }

    public Response showAgentsForGroup(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int parseInt = Integer.parseInt(httpServletRequest.getParameter("gId"));
        if (this.agentHandlerService.getHandlerGroupsById(parseInt, this.user) == null) {
            return new TextResponse(getResource().getString("error.invalidInputOrNoSuchObject", getLocale()));
        }
        List<EPORegisteredApacheServer> handlerListByGroupId = this.agentHandlerService.getHandlerListByGroupId(this.user, parseInt);
        ArrayList arrayList = new ArrayList();
        for (EPORegisteredApacheServer autoId : handlerListByGroupId) {
            arrayList.add(Integer.valueOf(autoId.getAutoId()));
        }
        String parameter = httpServletRequest.getParameter("ahId");
        try {
            this.ds.setLocale(this.locale);
            this.ds.setUser(this.user);
            this.ds.setAgentHandlerIDList(arrayList);
            this.ds.setGroupID(0);
            this.ds.setFilter("l:0");
            httpServletRequest.setAttribute("datasourceAttr", "ah.list.agents");
            httpServletRequest.setAttribute("ah.list.agents", this.ds);
            this.user.setAttribute("ah.list.agents", this.ds);
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(parameter), getUser());
            httpServletRequest.setAttribute("tableTitle", getResource().formatString("listAgents.title", getLocale(), new Object[]{handlerById.getDnsName()}));
        } catch (Exception e) {
            log.debug(e, e);
        }
        return new Forward("/AgentMgmt", "/ListAgents.jsp");
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

    public ComputerDS getDs() {
        return this.ds;
    }

    public void setDs(ComputerDS computerDS) {
        this.ds = computerDS;
    }
}
