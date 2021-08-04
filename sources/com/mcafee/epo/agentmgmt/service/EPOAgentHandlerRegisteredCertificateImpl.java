package com.mcafee.epo.agentmgmt.service;

import com.mcafee.epo.agentmgmt.dao.AgentDao;
import com.mcafee.epo.computermgmt.services.AgentUtilsService;
import com.mcafee.epo.core.dao.EPOAgentHandlerCertDao;
import com.mcafee.epo.core.model.EPOServerCerts;
import com.mcafee.epo.core.services.EPOApacheNotifyService;
import com.mcafee.epo.core.util.ServerConfiguration;
import com.mcafee.epo.repositorymgmt.engine.services.PackagingService;
import com.mcafee.orion.certmanager.RegisteredCertificate;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserLoader;
import com.mcafee.orion.core.cert.CaCertUtil;
import com.mcafee.orion.core.cert.CertEncoder;
import com.mcafee.orion.core.cert.CertUtil;
import com.mcafee.orion.core.cert.OrionCert;
import com.mcafee.orion.core.cmd.CommandInvoker;
import com.mcafee.orion.core.config.DefaultLocaleService;
import com.mcafee.orion.core.config.OrionServerPropertiesService;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.util.IOUtil;
import com.mcafee.orion.core.util.ValidateUtil;
import com.mcafee.orion.core.util.resource.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;

public class EPOAgentHandlerRegisteredCertificateImpl implements RegisteredCertificate {
    public static final String AH_CA_KEYSTORE = "agentHandler.keystore";
    private static final String DN_SPEC = "CN=%1$s_CA_%2$s, OU=%3$s, O=McAfee";
    private static final Logger m_log = Logger.getLogger(EPOAgentHandlerRegisteredCertificateImpl.class);
    private AgentDao agentDao;
    private AgentUtilsService agentUtilsService = null;
    private CommandInvoker commandInvoker;
    private Database database;
    private DefaultLocaleService defaultLocaleService;
    private EPOApacheNotifyService epoApacheNotifyService;
    private String extensionName;
    private String hostName;
    private boolean isCa;
    private String keystore;
    private Object m_siteListAndRepoSynch = new Object();
    private boolean overwrite;
    private PackagingService packagingService;
    private OrionServerPropertiesService propertyService;
    private Resource resource;
    private ServerConfiguration serverConfiguration;
    private UserLoader userLoader;

    public enum STATUS {
        BEGIN_MIGRATION,
        ACTIVATE_CERTIFICATES,
        FINISH_MIGRATION,
        CANCEL_MIGRATION
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-custom' in method: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.deleteKeystoreFiles(java.lang.String[]):void, dex: AgentMgmt.jar
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:151)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:286)
        	at jadx.core.ProcessClass.process(ProcessClass.java:36)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:58)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-custom'
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:588)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:78)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:136)
        	... 4 more
        */
    protected void deleteKeystoreFiles(java.lang.String[] r1) {
        /*
        // Can't load method instructions: Load method exception: Unknown instruction: 'invoke-custom' in method: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.deleteKeystoreFiles(java.lang.String[]):void, dex: AgentMgmt.jar
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.deleteKeystoreFiles(java.lang.String[]):void");
    }

    public OrionServerPropertiesService getPropertyService() {
        return this.propertyService;
    }

    public void setPropertyService(OrionServerPropertiesService orionServerPropertiesService) {
        this.propertyService = orionServerPropertiesService;
    }

    public DefaultLocaleService getDefaultLocaleService() {
        return this.defaultLocaleService;
    }

    public void setDefaultLocaleService(DefaultLocaleService defaultLocaleService2) {
        this.defaultLocaleService = defaultLocaleService2;
    }

    public AgentUtilsService getAgentUtilsService() {
        return this.agentUtilsService;
    }

    public void setAgentUtilsService(AgentUtilsService agentUtilsService2) {
        this.agentUtilsService = agentUtilsService2;
    }

    public void setAgentDao(AgentDao agentDao2) {
        this.agentDao = agentDao2;
    }

    public AgentDao getAgentDao() {
        return this.agentDao;
    }

    public EPOApacheNotifyService getEpoApacheNotifyService() {
        return this.epoApacheNotifyService;
    }

    public void setEpoApacheNotifyService(EPOApacheNotifyService ePOApacheNotifyService) {
        this.epoApacheNotifyService = ePOApacheNotifyService;
    }

    public Database getDatabase() {
        return this.database;
    }

    public void setDatabase(Database database2) {
        this.database = database2;
    }

    public ServerConfiguration getServerConfiguration() {
        return this.serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration2) {
        this.serverConfiguration = serverConfiguration2;
    }

    public UserLoader getUserLoader() {
        return this.userLoader;
    }

    public void setUserLoader(UserLoader userLoader2) {
        this.userLoader = userLoader2;
    }

    public PackagingService getPackagingService() {
        return this.packagingService;
    }

    public void setPackagingService(PackagingService packagingService2) {
        this.packagingService = packagingService2;
    }

    public CommandInvoker getCommandInvoker() {
        return this.commandInvoker;
    }

    public void setCommandInvoker(CommandInvoker commandInvoker2) {
        this.commandInvoker = commandInvoker2;
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource2) {
        this.resource = resource2;
    }

    public String getExtensionName() {
        return this.extensionName;
    }

    public void setExtensionName(String str) {
        this.extensionName = str;
    }

    public String getKeystore() {
        return this.keystore;
    }

    public void setKeystore(String str) {
        this.keystore = str;
    }

    public boolean isCa() {
        return this.isCa;
    }

    public void setCa(boolean z) {
        this.isCa = z;
    }

    public boolean isOverwrite() {
        return this.overwrite;
    }

    public void setOverwrite(boolean z) {
        this.overwrite = z;
    }

    public String getProductName(Locale locale) {
        return this.resource.getString("AgentMgmt.product.name", locale);
    }

    public String getDescription(Locale locale) {
        return this.resource.getString("AgentMgmt.product.desc", locale);
    }

    public String getErrorMsg(Locale locale) {
        return null;
    }

    public String getId() {
        return AH_CA_KEYSTORE;
    }

    public X509Certificate[] getCertificates() {
        X509Certificate[] x509CertificateArr = new X509Certificate[1];
        try {
            x509CertificateArr[0] = loadCertificate(createKeystoreFile().getCanonicalPath());
        } catch (Exception e) {
            m_log.info("Exception occurred while loading agentHandler certs", e);
        }
        return x509CertificateArr;
    }

    public void beginMigration() throws Exception {
        OrionUser defaultTenantSystemUser = getUserLoader().getDefaultTenantSystemUser();
        Connection connection = getDatabase().getConnection(defaultTenantSystemUser);
        try {
            m_log.info("Regenerating agentHandler.keystore");
            createCert();
            m_log.info("Regeneration of agentHandler.keystore is finished");
            try {
                saveRootAHCertificateToDb(defaultTenantSystemUser);
                try {
                    disableNewCerts(connection);
                    connection.commit();
                    try {
                        regenerateSiteListAndBuildAgent(STATUS.BEGIN_MIGRATION);
                        try {
                            saveCertificateGeneratedTime(connection, defaultTenantSystemUser);
                            connection.commit();
                        } catch (Exception e) {
                            regenerateSiteListAndBuildAgent(STATUS.BEGIN_MIGRATION);
                            m_log.error("Exception occurred while saving certificate generated time in DB", e);
                            throw e;
                        }
                    } catch (Exception e2) {
                        deleteNewCerts(connection);
                        connection.commit();
                        m_log.error("Exception occurred while regenerating sitelist", e2);
                        throw e2;
                    }
                } catch (Exception e3) {
                    deleteNewCerts(connection);
                    connection.commit();
                    m_log.error("Exception occurred while disabling new certificates", e3);
                    throw e3;
                }
            } catch (Exception e4) {
                m_log.error("Exception occurred while saving new certificates in database", e4);
                throw e4;
            }
        } catch (Exception e5) {
            m_log.error("Exception occurred while generating certificates in keystoreTemp folder", e5);
            throw e5;
        }
    }

    public String getProgress(Locale locale) {
        int i;
        try {
            i = (int) getAgentDao().getAgentCertificationDistributionProgress();
        } catch (Exception e) {
            m_log.error("Exception occurred in getting certificate distribution process", e);
            i = 0;
        }
        if (locale == null) {
            locale = getDefaultLocaleService().resolveLocale(getUserLoader().getDefaultTenantSystemUser());
        }
        StringBuilder sb = new StringBuilder("");
        String str = "<a href=/AgentMgmt/showComputerList.do>" + this.resource.getHtmlString("AgentMgmt.click.here", locale) + "</a>";
        if (i == 100) {
            sb.append(this.resource.formatString("AgentMgmt.progress.text.complete", locale, new Object[]{Integer.valueOf(i)}));
        } else {
            sb.append(this.resource.formatString("AgentMgmt.progress.text", locale, new Object[]{Integer.valueOf(i), str, Integer.valueOf(100 - i)}));
        }
        return sb.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0042, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0055, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void activateCertificate() throws java.lang.Exception {
        /*
            r4 = this;
            r4.regenerateSslCerfificateNotfy()
            r0 = 30000(0x7530, double:1.4822E-319)
            java.lang.Thread.sleep(r0)
            com.mcafee.orion.core.db.base.Database r0 = r4.database     // Catch:{ Exception -> 0x0032 }
            com.mcafee.orion.core.auth.UserLoader r1 = r4.getUserLoader()     // Catch:{ Exception -> 0x0032 }
            com.mcafee.orion.core.auth.OrionUser r1 = r1.getDefaultTenantSystemUser()     // Catch:{ Exception -> 0x0032 }
            java.sql.Connection r3 = r0.getConnection(r1)     // Catch:{ Exception -> 0x0032 }
            r2 = 0
            r4.enableNewCerts(r3)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r4.disableOriginalCerts(r3)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r3.commit()     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl$STATUS r0 = com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.STATUS.ACTIVATE_CERTIFICATES     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r4.regenerateSiteListAndBuildAgent(r0)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            if (r3 == 0) goto L_0x002c
            if (r2 == 0) goto L_0x003b
            r3.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            return
        L_0x002d:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x0032 }
            goto L_0x002c
        L_0x0032:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Exception occurred enabling new certificates "
            r1.error(r2, r0)
            throw r0
        L_0x003b:
            r3.close()     // Catch:{ Exception -> 0x0032 }
            goto L_0x002c
        L_0x003f:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0041 }
        L_0x0041:
            r1 = move-exception
            r2 = r0
        L_0x0043:
            if (r3 == 0) goto L_0x004a
            if (r2 == 0) goto L_0x0050
            r3.close()     // Catch:{ Throwable -> 0x004b }
        L_0x004a:
            throw r1     // Catch:{ Exception -> 0x0032 }
        L_0x004b:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x0032 }
            goto L_0x004a
        L_0x0050:
            r3.close()     // Catch:{ Exception -> 0x0032 }
            goto L_0x004a
        L_0x0054:
            r0 = move-exception
            r1 = r0
            goto L_0x0043
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.activateCertificate():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0042, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0055, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void finishMigration() throws java.lang.Exception {
        /*
            r4 = this;
            com.mcafee.orion.core.auth.UserLoader r0 = r4.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r0 = r0.getDefaultTenantSystemUser()
            com.mcafee.orion.core.db.base.Database r1 = r4.database     // Catch:{ Exception -> 0x0032 }
            java.sql.Connection r3 = r1.getConnection(r0)     // Catch:{ Exception -> 0x0032 }
            r2 = 0
            r4.deleteOriginalCerts(r3)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper r1 = com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper.DB     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r1.deleteAll(r3)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r3.commit()     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl$STATUS r1 = com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.STATUS.FINISH_MIGRATION     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r4.regenerateSiteListAndBuildAgent(r1)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r4.removeCertificateGeneratedTime(r0)     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            r4.deleteAgentHandlerCertificates()     // Catch:{ Throwable -> 0x003f, all -> 0x0054 }
            if (r3 == 0) goto L_0x002c
            if (r2 == 0) goto L_0x003b
            r3.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            return
        L_0x002d:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x0032 }
            goto L_0x002c
        L_0x0032:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Exception occurred while finishing certificate migration"
            r1.error(r2, r0)
            throw r0
        L_0x003b:
            r3.close()     // Catch:{ Exception -> 0x0032 }
            goto L_0x002c
        L_0x003f:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0041 }
        L_0x0041:
            r1 = move-exception
            r2 = r0
        L_0x0043:
            if (r3 == 0) goto L_0x004a
            if (r2 == 0) goto L_0x0050
            r3.close()     // Catch:{ Throwable -> 0x004b }
        L_0x004a:
            throw r1     // Catch:{ Exception -> 0x0032 }
        L_0x004b:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x0032 }
            goto L_0x004a
        L_0x0050:
            r3.close()     // Catch:{ Exception -> 0x0032 }
            goto L_0x004a
        L_0x0054:
            r0 = move-exception
            r1 = r0
            goto L_0x0043
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.finishMigration():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0060, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelMigration() throws java.lang.Exception {
        /*
            r4 = this;
            com.mcafee.orion.core.auth.UserLoader r0 = r4.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r0 = r0.getDefaultTenantSystemUser()
            r4.regenerateSslCerfificateNotfy()
            r2 = 30000(0x7530, double:1.4822E-319)
            java.lang.Thread.sleep(r2)
            com.mcafee.orion.core.db.base.Database r1 = r4.database     // Catch:{ Exception -> 0x003d }
            java.sql.Connection r3 = r1.getConnection(r0)     // Catch:{ Exception -> 0x003d }
            r2 = 0
            r4.deleteNewCerts(r3)     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r4.enableOriginalCerts(r3)     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper r1 = com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper.DB     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r1.deleteAll(r3)     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r3.commit()     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl$STATUS r1 = com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.STATUS.CANCEL_MIGRATION     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r4.regenerateSiteListAndBuildAgent(r1)     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r4.removeCertificateGeneratedTime(r0)     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            r4.deleteAgentHandlerCertificates()     // Catch:{ Throwable -> 0x004a, all -> 0x005f }
            if (r3 == 0) goto L_0x0037
            if (r2 == 0) goto L_0x0046
            r3.close()     // Catch:{ Throwable -> 0x0038 }
        L_0x0037:
            return
        L_0x0038:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x003d }
            goto L_0x0037
        L_0x003d:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Exception occurred while cancelling certificate migration"
            r1.error(r2, r0)
            throw r0
        L_0x0046:
            r3.close()     // Catch:{ Exception -> 0x003d }
            goto L_0x0037
        L_0x004a:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x004c }
        L_0x004c:
            r1 = move-exception
            r2 = r0
        L_0x004e:
            if (r3 == 0) goto L_0x0055
            if (r2 == 0) goto L_0x005b
            r3.close()     // Catch:{ Throwable -> 0x0056 }
        L_0x0055:
            throw r1     // Catch:{ Exception -> 0x003d }
        L_0x0056:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x003d }
            goto L_0x0055
        L_0x005b:
            r3.close()     // Catch:{ Exception -> 0x003d }
            goto L_0x0055
        L_0x005f:
            r0 = move-exception
            r1 = r0
            goto L_0x004e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.cancelMigration():void");
    }

    private void regenerateSiteListAndBuildAgent(Enum enumR) throws Exception {
        synchronized (this.m_siteListAndRepoSynch) {
            m_log.info(enumR.toString() + "Rebuild agent package and sitelist after updating new certs");
            this.packagingService.buildAgentPackageIfNecessary();
            siteListChangedNotify();
            m_log.info(enumR + "Finished rebuilding agent package and sitelist after updating new certs");
        }
        updateAgentDeploymentUrls();
    }

    private void createCert() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, SignatureException, InvalidKeyException {
        ValidateUtil.throwIfBlank(this.keystore, "keystore name");
        ValidateUtil.throwIfBlank(this.extensionName, "extension name");
        if (this.hostName == null) {
            this.hostName = OrionCert.getServerHostname();
        }
        String formatDN = formatDN(this.extensionName, this.hostName);
        if (!this.isCa) {
            formatDN = formatDN.replace("_CA_", "_");
        }
        regenerateCASignedCert(formatDN);
    }

    private static String formatDN(String str, String str2) {
        return String.format(DN_SPEC, (Object[]) new String[]{str, str2, str});
    }

    public String getKeystorePassword() {
        return getStorePassword();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008d, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008e, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a1, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveRootAHCertificateToDb(com.mcafee.orion.core.auth.OrionUser r8) throws java.sql.SQLException, java.security.cert.CertificateException, java.io.IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException {
        /*
            r7 = this;
            r2 = 1
            java.security.KeyStore r0 = r7.loadKeyStore()
            if (r0 != 0) goto L_0x000f
            java.security.KeyStoreException r0 = new java.security.KeyStoreException
            java.lang.String r1 = "Failed to load Agent Handler keystore."
            r0.<init>(r1)
            throw r0
        L_0x000f:
            java.security.cert.Certificate[] r1 = r7.getKeyStoreCertificateChain(r0)
            r0 = 0
            r0 = r1[r0]
            java.security.cert.X509Certificate r0 = (java.security.cert.X509Certificate) r0
            r1 = r1[r2]
            java.security.cert.X509Certificate r1 = (java.security.cert.X509Certificate) r1
            java.lang.String r2 = r7.makePEM(r1)
            java.lang.String r3 = com.mcafee.epo.core.util.EPOCertEncoder.strippedCert(r2)
            java.lang.String r0 = r7.makePEM(r0)
            java.lang.String r4 = com.mcafee.epo.core.util.EPOCertEncoder.strippedCert(r0)
            java.security.cert.X509Certificate r0 = r7.certFromPEM(r0)
            if (r0 != 0) goto L_0x006b
            org.apache.log4j.Logger r0 = m_log
            java.lang.String r1 = "Error, failed to regenerate agent handler signing certificate"
            r0.error(r1)
        L_0x0039:
            com.mcafee.orion.core.db.base.Database r0 = r7.getDatabase()     // Catch:{ Exception -> 0x007e }
            java.sql.Connection r5 = r0.getConnection(r8)     // Catch:{ Exception -> 0x007e }
            r2 = 0
            com.mcafee.epo.core.dao.EPOAgentHandlerCertDao r0 = new com.mcafee.epo.core.dao.EPOAgentHandlerCertDao     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r0.<init>(r5)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r1 = 1
            java.util.List r1 = r0.getInternalCerts(r1)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper r6 = com.mcafee.epo.agentmgmt.dao.EPOServerCertsBackupMapper.DB     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r6.addBatch(r5, r1)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r5.commit()     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            boolean r1 = r7.haveCACertsChanged((com.mcafee.epo.core.dao.EPOAgentHandlerCertDao) r0, (java.lang.String) r3, (java.lang.String) r4)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            if (r1 == 0) goto L_0x0063
            r0.saveCert(r3)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r0.saveCert(r4)     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
            r0.commit()     // Catch:{ Throwable -> 0x008b, all -> 0x00a0 }
        L_0x0063:
            if (r5 == 0) goto L_0x006a
            if (r2 == 0) goto L_0x0087
            r5.close()     // Catch:{ Throwable -> 0x0079 }
        L_0x006a:
            return
        L_0x006b:
            boolean r0 = r7.verifyCert(r0, r1)
            if (r0 != 0) goto L_0x0039
            org.apache.log4j.Logger r0 = m_log
            java.lang.String r1 = "Error, failed to verify regenerated agent handler signing certificate"
            r0.error(r1)
            goto L_0x0039
        L_0x0079:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x007e }
            goto L_0x006a
        L_0x007e:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Exception occurred while saving root certificates into DB "
            r1.error(r2, r0)
            goto L_0x006a
        L_0x0087:
            r5.close()     // Catch:{ Exception -> 0x007e }
            goto L_0x006a
        L_0x008b:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x008d }
        L_0x008d:
            r1 = move-exception
            r2 = r0
        L_0x008f:
            if (r5 == 0) goto L_0x0096
            if (r2 == 0) goto L_0x009c
            r5.close()     // Catch:{ Throwable -> 0x0097 }
        L_0x0096:
            throw r1     // Catch:{ Exception -> 0x007e }
        L_0x0097:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ Exception -> 0x007e }
            goto L_0x0096
        L_0x009c:
            r5.close()     // Catch:{ Exception -> 0x007e }
            goto L_0x0096
        L_0x00a0:
            r0 = move-exception
            r1 = r0
            goto L_0x008f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.saveRootAHCertificateToDb(com.mcafee.orion.core.auth.OrionUser):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void disableNewCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "UPDATE EPOServerCerts SET Active = 0 WHERE Certificate NOT IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.executeUpdate()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.disableNewCerts(java.sql.Connection):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void disableOriginalCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "UPDATE EPOServerCerts SET Active = 0 WHERE Certificate IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.executeUpdate()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.disableOriginalCerts(java.sql.Connection):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enableNewCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "UPDATE EPOServerCerts SET Active = 1 WHERE Certificate NOT IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.executeUpdate()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.enableNewCerts(java.sql.Connection):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enableOriginalCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "UPDATE EPOServerCerts SET Active = 1 WHERE Certificate IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.executeUpdate()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.enableOriginalCerts(java.sql.Connection):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deleteNewCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "DELETE FROM EPOServerCerts WHERE Certificate NOT IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.execute()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.deleteNewCerts(java.sql.Connection):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0025, code lost:
        if (r2 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        if (r1 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0032, code lost:
        r2.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deleteOriginalCerts(java.sql.Connection r4) throws java.sql.SQLException {
        /*
            r3 = this;
            java.lang.String r0 = "DELETE FROM EPOServerCerts WHERE Certificate IN (SELECT Certificate FROM EPOServerCertsBackup)"
            java.sql.PreparedStatement r2 = r4.prepareStatement(r0)     // Catch:{ SQLException -> 0x0017 }
            r1 = 0
            r2.execute()     // Catch:{ Throwable -> 0x0022 }
            if (r2 == 0) goto L_0x0011
            if (r1 == 0) goto L_0x001e
            r2.close()     // Catch:{ Throwable -> 0x0012 }
        L_0x0011:
            return
        L_0x0012:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0017:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            r1.error(r0)
            throw r0
        L_0x001e:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x0011
        L_0x0022:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            if (r2 == 0) goto L_0x002c
            if (r1 == 0) goto L_0x0032
            r2.close()     // Catch:{ Throwable -> 0x002d }
        L_0x002c:
            throw r0     // Catch:{ SQLException -> 0x0017 }
        L_0x002d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        L_0x0032:
            r2.close()     // Catch:{ SQLException -> 0x0017 }
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.deleteOriginalCerts(java.sql.Connection):void");
    }

    private boolean haveCACertsChanged(EPOAgentHandlerCertDao ePOAgentHandlerCertDao, String str, String str2) throws SQLException {
        return haveCACertsChanged((List<EPOServerCerts>) ePOAgentHandlerCertDao.getAllCerts(true), str, str2);
    }

    private boolean haveCACertsChanged(List<EPOServerCerts> list, String str, String str2) {
        boolean z;
        boolean z2 = false;
        boolean z3 = false;
        for (EPOServerCerts next : list) {
            if (str.equals(next.getCertificate())) {
                z3 = true;
            }
            if (str2.equals(next.getCertificate())) {
                z = true;
            } else {
                z = z2;
            }
            z2 = z;
        }
        if (!z3 || !z2) {
            return true;
        }
        return false;
    }

    private KeyStore loadKeyStore() {
        FileInputStream fileInputStream;
        KeyStore keyStore = null;
        try {
            fileInputStream = createTempInputStream();
            try {
                keyStore = loadKeyStore(fileInputStream);
                IOUtil.close(fileInputStream);
            } catch (Exception e) {
                e = e;
                try {
                    m_log.error("Failed to load the Agent Handler CA Keystore.  Error=", e);
                    IOUtil.close(fileInputStream);
                    return keyStore;
                } catch (Throwable th) {
                    th = th;
                    IOUtil.close(fileInputStream);
                    throw th;
                }
            }
        } catch (Exception e2) {
            e = e2;
            fileInputStream = null;
        } catch (Throwable th2) {
            th = th2;
            fileInputStream = null;
            IOUtil.close(fileInputStream);
            throw th;
        }
        return keyStore;
    }

    private boolean verifyCert(X509Certificate x509Certificate, X509Certificate x509Certificate2) {
        try {
            x509Certificate.verify(x509Certificate2.getPublicKey());
            return true;
        } catch (SignatureException e) {
            m_log.error("Signature does not verify", e);
            return false;
        } catch (Exception e2) {
            m_log.error("Error verifying certificate signature", e2);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0049, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004a, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void siteListChangedNotify() {
        /*
            r4 = this;
            com.mcafee.orion.core.auth.UserLoader r0 = r4.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r0 = r0.getDefaultTenantSystemUser()
            com.mcafee.orion.core.db.base.Database r1 = r4.getDatabase()     // Catch:{ SQLException -> 0x0027 }
            java.sql.Connection r3 = r1.getConnection(r0)     // Catch:{ SQLException -> 0x0027 }
            r2 = 0
            com.mcafee.epo.core.services.EPOApacheNotifyService r0 = r4.getEpoApacheNotifyService()     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            com.mcafee.epo.core.services.EPOApacheNotifyService$enumApacheNotifyMessages r1 = com.mcafee.epo.core.services.EPOApacheNotifyService.enumApacheNotifyMessages.SitelistChanged     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            r0.ApacheNotify(r1, r3)     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            if (r3 == 0) goto L_0x0021
            if (r2 == 0) goto L_0x0030
            r3.close()     // Catch:{ Throwable -> 0x0022 }
        L_0x0021:
            return
        L_0x0022:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ SQLException -> 0x0027 }
            goto L_0x0021
        L_0x0027:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Error obtaining database connection"
            r1.error(r2, r0)
            goto L_0x0021
        L_0x0030:
            r3.close()     // Catch:{ SQLException -> 0x0027 }
            goto L_0x0021
        L_0x0034:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0036 }
        L_0x0036:
            r1 = move-exception
            r2 = r0
        L_0x0038:
            if (r3 == 0) goto L_0x003f
            if (r2 == 0) goto L_0x0045
            r3.close()     // Catch:{ Throwable -> 0x0040 }
        L_0x003f:
            throw r1     // Catch:{ SQLException -> 0x0027 }
        L_0x0040:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ SQLException -> 0x0027 }
            goto L_0x003f
        L_0x0045:
            r3.close()     // Catch:{ SQLException -> 0x0027 }
            goto L_0x003f
        L_0x0049:
            r0 = move-exception
            r1 = r0
            goto L_0x0038
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.siteListChangedNotify():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0049, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004a, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void regenerateSslCerfificateNotfy() {
        /*
            r4 = this;
            com.mcafee.orion.core.auth.UserLoader r0 = r4.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r0 = r0.getDefaultTenantSystemUser()
            com.mcafee.orion.core.db.base.Database r1 = r4.getDatabase()     // Catch:{ SQLException -> 0x0027 }
            java.sql.Connection r3 = r1.getConnection(r0)     // Catch:{ SQLException -> 0x0027 }
            r2 = 0
            com.mcafee.epo.core.services.EPOApacheNotifyService r0 = r4.getEpoApacheNotifyService()     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            com.mcafee.epo.core.services.EPOApacheNotifyService$enumApacheNotifyMessages r1 = com.mcafee.epo.core.services.EPOApacheNotifyService.enumApacheNotifyMessages.RegenerateSslCertificate     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            r0.ApacheNotify(r1, r3)     // Catch:{ Throwable -> 0x0034, all -> 0x0049 }
            if (r3 == 0) goto L_0x0021
            if (r2 == 0) goto L_0x0030
            r3.close()     // Catch:{ Throwable -> 0x0022 }
        L_0x0021:
            return
        L_0x0022:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ SQLException -> 0x0027 }
            goto L_0x0021
        L_0x0027:
            r0 = move-exception
            org.apache.log4j.Logger r1 = m_log
            java.lang.String r2 = "Error obtaining database connection"
            r1.error(r2, r0)
            goto L_0x0021
        L_0x0030:
            r3.close()     // Catch:{ SQLException -> 0x0027 }
            goto L_0x0021
        L_0x0034:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0036 }
        L_0x0036:
            r1 = move-exception
            r2 = r0
        L_0x0038:
            if (r3 == 0) goto L_0x003f
            if (r2 == 0) goto L_0x0045
            r3.close()     // Catch:{ Throwable -> 0x0040 }
        L_0x003f:
            throw r1     // Catch:{ SQLException -> 0x0027 }
        L_0x0040:
            r0 = move-exception
            r2.addSuppressed(r0)     // Catch:{ SQLException -> 0x0027 }
            goto L_0x003f
        L_0x0045:
            r3.close()     // Catch:{ SQLException -> 0x0027 }
            goto L_0x003f
        L_0x0049:
            r0 = move-exception
            r1 = r0
            goto L_0x0038
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.regenerateSslCerfificateNotfy():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0052, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0076, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0077, code lost:
        r1 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateAgentDeploymentUrls() throws java.lang.Exception {
        /*
            r7 = this;
            com.mcafee.orion.core.auth.UserLoader r0 = r7.getUserLoader()
            com.mcafee.orion.core.auth.OrionUser r1 = r0.getDefaultTenantSystemUser()
            com.mcafee.orion.core.db.base.Database r0 = r7.database
            java.sql.Connection r3 = r0.getConnection(r1)
            r2 = 0
            com.mcafee.orion.core.db.base.DatabaseMapper r0 = com.mcafee.epo.computermgmt.dao.EPOAgentDeploymentInfoDao.myMapper     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            java.util.List r0 = r0.getAll(r3)     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            java.util.Iterator r4 = r0.iterator()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
        L_0x0019:
            boolean r0 = r4.hasNext()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            if (r0 == 0) goto L_0x005c
            java.lang.Object r0 = r4.next()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            com.mcafee.epo.computermgmt.model.EPOAgentDeploymentInfo r0 = (com.mcafee.epo.computermgmt.model.EPOAgentDeploymentInfo) r0     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            com.mcafee.epo.computermgmt.services.AgentUtilsService r5 = r7.getAgentUtilsService()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            int r6 = r0.getPrimaryAgentHandlerId()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            boolean r5 = r5.isValidAgentHandler(r1, r6)     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            if (r5 != 0) goto L_0x0041
            com.mcafee.epo.computermgmt.services.AgentUtilsService r5 = r7.getAgentUtilsService()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            int r6 = r0.getFallbackAgentHandlerId()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            boolean r5 = r5.isValidAgentHandler(r1, r6)     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            if (r5 == 0) goto L_0x0019
        L_0x0041:
            boolean r5 = r0.isAgentValid()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            if (r5 == 0) goto L_0x0019
            com.mcafee.epo.computermgmt.services.AgentUtilsService r5 = r7.getAgentUtilsService()     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            r6 = 1
            r5.updateConfigAndBootstrap(r3, r0, r6)     // Catch:{ Throwable -> 0x0050, all -> 0x0076 }
            goto L_0x0019
        L_0x0050:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0052 }
        L_0x0052:
            r1 = move-exception
            r2 = r0
        L_0x0054:
            if (r3 == 0) goto L_0x005b
            if (r2 == 0) goto L_0x0072
            r3.close()     // Catch:{ Throwable -> 0x006d }
        L_0x005b:
            throw r1
        L_0x005c:
            if (r3 == 0) goto L_0x0063
            if (r2 == 0) goto L_0x0069
            r3.close()     // Catch:{ Throwable -> 0x0064 }
        L_0x0063:
            return
        L_0x0064:
            r0 = move-exception
            r2.addSuppressed(r0)
            goto L_0x0063
        L_0x0069:
            r3.close()
            goto L_0x0063
        L_0x006d:
            r0 = move-exception
            r2.addSuppressed(r0)
            goto L_0x005b
        L_0x0072:
            r3.close()
            goto L_0x005b
        L_0x0076:
            r0 = move-exception
            r1 = r0
            goto L_0x0054
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.updateAgentDeploymentUrls():void");
    }

    private void saveCertificateGeneratedTime(Connection connection, OrionUser orionUser) throws Exception {
        getPropertyService().setProperty(orionUser, "agenthandler.certificate.generated.time", getCurrentTimeStamp(connection));
    }

    private void removeCertificateGeneratedTime(OrionUser orionUser) throws Exception {
        getPropertyService().removeProperty(orionUser, "agenthandler.certificate.generated.time");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: java.lang.Throwable} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.Throwable} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.lang.Throwable} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: java.lang.String} */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0045, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0057, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0058, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getCurrentTimeStamp(java.sql.Connection r6) throws java.sql.SQLException {
        /*
            r5 = this;
            r2 = 0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "SELECT "
            java.lang.StringBuilder r0 = r0.append(r1)
            com.mcafee.orion.core.db.base.Dialect r1 = com.mcafee.orion.core.db.base.DatabaseUtil.getDialect(r6)
            java.lang.String r1 = r1.getCurrentUTCTimeFunction()
            java.lang.StringBuilder r0 = r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.sql.PreparedStatement r3 = r6.prepareStatement(r0)
            java.sql.ResultSet r1 = r3.executeQuery()     // Catch:{ Throwable -> 0x0042, all -> 0x0057 }
            r0 = r2
        L_0x0025:
            boolean r4 = r1.next()     // Catch:{ Throwable -> 0x0042, all -> 0x0057 }
            if (r4 == 0) goto L_0x0031
            r0 = 1
            java.lang.String r0 = r1.getString(r0)     // Catch:{ Throwable -> 0x0042, all -> 0x0057 }
            goto L_0x0025
        L_0x0031:
            if (r3 == 0) goto L_0x0038
            if (r2 == 0) goto L_0x003e
            r3.close()     // Catch:{ Throwable -> 0x0039 }
        L_0x0038:
            return r0
        L_0x0039:
            r1 = move-exception
            r2.addSuppressed(r1)
            goto L_0x0038
        L_0x003e:
            r3.close()
            goto L_0x0038
        L_0x0042:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x0044 }
        L_0x0044:
            r1 = move-exception
            r2 = r0
        L_0x0046:
            if (r3 == 0) goto L_0x004d
            if (r2 == 0) goto L_0x0053
            r3.close()     // Catch:{ Throwable -> 0x004e }
        L_0x004d:
            throw r1
        L_0x004e:
            r0 = move-exception
            r2.addSuppressed(r0)
            goto L_0x004d
        L_0x0053:
            r3.close()
            goto L_0x004d
        L_0x0057:
            r0 = move-exception
            r1 = r0
            goto L_0x0046
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mcafee.epo.agentmgmt.service.EPOAgentHandlerRegisteredCertificateImpl.getCurrentTimeStamp(java.sql.Connection):java.lang.String");
    }

    private void deleteAgentHandlerCertificates() {
        m_log.info("Deleting agent handler certificates");
        deleteKeystoreFiles(new String[]{"crt"});
        m_log.info("Completed deleting agent handler certificates");
    }

    /* access modifiers changed from: protected */
    public File createKeystoreFile() {
        return new File(CertUtil.getKeystoreFolder(), AH_CA_KEYSTORE);
    }

    /* access modifiers changed from: protected */
    public FileInputStream createTempInputStream() throws FileNotFoundException {
        return new FileInputStream(new File(CaCertUtil.getTempFolder(), AH_CA_KEYSTORE));
    }

    /* access modifiers changed from: protected */
    public X509Certificate loadCertificate(String str) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        return CaCertUtil.loadCertificate(str);
    }

    /* access modifiers changed from: protected */
    public String getStorePassword() {
        return CaCertUtil.getStorePassword();
    }

    /* access modifiers changed from: protected */
    public void regenerateCASignedCert(String str) throws NoSuchAlgorithmException, IOException, SignatureException, CertificateException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException {
        CaCertUtil.regenerateCASignedCert(str, this.isCa, getKeystorePassword(), this.keystore, this.overwrite);
    }

    /* access modifiers changed from: package-private */
    public String makePEM(X509Certificate x509Certificate) throws CertificateEncodingException {
        return CertEncoder.makePEM(x509Certificate);
    }

    /* access modifiers changed from: protected */
    public X509Certificate certFromPEM(String str) throws IOException {
        return CertEncoder.certFromPEM(str);
    }

    /* access modifiers changed from: protected */
    public KeyStore loadKeyStore(FileInputStream fileInputStream) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        String storePassword = CaCertUtil.getStorePassword();
        KeyStore instance = KeyStore.getInstance(KeyStore.getDefaultType());
        instance.load(fileInputStream, storePassword.toCharArray());
        return instance;
    }

    /* access modifiers changed from: protected */
    public Certificate[] getKeyStoreCertificateChain(KeyStore keyStore) throws KeyStoreException {
        return keyStore.getCertificateChain("mykey");
    }
}
