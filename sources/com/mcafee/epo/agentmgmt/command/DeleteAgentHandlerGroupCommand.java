package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import java.sql.Connection;
import javax.validation.constraints.Null;

public class DeleteAgentHandlerGroupCommand extends EnhancedCommandBase implements ConnectionBean, Auditable {
    public static final String COMMAND_NAME = "AgentMgmt.deleteAgentHandlerGroup";
    public static final String GROUP_ID_PARAM = "groupId";
    private CommandSpec commandSpec;
    private Connection connection;
    private int groupId;
    private EPOAgentHandlerServiceInternal service;
    private boolean success = false;

    public String getStatusMessage() {
        if (this.success) {
            return getResource().formatString("AgentMgmt.deleteAgentHandlerGroup.success", getLocale(), new Object[0]);
        }
        return getResource().formatString("AgentMgmt.deleteAgentHandlerGroup.failure", getLocale(), new Object[0]);
    }

    public Object invoke() throws Exception {
        if (this.groupId <= 0) {
            throwException("AgentMgmt.deleteAgentHandlerGroup.negativeGroupId", new Object[0]);
        }
        if (this.service.getHandlerGroupsById(this.groupId, this.user) == null) {
            throwException("AgentMgmt.deleteAgentHandlerGroup.groupDoesNotExist", Integer.valueOf(this.groupId));
        }
        this.service.deleteHandlerGroupByID(this.user, this.groupId);
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
}
