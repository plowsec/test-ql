package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.agentmgmt.ah.AgentHandlerManagement;
import com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignment;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority;
import com.mcafee.epo.core.model.EPOGroup;
import com.mcafee.epo.core.services.EPOAgentHandlerService;
import com.mcafee.epo.core.services.EPOGroupService;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.EnhancedCommandBase;
import com.mcafee.orion.core.cmd.remote.RemoteInvocationAware;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.db.base.IDatabase;
import com.mcafee.orion.core.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.validation.constraints.Null;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class CreateAgentHandlerAssignmentCommand extends EnhancedCommandBase implements ConnectionBean, Auditable, RemoteInvocationAware {
    public static final String COMMAND_NAME = "AgentMgmt.createAgentHandlerAssignment";
    private static final Logger LOG = Logger.getLogger(CreateAgentHandlerAssignmentCommand.class);
    private AgentHandlerManagement agentHandlerManagement;
    private int assignmentId;
    private String assignmentName;
    private CommandSpec commandSpec;
    private Connection connection;
    protected IDatabase database;
    private EPOGroupService groupService;
    private String handlerOrGroups;
    private EPOAgentHandlerService handlerService;
    private String ipAddresses;
    private String ipRanges;
    private boolean remoteInvocation;
    private String subnetMasks;
    private boolean success = false;
    private String systemGroupIds;
    private boolean useAllHandlers;

    public String getStatusMessage() {
        if (this.success) {
            return getResource().formatString("AgentMgmt.createAgentHandlerAssignment.success", getLocale(), new Object[0]);
        }
        return getResource().formatString("AgentMgmt.createAgentHandlerAssignment.failure", getLocale(), new Object[0]);
    }

    public Object invoke() throws Exception {
        EPOAgentHandlerAssignment byId;
        boolean z;
        boolean z2;
        int parseInt;
        PreparedStatement preparedStatement;
        validateAssignmentName();
        EPOAgentHandlerAssignmentDBDao ePOAgentHandlerAssignmentDBDao = getEPOAgentHandlerAssignmentDBDao();
        if (this.assignmentId == 0) {
            if (ePOAgentHandlerAssignmentDBDao.getByName(this.assignmentName) != null) {
                throwException("assignment.exists");
            }
            byId = new EPOAgentHandlerAssignment();
            z = true;
        } else {
            byId = ePOAgentHandlerAssignmentDBDao.getById(this.assignmentId);
            z = false;
        }
        if (byId == null) {
            throwException("AgentMgmt.createAgentHandlerAssignment.invalidAssignment");
        }
        ArrayList arrayList = new ArrayList();
        if (this.systemGroupIds != null && this.systemGroupIds.length() > 0 && !this.systemGroupIds.equals("_")) {
            for (String str : this.systemGroupIds.split(",")) {
                if (!str.equals("_")) {
                    EPOGroup groupById = getGroupService().getGroupById(getUser(), getConnection(), Integer.parseInt(str.split("_")[0]));
                    if (groupById != null) {
                        arrayList.add(groupById);
                    }
                }
            }
        }
        try {
            byId.setGroups(arrayList);
            ArrayList arrayList2 = new ArrayList();
            this.agentHandlerManagement.setConnection(getConnection());
            if (this.ipRanges != null && !this.ipRanges.equals("")) {
                this.ipRanges = this.ipRanges.trim();
                if (this.ipRanges != null && !this.ipRanges.equals("")) {
                    String[] strArr = new String[0];
                    List<String> asList = Arrays.asList(this.ipRanges.split("(\\s*,\\s*)|(\\s+,*\\s*)"));
                    HashSet hashSet = new HashSet();
                    for (String add : asList) {
                        hashSet.add(add);
                    }
                    String[] strArr2 = (String[]) hashSet.toArray(new String[hashSet.size()]);
                    Collections.sort(Arrays.asList(strArr2));
                    for (String addIPRule : strArr2) {
                        arrayList2.add(this.agentHandlerManagement.addIPRule(addIPRule));
                    }
                }
            }
            byId.setIPRanges(arrayList2);
            if (arrayList.size() == 0 && arrayList2.size() == 0) {
                throwException("error.invalidCriteria");
            }
            if (this.useAllHandlers) {
                byId.setUseAllHandlers(true);
            } else {
                ArrayList arrayList3 = new ArrayList();
                if (this.handlerOrGroups == null || this.handlerOrGroups.length() <= 0) {
                    z2 = false;
                } else {
                    int i = 1;
                    z2 = false;
                    for (String split : this.handlerOrGroups.split(",")) {
                        String[] split2 = split.split("_");
                        EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority = new EPOAgentHandlerAssignmentHandlerPriority();
                        ePOAgentHandlerAssignmentHandlerPriority.setAssignmentId(this.assignmentId);
                        ePOAgentHandlerAssignmentHandlerPriority.setPriority(i);
                        if ("HANDLER".equals(split2[0]) || "MASTERHANDLER".equals(split2[0])) {
                            int parseInt2 = Integer.parseInt(split2[1]);
                            if (getHandlerService().getHandlerById(parseInt2, getUser()) != null) {
                                ePOAgentHandlerAssignmentHandlerPriority.setHandlerId(parseInt2);
                                arrayList3.add(ePOAgentHandlerAssignmentHandlerPriority);
                                i++;
                                z2 = true;
                            }
                        } else if ("GROUP".equals(split2[0])) {
                            if ("HASMASTERHANDLER".equals(split2[1])) {
                                parseInt = Integer.parseInt(split2[2]);
                            } else {
                                parseInt = Integer.parseInt(split2[1]);
                            }
                            if (getHandlerService().getHandlerGroupsById(parseInt, getUser()) != null) {
                                ePOAgentHandlerAssignmentHandlerPriority.setHandlerGroupId(parseInt);
                                arrayList3.add(ePOAgentHandlerAssignmentHandlerPriority);
                                i++;
                                z2 = true;
                            }
                        }
                    }
                }
                if (z2) {
                    byId.setAssociatedHandlerPriorities(arrayList3);
                    byId.setUseAllHandlers(false);
                } else {
                    throwException("error.invalidAgentHandlers");
                }
            }
            getHandlerService().setResource(getResource());
            getHandlerService().setDatabase(getDatabase());
            boolean equals = this.assignmentName.equals(getResource().formatString("ah.defaultAssignmentRule", getLocale(), new Object[0]));
            if (!equals) {
                preparedStatement = null;
                preparedStatement = getConnection().prepareStatement("SELECT COUNT(*) FROM EPOAgentHandlerAssignment WHERE [Name] = ? AND [AutoID] <> ?");
                preparedStatement.setString(1, this.assignmentName);
                preparedStatement.setInt(2, this.assignmentId);
                ResultSet executeQuery = preparedStatement.executeQuery();
                executeQuery.next();
                if (executeQuery.getInt(1) > 0) {
                    equals = true;
                }
                IOUtil.close(preparedStatement);
            }
            if (equals) {
                throwException("error.dupeName");
            }
            byId.setName(this.assignmentName);
            if (z) {
                getHandlerService().addHandlerAssignment(this.user, byId);
            } else {
                getHandlerService().updateHandlerAssignment(this.user, byId);
            }
            this.agentHandlerManagement.NotifyChanges();
            this.success = true;
        } catch (Exception e) {
            LOG.error("Failed to save HandlerAssignment", e);
            throwException("error.generic");
        } catch (Throwable th) {
            IOUtil.close(preparedStatement);
            throw th;
        }
        return getStatusMessage();
    }

    private void validateAssignmentName() throws CommandException {
        if (StringUtils.isBlank(this.assignmentName)) {
            throwException("error.noName");
        }
        if (this.assignmentName.length() > 256) {
            throwException("error.nameTooLong");
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

    public void setRemoteInvocation(boolean z) {
        this.remoteInvocation = z;
    }

    public IDatabase getDatabase() {
        return this.database;
    }

    public void setDatabase(IDatabase iDatabase) {
        this.database = iDatabase;
    }

    public EPOAgentHandlerService getHandlerService() {
        return this.handlerService;
    }

    public void setHandlerService(EPOAgentHandlerService ePOAgentHandlerService) {
        this.handlerService = ePOAgentHandlerService;
    }

    public EPOGroupService getGroupService() {
        return this.groupService;
    }

    public void setGroupService(EPOGroupService ePOGroupService) {
        this.groupService = ePOGroupService;
    }

    public AgentHandlerManagement getAgentHandlerManagement() {
        return this.agentHandlerManagement;
    }

    public void setAgentHandlerManagement(AgentHandlerManagement agentHandlerManagement2) {
        this.agentHandlerManagement = agentHandlerManagement2;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean z) {
        this.success = z;
    }

    public int getAssignmentId() {
        return this.assignmentId;
    }

    public void setAssignmentId(int i) {
        this.assignmentId = i;
    }

    public String getAssignmentName() {
        return this.assignmentName;
    }

    public void setAssignmentName(String str) {
        this.assignmentName = str;
    }

    public String getSystemGroupIds() {
        return this.systemGroupIds;
    }

    public void setSystemGroupIds(String str) {
        this.systemGroupIds = str;
    }

    public String getIpAddresses() {
        return this.ipAddresses;
    }

    public void setIpAddresses(String str) {
        this.ipAddresses = str;
    }

    public String getIpRanges() {
        return this.ipRanges;
    }

    public void setIpRanges(String str) {
        this.ipRanges = str;
    }

    public String getSubnetMasks() {
        return this.subnetMasks;
    }

    public void setSubnetMasks(String str) {
        this.subnetMasks = str;
    }

    public boolean isUseAllHandlers() {
        return this.useAllHandlers;
    }

    public void setUseAllHandlers(boolean z) {
        this.useAllHandlers = z;
    }

    public String getHandlerOrGroups() {
        return this.handlerOrGroups;
    }

    public void setHandlerOrGroups(String str) {
        this.handlerOrGroups = str;
    }

    /* access modifiers changed from: protected */
    public EPOAgentHandlerAssignmentDBDao getEPOAgentHandlerAssignmentDBDao() {
        return new EPOAgentHandlerAssignmentDBDao(getConnection());
    }
}
