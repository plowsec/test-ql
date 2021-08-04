package com.mcafee.epo.agentmgmt;

import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOServerEventService;
import com.mcafee.epo.core.services.IEPOAgentHandlerService;
import com.mcafee.orion.core.auth.UserLoader;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.util.IOUtil;
import java.sql.Connection;
import java.util.List;
import org.apache.log4j.Logger;

public class AgentHandlerStatusTask extends EnhancedCommandBase {
    private static final Logger m_log = Logger.getLogger(AgentHandlerStatusTask.class);
    private IEPOAgentHandlerService agentHandlerService;
    private CommandSpec commandSpec;
    private Database m_database;
    private UserLoader m_userLoader;
    private EPOServerEventService serverEventService = null;
    private boolean success = false;

    public EPOServerEventService getServerEventService() {
        return this.serverEventService;
    }

    public void setServerEventService(EPOServerEventService ePOServerEventService) {
        this.serverEventService = ePOServerEventService;
    }

    public IEPOAgentHandlerService getAgentHandlerService() {
        return this.agentHandlerService;
    }

    public void setAgentHandlerService(IEPOAgentHandlerService iEPOAgentHandlerService) {
        this.agentHandlerService = iEPOAgentHandlerService;
    }

    public void setDatabase(Database database) {
        this.m_database = database;
    }

    public void setUserLoader(UserLoader userLoader) {
        this.m_userLoader = userLoader;
    }

    /* access modifiers changed from: protected */
    public CommandSpec createSpec() {
        return this.commandSpec;
    }

    public void setCommandSpec(CommandSpec commandSpec2) {
        this.commandSpec = commandSpec2;
    }

    public String getStatusMessage() {
        if (this.success) {
            return getResource().formatString("AgentMgmt.AgentHandlerStatusTask.success", getLocale(), new Object[0]);
        }
        return getResource().formatString("AgentMgmt.AgentHandlerStatusTask.failure", getLocale(), new Object[0]);
    }

    public Object invoke() throws Exception {
        Connection connection = null;
        try {
            m_log.info("Running agent handler status task");
            connection = this.m_database.getConnection(getUser());
            List<EPORegisteredApacheServer> inactiveHandlers = this.agentHandlerService.getInactiveHandlers(getUser());
            if (inactiveHandlers == null) {
                m_log.error("Apache server list null");
            } else {
                for (EPORegisteredApacheServer ePORegisteredApacheServer : inactiveHandlers) {
                    getServerEventService().saveServerEvent(connection, getUser(), 16025, 3, getResource().formatString("AHDownServerEventFmt", getResource().getDefaultLocale(), new Object[]{ePORegisteredApacheServer.getComputerName(), ePORegisteredApacheServer.getLastUpdate()}));
                    connection.commit();
                }
                this.success = true;
            }
        } catch (Exception e) {
            m_log.error("Error during agent handler status task", e);
        } finally {
            IOUtil.close(connection);
        }
        return getStatusMessage();
    }
}
