package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.computermgmt.agentmgmttemp.AgentHandlerListDA;
import com.mcafee.epo.core.model.EPOAgentHandlerAssignment;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.DataSource;
import com.mcafee.orion.core.data.DataSourceException;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.util.resource.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

public class AgentHandlerAssignmentsDS implements ListDataSource<AgentHandlerAssignment>, UserAware {
    private static final Logger m_log = Logger.getLogger(AgentHandlerAssignmentsDS.class);
    private boolean m_dataLoaded = false;
    private Database m_db;
    private List<AgentHandlerAssignment> m_handlerAssignments = new LinkedList();
    private EPOAgentHandlerServiceInternal m_oAgentHandlerService = null;
    private DisplayAdapter m_oDisplayAdapter = null;
    private Locale m_oLocale = null;
    private Resource m_oResource = null;
    private OrionUser m_oUser = null;
    private String[] m_szDisplayedColumns = {"assignment", "actions"};
    private boolean showDefaultRule = false;

    private List<AgentHandlerAssignment> getLoadedData() {
        return this.m_handlerAssignments;
    }

    public EPOAgentHandlerServiceInternal getAgentHandlerService() {
        return this.m_oAgentHandlerService;
    }

    public void setAgentHandlerService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.m_oAgentHandlerService = ePOAgentHandlerServiceInternal;
    }

    public Resource getResource() {
        return this.m_oResource;
    }

    public void setResource(Resource resource) {
        this.m_oResource = resource;
    }

    public Database getDatabase() {
        return this.m_db;
    }

    public void setDatabase(Database database) {
        this.m_db = database;
    }

    public AgentHandlerAssignment get(String str) throws DataSourceException {
        if (str != null) {
            try {
                for (AgentHandlerAssignment next : getLoadedData()) {
                    if (next.getHandlerAssignmentID() == NumberUtils.toInt(str, -1)) {
                        return next;
                    }
                }
            } catch (Exception e) {
                throw new DataSourceException(e);
            }
        }
        return null;
    }

    public String getUID(AgentHandlerAssignment agentHandlerAssignment) {
        return Integer.toString(agentHandlerAssignment.getHandlerAssignmentID());
    }

    public String[] getDisplayedColumns() {
        return this.m_szDisplayedColumns;
    }

    public String[] getAvailableColumns() {
        return this.m_szDisplayedColumns;
    }

    public void setDisplayedColumns(String[] strArr) throws DataSourceException {
        this.m_szDisplayedColumns = strArr;
    }

    public int getLength() throws Exception {
        LoadData();
        return getLoadedData().size();
    }

    public String getRegisteredTypeName() {
        return null;
    }

    public List<AgentHandlerAssignment> get() throws Exception {
        LoadData();
        return getLoadedData();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a3, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00af, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        m_log.error("An exception occurred trying to determine if we have valid handlers", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b7, code lost:
        com.mcafee.orion.core.util.IOUtil.close((java.lang.AutoCloseable) null);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00af A[ExcHandler: Exception (r0v1 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0003] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doesAssignmentHaveValidHandlers(com.mcafee.epo.core.model.EPOAgentHandlerAssignment r8) {
        /*
            r7 = this;
            r2 = 0
            r1 = 1
            r3 = 0
            com.mcafee.orion.core.db.base.Database r0 = r7.getDatabase()     // Catch:{ SQLException -> 0x00a2, Exception -> 0x00af }
            com.mcafee.orion.core.auth.OrionUser r4 = r7.getUser()     // Catch:{ SQLException -> 0x00a2, Exception -> 0x00af }
            java.sql.Connection r3 = r0.getConnection(r4)     // Catch:{ SQLException -> 0x00a2, Exception -> 0x00af }
            java.util.List r0 = r8.getAssociatedHandlerPriorities()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            boolean r4 = r8.getUseAllHandlers()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r4 == 0) goto L_0x001e
            com.mcafee.orion.core.util.IOUtil.close(r3)
            r0 = r1
        L_0x001d:
            return r0
        L_0x001e:
            java.util.Iterator r4 = r0.iterator()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
        L_0x0022:
            boolean r0 = r4.hasNext()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r0 == 0) goto L_0x009c
            java.lang.Object r0 = r4.next()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority r0 = (com.mcafee.epo.core.model.EPOAgentHandlerAssignmentHandlerPriority) r0     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            r0.getHandlerId()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            int r5 = r0.getHandlerId()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r5 <= 0) goto L_0x0050
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r5 = r7.m_oAgentHandlerService     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            int r0 = r0.getHandlerId()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.orion.core.auth.OrionUser r6 = r7.getUser()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r0 = r5.getHandlerById(r0, r6)     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            boolean r0 = r0.getEnabled()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r0 == 0) goto L_0x0022
            com.mcafee.orion.core.util.IOUtil.close(r3)
            r0 = r1
            goto L_0x001d
        L_0x0050:
            com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal r5 = r7.m_oAgentHandlerService     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            int r0 = r0.getHandlerGroupId()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.orion.core.auth.OrionUser r6 = r7.getUser()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.epo.core.model.EPOAgentHandlerGroup r0 = r5.getHandlerGroupsById(r0, r6)     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r0 != 0) goto L_0x006c
            org.apache.log4j.Logger r0 = m_log     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            java.lang.String r1 = "Agent handler group null"
            r0.error(r1)     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.orion.core.util.IOUtil.close(r3)
            r0 = r2
            goto L_0x001d
        L_0x006c:
            boolean r5 = r0.getEnabled()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r5 == 0) goto L_0x0022
            boolean r5 = r0.getLoadBalancerSet()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r5 == 0) goto L_0x007d
            com.mcafee.orion.core.util.IOUtil.close(r3)
            r0 = r1
            goto L_0x001d
        L_0x007d:
            java.util.List r0 = r0.getAgentHandlers()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            java.util.Iterator r5 = r0.iterator()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
        L_0x0085:
            boolean r0 = r5.hasNext()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r0 == 0) goto L_0x0022
            java.lang.Object r0 = r5.next()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            com.mcafee.epo.core.model.EPORegisteredApacheServer r0 = (com.mcafee.epo.core.model.EPORegisteredApacheServer) r0     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            boolean r0 = r0.getEnabled()     // Catch:{ SQLException -> 0x00c3, Exception -> 0x00af }
            if (r0 == 0) goto L_0x0085
            com.mcafee.orion.core.util.IOUtil.close(r3)
            r0 = r1
            goto L_0x001d
        L_0x009c:
            com.mcafee.orion.core.util.IOUtil.close(r3)
        L_0x009f:
            r0 = r2
            goto L_0x001d
        L_0x00a2:
            r0 = move-exception
            r1 = r3
        L_0x00a4:
            org.apache.log4j.Logger r3 = m_log     // Catch:{ all -> 0x00c0 }
            java.lang.String r4 = "A SQL exception occurred while trying to determine if we have valid handlers"
            r3.error(r4, r0)     // Catch:{ all -> 0x00c0 }
            com.mcafee.orion.core.util.IOUtil.close(r1)
            goto L_0x009f
        L_0x00af:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log     // Catch:{ all -> 0x00bb }
            java.lang.String r4 = "An exception occurred trying to determine if we have valid handlers"
            r1.error(r4, r0)     // Catch:{ all -> 0x00bb }
            com.mcafee.orion.core.util.IOUtil.close(r3)
            goto L_0x009f
        L_0x00bb:
            r0 = move-exception
        L_0x00bc:
            com.mcafee.orion.core.util.IOUtil.close(r3)
            throw r0
        L_0x00c0:
            r0 = move-exception
            r3 = r1
            goto L_0x00bc
        L_0x00c3:
            r0 = move-exception
            r1 = r3
            goto L_0x00a4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.ah.AgentHandlerAssignmentsDS.doesAssignmentHaveValidHandlers(com.mcafee.epo.core.model.EPOAgentHandlerAssignment):boolean");
    }

    private synchronized void LoadData() throws Exception {
        if (!this.m_dataLoaded) {
            for (EPOAgentHandlerAssignment ePOAgentHandlerAssignment : this.m_oAgentHandlerService.getHandlerAssignments(this.m_oUser)) {
                boolean doesAssignmentHaveValidHandlers = doesAssignmentHaveValidHandlers(ePOAgentHandlerAssignment);
                getLoadedData().add(new AgentHandlerAssignment(ePOAgentHandlerAssignment.getAutoId(), ePOAgentHandlerAssignment.getPriority(), ePOAgentHandlerAssignment.getName(), doesAssignmentHaveValidHandlers));
            }
            if (this.showDefaultRule) {
                getLoadedData().add(new AgentHandlerAssignment(-1, Integer.MAX_VALUE, this.m_oResource.formatString("ah.defaultAssignmentRule", this.m_oLocale, new Object[0]), true));
            }
            this.m_dataLoaded = true;
        }
    }

    public synchronized void ClearCachedData() {
        getLoadedData().clear();
        this.m_dataLoaded = false;
    }

    public DisplayAdapter getDisplayAdapter(DataSource.DisplayAdapterType displayAdapterType) {
        if (this.m_oDisplayAdapter == null) {
            this.m_oDisplayAdapter = new AgentHandlerListDA("AgentHandlerAssignments", this.m_oResource);
        }
        return this.m_oDisplayAdapter;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void setLocale(Locale locale) {
        this.m_oLocale = locale;
    }

    public void setUser(OrionUser orionUser) {
        this.m_oUser = orionUser;
    }

    public OrionUser getUser() {
        return this.m_oUser;
    }

    public boolean getShowDefaultRule() {
        return this.showDefaultRule;
    }

    public void setShowDefaultRule(boolean z) {
        this.showDefaultRule = z;
    }

    public class AgentHandlerAssignment {
        private boolean m_hasValidHandlers = false;
        private int m_iHandlerAssignmentID;
        private int m_iPriority;
        private String m_szAssignment;

        public AgentHandlerAssignment(int i, int i2, String str, boolean z) {
            this.m_iHandlerAssignmentID = i;
            this.m_iPriority = i2;
            this.m_szAssignment = str;
            this.m_hasValidHandlers = z;
        }

        public int getHandlerAssignmentID() {
            return this.m_iHandlerAssignmentID;
        }

        public void setHandlerAssignmentID(int i) {
            this.m_iHandlerAssignmentID = i;
        }

        public int getPriority() {
            return this.m_iPriority;
        }

        public void setPriority(int i) {
            this.m_iPriority = i;
        }

        public String getAssignment() {
            return this.m_szAssignment;
        }

        public String getActions() {
            return "";
        }

        public boolean getHasValidHandlers() {
            return this.m_hasValidHandlers;
        }

        public void setHasValidHandlers(boolean z) {
            this.m_hasValidHandlers = z;
        }
    }
}
