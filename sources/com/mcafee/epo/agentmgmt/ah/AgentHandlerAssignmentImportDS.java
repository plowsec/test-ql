package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.agentmgmt.impexp.AgentHandlerRuleImportData;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.data.DataSource;
import com.mcafee.orion.core.data.DataSourceException;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.util.OrionURI;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.util.resource.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;

public class AgentHandlerAssignmentImportDS implements ListDataSource<AssignmentImport>, UserAware, LocaleAware {
    private static final Logger log = Logger.getLogger(AgentHandlerAssignmentImportDS.class);
    private DisplayAdapter m_displayAdapter = null;
    private String[] m_displayedColumns = {"name", "conflict"};
    private Locale m_locale = null;
    private EPOAgentHandlerServiceInternal m_oAgentHandlerService = null;
    AgentHandlerRuleImportData m_piData = null;
    private Resource m_resource = null;
    OrionUser m_user = null;

    public class AssignmentImport {
        private boolean m_bConflict;
        private int m_iID;
        private String m_szName;

        public AssignmentImport(int i, String str, boolean z) {
            this.m_iID = i;
            this.m_szName = str;
            this.m_bConflict = z;
        }

        public int getId() {
            return this.m_iID;
        }

        public String getName() {
            return this.m_szName;
        }

        public boolean getConflict() {
            return this.m_bConflict;
        }
    }

    public OrionUser getUser() {
        return this.m_user;
    }

    public void setUser(OrionUser orionUser) {
        this.m_user = orionUser;
    }

    public EPOAgentHandlerServiceInternal getAgentHandlerService() {
        return this.m_oAgentHandlerService;
    }

    public void setAgentHandlerService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.m_oAgentHandlerService = ePOAgentHandlerServiceInternal;
    }

    public String[] getAvailableColumns() {
        return this.m_displayedColumns;
    }

    public String[] getDisplayedColumns(String str) {
        return this.m_displayedColumns;
    }

    public AssignmentImport get(String str) throws DataSourceException {
        try {
            for (AgentHandlerRuleImportData.RuleImportConflict next : this.m_piData.getRuleImportActions()) {
                if (Integer.parseInt(str) == next.getId()) {
                    return new AssignmentImport(next.getId(), next.getName(), next.getConflict());
                }
            }
            return null;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Caught exception on get(" + str + ")", e);
            }
            throw new DataSourceException(e);
        }
    }

    public void setData(AgentHandlerRuleImportData agentHandlerRuleImportData) {
        this.m_piData = agentHandlerRuleImportData;
    }

    public String getRegisteredTypeName() {
        return null;
    }

    public List<AssignmentImport> get() throws Exception {
        LinkedList linkedList = new LinkedList();
        for (AgentHandlerRuleImportData.RuleImportConflict next : this.m_piData.getRuleImportActions()) {
            linkedList.add(new AssignmentImport(next.getId(), next.getName(), next.getConflict()));
        }
        return linkedList;
    }

    public String getUID(AssignmentImport assignmentImport) {
        if (assignmentImport != null) {
            return Integer.toString(assignmentImport.getId());
        }
        return null;
    }

    public List<OrionURI> getRelatedDatasourceURIs(String str) throws DataSourceException {
        return new LinkedList();
    }

    public DisplayAdapter getDisplayAdapter(DataSource.DisplayAdapterType displayAdapterType) {
        if (this.m_displayAdapter == null) {
            this.m_displayAdapter = new AgentHandlerAssignmentImportDA("AssignmentImport", this.m_resource);
        }
        return this.m_displayAdapter;
    }

    public String[] getDisplayedColumns() {
        return this.m_displayedColumns;
    }

    public void setDisplayedColumns(String[] strArr) {
        this.m_displayedColumns = strArr;
    }

    public int getLength() throws Exception {
        return get().size();
    }

    public int getNumRowsPerPage() throws Exception {
        return 15;
    }

    public void setNumRowsPerPage(int i) {
    }

    public void setLocale(Locale locale) {
        this.m_locale = locale;
    }

    public Locale getLocale() {
        return this.m_locale;
    }

    public Resource getResource() {
        return this.m_resource;
    }

    public void setResource(Resource resource) {
        this.m_resource = resource;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
