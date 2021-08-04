package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.EpoPermissionException;
import com.mcafee.epo.core.EpoValidateException;
import com.mcafee.epo.core.services.IEPOAgentHandlerService;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ListHandlerCmd extends VisibleCommandBase implements ConnectionBean, UserAware {
    private IEPOAgentHandlerService agentHandlerService;
    private Connection m_con;
    private Resource m_resource;
    private String m_status;
    private OrionUser user;

    public void setResource(Resource resource) {
        this.m_resource = resource;
    }

    public void setConnection(Connection connection) {
        this.m_con = connection;
    }

    public Connection getConnection() {
        return this.m_con;
    }

    /* access modifiers changed from: protected */
    public CommandSpec createSpec() {
        CommandSpec commandSpec = new CommandSpec("cmd.listAgentHandlers");
        commandSpec.setName("agentmgmt.listAgentHandlers");
        commandSpec.setResource(this.m_resource);
        commandSpec.setPermissionDescKey("cmd.listAgentHandlers.permDesc");
        commandSpec.setPermission("ahRole.viewOnly");
        return commandSpec;
    }

    public String getStatusMessage() {
        if (this.m_status == null) {
            return "";
        }
        return this.m_resource.getString(this.m_status, getLocale());
    }

    public Object invoke() throws SQLException, EpoValidateException, EpoPermissionException {
        List handlers = this.agentHandlerService.getHandlers(this.user);
        this.m_status = "cmd.listAgentHandlers.statusDone";
        return handlers;
    }

    public IEPOAgentHandlerService getAgentHandlerService() {
        return this.agentHandlerService;
    }

    public void setAgentHandlerService(IEPOAgentHandlerService iEPOAgentHandlerService) {
        this.agentHandlerService = iEPOAgentHandlerService;
    }

    public OrionUser getUser() {
        return this.user;
    }

    public void setUser(OrionUser orionUser) {
        this.user = orionUser;
    }
}
