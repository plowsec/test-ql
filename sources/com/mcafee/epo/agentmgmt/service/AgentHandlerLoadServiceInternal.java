package com.mcafee.epo.agentmgmt.service;

import com.mcafee.epo.agentmgmt.dao.AgentHandlerLoadDao;
import com.mcafee.epo.agentmgmt.model.AgentHandlerLoadData;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

public class AgentHandlerLoadServiceInternal implements AgentHandlerLoadService {
    private static final Logger m_log = Logger.getLogger(AgentHandlerLoadServiceInternal.class);
    private AgentHandlerLoadDao agentHandlerLoadDao;

    public List<AgentHandlerLoadData> getAgentHandlerLoadData(Connection connection) throws SQLException {
        return this.agentHandlerLoadDao.getAgentHandlerLoadData(connection);
    }

    public void setAgentHandlerLoadDao(AgentHandlerLoadDao agentHandlerLoadDao2) {
        this.agentHandlerLoadDao = agentHandlerLoadDao2;
    }
}
