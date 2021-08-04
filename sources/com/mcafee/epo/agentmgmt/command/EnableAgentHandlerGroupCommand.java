package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.model.EPOAgentHandlerGroup;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import java.sql.Connection;
import javax.validation.constraints.Null;

public class EnableAgentHandlerGroupCommand extends EnhancedCommandBase implements ConnectionBean, Auditable {
    public static final String COMMAND_NAME = "AgentMgmt.enableAgentHandlerGroup";
    public static final String GROUP_ID_PARAM = "groupId";
    public static final String STATUS_PARAM = "status";
    private CommandSpec commandSpec;
    private Connection connection;
    private int groupId;
    private EPOAgentHandlerServiceInternal service;
    private boolean status = false;
    private boolean success = false;

    public String getStatusMessage() {
        if (this.success) {
            if (this.status) {
                return getResource().formatString("AgentMgmt.enableAgentHandlerGroup.success.enable", getLocale(), new Object[0]);
            }
            return getResource().formatString("AgentMgmt.enableAgentHandlerGroup.success.disable", getLocale(), new Object[0]);
        } else if (this.status) {
            return getResource().formatString("AgentMgmt.enableAgentHandlerGroup.failure.enable", getLocale(), new Object[0]);
        } else {
            return getResource().formatString("AgentMgmt.enableAgentHandlerGroup.failure.disable", getLocale(), new Object[0]);
        }
    }

    public Object invoke() throws Exception {
        if (this.groupId <= 0) {
            throwException("AgentMgmt.enableAgentHandlerGroup.negativeGroupId", new Object[0]);
        }
        EPOAgentHandlerGroup handlerGroupsById = this.service.getHandlerGroupsById(this.groupId, this.user);
        if (handlerGroupsById == null) {
            throwException("AgentMgmt.enableAgentHandlerGroup.groupDoesNotExist", Integer.valueOf(this.groupId));
        }
        this.service.toggleHandlerGroup(this.user, handlerGroupsById, this.status);
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

    public EPOAgentHandlerServiceInternal getService() {
        return this.service;
    }

    public void setService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.service = ePOAgentHandlerServiceInternal;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean z) {
        this.success = z;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int i) {
        this.groupId = i;
    }

    public boolean isStatus() {
        return this.status;
    }

    public void setStatus(boolean z) {
        this.status = z;
    }
}
