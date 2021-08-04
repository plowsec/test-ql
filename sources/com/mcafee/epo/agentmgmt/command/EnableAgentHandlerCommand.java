package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerService;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import java.sql.Connection;
import javax.validation.constraints.Null;

public class EnableAgentHandlerCommand extends EnhancedCommandBase implements ConnectionBean, Auditable {
    public static final String COMMAND_NAME = "AgentMgmt.enableAgentHandler";
    public static final String HANDLER_ID_PARAM = "handlerId";
    public static final String STATUS_PARAM = "status";
    private CommandSpec commandSpec;
    private Connection connection;
    private EPORegisteredApacheServer handler;
    private Integer handlerId;
    private String handlerName;
    private EPOAgentHandlerService service;
    private boolean status = false;
    private boolean success = false;

    public String getStatusMessage() {
        if (this.success) {
            if (this.status) {
                return getResource().formatString("AgentMgmt.enableAgentHandler.success.enable", getLocale(), new Object[0]);
            }
            return getResource().formatString("AgentMgmt.enableAgentHandler.success.disable", getLocale(), new Object[0]);
        } else if (this.status) {
            return getResource().formatString("AgentMgmt.enableAgentHandler.failure.enable", getLocale(), new Object[0]);
        } else {
            return getResource().formatString("AgentMgmt.enableAgentHandler.failure.disable", getLocale(), new Object[0]);
        }
    }

    public Object invoke() throws Exception {
        if (this.handlerId != null && this.handlerId.intValue() > 0) {
            this.handler = this.service.getHandlerById(this.handlerId.intValue(), this.user);
        } else if (this.handlerName == null || this.handlerName.isEmpty()) {
            throwException("AgentMgmt.enableAgentHandler.negativeGroupIdOrGroupNameNotSpecified", new Object[0]);
        } else {
            this.handler = this.service.getHandlerByNameWithOutPermissionCheck(this.handlerName, this.user);
        }
        if (this.handler == null) {
            Object[] objArr = new Object[1];
            objArr[0] = this.handlerId == null ? this.handlerName : this.handlerId;
            throwException("AgentMgmt.enableAgentHandler.handlerDoesNotExist", objArr);
        }
        this.service.enableHandler(this.user, this.handler, this.status);
        this.success = true;
        return getStatusMessage();
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

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean z) {
        this.success = z;
    }

    public Integer getHandlerId() {
        return this.handlerId;
    }

    public void setHandlerId(Integer num) {
        this.handlerId = num;
    }

    public boolean isStatus() {
        return this.status;
    }

    public void setStatus(boolean z) {
        this.status = z;
    }

    public String getHandlerName() {
        return this.handlerName;
    }

    public void setHandlerName(String str) {
        this.handlerName = str;
    }
}
