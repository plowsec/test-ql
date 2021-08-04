package com.mcafee.epo.agentmgmt.datasource;

import com.mcafee.epo.computermgmt.ui.datasource.QueryTableSearchableDS;
import com.mcafee.orion.core.query.sexp.SexpException;

public class CertificateNotDistributedDS extends QueryTableSearchableDS {
    private static final String TABLE_NAME = "EPOLeafNode";

    public CertificateNotDistributedDS() throws SexpException {
        super(TABLE_NAME);
    }
}
