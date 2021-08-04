package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandBase;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.HelpDisplayer;
import com.mcafee.orion.core.cmd.Schedulable;
import com.mcafee.orion.core.cmd.remote.RemoteInvocationAware;
import com.mcafee.orion.core.db.base.Database;
import java.net.URISyntaxException;

public class ImportAgentHandlerAssignmentsCommand extends CommandBase implements UserAware, Command, Auditable, Schedulable, HelpDisplayer, RemoteInvocationAware {
    private String m_ConfigURI = "";
    private String m_Context = "";
    private String m_SummaryURI = "";
    private OrionUser m_User = null;
    private EPOAgentHandlerImportExportService m_agentHandlerImpExpService = null;
    private Database m_db = null;
    private String m_descriptionTokenName;
    private String m_detailedHelpStringTokenName;
    private String m_displayNameTokenName;
    private String m_fileName = null;
    private String m_force;
    private String m_oneLineHelpStringTokenName;
    private String m_validateMessageTokenName;
    private boolean remoteInvocation = false;

    public boolean authorize(OrionUser orionUser) throws CommandException, URISyntaxException {
        if ((OrionCore.isCloudHosted() && !this.remoteInvocation) || orionUser == null) {
            return false;
        }
        try {
            return orionUser.isAllowed("perm:ahRole.addEdit");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0051, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0052, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b5, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Object invoke() throws java.lang.Exception {
        /*
            r7 = this;
            r5 = 1
            java.lang.String r0 = r7.m_fileName
            if (r0 != 0) goto L_0x000d
            com.mcafee.orion.core.cmd.CommandException r0 = new com.mcafee.orion.core.cmd.CommandException
            java.lang.String r1 = "Missing required parameter: file"
            r0.<init>(r1)
            throw r0
        L_0x000d:
            java.io.File r0 = new java.io.File
            java.lang.String r1 = r7.m_fileName
            r0.<init>(r1)
            boolean r1 = r0.exists()
            if (r1 != 0) goto L_0x0035
            com.mcafee.orion.core.cmd.CommandException r0 = new com.mcafee.orion.core.cmd.CommandException
            com.mcafee.orion.core.util.resource.Resource r1 = r7.getResource()
            java.lang.String r2 = "cmd.import.failure.non_existing_file_specified"
            java.util.Locale r3 = com.mcafee.orion.core.OrionCore.getServerLocale()
            java.lang.Object[] r4 = new java.lang.Object[r5]
            r5 = 0
            java.lang.String r6 = r7.m_fileName
            r4[r5] = r6
            java.lang.String r1 = r1.formatString(r2, r3, r4)
            r0.<init>(r1)
            throw r0
        L_0x0035:
            java.io.BufferedReader r3 = new java.io.BufferedReader
            java.io.FileReader r1 = new java.io.FileReader
            r1.<init>(r0)
            r3.<init>(r1)
            r2 = 0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x004f, all -> 0x00b4 }
            r0.<init>()     // Catch:{ Throwable -> 0x004f, all -> 0x00b4 }
        L_0x0045:
            java.lang.String r1 = r3.readLine()     // Catch:{ Throwable -> 0x004f, all -> 0x00b4 }
            if (r1 == 0) goto L_0x005b
            r0.append(r1)     // Catch:{ Throwable -> 0x004f, all -> 0x00b4 }
            goto L_0x0045
        L_0x004f:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r1 = move-exception
            r2 = r0
        L_0x0053:
            if (r3 == 0) goto L_0x005a
            if (r2 == 0) goto L_0x00a4
            r3.close()     // Catch:{ Throwable -> 0x009f }
        L_0x005a:
            throw r1
        L_0x005b:
            java.lang.String r1 = r0.toString()     // Catch:{ Throwable -> 0x004f, all -> 0x00b4 }
            if (r3 == 0) goto L_0x0066
            if (r2 == 0) goto L_0x009b
            r3.close()     // Catch:{ Throwable -> 0x0096 }
        L_0x0066:
            com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData r2 = new com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData
            r2.<init>()
            com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService r0 = r7.m_agentHandlerImpExpService
            com.mcafee.orion.core.auth.OrionUser r3 = r7.m_User
            r0.prepareImportAgentHandlerAssignmentsFromXML(r3, r1, r2)
            java.util.List r0 = r2.getRuleImportActions()
            java.util.Iterator r3 = r0.iterator()
        L_0x007a:
            boolean r0 = r3.hasNext()
            if (r0 == 0) goto L_0x00a8
            java.lang.Object r0 = r3.next()
            com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData$RuleImportConflict r0 = (com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData.RuleImportConflict) r0
            boolean r4 = r7.isForce()
            if (r4 != 0) goto L_0x0092
            boolean r4 = r0.getConflict()
            if (r4 != 0) goto L_0x007a
        L_0x0092:
            r0.setSelected(r5)
            goto L_0x007a
        L_0x0096:
            r0 = move-exception
            r2.addSuppressed(r0)
            goto L_0x0066
        L_0x009b:
            r3.close()
            goto L_0x0066
        L_0x009f:
            r0 = move-exception
            r2.addSuppressed(r0)
            goto L_0x005a
        L_0x00a4:
            r3.close()
            goto L_0x005a
        L_0x00a8:
            com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService r0 = r7.m_agentHandlerImpExpService
            com.mcafee.orion.core.auth.OrionUser r3 = r7.m_User
            r0.importAgentHandlerAssignmentsFromXML(r3, r1, r2)
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r5)
            return r0
        L_0x00b4:
            r0 = move-exception
            r1 = r0
            goto L_0x0053
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.command.ImportAgentHandlerAssignmentsCommand.invoke():java.lang.Object");
    }

    public boolean authorizedToSchedule(OrionUser orionUser) {
        if (orionUser == null) {
            return false;
        }
        try {
            return authorize(orionUser);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean terminate() {
        return false;
    }

    public String getConfigURI() {
        return this.m_ConfigURI;
    }

    public void setConfigURI(String str) {
        this.m_ConfigURI = str;
    }

    public String getSummaryURI() {
        return this.m_SummaryURI;
    }

    public void setSummaryURI(String str) {
        this.m_SummaryURI = str;
    }

    public String getContext() {
        return this.m_Context;
    }

    public void setContext(String str) {
        this.m_Context = str;
    }

    public int getPriority() {
        return 3;
    }

    public double getPercentComplete() {
        return 0.0d;
    }

    public void setDatabase(Database database) {
        this.m_db = database;
    }

    public Database getDatabase() {
        return this.m_db;
    }

    public String getDisplayName() {
        return getResource().getString(this.m_displayNameTokenName, getLocale());
    }

    public String getDescription() {
        return getResource().getString(this.m_descriptionTokenName, getLocale());
    }

    public void setFilePath(String str) throws CommandException {
        this.m_fileName = str;
    }

    public void setUser(OrionUser orionUser) {
        this.m_User = orionUser;
    }

    public OrionUser getUser() {
        return this.m_User;
    }

    public void setDescriptionTokenName(String str) {
        this.m_descriptionTokenName = str;
    }

    public String getDescriptionTokenName() {
        return this.m_descriptionTokenName;
    }

    public String getValidateMessageTokenName() {
        return this.m_validateMessageTokenName;
    }

    public void setValidateMessageTokenName(String str) {
        this.m_validateMessageTokenName = str;
    }

    public String getDisplayNameTokenName() {
        return this.m_displayNameTokenName;
    }

    public void setDisplayNameTokenName(String str) {
        this.m_displayNameTokenName = str;
    }

    public String getDetailedHelpStringTokenName() {
        return this.m_detailedHelpStringTokenName;
    }

    public void setDetailedHelpStringTokenName(String str) {
        this.m_detailedHelpStringTokenName = str;
    }

    public String getOneLineHelpStringTokenName() {
        return this.m_oneLineHelpStringTokenName;
    }

    public void setOneLineHelpStringTokenName(String str) {
        this.m_oneLineHelpStringTokenName = str;
    }

    /* renamed from: com.mcafee.epo.agentmgmt.command.ImportAgentHandlerAssignmentsCommand$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$mcafee$orion$core$cmd$HelpDisplayer$HelpType = new int[HelpDisplayer.HelpType.values().length];

        static {
            try {
                $SwitchMap$com$mcafee$orion$core$cmd$HelpDisplayer$HelpType[HelpDisplayer.HelpType.DETAILED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mcafee$orion$core$cmd$HelpDisplayer$HelpType[HelpDisplayer.HelpType.ONE_LINE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public String getHelpString(HelpDisplayer.HelpType helpType) {
        switch (AnonymousClass1.$SwitchMap$com$mcafee$orion$core$cmd$HelpDisplayer$HelpType[helpType.ordinal()]) {
            case 1:
                return getResource().getString(getDetailedHelpStringTokenName(), getLocale());
            default:
                return getResource().getString(getOneLineHelpStringTokenName(), getLocale());
        }
    }

    public void setParam1(String str) {
        this.m_fileName = str;
    }

    public void setParam2(Object obj) {
        setForce(obj.toString());
    }

    public void setImportFileName(String str) {
        this.m_fileName = str;
    }

    public String getImportFileName() {
        return this.m_fileName;
    }

    public void setForce(String str) {
        this.m_force = str;
    }

    public String getForce() {
        return this.m_force;
    }

    /* access modifiers changed from: protected */
    public boolean isForce() {
        return "force".equalsIgnoreCase(this.m_force) || Boolean.parseBoolean(this.m_force);
    }

    public void setAgentHandlerImportExportServices(EPOAgentHandlerImportExportService ePOAgentHandlerImportExportService) {
        this.m_agentHandlerImpExpService = ePOAgentHandlerImportExportService;
    }

    public EPOAgentHandlerServiceInternal getAgentHandlerServices() {
        return this.m_agentHandlerImpExpService;
    }

    public void setRemoteInvocation(boolean z) {
        this.remoteInvocation = z;
    }
}
