package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService;
import com.mcafee.epo.core.EpoValidateException;
import com.mcafee.orion.console.export.ExportManager;
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
import com.mcafee.orion.core.util.FileType;
import com.mcafee.orion.core.util.IOUtil;
import com.mcafee.orion.core.util.resource.LocaleAware;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.Locale;

public class ExportAgentHandlerAssignmentsCommand extends CommandBase implements UserAware, LocaleAware, Command, Auditable, Schedulable, HelpDisplayer, RemoteInvocationAware {
    private Locale locale;
    private String m_ConfigURI = "";
    private String m_Context = "";
    private String m_SummaryURI = "";
    private OrionUser m_User = null;
    private EPOAgentHandlerImportExportService m_agentHandlerImpExpService = null;
    private Database m_db = null;
    private String m_descriptionTokenName;
    private String m_detailedHelpStringTokenName;
    private String m_displayNameTokenName;
    private ExportManager m_exportManager = null;
    private String m_fileName;
    private String m_oneLineHelpStringTokenName;
    private String m_validateMessageTokenName;
    private boolean remoteInvocation = false;

    public boolean authorize(OrionUser orionUser) throws CommandException, URISyntaxException {
        if ((OrionCore.isCloudHosted() && !this.remoteInvocation) || orionUser == null) {
            return false;
        }
        try {
            return orionUser.isAllowed("perm:ahRole.viewOnly");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public Object invoke() throws Exception {
        String str = this.m_fileName;
        FileOutputStream fileOutputStream = null;
        try {
            if (this.m_fileName == null || this.m_fileName.length() == 0) {
                String string = getResource().getString(getDetailedHelpStringTokenName(), getLocale());
                IOUtil.close((Closeable) null);
                return string;
            } else if (this.m_fileName.contains("..") || this.m_fileName.contains(":")) {
                throw new EpoValidateException("Invalid agent handler assignments export path: " + this.m_fileName);
            } else {
                byte[] bytes = this.m_agentHandlerImpExpService.exportAgentHandlerAssignmentsToXML(this.m_User).getBytes("UTF-8");
                if (!new File(this.m_fileName).isAbsolute()) {
                    getExportManager().setDatabase(this.m_db);
                    String str2 = getExportManager().getExportUIData(this.m_User).getExportDir() + File.separator + this.m_fileName;
                    if (!str2.endsWith("." + FileType.XML.toString().toLowerCase(Locale.ENGLISH))) {
                        str2 = str2 + "." + FileType.XML.toString().toLowerCase(Locale.ENGLISH);
                    }
                    File file = new File(str2);
                    String absolutePath = file.getAbsolutePath();
                    File parentFile = file.getParentFile();
                    if (parentFile == null || parentFile.exists() || parentFile.mkdirs()) {
                        file.createNewFile();
                        FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                        try {
                            fileOutputStream2.write(bytes);
                            fileOutputStream2.flush();
                            IOUtil.close(fileOutputStream2);
                            return "Exported to " + absolutePath;
                        } catch (Exception e) {
                            e = e;
                            fileOutputStream = fileOutputStream2;
                            try {
                                getLog().debug("Export Agent Handler Assignment: Exception", e);
                                throw new CommandException(e.getMessage(), e);
                            } catch (Throwable th) {
                                th = th;
                                IOUtil.close(fileOutputStream);
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fileOutputStream = fileOutputStream2;
                            IOUtil.close(fileOutputStream);
                            throw th;
                        }
                    } else {
                        throw new CommandException("Cannot create directory - " + parentFile.getAbsolutePath());
                    }
                } else {
                    throw new Exception("Export failed: the given file path must not be an absolute path.");
                }
            }
        } catch (Exception e2) {
            e = e2;
            getLog().debug("Export Agent Handler Assignment: Exception", e);
            throw new CommandException(e.getMessage(), e);
        }
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

    public ExportManager getExportManager() {
        return this.m_exportManager;
    }

    public void setExportManager(ExportManager exportManager) {
        this.m_exportManager = exportManager;
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

    /* renamed from: com.mcafee.epo.agentmgmt.command.ExportAgentHandlerAssignmentsCommand$1  reason: invalid class name */
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

    public void setParam1(String str) throws CommandException {
        this.m_fileName = str;
    }

    public void setExportFilename(String str) {
        this.m_fileName = str;
    }

    public void setAgentHandlerImportExportServices(EPOAgentHandlerImportExportService ePOAgentHandlerImportExportService) {
        this.m_agentHandlerImpExpService = ePOAgentHandlerImportExportService;
    }

    public EPOAgentHandlerImportExportService getAgentHandlerImportExportServices() {
        return this.m_agentHandlerImpExpService;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    public void setRemoteInvocation(boolean z) {
        this.remoteInvocation = z;
    }
}
