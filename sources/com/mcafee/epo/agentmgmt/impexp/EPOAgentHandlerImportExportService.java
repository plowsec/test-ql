package com.mcafee.epo.agentmgmt.impexp;

import com.mcafee.epo.core.EpoPermissionException;
import com.mcafee.epo.core.EpoValidateException;
import com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignment;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority;
import com.mcafee.epo.core.model.EPOAgentHandlerGroup;
import com.mcafee.epo.core.model.EPOAgentHandlerRule_IPRange;
import com.mcafee.epo.core.model.EPOGroup;
import com.mcafee.epo.core.model.EPORegisteredApacheServer;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.epo.core.util.EPOAuditLogUtil;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.config.LocaleService;
import com.mcafee.orion.core.util.IPUtil;
import com.mcafee.orion.core.util.xml.DomUtil;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class EPOAgentHandlerImportExportService extends EPOAgentHandlerServiceInternal {
    private static Logger m_log = Logger.getLogger(EPOAgentHandlerImportExportService.class);
    private LocaleService localeService = null;

    public String exportAgentHandlerAssignmentsToXML(OrionUser orionUser) throws SQLException, EpoValidateException, EpoPermissionException {
        checkViewPermission(orionUser);
        StringWriter stringWriter = new StringWriter();
        try {
            stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            stringWriter.write("<epo:EPOAgentHandlerAssignmentSchema xmlns:epo=\"mcafee-epo-agenthandlerassignment\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n");
            for (EPOAgentHandlerAssignment ePOAgentHandlerAssignment : getHandlerAssignments(orionUser)) {
                stringWriter.write("<EPOAgentHandlerAssignment name=\"" + StringEscapeUtils.escapeXml(ePOAgentHandlerAssignment.getName()) + "\"" + " id=" + "\"" + ePOAgentHandlerAssignment.getAutoId() + "\"" + " useallhandlers=" + "\"" + ePOAgentHandlerAssignment.getUseAllHandlers() + "\"" + ">\r\n");
                for (EPOAgentHandlerRule_IPRange ePOAgentHandlerRule_IPRange : ePOAgentHandlerAssignment.getIPRanges()) {
                    stringWriter.write("<EPOAgentHandlerRuleIPRange ip6start=\"" + IPUtil.getInet6Address(ePOAgentHandlerRule_IPRange.getIP6Start()).getHostAddress() + "\"" + " ip6end=\"" + IPUtil.getInet6Address(ePOAgentHandlerRule_IPRange.getIP6End()).getHostAddress() + "\"" + " bitcount=" + "\"" + ePOAgentHandlerRule_IPRange.getBitCount() + "\"" + ">\r\n");
                    stringWriter.write("</EPOAgentHandlerRuleIPRange>\r\n");
                }
                for (EPOGroup nodeTextPath2 : ePOAgentHandlerAssignment.getGroups()) {
                    stringWriter.write("<EPOAgentHandlerRuleBranchNode nodeTextPath2=\"" + StringEscapeUtils.escapeXml(nodeTextPath2.getNodeTextPath2()) + "\"" + ">\r\n");
                    stringWriter.write("</EPOAgentHandlerRuleBranchNode>\r\n");
                }
                for (EPOAgentHandlerAssignmentHandlerPriority ePOAgentHandlerAssignmentHandlerPriority : ePOAgentHandlerAssignment.getAssociatedHandlerPriorities()) {
                    if (ePOAgentHandlerAssignmentHandlerPriority.isHandler()) {
                        EPORegisteredApacheServer handlerById = getHandlerById(ePOAgentHandlerAssignmentHandlerPriority.getHandlerId(), orionUser);
                        if (handlerById == null) {
                            m_log.error("Apache server null");
                            if (this.m_audit != null) {
                                Locale resolveLocale = this.localeService.resolveLocale(orionUser);
                                EPOAuditLogUtil.write(this.m_audit, orionUser, "actioninfo.agenthandlerrule.export.audit.displayname", this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.displayname", resolveLocale), 2, this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.message", resolveLocale), false);
                            }
                            return "";
                        }
                        stringWriter.write("<EPOAgentHandlerAssignmentHandler name=\"" + StringEscapeUtils.escapeXml(handlerById.getDnsName()) + "\"" + " type=" + "\"" + "handler" + "\"" + ">\r\n");
                        stringWriter.write("</EPOAgentHandlerAssignmentHandler>\r\n");
                    } else {
                        EPOAgentHandlerGroup handlerGroupsById = getHandlerGroupsById(ePOAgentHandlerAssignmentHandlerPriority.getHandlerGroupId(), orionUser);
                        if (handlerGroupsById == null) {
                            m_log.error("Agent handler group null");
                            if (this.m_audit != null) {
                                Locale resolveLocale2 = this.localeService.resolveLocale(orionUser);
                                EPOAuditLogUtil.write(this.m_audit, orionUser, "actioninfo.agenthandlerrule.export.audit.displayname", this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.displayname", resolveLocale2), 2, this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.message", resolveLocale2), false);
                            }
                            return "";
                        }
                        stringWriter.write("<EPOAgentHandlerAssignmentHandler name=\"" + StringEscapeUtils.escapeXml(handlerGroupsById.getName()) + "\"" + " type=" + "\"" + "group" + "\"" + ">\r\n");
                        stringWriter.write("</EPOAgentHandlerAssignmentHandler>\r\n");
                    }
                }
                stringWriter.write("</EPOAgentHandlerAssignment>\r\n");
            }
            stringWriter.write("</epo:EPOAgentHandlerAssignmentSchema>");
            if (this.m_audit != null) {
                Locale resolveLocale3 = this.localeService.resolveLocale(orionUser);
                EPOAuditLogUtil.write(this.m_audit, orionUser, "actioninfo.agenthandlerrule.export.audit.displayname", this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.displayname", resolveLocale3), 2, this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.message", resolveLocale3), true);
            }
            return stringWriter.toString();
        } catch (Throwable th) {
            Throwable th2 = th;
            if (this.m_audit != null) {
                Locale resolveLocale4 = this.localeService.resolveLocale(orionUser);
                EPOAuditLogUtil.write(this.m_audit, orionUser, "actioninfo.agenthandlerrule.export.audit.displayname", this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.displayname", resolveLocale4), 2, this.m_resource.getString("actioninfo.agenthandlerrule.export.audit.message", resolveLocale4), false);
            }
            throw th2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0206, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0207, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0220, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0221, code lost:
        r1 = r0;
        r2 = r4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01e6  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:59:0x01ab=Splitter:B:59:0x01ab, B:72:0x01dc=Splitter:B:72:0x01dc} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean prepareImportAgentHandlerAssignmentsFromXML(com.mcafee.orion.core.auth.OrionUser r12, java.lang.String r13, com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData r14) throws java.sql.SQLException, com.mcafee.epo.core.EpoValidateException, java.io.IOException, com.mcafee.epo.core.EpoPermissionException, org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException {
        /*
            r11 = this;
            r11.checkEditPermission(r12)
            r3 = 0
            com.mcafee.orion.core.db.base.IDatabase r0 = r11.m_database     // Catch:{ RuntimeException -> 0x0210, Exception -> 0x0217 }
            java.sql.Connection r7 = r0.getConnection(r12)     // Catch:{ RuntimeException -> 0x0210, Exception -> 0x0217 }
            r4 = 0
            java.io.StringReader r0 = new java.io.StringReader     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0.<init>(r13)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.xml.sax.InputSource r1 = new org.xml.sax.InputSource     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r1.<init>(r0)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.w3c.dom.Document r0 = com.mcafee.orion.core.util.xml.ParserUtil.parse(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.w3c.dom.Element r1 = r0.getDocumentElement()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r1.normalize()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.w3c.dom.Element r1 = r0.getDocumentElement()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r1 == 0) goto L_0x00aa
            r0 = 1
        L_0x0027:
            if (r0 == 0) goto L_0x017a
            java.lang.String r0 = "EPOAgentHandlerAssignment"
            java.util.List r0 = r11.getDomChildren(r1, r0)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.util.Iterator r6 = r0.iterator()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
        L_0x0033:
            boolean r0 = r6.hasNext()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r0 == 0) goto L_0x017a
            java.lang.Object r0 = r6.next()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.w3c.dom.Element r0 = (org.w3c.dom.Element) r0     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r2 = 0
            java.lang.String r1 = "EPOAgentHandlerRuleBranchNode"
            java.util.List r1 = r11.getDomChildren(r0, r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.util.Iterator r5 = r1.iterator()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
        L_0x004a:
            boolean r1 = r5.hasNext()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r1 == 0) goto L_0x00ad
            java.lang.Object r1 = r5.next()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            org.w3c.dom.Element r1 = (org.w3c.dom.Element) r1     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r8 = "nodeTextPath2"
            java.lang.String r8 = r1.getAttribute(r8)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            com.mcafee.epo.core.services.EPOGroupServiceInternal r1 = r11.getGroupService()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r9.<init>()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r10 = "NodeTextPath2 = '"
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r10 = org.apache.commons.lang.StringEscapeUtils.escapeSql(r8)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r10 = "'"
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r9 = r9.toString()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.util.List r1 = r1.getWhere(r12, r7, r9)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r1 == 0) goto L_0x0089
            int r1 = r1.size()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r1 != 0) goto L_0x0227
        L_0x0089:
            r1 = 1
            org.apache.log4j.Logger r2 = m_log     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r9.<init>()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r10 = "The group "
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r8 = r9.append(r8)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r9 = " in the imported Agent Handler Assignment list was not found in the System Tree and this Assignment will not be imported."
            java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r8 = r8.toString()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r2.warn(r8)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
        L_0x00a8:
            r2 = r1
            goto L_0x004a
        L_0x00aa:
            r0 = 0
            goto L_0x0027
        L_0x00ad:
            if (r2 != 0) goto L_0x0224
            java.lang.String r1 = "name"
            java.lang.String r1 = r0.getAttribute(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            int r2 = r1.length()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r5 = 255(0xff, float:3.57E-43)
            if (r2 <= r5) goto L_0x00c4
            r2 = 0
            r5 = 255(0xff, float:3.57E-43)
            java.lang.String r1 = r1.substring(r2, r5)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
        L_0x00c4:
            java.lang.String r2 = "id"
            java.lang.String r8 = r0.getAttribute(r2)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao r9 = r11.getEPOAgentHandlerAssignmentDBDao(r7)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r0 = r9.getByName(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r0 != 0) goto L_0x0174
            com.mcafee.orion.core.config.LocaleService r0 = r11.localeService     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.util.Locale r0 = r0.resolveLocale(r12)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            com.mcafee.orion.core.util.resource.Resource r2 = r11.getResource()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r5 = "ah.defaultAssignmentRule"
            r10 = 0
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r2 = r2.formatString(r5, r0, r10)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r5 = r1.trim()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            boolean r2 = r5.equals(r2)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r2 == 0) goto L_0x012d
            r2 = 1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0.<init>()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r1 = "-"
            java.lang.StringBuilder r0 = r0.append(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r1 = r0.toString()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0 = r1
            r5 = r1
        L_0x010b:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r1 = r9.getByName(r0)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r1 == 0) goto L_0x016c
            int r2 = r2 + 1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0.<init>()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r5)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r1 = "-"
            java.lang.StringBuilder r0 = r0.append(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r1 = r0.toString()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0 = r1
            r5 = r1
            goto L_0x010b
        L_0x012d:
            java.lang.String r2 = r1.trim()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r5 = ""
            boolean r2 = r2.equals(r5)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r2 == 0) goto L_0x016b
            com.mcafee.orion.core.util.resource.Resource r1 = r11.getResource()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r2 = "actioninfo.agenthandlerrule.import.audit.displayname"
            r5 = 0
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r2 = r1.formatString(r2, r0, r5)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r1 = 1
            r0 = r2
            r5 = r2
        L_0x0149:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r2 = r9.getByName(r0)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            if (r2 == 0) goto L_0x016c
            int r1 = r1 + 1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0.<init>()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r5)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r2 = "-"
            java.lang.StringBuilder r0 = r0.append(r2)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.StringBuilder r0 = r0.append(r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            java.lang.String r2 = r0.toString()     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0 = r2
            r5 = r2
            goto L_0x0149
        L_0x016b:
            r0 = r1
        L_0x016c:
            r1 = 0
            r14.addRuleImportAction(r8, r0, r1)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0 = 1
        L_0x0171:
            r3 = r0
            goto L_0x0033
        L_0x0174:
            r0 = 1
            r14.addRuleImportAction(r8, r1, r0)     // Catch:{ Throwable -> 0x0204, all -> 0x0220 }
            r0 = 1
            goto L_0x0171
        L_0x017a:
            r6 = r3
            if (r7 == 0) goto L_0x0182
            if (r4 == 0) goto L_0x01d6
            r7.close()     // Catch:{ Throwable -> 0x01a4 }
        L_0x0182:
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            if (r0 == 0) goto L_0x01a3
            com.mcafee.orion.core.config.LocaleService r0 = r11.localeService
            java.util.Locale r0 = r0.resolveLocale(r12)
            java.lang.String r2 = "actioninfo.agenthandlerrule.import.audit.displayname"
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r3 = r1.getString(r2, r0)
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r4 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r5 = r1.getString(r4, r0)
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            r4 = 2
            r1 = r12
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r0, r1, r2, r3, r4, r5, r6)
        L_0x01a3:
            return r6
        L_0x01a4:
            r0 = move-exception
            r4.addSuppressed(r0)     // Catch:{ RuntimeException -> 0x01a9, Exception -> 0x01da, all -> 0x021d }
            goto L_0x0182
        L_0x01a9:
            r0 = move-exception
            r3 = r6
        L_0x01ab:
            org.apache.log4j.Logger r1 = m_log     // Catch:{ all -> 0x01b1 }
            r1.debug(r0)     // Catch:{ all -> 0x01b1 }
            throw r0     // Catch:{ all -> 0x01b1 }
        L_0x01b1:
            r0 = move-exception
            r7 = r0
            r6 = r3
        L_0x01b4:
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            if (r0 == 0) goto L_0x01d5
            com.mcafee.orion.core.config.LocaleService r0 = r11.localeService
            java.util.Locale r0 = r0.resolveLocale(r12)
            java.lang.String r2 = "actioninfo.agenthandlerrule.import.audit.displayname"
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r3 = r1.getString(r2, r0)
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r4 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r5 = r1.getString(r4, r0)
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            r4 = 2
            r1 = r12
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r0, r1, r2, r3, r4, r5, r6)
        L_0x01d5:
            throw r7
        L_0x01d6:
            r7.close()     // Catch:{ RuntimeException -> 0x01a9, Exception -> 0x01da, all -> 0x021d }
            goto L_0x0182
        L_0x01da:
            r0 = move-exception
            r3 = r6
        L_0x01dc:
            org.apache.log4j.Logger r1 = m_log     // Catch:{ all -> 0x01b1 }
            r1.debug(r0)     // Catch:{ all -> 0x01b1 }
            r6 = 0
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            if (r0 == 0) goto L_0x01a3
            com.mcafee.orion.core.config.LocaleService r0 = r11.localeService
            java.util.Locale r0 = r0.resolveLocale(r12)
            java.lang.String r2 = "actioninfo.agenthandlerrule.import.audit.displayname"
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r3 = r1.getString(r2, r0)
            com.mcafee.orion.core.util.resource.Resource r1 = r11.m_resource
            java.lang.String r4 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r5 = r1.getString(r4, r0)
            com.mcafee.orion.core.audit.AuditLogWriter r0 = r11.m_audit
            r4 = 2
            r1 = r12
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r0, r1, r2, r3, r4, r5, r6)
            goto L_0x01a3
        L_0x0204:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0206 }
        L_0x0206:
            r1 = move-exception
            r2 = r0
        L_0x0208:
            if (r7 == 0) goto L_0x020f
            if (r2 == 0) goto L_0x0219
            r7.close()     // Catch:{ Throwable -> 0x0212 }
        L_0x020f:
            throw r1     // Catch:{ RuntimeException -> 0x0210, Exception -> 0x0217 }
        L_0x0210:
            r0 = move-exception
            goto L_0x01ab
        L_0x0212:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ RuntimeException -> 0x0210, Exception -> 0x0217 }
            goto L_0x020f
        L_0x0217:
            r0 = move-exception
            goto L_0x01dc
        L_0x0219:
            r7.close()     // Catch:{ RuntimeException -> 0x0210, Exception -> 0x0217 }
            goto L_0x020f
        L_0x021d:
            r0 = move-exception
            r7 = r0
            goto L_0x01b4
        L_0x0220:
            r0 = move-exception
            r1 = r0
            r2 = r4
            goto L_0x0208
        L_0x0224:
            r0 = r3
            goto L_0x0171
        L_0x0227:
            r1 = r2
            goto L_0x00a8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService.prepareImportAgentHandlerAssignmentsFromXML(com.mcafee.orion.core.auth.OrionUser, java.lang.String, com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:144:0x03a5  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x01a7  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01c3  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:140:0x0393=Splitter:B:140:0x0393, B:67:0x01ae=Splitter:B:67:0x01ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean importAgentHandlerAssignmentsFromXML(com.mcafee.orion.core.auth.OrionUser r21, java.lang.String r22, com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData r23) throws java.sql.SQLException, com.mcafee.epo.core.EpoValidateException, java.io.IOException, com.mcafee.epo.core.EpoPermissionException, javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException {
        /*
            r20 = this;
            r20.checkEditPermission(r21)
            r9 = 0
            r4 = 0
            r0 = r20
            com.mcafee.orion.core.db.base.IDatabase r3 = r0.m_database     // Catch:{ SQLException -> 0x03e5, Exception -> 0x03e2, all -> 0x03dd }
            r0 = r21
            java.sql.Connection r11 = r3.getConnection(r0)     // Catch:{ SQLException -> 0x03e5, Exception -> 0x03e2, all -> 0x03dd }
            r8 = 0
            java.io.StringReader r3 = new java.io.StringReader     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r0 = r22
            r3.<init>(r0)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            org.xml.sax.InputSource r5 = new org.xml.sax.InputSource     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r5.<init>(r3)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            org.w3c.dom.Document r3 = com.mcafee.orion.core.util.xml.ParserUtil.parse(r5)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            org.w3c.dom.Element r5 = r3.getDocumentElement()     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r5.normalize()     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            org.w3c.dom.Element r5 = r3.getDocumentElement()     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            if (r5 == 0) goto L_0x00c9
            r3 = 1
        L_0x002e:
            if (r3 == 0) goto L_0x03ff
            r0 = r20
            com.mcafee.orion.core.config.LocaleService r3 = r0.localeService     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r0 = r21
            java.util.Locale r12 = r3.resolveLocale(r0)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r0 = r20
            com.mcafee.epo.core.dao.EPOAgentHandlerAssignmentDBDao r13 = r0.getEPOAgentHandlerAssignmentDBDao(r11)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            java.lang.String r3 = "EPOAgentHandlerAssignment"
            r0 = r20
            java.util.List r3 = r0.getDomChildren(r5, r3)     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            java.util.Iterator r14 = r3.iterator()     // Catch:{ Throwable -> 0x03f3, all -> 0x03e9 }
            r5 = r4
        L_0x004d:
            boolean r3 = r14.hasNext()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r3 == 0) goto L_0x034f
            java.lang.Object r3 = r14.next()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r0 = r3
            org.w3c.dom.Element r0 = (org.w3c.dom.Element) r0     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r4 = r0
            java.lang.String r3 = "useallhandlers"
            java.lang.String r15 = r4.getAttribute(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r3 = "name"
            java.lang.String r3 = r4.getAttribute(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            int r6 = r3.length()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r7 = 255(0xff, float:3.57E-43)
            if (r6 <= r7) goto L_0x0076
            r6 = 0
            r7 = 255(0xff, float:3.57E-43)
            java.lang.String r3 = r3.substring(r6, r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
        L_0x0076:
            com.mcafee.orion.core.util.resource.Resource r6 = r20.getResource()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = "ah.defaultAssignmentRule"
            r10 = 0
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = r6.formatString(r7, r12, r10)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = r3.trim()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            boolean r6 = r7.equals(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r6 == 0) goto L_0x013a
            r7 = 1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r6.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r6.append(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = "-"
            java.lang.StringBuilder r3 = r3.append(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r3.append(r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = r3.toString()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3 = r6
            r10 = r6
        L_0x00a7:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r6 = r13.getByName(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r6 == 0) goto L_0x00cc
            int r7 = r7 + 1
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r3.append(r10)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = "-"
            java.lang.StringBuilder r3 = r3.append(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r3.append(r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = r3.toString()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3 = r6
            r10 = r6
            goto L_0x00a7
        L_0x00c9:
            r3 = 0
            goto L_0x002e
        L_0x00cc:
            r6 = r3
        L_0x00cd:
            r0 = r23
            boolean r3 = r0.existsInSelectedRuleImportActions(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r3 == 0) goto L_0x004d
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r3 = r13.getByName(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r3 != 0) goto L_0x03f6
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r3 = new com.mcafee.epo.core.model.EPOAgentHandlerAssignment     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r7 = 0
            r3.setAutoId(r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3.setName(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r7 = r3
        L_0x00e8:
            java.lang.String r3 = "true"
            boolean r3 = r15.equals(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r7.setUseAllHandlers(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r3 = "EPOAgentHandlerRuleBranchNode"
            r0 = r20
            java.util.List r3 = r0.getDomChildren(r4, r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.util.ArrayList r10 = new java.util.ArrayList     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r10.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.util.Iterator r16 = r3.iterator()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r6 = r5
        L_0x0103:
            boolean r3 = r16.hasNext()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x01ec
            java.lang.Object r3 = r16.next()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            org.w3c.dom.Element r3 = (org.w3c.dom.Element) r3     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            com.mcafee.epo.product.impexp.NodeIdFinder r5 = new com.mcafee.epo.product.impexp.NodeIdFinder     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r21
            r5.<init>(r11, r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r6 = "nodeTextPath2"
            java.lang.String r3 = r3.getAttribute(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r3 = org.apache.commons.lang.StringEscapeUtils.unescapeXml(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r3 == 0) goto L_0x0138
            r6 = 3
            r17 = 0
            r0 = r17
            int r6 = r5.queryNodeId(r6, r3, r0)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r6 <= 0) goto L_0x017b
            com.mcafee.epo.core.services.EPOGroupServiceInternal r3 = r20.getGroupService()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            com.mcafee.epo.core.model.EPOGroup r3 = r3.getById(r11, r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r10.add(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
        L_0x0138:
            r6 = r5
            goto L_0x0103
        L_0x013a:
            java.lang.String r6 = r3.trim()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = ""
            boolean r6 = r6.equals(r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r6 == 0) goto L_0x03f9
            com.mcafee.orion.core.util.resource.Resource r3 = r20.getResource()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r6 = "actioninfo.agenthandlerrule.import.audit.displayname"
            r7 = 0
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = r3.formatString(r6, r12, r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r6 = 1
            r3 = r7
            r10 = r7
        L_0x0156:
            com.mcafee.epo.core.model.EPOAgentHandlerAssignment r7 = r13.getByName(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r7 == 0) goto L_0x0178
            int r6 = r6 + 1
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r3.append(r10)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = "-"
            java.lang.StringBuilder r3 = r3.append(r7)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r3 = r3.append(r6)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r7 = r3.toString()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r3 = r7
            r10 = r7
            goto L_0x0156
        L_0x0178:
            r6 = r3
            goto L_0x00cd
        L_0x017b:
            org.apache.log4j.Logger r6 = m_log     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            boolean r6 = r6.isInfoEnabled()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            if (r6 == 0) goto L_0x0138
            org.apache.log4j.Logger r6 = m_log     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.StringBuilder r17 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r17.<init>()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r18 = "Ignore group with node path that does not exist or user cannot access: nodeTextPath2="
            java.lang.StringBuilder r17 = r17.append(r18)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r0 = r17
            java.lang.StringBuilder r3 = r0.append(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            java.lang.String r3 = r3.toString()     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            r6.info(r3)     // Catch:{ Throwable -> 0x019e, all -> 0x03ee }
            goto L_0x0138
        L_0x019e:
            r3 = move-exception
            r4 = r5
        L_0x01a0:
            throw r3     // Catch:{ all -> 0x01a1 }
        L_0x01a1:
            r5 = move-exception
            r7 = r5
            r8 = r3
            r6 = r4
        L_0x01a5:
            if (r11 == 0) goto L_0x01ac
            if (r8 == 0) goto L_0x03d8
            r11.close()     // Catch:{ Throwable -> 0x03d2 }
        L_0x01ac:
            throw r7     // Catch:{ SQLException -> 0x01ad, Exception -> 0x0392 }
        L_0x01ad:
            r3 = move-exception
        L_0x01ae:
            org.apache.log4j.Logger r4 = m_log     // Catch:{ all -> 0x01b5 }
            r4.debug(r3)     // Catch:{ all -> 0x01b5 }
            r9 = 0
            throw r3     // Catch:{ all -> 0x01b5 }
        L_0x01b5:
            r3 = move-exception
            r10 = r3
        L_0x01b7:
            com.mcafee.orion.core.util.IOUtil.close(r6)
            r23.clearRuleImportActions()
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            if (r3 == 0) goto L_0x01eb
            r0 = r20
            com.mcafee.orion.core.config.LocaleService r3 = r0.localeService
            r0 = r21
            java.util.Locale r3 = r3.resolveLocale(r0)
            java.lang.String r5 = "actioninfo.agenthandlerrule.import.audit.displayname"
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r6 = r4.getString(r5, r3)
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r7 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r8 = r4.getString(r7, r3)
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            r7 = 2
            r4 = r21
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r3, r4, r5, r6, r7, r8, r9)
        L_0x01eb:
            throw r10
        L_0x01ec:
            int r3 = r10.size()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 <= 0) goto L_0x01f5
            r7.setGroups(r10)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x01f5:
            java.lang.String r3 = "EPOAgentHandlerRuleIPRange"
            r0 = r20
            java.util.List r3 = r0.getDomChildren(r4, r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.util.ArrayList r5 = new java.util.ArrayList     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r5.<init>()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.util.Iterator r16 = r3.iterator()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x0206:
            boolean r3 = r16.hasNext()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x0268
            java.lang.Object r3 = r16.next()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            org.w3c.dom.Element r3 = (org.w3c.dom.Element) r3     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            com.mcafee.epo.core.model.EPOAgentHandlerRule_IPRange r17 = new com.mcafee.epo.core.model.EPOAgentHandlerRule_IPRange     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r17.<init>()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r18 = "ip6start"
            r0 = r18
            java.lang.String r18 = r3.getAttribute(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r18 == 0) goto L_0x0232
            int r19 = r18.length()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r19 <= 0) goto L_0x0232
            java.net.Inet6Address r18 = com.mcafee.orion.core.util.IPUtil.getInet6Address(r18)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            byte[] r18 = r18.getAddress()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r17.setIP6Start(r18)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x0232:
            java.lang.String r18 = "ip6end"
            r0 = r18
            java.lang.String r18 = r3.getAttribute(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r18 == 0) goto L_0x024d
            int r19 = r18.length()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r19 <= 0) goto L_0x024d
            java.net.Inet6Address r18 = com.mcafee.orion.core.util.IPUtil.getInet6Address(r18)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            byte[] r18 = r18.getAddress()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r17.setIP6End(r18)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x024d:
            java.lang.String r18 = "bitcount"
            r0 = r18
            java.lang.String r3 = r3.getAttribute(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            int r3 = java.lang.Integer.parseInt(r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r17
            r0.setBitCount(r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r17
            r5.add(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            goto L_0x0206
        L_0x0264:
            r3 = move-exception
            r4 = r6
            goto L_0x01a0
        L_0x0268:
            int r3 = r5.size()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 <= 0) goto L_0x0271
            r7.setIPRanges(r5)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x0271:
            java.util.ArrayList r16 = new java.util.ArrayList     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r16.<init>()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r3 = "true"
            boolean r3 = r15.equals(r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 != 0) goto L_0x030e
            java.lang.String r3 = "EPOAgentHandlerAssignmentHandler"
            r0 = r20
            java.util.List r3 = r0.getDomChildren(r4, r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r4 = 1
            java.util.Iterator r15 = r3.iterator()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x028b:
            boolean r3 = r15.hasNext()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x0303
            java.lang.Object r3 = r15.next()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            org.w3c.dom.Element r3 = (org.w3c.dom.Element) r3     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r17 = "name"
            r0 = r17
            java.lang.String r17 = r3.getAttribute(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r18 = "type"
            r0 = r18
            java.lang.String r3 = r3.getAttribute(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority r18 = new com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r18.<init>()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r19 = "group"
            r0 = r19
            boolean r3 = r3.equals(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x02db
            r0 = r20
            r1 = r21
            r2 = r17
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r3 = r0.getHandlerGroupByNameWithOutPermissionCheck(r1, r2)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x03fc
            int r3 = r3.getAutoId()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r18
            r0.setHandlerGroupId(r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            int r3 = r4 + 1
            r0 = r18
            r0.setPriority(r4)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r16
            r1 = r18
            r0.add(r1)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x02d9:
            r4 = r3
            goto L_0x028b
        L_0x02db:
            r0 = r20
            r1 = r17
            r2 = r21
            com.mcafee.epo.core.model.EPORegisteredApacheServer r3 = r0.getHandlerByDNSNameWithOutPermissionCheck(r1, r2)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x03fc
            int r3 = r3.getAutoId()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r18
            r0.setHandlerId(r3)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            int r3 = r4 + 1
            r0 = r18
            r0.setPriority(r4)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r0 = r16
            r1 = r18
            r0.add(r1)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            goto L_0x02d9
        L_0x02ff:
            r3 = move-exception
            r7 = r3
            goto L_0x01a5
        L_0x0303:
            int r3 = r16.size()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 <= 0) goto L_0x030e
            r0 = r16
            r7.setAssociatedHandlerPriorities(r0)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x030e:
            int r3 = r10.size()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 != 0) goto L_0x0347
            int r3 = r5.size()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 != 0) goto L_0x0347
            org.apache.log4j.Logger r3 = m_log     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            boolean r3 = r3.isInfoEnabled()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            if (r3 == 0) goto L_0x0344
            org.apache.log4j.Logger r3 = m_log     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r4.<init>()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r5 = "Ignore assignment because agent criteria (system tree location or subnet) is empty for assignment '"
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r5 = r7.getName()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r5 = "' or the handler list is empty."
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            java.lang.String r4 = r4.toString()     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            r3.info(r4)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
        L_0x0344:
            r5 = r6
            goto L_0x004d
        L_0x0347:
            r0 = r20
            r1 = r21
            r0.addUpdateHandlerAssignment(r1, r7)     // Catch:{ Throwable -> 0x0264, all -> 0x02ff }
            goto L_0x0344
        L_0x034f:
            r9 = 1
            r6 = r5
        L_0x0351:
            if (r11 == 0) goto L_0x0358
            if (r8 == 0) goto L_0x03ce
            r11.close()     // Catch:{ Throwable -> 0x038d }
        L_0x0358:
            com.mcafee.orion.core.util.IOUtil.close(r6)
            r23.clearRuleImportActions()
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            if (r3 == 0) goto L_0x038c
            r0 = r20
            com.mcafee.orion.core.config.LocaleService r3 = r0.localeService
            r0 = r21
            java.util.Locale r3 = r3.resolveLocale(r0)
            java.lang.String r5 = "actioninfo.agenthandlerrule.import.audit.displayname"
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r6 = r4.getString(r5, r3)
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r7 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r8 = r4.getString(r7, r3)
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            r7 = 2
            r4 = r21
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r3, r4, r5, r6, r7, r8, r9)
        L_0x038c:
            return r9
        L_0x038d:
            r3 = move-exception
            r8.addSuppressed(r3)     // Catch:{ SQLException -> 0x01ad, Exception -> 0x0392 }
            goto L_0x0358
        L_0x0392:
            r3 = move-exception
        L_0x0393:
            org.apache.log4j.Logger r4 = m_log     // Catch:{ all -> 0x01b5 }
            r4.debug(r3)     // Catch:{ all -> 0x01b5 }
            r9 = 0
            com.mcafee.orion.core.util.IOUtil.close(r6)
            r23.clearRuleImportActions()
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            if (r3 == 0) goto L_0x038c
            r0 = r20
            com.mcafee.orion.core.config.LocaleService r3 = r0.localeService
            r0 = r21
            java.util.Locale r3 = r3.resolveLocale(r0)
            java.lang.String r5 = "actioninfo.agenthandlerrule.import.audit.displayname"
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r6 = r4.getString(r5, r3)
            r0 = r20
            com.mcafee.orion.core.util.resource.Resource r4 = r0.m_resource
            java.lang.String r7 = "actioninfo.agenthandlerrule.import.audit.message"
            java.lang.String r8 = r4.getString(r7, r3)
            r0 = r20
            com.mcafee.orion.core.audit.AuditLogWriter r3 = r0.m_audit
            r7 = 2
            r4 = r21
            com.mcafee.epo.core.util.EPOAuditLogUtil.write(r3, r4, r5, r6, r7, r8, r9)
            goto L_0x038c
        L_0x03ce:
            r11.close()     // Catch:{ SQLException -> 0x01ad, Exception -> 0x0392 }
            goto L_0x0358
        L_0x03d2:
            r3 = move-exception
            r8.addSuppressed(r3)     // Catch:{ SQLException -> 0x01ad, Exception -> 0x0392 }
            goto L_0x01ac
        L_0x03d8:
            r11.close()     // Catch:{ SQLException -> 0x01ad, Exception -> 0x0392 }
            goto L_0x01ac
        L_0x03dd:
            r3 = move-exception
            r10 = r3
            r6 = r4
            goto L_0x01b7
        L_0x03e2:
            r3 = move-exception
            r6 = r4
            goto L_0x0393
        L_0x03e5:
            r3 = move-exception
            r6 = r4
            goto L_0x01ae
        L_0x03e9:
            r3 = move-exception
            r7 = r3
            r6 = r4
            goto L_0x01a5
        L_0x03ee:
            r3 = move-exception
            r7 = r3
            r6 = r5
            goto L_0x01a5
        L_0x03f3:
            r3 = move-exception
            goto L_0x01a0
        L_0x03f6:
            r7 = r3
            goto L_0x00e8
        L_0x03f9:
            r6 = r3
            goto L_0x00cd
        L_0x03fc:
            r3 = r4
            goto L_0x02d9
        L_0x03ff:
            r6 = r4
            goto L_0x0351
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.impexp.EPOAgentHandlerImportExportService.importAgentHandlerAssignmentsFromXML(com.mcafee.orion.core.auth.OrionUser, java.lang.String, com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData):boolean");
    }

    public LocaleService getLocaleService() {
        return this.localeService;
    }

    public void setLocaleService(LocaleService localeService2) {
        this.localeService = localeService2;
    }

    /* access modifiers changed from: protected */
    public EPOAgentHandlerAssignmentDBDao getEPOAgentHandlerAssignmentDBDao(Connection connection) {
        return new EPOAgentHandlerAssignmentDBDao(connection);
    }

    /* access modifiers changed from: protected */
    public List<Element> getDomChildren(@NotNull Element element, @NotNull String str) {
        return DomUtil.getChildren(element, str);
    }
}
