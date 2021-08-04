package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao;
import com.mcafee.epo.core.model.EPOAgentHandlerGroup;
import com.mcafee.epo.core.model.EPOAgentHandlerGroupToHandlers;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerService;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import java.sql.Connection;
import javax.validation.constraints.Null;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CreateAgentHandlerGroupCommand extends EnhancedCommandBase implements ConnectionBean, Auditable {
    public static final String COMMAND_NAME = "AgentMgmt.createAgentHandlerGroup";
    private static final int SIZE_LIMIT = 256;
    static final Logger s_log = Logger.getLogger(CreateAgentHandlerGroupCommand.class);
    private String ahIDs;
    private CommandSpec commandSpec;
    private Connection connection;
    private boolean enabled;
    private String groupName;
    private boolean loadBalancerSet;
    private EPOAgentHandlerService service;
    private boolean success = false;
    private String virtualDNSName;
    private String virtualIP;
    private String virtualNetBiosName;

    public String getStatusMessage() {
        if (this.success) {
            return getResource().formatString("AgentMgmt.createAgentHandlerGroup.success", getLocale(), new Object[0]);
        }
        return getResource().formatString("AgentMgmt.createAgentHandlerGroup.failure", getLocale(), new Object[0]);
    }

    public Object invoke() throws Exception {
        validateInputParams();
        EPOAgentHandlerGroup ePOAgentHandlerGroup = new EPOAgentHandlerGroup();
        ePOAgentHandlerGroup.setName(this.groupName);
        ePOAgentHandlerGroup.setEnabled(this.enabled);
        ePOAgentHandlerGroup.setLoadBalancerSet(this.loadBalancerSet);
        if (this.loadBalancerSet) {
            ePOAgentHandlerGroup.setVirtualIP(this.virtualIP);
            ePOAgentHandlerGroup.setVirtualDNSName(this.virtualDNSName);
            ePOAgentHandlerGroup.setVirtualNetBiosName(this.virtualNetBiosName);
            this.service.addHandlerGroup(this.user, ePOAgentHandlerGroup);
        } else {
            this.service.addHandlerGroup(this.user, ePOAgentHandlerGroup);
            if (this.ahIDs != null) {
                EPOAgentHandlerGroup handlerGroupByNameWithOutPermissionCheck = this.service.getHandlerGroupByNameWithOutPermissionCheck(this.user, this.groupName);
                EPOAgentHandlerGroupToHandlersDBDao ePOAgentHandlerGroupToHandlersDBDao = getEPOAgentHandlerGroupToHandlersDBDao(this.connection);
                for (String str : this.ahIDs.split(",")) {
                    EPOAgentHandlerGroupToHandlers ePOAgentHandlerGroupToHandlers = new EPOAgentHandlerGroupToHandlers();
                    try {
                        EPORegisteredApacheServer handlerById = this.service.getHandlerById(Integer.parseInt(str), this.user);
                        if (handlerById != null) {
                            ePOAgentHandlerGroupToHandlers.setGroupId(handlerGroupByNameWithOutPermissionCheck.getAutoId());
                            ePOAgentHandlerGroupToHandlers.setHandlerId(handlerById.getAutoId());
                            ePOAgentHandlerGroupToHandlersDBDao.save(ePOAgentHandlerGroupToHandlers);
                            this.connection.commit();
                        }
                    } catch (Exception e) {
                        s_log.error("Invalid ID or server does not exist", e);
                    }
                }
            }
        }
        this.success = true;
        return getStatusMessage();
    }

    private void validateInputParams() throws CommandException {
        if (StringUtils.isBlank(this.groupName)) {
            throwException("AgentMgmt.createAgentHandlerGroup.blankGroupName");
        }
        if (this.groupName.length() > SIZE_LIMIT) {
            throwException("AgentMgmt.createAgentHandlerGroup.groupNameTooLong");
        }
    }

    private String throwException(String str) throws CommandException {
        throw new CommandException(getResource().getString(str, getLocale()));
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

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String str) {
        this.groupName = str;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    public boolean isLoadBalancerSet() {
        return this.loadBalancerSet;
    }

    public void setLoadBalancerSet(boolean z) {
        this.loadBalancerSet = z;
    }

    public String getVirtualIP() {
        return this.virtualIP;
    }

    public void setVirtualIP(String str) {
        this.virtualIP = str;
    }

    public String getVirtualDNSName() {
        return this.virtualDNSName;
    }

    public void setVirtualDNSName(String str) {
        this.virtualDNSName = str;
    }

    public String getVirtualNetBiosName() {
        return this.virtualNetBiosName;
    }

    public void setVirtualNetBiosName(String str) {
        this.virtualNetBiosName = str;
    }

    public String getAhIDs() {
        return this.ahIDs;
    }

    public void setAhIDs(String str) {
        this.ahIDs = str;
    }

    /* access modifiers changed from: protected */
    public EPOAgentHandlerGroupToHandlersDBDao getEPOAgentHandlerGroupToHandlersDBDao(Connection connection2) {
        return new EPOAgentHandlerGroupToHandlersDBDao(connection2);
    }
}
