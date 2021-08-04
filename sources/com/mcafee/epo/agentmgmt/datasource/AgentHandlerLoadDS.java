package com.mcafee.epo.agentmgmt.datasource;

import com.mcafee.epo.agentmgmt.model.AgentHandlerLoadData;
import com.mcafee.epo.agentmgmt.model.AgentHandlerLoadDataComparator;
import com.mcafee.epo.agentmgmt.service.AgentHandlerLoadService;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.UserLoader;
import com.mcafee.orion.core.data.DataSource;
import com.mcafee.orion.core.data.DataSourceException;
import com.mcafee.orion.core.data.DataSourceFilter;
import com.mcafee.orion.core.data.DataSourceOutputDescriptor;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.data.Sortable;
import com.mcafee.orion.core.ui.DisplayAdapter;
import com.mcafee.orion.core.util.IOUtil;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.validation.constraints.NotNull;

public class AgentHandlerLoadDS implements Sortable, ListDataSource<AgentHandlerLoadData>, DataSourceOutputDescriptor {
    private List<AgentHandlerLoadData> agentHandlerLoadDataList = new ArrayList();
    private AgentHandlerLoadService agentHandlerLoadService = null;
    private String[] availableColumns = {"agentHandlerName", "agentCount"};
    private String[] defaultColumns = {"agentHandlerName", "agentCount"};
    private DisplayAdapter displayAdapter = null;
    private String displayName = null;
    private String[] displayedColumns = this.defaultColumns;
    private DataSourceFilter filter;
    private List<DataSourceFilter> filters = null;
    private Locale locale = null;
    private String registeredTypeName = null;
    private Resource resource;
    private String sortColumn = null;
    private String[] sortColumns = {"agentHandlerName", "agentCount"};
    private int sortOrder = 0;
    private UserLoader userLoader;

    public void setAgentHandlerLoadDataList(List<AgentHandlerLoadData> list) {
        this.agentHandlerLoadDataList = list;
    }

    public AgentHandlerLoadData get(String str) throws DataSourceException {
        return null;
    }

    public String getUID(@NotNull AgentHandlerLoadData agentHandlerLoadData) {
        if (agentHandlerLoadData == null) {
            return null;
        }
        return agentHandlerLoadData.getAgentHandlerName();
    }

    public String[] getDisplayedColumns() {
        return this.displayedColumns;
    }

    public String[] getAvailableColumns() {
        return this.availableColumns;
    }

    public void setDisplayedColumns(String[] strArr) throws DataSourceException {
        this.displayedColumns = strArr;
    }

    public int getLength() throws Exception {
        loadData();
        return this.agentHandlerLoadDataList.size();
    }

    public UserLoader getUserLoader() {
        return this.userLoader;
    }

    public void setUserLoader(UserLoader userLoader2) {
        this.userLoader = userLoader2;
    }

    private synchronized void loadData() throws Exception {
        this.agentHandlerLoadDataList.clear();
        this.agentHandlerLoadDataList = getAgentHandlerLoadDataList();
        if ("agentHandlerName".equals(getSortColumn()) || "agentCount".equals(getSortColumn())) {
            this.agentHandlerLoadDataList = sortByColumn(this.agentHandlerLoadDataList, getSortColumn(), getSortOrder(), getLocale());
        }
    }

    /* access modifiers changed from: protected */
    public List<AgentHandlerLoadData> getAgentHandlerLoadDataList() throws SQLException {
        new ArrayList();
        Connection connection = null;
        try {
            connection = OrionCore.getDb().getConnection(getUserLoader().getDefaultTenantSystemUser());
            return getAgentHandlerLoadService().getAgentHandlerLoadData(connection);
        } finally {
            IOUtil.close(connection);
        }
    }

    private List<AgentHandlerLoadData> sortByColumn(List<AgentHandlerLoadData> list, String str, int i, Locale locale2) {
        Collections.sort(list, new AgentHandlerLoadDataComparator(str, i, locale2));
        return list;
    }

    public String getRegisteredTypeName() {
        return this.registeredTypeName;
    }

    public void setRegisteredTypeName(String str) {
        this.registeredTypeName = str;
    }

    public List<AgentHandlerLoadData> get() throws Exception {
        loadData();
        return this.agentHandlerLoadDataList;
    }

    public DisplayAdapter getDisplayAdapter(DataSource.DisplayAdapterType displayAdapterType) {
        return this.displayAdapter;
    }

    public boolean isListDataSource() {
        return true;
    }

    public boolean isChartDataSource() {
        return false;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public AgentHandlerLoadService getAgentHandlerLoadService() {
        return this.agentHandlerLoadService;
    }

    public void setAgentHandlerLoadService(AgentHandlerLoadService agentHandlerLoadService2) {
        this.agentHandlerLoadService = agentHandlerLoadService2;
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String str) {
        this.displayName = str;
    }

    public DisplayAdapter getDisplayAdapter() {
        return this.displayAdapter;
    }

    public void setDisplayAdapter(DisplayAdapter displayAdapter2) {
        this.displayAdapter = displayAdapter2;
    }

    public String getSortColumn() {
        return this.sortColumn;
    }

    public void setSortColumn(String str) {
        this.sortColumn = str;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortColumn(String str, int i) {
        setSortColumn(str);
        setSortOrder(i);
    }

    public String[] getSortableColumns() {
        return this.sortColumns;
    }

    public void setSortOrder(int i) {
        this.sortOrder = i;
    }

    public List<DataSourceFilter> getFilters() {
        return this.filters;
    }

    public void setFilters(List<DataSourceFilter> list) {
        this.filters = list;
    }

    public DataSourceFilter getFilter() {
        return this.filter;
    }

    public void setFilter(DataSourceFilter dataSourceFilter) {
        this.filter = dataSourceFilter;
    }

    public Locale getLocale() {
        return this.locale;
    }
}
