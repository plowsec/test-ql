package com.mcafee.epo.agentmgmt.dao;

import com.mcafee.epo.core.model.EPOServerCerts;
import com.mcafee.orion.core.db.base.DatabaseMapper;

public class EPOServerCertsBackupMapper extends DatabaseMapper<EPOServerCerts> {
    public static final EPOServerCertsBackupMapper DB = new EPOServerCertsBackupMapper();
    private static final String TABLE_NAME = "EPOServerCertsBackup";

    private EPOServerCertsBackupMapper() {
        super(EPOServerCerts.class, TABLE_NAME, new DatabaseMapper.Column[]{new DatabaseMapper.IntColumn("AutoID", "autoId", 9), new DatabaseMapper.StringColumn("Certificate"), new DatabaseMapper.BooleanColumn("active"), new DatabaseMapper.BooleanColumn("isInternal")});
    }
}
