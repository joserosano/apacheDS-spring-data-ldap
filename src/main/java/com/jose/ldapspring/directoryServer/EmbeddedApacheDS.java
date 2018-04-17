package com.jose.ldapspring.directoryServer;

import com.jose.ldapspring.configuration.ApacheDSConfig;
import com.sun.istack.internal.NotNull;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class EmbeddedApacheDS {
    /** The directory service */
    private DirectoryService service;

    /** The LDAP server to be exposed to Spring Data LDAP*/
    private LdapServer server;

    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    @Autowired
    public EmbeddedApacheDS(@NotNull ApacheDSConfig apacheDSConfig) throws Exception {
        initDirectoryService(new File(apacheDSConfig.getWorkDirectory()));
        startServer();
    }

    /**
     * Initialize the server. It creates the partitions, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
     */
    private void initDirectoryService(File workDir) throws Exception {
        service = new DefaultDirectoryService();
        service.setInstanceLayout(new InstanceLayout(workDir));

        CacheService cacheService = new CacheService();
        cacheService.initialize(service.getInstanceLayout());

        service.setCacheService(cacheService);

        // first load the schema
        initSchemaPartition();

        // create system partition if does not exist
        //TODO jose revisa como hacer para que no cree la system partition si ya existe
        JdbmPartition systemPartition = createSystemPartition();

        // mandatory to call this method to set the system partition
        service.setSystemPartition(systemPartition);

        //allow anonymous connect
        //service.setAllowAnonymousAccess(true);

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled(false);
        service.setDenormalizeOpAttrsEnabled(true);

        // Create SGR partition
        Partition sgrPartition = addPartition("sgrusers", "ou=sgrusers,dc=sgr,dc=com", service.getDnFactory());

        // Index some attributes on the partition
        // addIndex(apachePartition, "objectClass", "ou", "uid");

        // Start the service
        service.startup();

        // Inject the context entry for partition if it does not already exist
        /*try {
            service.getAdminSession().lookup(sgrPartition.getSuffixDn());
        }
        catch (LdapException lnnfe) {
            Dn dnFoo = new Dn( "ou=sgrusers,dc=example,dc=com" );
            Entry entryFoo = service.newEntry( dnFoo );
            entryFoo.add( "objectClass", "top", "domain", "extensibleObject" );
            entryFoo.add( "dc", "department" );
            service.getAdminSession().add( entryFoo );
        }*/
    }

    /**
     * Starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception {
        server = new LdapServer();
        int serverPort = 10412;
        server.setTransports(new TcpTransport(serverPort));
        server.setDirectoryService(service);

        server.start();
    }


    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @param dnFactory the DN factory
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(String partitionId, String partitionDn, DnFactory dnFactory) throws Exception {
        // Create a new partition with the given partition id
        JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
        partition.setId(partitionId);
        partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId ).toURI());
        partition.setSuffixDn(new Dn(partitionDn));
        service.addPartition(partition);

        return partition;
    }

    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex( Partition partition, String... attrs ) {
        Set indexedAttributes = new HashSet();
        for (String attribute : attrs) {
            indexedAttributes.add(new JdbmIndex(attribute, false ));
        }
        ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
    }


    /**
     * initialize the schema manager and add the schema partition to directory service
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception {
        InstanceLayout instanceLayout = service.getInstanceLayout();

        File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

        // Extract the schema on disk (a brand new one) and load the registries
        if (schemaPartitionDirectory.exists()) {
            System.out.println( "schema partition already exists, skipping schema extraction");
        } else {
            SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
            extractor.extractOrCopy();
        }

        SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
        SchemaManager schemaManager = new DefaultSchemaManager(loader);

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix Dn
        schemaManager.loadAllEnabled();

        List<Throwable> errors = schemaManager.getErrors();

        if (!errors.isEmpty()) {
            throw new Exception( I18n.err( I18n.ERR_317, Exceptions.printErrors(errors)));
        }

        service.setSchemaManager(schemaManager);

        // Init the LdifPartition with schema
        LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, service.getDnFactory());
        schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

        // The schema partition
        SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
        schemaPartition.setWrappedPartition(schemaLdifPartition);
        service.setSchemaPartition(schemaPartition);
    }

    /**
     * Creates the mandatory System partition
     * @return the created System partition
     * @throws LdapInvalidDnException
     */
    private JdbmPartition createSystemPartition() throws LdapInvalidDnException {
        // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
        JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager(), service.getDnFactory());
        systemPartition.setId("system");
        systemPartition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId()).toURI());
        systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
        systemPartition.setSchemaManager(service.getSchemaManager());
        return systemPartition;
    }
}
