package com.mcafee.epo.agentmgmt.command;

import com.mcafee.epo.core.services.EPOAgentHandlerServiceInternal;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandBase;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.util.IOUtil;
import com.mcafee.orion.core.util.resource.LocaleAware;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Locale;
import org.apache.log4j.Logger;

public class DeleteHandlerCmd extends CommandBase implements UserAware, LocaleAware, Command, Auditable {
    private static final Logger m_log = Logger.getLogger(DeleteHandlerCmd.class);
    private Locale locale;
    private EPOAgentHandlerServiceInternal m_agentHandlerService = null;
    private String m_ahComputerName = null;
    private Database m_database = null;
    private OrionUser m_user = null;

    public boolean authorize(OrionUser orionUser) throws CommandException, URISyntaxException {
        if (orionUser == null) {
            return false;
        }
        try {
            return orionUser.isAllowed("perm:ahRole.addEdit");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public Object invoke() throws Exception {
        String str;
        try {
            Connection connection = this.m_database.getConnection(getUser());
            if (this.m_agentHandlerService.getHandlerByNameWithOutPermissionCheck(this.m_ahComputerName, getUser()) == null) {
                str = "Nonexistent";
            } else if (this.m_agentHandlerService.deleteHandlerByComputerName(this.m_user, this.m_ahComputerName)) {
                str = "Deleted";
            } else {
                str = "Failed";
            }
            IOUtil.close(connection);
            return str;
        } catch (Exception e) {
            m_log.debug("Delete Agent Handler: Exception", e);
            throw new CommandException(e.getMessage(), e);
        } catch (Throwable th) {
            IOUtil.close((AutoCloseable) null);
            throw th;
        }
    }

    public int getPriority() {
        return 3;
    }

    public void setAgentHandlerComputerName(String str) {
        this.m_ahComputerName = str;
    }

    public void setAgentHandlerService(EPOAgentHandlerServiceInternal ePOAgentHandlerServiceInternal) {
        this.m_agentHandlerService = ePOAgentHandlerServiceInternal;
    }

    public void setDatabase(Database database) {
        this.m_database = database;
    }

    public String getDescription() {
        return getResource().getString("cmd.delete.handler.description", getLocale());
    }

    public String getDisplayName() {
        return getResource().getString("cmd.delete.handler.display.name", getLocale());
    }

    public void setUser(OrionUser orionUser) {
        this.m_user = orionUser;
    }

    public OrionUser getUser() {
        return this.m_user;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale2) {
        this.locale = locale2;
    }
}
