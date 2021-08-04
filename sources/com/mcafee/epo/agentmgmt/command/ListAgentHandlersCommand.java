package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.services.EPOAgentHandlerService;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import java.sql.Connection;
import java.util.List;
import javax.validation.constraints.Null;

public class ListAgentHandlersCommand extends VisibleCommandBase implements ConnectionBean, Auditable {
    public static final String COMMAND_NAME = "AgentMgmt.listAgentHandlers";
    private CommandSpec commandSpec;
    private Connection connection;
    private EPOAgentHandlerService service;
    private boolean success = false;

    public String getStatusMessage() {
        if (this.success) {
            return getResource().formatString("AgentMgmt.listAgentHandlers.success", getLocale(), new Object[0]);
        }
        return getResource().formatString("AgentMgmt.listAgentHandlers.failure", getLocale(), new Object[0]);
    }

    public Object invoke() throws Exception {
        List activeHandlersWithOutPermissionCheck = this.service.getActiveHandlersWithOutPermissionCheck(this.user);
        this.success = true;
        return activeHandlersWithOutPermissionCheck;
    }

    private String throwException(String str, Object... objArr) throws CommandException {
        throw new CommandException(getResource().formatString(str, getLocale(), objArr));
    }

    public int getPriority() {
        return 2;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setConnection(@Null Connection connection2) {
        this.connection = connection2;
    }

    /* access modifiers changed from: protected */
    public CommandSpec createSpec() {
        return this.commandSpec;
    }

    public void setCommandSpec(CommandSpec commandSpec2) {
        this.commandSpec = commandSpec2;
    }

    public EPOAgentHandlerService getService() {
        return this.service;
    }

    public void setService(EPOAgentHandlerService ePOAgentHandlerService) {
        this.service = ePOAgentHandlerService;
    }
}
