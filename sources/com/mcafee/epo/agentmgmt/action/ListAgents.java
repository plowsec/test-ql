package com.mcafee.epo.agentmgmt.action;

import com.mcafee.epo.computermgmt.ui.datasource.ComputerDS;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.IEPOAgentHandlerService;
import com.mcafee.orion.console.ui.chart.BaseChartAction;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.ChartElement;
import com.mcafee.orion.core.data.QueryDataSourceFactory;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.query.sexp.SerializationException;
import com.mcafee.orion.core.query.sexp.Sexp;
import com.mcafee.orion.core.query.sexp.SexpLong;
import com.mcafee.orion.core.query.sexp.SexpSerializer;
import com.mcafee.orion.core.servlet.Forward;
import com.mcafee.orion.core.servlet.PublicURL;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class ListAgents extends BaseChartAction implements LocaleAware, UserAware, ConnectionBean {
    private static final Logger logger = Logger.getLogger(ListAgents.class);
    private IEPOAgentHandlerService agentHandlerService;
    private ComputerDS computerDs = null;
    private Connection connection = null;
    private Locale locale = null;
    private QueryDataSourceFactory queryDataSourceFactory = null;
    private Resource resource = null;
    private Resource resourceCompMgmt = null;
    private SexpSerializer sexpSerializer;
    private OrionUser user = null;

    public Response listAgents(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String parameter = httpServletRequest.getParameter("ahId");
        try {
            this.computerDs.setLocale(getLocale());
            this.computerDs.setUser(this.user);
            this.computerDs.setAgentHandlerID(Integer.parseInt(parameter));
            this.computerDs.setGroupID(0);
            this.computerDs.setFilter("l:0");
            httpServletRequest.setAttribute("datasourceAttr", "ah.list.agents");
            httpServletRequest.setAttribute("ah.list.agents", this.computerDs);
            this.user.setAttribute("ah.list.agents", this.computerDs);
            EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(Integer.parseInt(parameter), this.user);
            if (handlerById == null) {
                logger.error("Apache server null");
            } else {
                httpServletRequest.setAttribute("tableTitle", getResource().formatString("listAgents.title", getLocale(), new Object[]{handlerById.getDnsName()}));
            }
        } catch (Exception e) {
            logger.debug(e, e);
        }
        return new Forward("/AgentMgmt", "/ListAgents.jsp");
    }

    public Response listAgentsFromChart(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String formatString;
        String dnsName;
        Locale locale2 = getLocale();
        try {
            ChartElement chartElement = getChartElement(httpServletRequest);
            Integer num = null;
            if (chartElement == null) {
                throw new NullPointerException("Chart Element is null");
            }
            try {
                num = Integer.valueOf(Integer.parseInt(chartElement.getRawItem().toString()));
            } catch (Exception e) {
            }
            this.computerDs.setLocale(this.locale);
            this.computerDs.setUser(this.user);
            if (num != null) {
                this.computerDs.setAgentHandlerID(num.intValue());
                EPORegisteredApacheServer handlerById = this.agentHandlerService.getHandlerById(num.intValue(), this.user);
                if (handlerById == null) {
                    dnsName = this.resourceCompMgmt.formatString("ComputerDA.typeUninstalled", getLocale(), new Object[]{Integer.valueOf(num.intValue())});
                } else {
                    dnsName = handlerById.getDnsName();
                }
                formatString = getResource().formatString("listAgents.title", locale2, new Object[]{dnsName});
            } else if (getAHList(chartElement.getDrillDownKey()).isEmpty()) {
                this.computerDs.setAgentHandlerID(-2);
                String formatString2 = getResource().formatString("ah.drilldown.noHandler", locale2, new Object[0]);
                formatString = getResource().formatString("listAgents.title", locale2, new Object[]{formatString2});
            } else {
                this.computerDs.setAgentHandlerIDList(getAHList(chartElement.getDrillDownKey()));
                formatString = getResource().formatString("ah.drilldown.other", locale2, new Object[0]);
            }
            httpServletRequest.setAttribute("tableTitle", formatString);
            this.computerDs.setGroupID(0);
            this.computerDs.setFilter("l:1");
            httpServletRequest.setAttribute("datasourceAttr", "ah.list.agents");
            httpServletRequest.setAttribute("ah.list.agents", this.computerDs);
            this.user.setAttribute("ah.list.agents", this.computerDs);
            return new Forward("/AgentMgmt", "/ListAgents.jsp");
        } catch (Exception e2) {
            logger.debug(e2, e2);
            httpServletRequest.setAttribute("friendlyErrorMessage", getResource().formatString("error.chartDrilldown", locale2, new Object[0]));
            httpServletRequest.setAttribute("okUrl", "/AgentMgmt/home.do");
            return new Forward(PublicURL.ERROR_MESSAGE);
        }
    }

    private List<Integer> getAHList(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            addAHID(readSexpSerializer(str), arrayList);
        } catch (Exception e) {
            logger.error(e, e);
        }
        return arrayList;
    }

    private void addAHID(Sexp sexp, List<Integer> list) {
        if (sexp != null) {
            if (sexp instanceof SexpLong) {
                list.add(new Integer((int) ((SexpLong) sexp).getValue()));
            }
            if (sexp.children() != null) {
                for (Sexp addAHID : sexp.children()) {
                    addAHID(addAHID, list);
                }
            }
        }
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
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

    public QueryDataSourceFactory getQueryDataSourceFactory() {
        return this.queryDataSourceFactory;
    }

    public void setQueryDataSourceFactory(QueryDataSourceFactory queryDataSourceFactory2) {
        this.queryDataSourceFactory = queryDataSourceFactory2;
    }

    public ComputerDS getComputerDs() {
        return this.computerDs;
    }

    public void setComputerDs(ComputerDS computerDS) {
        this.computerDs = computerDS;
    }

    public void setResourceCompMgmt(Resource resource2) {
        this.resourceCompMgmt = resource2;
    }

    public SexpSerializer getSexpSerializer() {
        return this.sexpSerializer;
    }

    public void setSexpSerializer(SexpSerializer sexpSerializer2) {
        this.sexpSerializer = sexpSerializer2;
    }

    public IEPOAgentHandlerService getAgentHandlerService() {
        return this.agentHandlerService;
    }

    public void setAgentHandlerService(IEPOAgentHandlerService iEPOAgentHandlerService) {
        this.agentHandlerService = iEPOAgentHandlerService;
    }

    /* access modifiers changed from: protected */
    public Sexp readSexpSerializer(String str) throws SerializationException {
        return this.sexpSerializer.read(str);
    }
}
