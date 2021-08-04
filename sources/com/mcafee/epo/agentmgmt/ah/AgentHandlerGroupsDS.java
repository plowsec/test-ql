package com.mcafee.epo.agentmgmt.ah;

import com.mcafee.epo.computermgmt.agentmgmttemp.AgentHandlerListDA;
import com.mcafee.epo.core.dao.EPOAgentHandlerGroupToHandlersDBDao;
import com.mcafee.epo.core.model.EPOAgentHandlerGroup;
import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.auth.UserLoader;
import com.mcafee.orion.core.data.DataSource;
import com.mcafee.orion.core.data.DataSourceException;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.data.Sortable;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.ui.Nameable;
import com.mcafee.orion.core.util.IOUtil;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;

public class AgentHandlerGroupsDS implements ListDataSource<AgentHandlerGroup>, Sortable, UserAware, Nameable {
    private Logger log = Logger.getLogger(AgentHandlerGroupsDS.class);
    private EPOAgentHandlerServiceInternal m_oAgentHandlerService = null;
    private DisplayAdapter m_oDisplayAdapter = null;
    private Locale m_oLocale = null;
    private Resource m_oResource = null;
    private OrionUser m_oUser = null;
    private String m_sortColumn = null;
    private int m_sortOrder = 0;
    private String[] m_szAvailableColumns = {"groupname", "virtualdnsname", "virtualipaddress", "loadBalancerSet", "handlercount", "agentcount"};
    private String[] m_szDisplayedColumns = {"groupname", "virtualdnsname", "virtualipaddress", "handlercount", "actions"};
    private String[] m_szInternalColumns = {"Name", "VirtualDNSName", null, null};
    private String[] m_szSortableColumns = {"groupname", "virtualdnsname", "handlercount", "agentcount"};
    private UserLoader userLoader = null;

    public String getDisplayName(Locale locale) {
        return this.m_oResource.formatString("agentmgmt.exported.table.name", locale, new Object[0]);
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

    public AgentHandlerGroup get(String str) throws DataSourceException {
        return null;
    }

    public String getUID(AgentHandlerGroup agentHandlerGroup) {
        if (agentHandlerGroup == null) {
            return null;
        }
        return Integer.toString(agentHandlerGroup.getHandlerGroupID());
    }

    public String[] getDisplayedColumns() {
        return this.m_szDisplayedColumns;
    }

    public String[] getAvailableColumns() {
        return this.m_szAvailableColumns;
    }

    public void setDisplayedColumns(String[] strArr) throws DataSourceException {
        this.m_szDisplayedColumns = strArr;
    }

    public void setSortColumn(String str, int i) {
        setSortColumn(str);
        setSortOrder(i);
    }

    public String[] getSortableColumns() {
        return this.m_szSortableColumns;
    }

    public String getSortColumn() {
        return this.m_sortColumn;
    }

    public void setSortColumn(String str) {
        this.m_sortColumn = str;
    }

    public int getSortOrder() {
        return this.m_sortOrder;
    }

    public void setSortOrder(int i) {
        this.m_sortOrder = i;
    }

    public int getLength() throws Exception {
        return LoadData().size();
    }

    public String getRegisteredTypeName() {
        return null;
    }

    public List<AgentHandlerGroup> get() throws Exception {
        return LoadData();
    }

    private LinkedList<AgentHandlerGroup> LoadData() throws Exception {
        String str;
        boolean z;
        Connection connection;
        int i;
        int i2;
        Connection connection2 = null;
        LinkedList<AgentHandlerGroup> linkedList = new LinkedList<>();
        int i3 = 0;
        while (true) {
            if (i3 >= this.m_szSortableColumns.length) {
                str = null;
                break;
            } else if (this.m_szSortableColumns[i3].equals(this.m_sortColumn)) {
                str = this.m_szInternalColumns[i3];
                break;
            } else {
                i3++;
            }
        }
        EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal = this.m_oAgentHandlerService;
        OrionUser orionUser = this.m_oUser;
        if (this.m_sortOrder == 0) {
            z = true;
        } else {
            z = false;
        }
        List<EPOAgentHandlerGroup> handlerGroups = ePOAgentHandlerServiceInternal.getHandlerGroups(orionUser, str, z);
        try {
            connection = OrionCore.getDb().getConnection(this.m_oUser);
            try {
                for (EPOAgentHandlerGroup ePOAgentHandlerGroup : handlerGroups) {
                    if (Arrays.asList(getDisplayedColumns()).contains("agentcount")) {
                        i = this.m_oAgentHandlerService.getAgentCountByHandlerGroupID(this.m_oUser, ePOAgentHandlerGroup.getAutoId());
                    } else {
                        i = 0;
                    }
                    if (Arrays.asList(getDisplayedColumns()).contains("handlercount")) {
                        i2 = new EPOAgentHandlerGroupToHandlersDBDao(connection).getByGroupId(ePOAgentHandlerGroup.getAutoId()).size();
                    } else {
                        i2 = 0;
                    }
                    linkedList.add(new AgentHandlerGroup(ePOAgentHandlerGroup.getAutoId(), i2, i, ePOAgentHandlerGroup.getVirtualDNSName(), ePOAgentHandlerGroup.getVirtualIP(), ePOAgentHandlerGroup.getEnabled(), ePOAgentHandlerGroup.getName(), ePOAgentHandlerGroup.getLoadBalancerSet()));
                }
                IOUtil.close(connection);
                if (str == null && (this.m_sortColumn.equals("handlercount") || this.m_sortColumn.equals("agentcount"))) {
                    Collections.sort(linkedList, new AgentHandlerGroupComparator(this.m_sortColumn, this.m_sortOrder));
                }
                return linkedList;
            } catch (Exception e) {
                e = e;
                connection2 = connection;
                try {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (Throwable th) {
                    th = th;
                    connection = connection2;
                    IOUtil.close(connection);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                IOUtil.close(connection);
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
        } catch (Throwable th3) {
            th = th3;
            connection = null;
            IOUtil.close(connection);
            throw th;
        }
    }

    public DisplayAdapter getDisplayAdapter(DataSource.DisplayAdapterType displayAdapterType) {
        if (this.m_oDisplayAdapter == null) {
            this.m_oDisplayAdapter = new AgentHandlerListDA("AgentHandlerGroups", this.m_oResource);
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

    class AgentHandlerGroupComparator implements Comparator<AgentHandlerGroup> {
        private String m_sortColumn;
        private int m_sortOrder;

        public AgentHandlerGroupComparator(String str, int i) {
            this.m_sortColumn = str;
            this.m_sortOrder = i;
        }

        public int compare(AgentHandlerGroup agentHandlerGroup, AgentHandlerGroup agentHandlerGroup2) {
            if (this.m_sortColumn.equals("handlercount")) {
                if (this.m_sortOrder == 0) {
                    return agentHandlerGroup.getHandlercount() - agentHandlerGroup2.getHandlercount();
                }
                return agentHandlerGroup2.getHandlercount() - agentHandlerGroup.getHandlercount();
            } else if (this.m_sortOrder == 0) {
                return agentHandlerGroup.getAgentcount() - agentHandlerGroup2.getAgentcount();
            } else {
                return agentHandlerGroup2.getAgentcount() - agentHandlerGroup.getAgentcount();
            }
        }
    }

    public class AgentHandlerGroup extends EPOAgentHandlerGroup {
        private boolean m_bEnabled;
        private int m_iAgentCount;
        private int m_iHandlerCount;
        private int m_iHandlerGroupID;
        private boolean m_loadBalancerSet;
        private String m_szGroupName;
        private String m_szVirtualDnsName;
        private String m_szVirtualIPAddress;

        public AgentHandlerGroup(int i, int i2, int i3, String str, String str2, boolean z, String str3, boolean z2) {
            this.m_iHandlerGroupID = i;
            this.m_iHandlerCount = i2;
            this.m_iAgentCount = i3;
            this.m_szVirtualDnsName = str;
            this.m_szVirtualIPAddress = str2;
            this.m_bEnabled = z;
            this.m_szGroupName = str3;
            this.m_loadBalancerSet = z2;
        }

        public boolean getEnabled() {
            return this.m_bEnabled;
        }

        public int getHandlerGroupID() {
            return this.m_iHandlerGroupID;
        }

        public void setHandlerGroupID(int i) {
            this.m_iHandlerGroupID = i;
        }

        public int getHandlercount() {
            return this.m_iHandlerCount;
        }

        public void setHandlercount(int i) {
            this.m_iHandlerCount = i;
        }

        public int getAgentcount() {
            return this.m_iAgentCount;
        }

        public void setAgentcount(int i) {
            this.m_iAgentCount = i;
        }

        public String getVirtualdnsname() {
            return this.m_szVirtualDnsName;
        }

        public String getGroupname() {
            return this.m_szGroupName;
        }

        public void setVirtualdnsname(String str) {
            this.m_szVirtualDnsName = str;
        }

        public String getVirtualipaddress() {
            return this.m_szVirtualIPAddress;
        }

        public void setVirtualipaddress(String str) {
            this.m_szVirtualIPAddress = str;
        }

        public String getActions() {
            return "";
        }

        public boolean isLoadBalancerSet() {
            return this.m_loadBalancerSet;
        }

        public boolean getLoadBalancerSet() {
            return this.m_loadBalancerSet;
        }

        public void setLoadBalancerSet(boolean z) {
            this.m_loadBalancerSet = z;
        }
    }

    private UserLoader getUserLoader() {
        return this.userLoader;
    }

    public void setUserLoader(UserLoader userLoader2) {
        this.userLoader = userLoader2;
    }
}
