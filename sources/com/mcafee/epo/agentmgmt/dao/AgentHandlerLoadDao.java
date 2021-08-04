package com.mcafee.epo.agentmgmt.dao;

import com.mcafee.epo.agentmgmt.model.AgentHandlerLoadData;
import com.mcafee.orion.core.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class AgentHandlerLoadDao {
    private static final Logger m_log = Logger.getLogger(AgentHandlerLoadDao.class);

    public List<AgentHandlerLoadData> getAgentHandlerLoadData(Connection connection) throws SQLException {
        ArrayList arrayList = new ArrayList();
        try {
            PreparedStatement prepareStatement = connection.prepareStatement("select agn.DNSName  as agentHandlerName, count(cpr.LastAgentHandler) as agentCount from EPOAgentHandlers agn left join EPOComputerProperties cpr on cpr.LastAgentHandler = agn.AutoID left join EPOLeafNode lnd on lnd.AutoID = cpr.ParentID and lnd.AgentGUID is not null group by agn.DNSName order by agentCount desc, DNSName asc");
            prepareStatement.executeQuery();
            ResultSet resultSet = prepareStatement.getResultSet();
            while (resultSet.next()) {
                AgentHandlerLoadData agentHandlerLoadData = new AgentHandlerLoadData();
                agentHandlerLoadData.setAgentHandlerName(resultSet.getString("agentHandlerName"));
                agentHandlerLoadData.setAgentCount(resultSet.getInt("agentCount"));
                arrayList.add(agentHandlerLoadData);
            }
            IOUtil.close(prepareStatement);
            return arrayList;
        } catch (SQLException e) {
            m_log.error(e);
            throw e;
        } catch (Throwable th) {
            IOUtil.close((AutoCloseable) null);
            throw th;
        }
    }
}
