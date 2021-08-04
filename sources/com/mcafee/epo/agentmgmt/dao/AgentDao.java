package com.mcafee.epo.agentmgmt.dao;

import com.mcafee.orion.certmanager.db.OrionCertStateManager;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserLoader;
import com.mcafee.orion.core.config.OrionServerPropertiesService;
import com.mcafee.orion.core.db.base.Database;
import java.sql.SQLException;
import java.util.Optional;
import org.apache.log4j.Logger;

public class AgentDao {
    private static String certificationGeneratedTimeSQL = ("select ( " + countLeafNode + " ) as totalNodes, (" + countLeafNode + " AND LastUpdate > ? ) as updatedNodes");
    private static String countLeafNode = " select count(1) from EPOLeafNodeMT where 1=1 ";
    private static final Logger log = Logger.getLogger(AgentDao.class);
    private Database database;
    private OrionServerPropertiesService propertyService;
    private UserLoader userLoader;

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004e, code lost:
        if (r3 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0061, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0062, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00e9, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00ea, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x00fc, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        r3.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0102, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0107, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0108, code lost:
        r1 = r0;
        r2 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0061 A[ExcHandler: all (r0v3 'th' java.lang.Throwable A[CUSTOM_DECLARE])] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float getAgentCertificationDistributionProgress() throws java.sql.SQLException, com.mcafee.orion.core.config.OrionServerPropertiesServiceException {
        /*
            r12 = this;
            r3 = 0
            r2 = 0
            r1 = 1120403456(0x42c80000, float:100.0)
            com.mcafee.orion.core.auth.UserLoader r0 = r12.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r0 = r0.getDefaultTenantSystemUser()
            com.mcafee.orion.core.db.base.Database r4 = r12.database     // Catch:{ SQLException -> 0x0054 }
            java.sql.Connection r5 = r4.getConnection(r0)     // Catch:{ SQLException -> 0x0054 }
            r6 = 0
            java.lang.String r4 = certificationGeneratedTimeSQL     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            java.sql.PreparedStatement r7 = r5.prepareStatement(r4)     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            r8 = 0
            java.util.Optional r4 = r12.getAgentCertificateGeneratedTime(r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            boolean r9 = r4.isPresent()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            if (r9 != 0) goto L_0x00aa
            com.mcafee.orion.core.config.OrionServerPropertiesService r4 = r12.getPropertyService()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.lang.String r9 = "agenthandler.certificate.generated.time"
            java.lang.String r0 = r4.getProperty(r0, r9)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            boolean r4 = org.apache.commons.lang3.StringUtils.isBlank(r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            if (r4 == 0) goto L_0x006d
            if (r7 == 0) goto L_0x003b
            if (r3 == 0) goto L_0x005d
            r7.close()     // Catch:{ Throwable -> 0x0043, all -> 0x0061 }
        L_0x003b:
            if (r5 == 0) goto L_0x0042
            if (r3 == 0) goto L_0x0069
            r5.close()     // Catch:{ Throwable -> 0x0064 }
        L_0x0042:
            return r1
        L_0x0043:
            r0 = move-exception
            r8.addSuppressed(r0)     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x003b
        L_0x0048:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x004a }
        L_0x004a:
            r1 = move-exception
            r3 = r0
        L_0x004c:
            if (r5 == 0) goto L_0x0053
            if (r3 == 0) goto L_0x0102
            r5.close()     // Catch:{ Throwable -> 0x00fc }
        L_0x0053:
            throw r1     // Catch:{ SQLException -> 0x0054 }
        L_0x0054:
            r0 = move-exception
            org.apache.log4j.Logger r1 = log
            java.lang.String r2 = "Failed to get the distribution progress"
            r1.error(r2, r0)
            throw r0
        L_0x005d:
            r7.close()     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x003b
        L_0x0061:
            r0 = move-exception
            r1 = r0
            goto L_0x004c
        L_0x0064:
            r0 = move-exception
            r6.addSuppressed(r0)     // Catch:{ SQLException -> 0x0054 }
            goto L_0x0042
        L_0x0069:
            r5.close()     // Catch:{ SQLException -> 0x0054 }
            goto L_0x0042
        L_0x006d:
            java.sql.Timestamp r0 = java.sql.Timestamp.valueOf(r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
        L_0x0071:
            java.util.Date r4 = new java.util.Date     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            long r10 = r0.getTime()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            r4.<init>(r10)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.util.TimeZone r0 = java.util.TimeZone.getDefault()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.lang.String r0 = com.mcafee.orion.core.db.base.DatabaseUtil.formatForDatabase(r4, r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            r4 = 1
            r7.setString(r4, r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            org.apache.log4j.Logger r0 = log     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.lang.String r4 = "Fetching certificate distribution percentage"
            r0.info(r4)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            r7.execute()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.sql.ResultSet r9 = r7.getResultSet()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            r0 = r2
            r4 = r2
        L_0x0096:
            boolean r2 = r9.next()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            if (r2 == 0) goto L_0x00b5
            java.lang.String r0 = "totalNodes"
            int r2 = r9.getInt(r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.lang.String r0 = "updatedNodes"
            int r0 = r9.getInt(r0)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            r4 = r2
            goto L_0x0096
        L_0x00aa:
            java.lang.Object r0 = r4.get()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            com.mcafee.orion.certmanager.db.OrionCertStateManager r0 = (com.mcafee.orion.certmanager.db.OrionCertStateManager) r0     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.sql.Timestamp r0 = r0.getLastUpdated()     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            goto L_0x0071
        L_0x00b5:
            org.apache.log4j.Logger r2 = log     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            java.lang.String r9 = "Fetched certificate distribution percentage"
            r2.info(r9)     // Catch:{ Throwable -> 0x00e7, all -> 0x0107 }
            if (r4 != 0) goto L_0x00d0
            r0 = r1
        L_0x00bf:
            if (r7 == 0) goto L_0x00c6
            if (r3 == 0) goto L_0x00da
            r7.close()     // Catch:{ Throwable -> 0x00d5, all -> 0x0061 }
        L_0x00c6:
            if (r5 == 0) goto L_0x00cd
            if (r3 == 0) goto L_0x00e3
            r5.close()     // Catch:{ Throwable -> 0x00de }
        L_0x00cd:
            r1 = r0
            goto L_0x0042
        L_0x00d0:
            float r0 = (float) r0
            float r2 = (float) r4
            float r0 = r0 / r2
            float r0 = r0 * r1
            goto L_0x00bf
        L_0x00d5:
            r1 = move-exception
            r8.addSuppressed(r1)     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x00c6
        L_0x00da:
            r7.close()     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x00c6
        L_0x00de:
            r1 = move-exception
            r6.addSuppressed(r1)     // Catch:{ SQLException -> 0x0054 }
            goto L_0x00cd
        L_0x00e3:
            r5.close()     // Catch:{ SQLException -> 0x0054 }
            goto L_0x00cd
        L_0x00e7:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x00e9 }
        L_0x00e9:
            r1 = move-exception
            r2 = r0
        L_0x00eb:
            if (r7 == 0) goto L_0x00f2
            if (r2 == 0) goto L_0x00f8
            r7.close()     // Catch:{ Throwable -> 0x00f3, all -> 0x0061 }
        L_0x00f2:
            throw r1     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
        L_0x00f3:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x00f2
        L_0x00f8:
            r7.close()     // Catch:{ Throwable -> 0x0048, all -> 0x0061 }
            goto L_0x00f2
        L_0x00fc:
            r0 = move-exception
            r3.addSuppressed(r0)     // Catch:{ SQLException -> 0x0054 }
            goto L_0x0053
        L_0x0102:
            r5.close()     // Catch:{ SQLException -> 0x0054 }
            goto L_0x0053
        L_0x0107:
            r0 = move-exception
            r1 = r0
            r2 = r3
            goto L_0x00eb
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.dao.AgentDao.getAgentCertificationDistributionProgress():float");
    }

    public Optional<OrionCertStateManager> getAgentCertificateGeneratedTime(OrionUser orionUser) throws SQLException {
        return OrionCertStateManager.DB.getWhere(this.database.getConnection(orionUser), "PRODUCTID = 'agentHandler.keystore' AND PRODUCTSTATE = 'MIGRATION_COMPLETED'").stream().findAny();
    }

    public void setDatabase(Database database2) {
        this.database = database2;
    }

    public OrionServerPropertiesService getPropertyService() {
        return this.propertyService;
    }

    public void setPropertyService(OrionServerPropertiesService orionServerPropertiesService) {
        this.propertyService = orionServerPropertiesService;
    }

    public UserLoader getUserLoader() {
        return this.userLoader;
    }

    public void setUserLoader(UserLoader userLoader2) {
        this.userLoader = userLoader2;
    }
}
