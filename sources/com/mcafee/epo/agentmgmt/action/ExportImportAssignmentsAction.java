package com.mcafee.epo.agentmgmt.action;

import com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData;
import com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService;
import com.mcafee.epo.core.util.ServerConfiguration;
import com.mcafee.orion.console.export.ExportManager;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.servlet.ActionResponse;
import com.mcafee.orion.core.servlet.Forward;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.servlet.util.ServletUtil;
import com.mcafee.orion.core.servlet.util.UserUtil;
import com.mcafee.orion.core.ui.MultipartFormAction;
import com.mcafee.orion.core.ui.MultipartFormPolicy;
import com.mcafee.orion.core.util.FileType;
import com.mcafee.orion.core.util.FileUtil;
import com.mcafee.orion.core.util.OrionUploadFileItem;
import com.mcafee.orion.core.util.ioc.ContextInjectible;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

public class ExportImportAssignmentsAction implements ContextInjectible, UserAware, LocaleAware, MultipartFormAction {
    public static final String IMPORT_ERROR = "EPOImportAssignmentError";
    private static final String STR_IMPORT_DATA = "AssignemntImportData";
    private static final String STR_IMPORT_XML = "AssignmentImportXML";
    private static final String STR_IMPORT_XML_FILE = "AssignmentImportXMLFile";
    private static final String STR_IMPORT_XML_FILENAME = "AssignmentImportXMLFilename";
    private static final Logger m_oLog = Logger.getLogger(ExportImportAssignmentsAction.class);
    private OrionUploadFileItem importFile = null;
    private String importFileName = null;
    private Locale locale;
    private String m_exportDir = null;
    private ExportManager m_exportManager = null;
    private EPOAgentHandlerImportExportService m_impexpService = null;
    private Resource m_oResource = null;
    private OrionUser m_user;
    private ServerConfiguration serverConfig = null;

    public void setServerConfig(ServerConfiguration serverConfiguration) {
        this.serverConfig = serverConfiguration;
    }

    public EPOAgentHandlerImportExportService getAgentHandlerImportExportService() {
        return this.m_impexpService;
    }

    public void setAgentHandlerImportExportService(EPOAgentHandlerImportExportService ePOAgentHandlerImportExportService) {
        this.m_impexpService = ePOAgentHandlerImportExportService;
    }

    public ExportManager getExportManager() {
        return this.m_exportManager;
    }

    public void setExportManager(ExportManager exportManager) {
        this.m_exportManager = exportManager;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    public Resource getResource() {
        return this.m_oResource;
    }

    public void setResource(Resource resource) {
        this.m_oResource = resource;
    }

    public OrionUser getUser() {
        return this.m_user;
    }

    public void setUser(OrionUser orionUser) {
        this.m_user = orionUser;
    }

    public Response ConfigExportHandlerAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        httpServletRequest.setAttribute("exportDir", new File(getExportDir(UserUtil.getOrionUser(httpServletRequest))).getAbsolutePath() + File.separator);
        if (httpServletRequest.getParameter("parentIndex") != null) {
            httpServletRequest.setAttribute("parentIndex", Integer.valueOf(Integer.parseInt(httpServletRequest.getParameter("parentIndex"))));
        }
        return new Forward("/AgentMgmt", "/ConfigExportHandlerAssignments.jsp");
    }

    public Response SummaryExportHandlerAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        File file = new File(getExportDir(UserUtil.getOrionUser(httpServletRequest)), (String) ((Map) ServletUtil.find("commandSummaryMap", httpServletRequest)).get("exportFilename"));
        httpServletRequest.setAttribute("exportSummary", this.m_oResource.getString("cmd.exportAgentHandlerAssignments.Summary", this.locale) + StringEscapeUtils.escapeHtml(file.getAbsolutePath()));
        return new Forward("/AgentMgmt", "/SummaryExportHandlerAssignments.jsp");
    }

    public Response ConfigImportHandlerAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        if (httpServletRequest.getParameter("parentIndex") != null) {
            httpServletRequest.setAttribute("parentIndex", Integer.valueOf(Integer.parseInt(httpServletRequest.getParameter("parentIndex"))));
        }
        return new Forward("/AgentMgmt", "/ConfigImportHandlerAssignments.jsp");
    }

    public Response SummaryImportHandlerAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        boolean z;
        String str;
        Map map = (Map) ServletUtil.find("commandSummaryMap", httpServletRequest);
        String str2 = (String) map.get("importFileName");
        if (((String) map.get("force")).equals("force")) {
            z = true;
        } else {
            z = false;
        }
        String string = this.m_oResource.getString("cmd.importAgentHandlerAssignments.Summary", this.locale);
        String str3 = this.m_oResource.formatString(string, getLocale(), new Object[]{StringEscapeUtils.escapeHtml(str2)}) + "\r\n";
        if (z) {
            str = str3 + this.m_oResource.getString("cmd.importAgentHandlerAssignments.SummaryForceYes", this.locale);
        } else {
            str = str3 + this.m_oResource.getString("cmd.importAgentHandlerAssignments.SummaryForceNo", this.locale);
        }
        httpServletRequest.setAttribute("ImportSummary", str);
        return new Forward("/AgentMgmt", "/SummaryImportHandlerAssignments.jsp");
    }

    /* access modifiers changed from: protected */
    public String getExportDir(OrionUser orionUser) {
        if (this.m_exportDir == null) {
            getExportManager().setDatabase(OrionCore.getDb());
            this.m_exportDir = getExportManager().getExportUIData(orionUser).getExportDir();
        }
        return this.m_exportDir;
    }

    public ActionResponse ExportAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        OrionUser orionUser = UserUtil.getOrionUser(httpServletRequest);
        this.m_impexpService.setResource(this.m_oResource);
        byte[] bytes = this.m_impexpService.exportAgentHandlerAssignmentsToXML(UserUtil.getOrionUser(httpServletRequest)).getBytes("UTF-8");
        String replaceAll = getResource().getString("export.AgentHandlerAssignments.filename", this.locale).replaceAll("[ /\\\\:\\x2A\\x3F\"<>\\x7C]", "_");
        if (replaceAll.length() > 128) {
            replaceAll = replaceAll.substring(0, 127);
        }
        httpServletRequest.setAttribute("uri", FileUtil.getCacheFileURI(orionUser, new File(FileUtil.writeToCache(orionUser, URLEncoder.encode(replaceAll, "UTF-8"), FileType.XML, bytes))));
        httpServletRequest.setAttribute("displayName", replaceAll);
        return ActionResponse.forward("/AgentMgmt", "/AssignmentExportDownload.jsp");
    }

    public ActionResponse PrepareImportUIAction(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return ActionResponse.forward("/AgentMgmt", "/ImportAssignments.jsp");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005b, code lost:
        r4 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0066, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0067, code lost:
        r5 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0149, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x014a, code lost:
        r3 = r2;
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0153, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0154, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0153 A[Catch:{ all -> 0x0153, all -> 0x0066 }, ExcHandler: all (r2v16 'th' java.lang.Throwable A[CUSTOM_DECLARE, Catch:{ all -> 0x0153, all -> 0x0066 }])] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.mcafee.orion.core.servlet.ActionResponse PrepareImportAssignment(javax.servlet.http.HttpServletRequest r15, javax.servlet.http.HttpServletResponse r16) throws java.lang.Exception {
        /*
            r14 = this;
            r12 = 1
            r5 = 0
            r3 = 0
            com.mcafee.orion.core.auth.OrionUser r6 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r15)
            java.lang.String r2 = r14.importFileName
            if (r2 != 0) goto L_0x0019
            javax.servlet.http.HttpSession r2 = r15.getSession()
            java.lang.String r4 = "AssignmentImportXMLFilename"
            java.lang.Object r2 = r2.getAttribute(r4)
            java.lang.String r2 = (java.lang.String) r2
            r14.importFileName = r2
        L_0x0019:
            com.mcafee.orion.core.util.OrionUploadFileItem r2 = r14.importFile
            if (r2 != 0) goto L_0x002b
            javax.servlet.http.HttpSession r2 = r15.getSession()
            java.lang.String r4 = "AssignmentImportXMLFile"
            java.lang.Object r2 = r2.getAttribute(r4)
            com.mcafee.orion.core.util.OrionUploadFileItem r2 = (com.mcafee.orion.core.util.OrionUploadFileItem) r2
            r14.importFile = r2
        L_0x002b:
            java.lang.String r7 = r14.importFileName
            com.mcafee.orion.core.util.OrionUploadFileItem r2 = r14.importFile
            if (r2 == 0) goto L_0x017a
            com.mcafee.orion.core.util.OrionUploadFileItem r2 = r14.importFile
            long r8 = r2.getSize()
            r10 = 0
            int r2 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r2 <= 0) goto L_0x017a
            com.mcafee.orion.core.util.OrionUploadFileItem r2 = r14.importFile     // Catch:{ Exception -> 0x0070 }
            java.io.InputStream r8 = r2.getInputStream()     // Catch:{ Exception -> 0x0070 }
            r9 = 0
            java.io.BufferedReader r10 = r14.getBufferedReader(r8)     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
            r11 = 0
            java.lang.StringBuffer r2 = new java.lang.StringBuffer     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r2.<init>()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
        L_0x004e:
            java.lang.String r3 = r10.readLine()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            if (r3 == 0) goto L_0x0079
            r2.append(r3)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            goto L_0x004e
        L_0x0058:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x005a }
        L_0x005a:
            r3 = move-exception
            r4 = r2
        L_0x005c:
            if (r10 == 0) goto L_0x0063
            if (r4 == 0) goto L_0x0161
            r10.close()     // Catch:{ Throwable -> 0x015b, all -> 0x0153 }
        L_0x0063:
            throw r3     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
        L_0x0064:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0066 }
        L_0x0066:
            r3 = move-exception
            r5 = r2
        L_0x0068:
            if (r8 == 0) goto L_0x006f
            if (r5 == 0) goto L_0x0175
            r8.close()     // Catch:{ Throwable -> 0x016f }
        L_0x006f:
            throw r3     // Catch:{ Exception -> 0x0070 }
        L_0x0070:
            r2 = move-exception
            org.apache.log4j.Logger r3 = m_oLog
            java.lang.String r4 = "PrepareImportPolicy: in Exception"
            r3.debug(r4, r2)
            throw r2
        L_0x0079:
            java.lang.String r2 = r2.toString()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData r12 = new com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r12.<init>()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService r3 = r14.m_impexpService     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.auth.OrionUser r4 = r14.m_user     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            boolean r4 = r3.prepareImportAgentHandlerAssignmentsFromXML(r4, r2, r12)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            if (r4 == 0) goto L_0x011b
            javax.servlet.http.HttpSession r3 = r15.getSession()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r13 = "AssignemntImportData"
            r3.setAttribute(r13, r12)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r13 = "AssignmentImportXML"
            r3.setAttribute(r13, r2)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r2 = "AssignmentImportXMLFilename"
            r3.setAttribute(r2, r7)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r2 = "AssignmentImportXMLFile"
            com.mcafee.orion.core.util.OrionUploadFileItem r13 = r14.importFile     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r3.setAttribute(r2, r13)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportCR r2 = new com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportCR     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r2.<init>()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "cellRendererAttr"
            java.lang.String r13 = "AssignmentImportCellRenderer"
            r15.setAttribute(r3, r13)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.auth.OrionUser r3 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r15)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r13 = "AssignmentImportCellRenderer"
            r3.setAttribute(r13, r2)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.util.OrionURI r2 = new com.mcafee.orion.core.util.OrionURI     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "datasource:Agent.Handler.Assignment.Import.DS"
            r2.<init>(r3)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.data.ListDataSource r3 = com.mcafee.orion.core.data.DataSourceUtil.getListDataSource(r2)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r0 = r3
            com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportDS r0 = (com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportDS) r0     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r2 = r0
            r2.setData(r12)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r0 = r3
            com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportDS r0 = (com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentImportDS) r0     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r2 = r0
            r2.setUser(r6)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.auth.OrionUser r2 = com.mcafee.orion.core.servlet.util.UserUtil.getOrionUser(r15)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r6 = "AgentMgmt.assignment.import.datasource"
            r2.setAttribute(r6, r3)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            com.mcafee.orion.core.util.resource.Resource r2 = r14.m_oResource     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "import.assignment.title"
            java.util.Locale r6 = r14.locale     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r12 = 1
            java.lang.Object[] r12 = new java.lang.Object[r12]     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r13 = 0
            r12[r13] = r7     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r2 = r2.formatString(r3, r6, r12)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "pageTitle"
            r15.setAttribute(r3, r2)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
        L_0x00f2:
            if (r10 == 0) goto L_0x00f9
            if (r5 == 0) goto L_0x0157
            r10.close()     // Catch:{ Throwable -> 0x014e, all -> 0x0153 }
        L_0x00f9:
            if (r8 == 0) goto L_0x0100
            if (r5 == 0) goto L_0x016b
            r8.close()     // Catch:{ Throwable -> 0x0166 }
        L_0x0100:
            r2 = r4
        L_0x0101:
            if (r2 != 0) goto L_0x0194
            java.lang.String r2 = "pageTitle"
            com.mcafee.orion.core.util.resource.Resource r3 = r14.m_oResource
            java.lang.String r4 = "import.assignments.error"
            java.util.Locale r5 = r14.locale
            java.lang.String r3 = r3.getString(r4, r5)
            r15.setAttribute(r2, r3)
            java.lang.String r2 = "/AgentMgmt"
            java.lang.String r3 = "/ImportAssignmentsError.jsp"
            com.mcafee.orion.core.servlet.Forward r2 = com.mcafee.orion.core.servlet.ActionResponse.forward(r2, r3)
        L_0x011a:
            return r2
        L_0x011b:
            com.mcafee.orion.core.util.resource.Resource r2 = r14.m_oResource     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "import_file_invalid"
            java.util.Locale r6 = r14.locale     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r12 = 1
            java.lang.Object[] r12 = new java.lang.Object[r12]     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            r13 = 0
            r12[r13] = r7     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r2 = r2.formatString(r3, r6, r12)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            javax.servlet.http.HttpSession r3 = r15.getSession()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r6 = "EPOImportAssignmentError"
            r3.setAttribute(r6, r2)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            javax.servlet.http.HttpSession r2 = r15.getSession()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "AssignmentImportXMLFilename"
            r6 = 0
            r2.setAttribute(r3, r6)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            javax.servlet.http.HttpSession r2 = r15.getSession()     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            java.lang.String r3 = "AssignmentImportXMLFile"
            r6 = 0
            r2.setAttribute(r3, r6)     // Catch:{ Throwable -> 0x0058, all -> 0x0149 }
            goto L_0x00f2
        L_0x0149:
            r2 = move-exception
            r3 = r2
            r4 = r5
            goto L_0x005c
        L_0x014e:
            r2 = move-exception
            r11.addSuppressed(r2)     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
            goto L_0x00f9
        L_0x0153:
            r2 = move-exception
            r3 = r2
            goto L_0x0068
        L_0x0157:
            r10.close()     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
            goto L_0x00f9
        L_0x015b:
            r2 = move-exception
            r4.addSuppressed(r2)     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
            goto L_0x0063
        L_0x0161:
            r10.close()     // Catch:{ Throwable -> 0x0064, all -> 0x0153 }
            goto L_0x0063
        L_0x0166:
            r2 = move-exception
            r9.addSuppressed(r2)     // Catch:{ Exception -> 0x0070 }
            goto L_0x0100
        L_0x016b:
            r8.close()     // Catch:{ Exception -> 0x0070 }
            goto L_0x0100
        L_0x016f:
            r2 = move-exception
            r5.addSuppressed(r2)     // Catch:{ Exception -> 0x0070 }
            goto L_0x006f
        L_0x0175:
            r8.close()     // Catch:{ Exception -> 0x0070 }
            goto L_0x006f
        L_0x017a:
            com.mcafee.orion.core.util.resource.Resource r2 = r14.m_oResource
            java.lang.String r4 = "import_file_empty"
            java.util.Locale r5 = r14.locale
            java.lang.Object[] r6 = new java.lang.Object[r12]
            r6[r3] = r7
            java.lang.String r2 = r2.formatString(r4, r5, r6)
            javax.servlet.http.HttpSession r4 = r15.getSession()
            java.lang.String r5 = "EPOImportAssignmentError"
            r4.setAttribute(r5, r2)
            r2 = r3
            goto L_0x0101
        L_0x0194:
            java.lang.String r2 = "/AgentMgmt"
            java.lang.String r3 = "/ImportAssignmentsConflict.jsp"
            com.mcafee.orion.core.servlet.Forward r2 = com.mcafee.orion.core.servlet.ActionResponse.forward(r2, r3)
            goto L_0x011a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.action.ExportImportAssignmentsAction.PrepareImportAssignment(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):com.mcafee.orion.core.servlet.ActionResponse");
    }

    public ActionResponse ImportAssignments(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        HttpSession session = httpServletRequest.getSession();
        AgentHandlerRuleImportData agentHandlerRuleImportData = (AgentHandlerRuleImportData) session.getAttribute(STR_IMPORT_DATA);
        if (agentHandlerRuleImportData != null) {
            processSelectedInfo(httpServletRequest.getParameter("UIDs"), agentHandlerRuleImportData);
            this.m_impexpService.importAgentHandlerAssignmentsFromXML(UserUtil.getOrionUser(httpServletRequest), (String) session.getAttribute(STR_IMPORT_XML), agentHandlerRuleImportData);
            session.removeAttribute(STR_IMPORT_DATA);
            session.removeAttribute(STR_IMPORT_XML);
        }
        this.m_impexpService.setResource(this.m_oResource);
        return ActionResponse.none();
    }

    private void processSelectedInfo(String str, AgentHandlerRuleImportData agentHandlerRuleImportData) throws Exception {
        if (str != null && str.length() > 0) {
            String[] split = str.split(",");
            for (AgentHandlerRuleImportData.RuleImportConflict next : agentHandlerRuleImportData.getRuleImportActions()) {
                if (FindIfSelected(split, next.getId())) {
                    next.setSelected(true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean FindIfSelected(String[] strArr, int i) throws Exception {
        for (String parseInt : strArr) {
            if (Integer.parseInt(parseInt) == i) {
                return true;
            }
        }
        return false;
    }

    public MultipartFormPolicy getMultipartFormPolicy() {
        MultipartFormPolicy multipartFormPolicy = new MultipartFormPolicy();
        multipartFormPolicy.setMaxUploadSize(Integer.valueOf(this.serverConfig.getFileUploadLimit() * ((int) 1048576)));
        return multipartFormPolicy;
    }

    public OrionUploadFileItem getImportFile() {
        return this.importFile;
    }

    public void setImportFile(OrionUploadFileItem orionUploadFileItem) {
        this.importFile = orionUploadFileItem;
    }

    public String getImportFileName() {
        return this.importFileName;
    }

    public void setImportFileName(String str) {
        this.importFileName = str;
    }

    /* access modifiers changed from: protected */
    public BufferedReader getBufferedReader(InputStream inputStream) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    }
}
