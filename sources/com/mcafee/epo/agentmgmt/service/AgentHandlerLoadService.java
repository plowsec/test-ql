package com.mcafee.epo.agentmgmt.service;

import com.mcafee.epo.agentmgmt.model.AgentHandlerLoadData;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AgentHandlerLoadService {
    List<AgentHandlerLoadData> getAgentHandlerLoadData(Connection connection) throws SQLException;
}
