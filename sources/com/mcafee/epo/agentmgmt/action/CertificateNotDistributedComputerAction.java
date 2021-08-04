package com.mcafee.epo.agentmgmt.action;

import com.mcafee.epo.agentmgmt.dao.AgentDao;
import com.mcafee.epo.agentmgmt.datasource.CertificateNotDistributedDS;
import com.mcafee.epo.computermgmt.ui.datasource.BasicSexpDataSourceFilter;
import com.mcafee.epo.computermgmt.ui.datasource.SexpDataSourceFilter;
import com.mcafee.orion.certmanager.db.OrionCertStateManager;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.config.OrionServerPropertiesService;
import com.mcafee.orion.core.db.base.DatabaseUtil;
import com.mcafee.orion.core.query.sexp.SexpProp;
import com.mcafee.orion.core.query.sexp.SexpString;
import com.mcafee.orion.core.query.sexp.ops.SexpEquals;
import com.mcafee.orion.core.query.sexp.ops.SexpIsNull;
import com.mcafee.orion.core.query.sexp.ops.SexpLessEquals;
import com.mcafee.orion.core.query.sexp.ops.SexpOr;
import com.mcafee.orion.core.servlet.ActionResponse;
import com.mcafee.orion.core.servlet.Response;
import com.mcafee.orion.core.util.resource.Resource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CertificateNotDistributedComputerAction implements UserAware {
    public static final String COL_NAME = "EPOLeafNode.LastUpdate";
    private static final Logger log = Logger.getLogger(CertificateNotDistributedComputerAction.class);
    private AgentDao agentDao;
    private CertificateNotDistributedDS dataSource;
    private OrionServerPropertiesService propertyService;
    private OrionUser user;

    public Response showComputerList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        CertificateNotDistributedDS dataSource2 = getDataSource();
        dataSource2.setUser(getUser());
        dataSource2.setStaticFilter(buildFilter());
        this.user.setAttribute("AgentMgmt.CertificateNotDistributedDS", dataSource2);
        httpServletRequest.setAttribute("datasourceAttr", "AgentMgmt.CertificateNotDistributedDS");
        return ActionResponse.forward("/AgentMgmt", "/SystemList.jsp");
    }

    public SexpDataSourceFilter buildFilter() throws Exception {
        SexpOr sexpOr = new SexpOr();
        SexpLessEquals sexpLessEquals = new SexpLessEquals();
        SexpIsNull sexpIsNull = new SexpIsNull(new SexpProp(COL_NAME));
        String property = getPropertyService().getProperty(this.user, "agenthandler.certificate.generated.time");
        Optional<OrionCertStateManager> agentCertificateGeneratedTime = getAgentDao().getAgentCertificateGeneratedTime(this.user);
        if (agentCertificateGeneratedTime.isPresent()) {
            String formatForDatabase = DatabaseUtil.formatForDatabase(new Date(agentCertificateGeneratedTime.get().getLastUpdated().getTime()), TimeZone.getDefault());
            sexpLessEquals.addChild(new SexpProp(COL_NAME));
            sexpLessEquals.addChild(new SexpString(formatForDatabase));
            sexpOr.addChild(sexpLessEquals);
            sexpOr.addChild(sexpIsNull);
            log.info("Fetched certificate generated time from database:" + formatForDatabase);
        } else if (!StringUtils.isNotBlank(property)) {
            return new BasicSexpDataSourceFilter("AgentGUIDFilter", "", (Resource) null, new SexpEquals(new SexpString("1"), new SexpString("1")));
        } else {
            String formatForDatabase2 = DatabaseUtil.formatForDatabase(new Date(Timestamp.valueOf(property).getTime()), TimeZone.getDefault());
            sexpLessEquals.addChild(new SexpProp(COL_NAME));
            sexpLessEquals.addChild(new SexpString(formatForDatabase2));
            sexpOr.addChild(sexpLessEquals);
            sexpOr.addChild(sexpIsNull);
            log.info("Fetched certificate generated time from properties:" + formatForDatabase2);
        }
        return new BasicSexpDataSourceFilter("AgentGUIDFilter", "", (Resource) null, sexpOr);
    }

    public CertificateNotDistributedDS getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(CertificateNotDistributedDS certificateNotDistributedDS) {
        this.dataSource = certificateNotDistributedDS;
    }

    public void setUser(OrionUser orionUser) {
        this.user = orionUser;
    }

    public OrionUser getUser() {
        return this.user;
    }

    public AgentDao getAgentDao() {
        return this.agentDao;
    }

    public void setAgentDao(AgentDao agentDao2) {
        this.agentDao = agentDao2;
    }

    public OrionServerPropertiesService getPropertyService() {
        return this.propertyService;
    }

    public void setPropertyService(OrionServerPropertiesService orionServerPropertiesService) {
        this.propertyService = orionServerPropertiesService;
    }
}
