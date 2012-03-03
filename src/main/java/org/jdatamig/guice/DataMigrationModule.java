package org.jdatamig.guice;

import com.google.inject.AbstractModule;
import static com.google.inject.jndi.JndiIntegration.fromJndi;
import org.jdatamig.exporter.XMLExportService;
import org.jdatamig.exporter.XStreamXMLExporter;
import org.jdatamig.importer.XMLImportService;
import org.jdatamig.importer.XStreamXMLImporter;
import java.util.ResourceBundle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Guice Config Module for Dependency Injection.
 *
 * @author Babji, Chetty.
 */
public class DataMigrationModule extends AbstractModule {
    @Override
    public void configure() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("config");
            String datasourceName = bundle.getString("datasourceName");
            //Below : JNDI bindings
            bind(Context.class).to(InitialContext.class);
            bind(DataSource.class).toProvider(fromJndi(DataSource.class, datasourceName));            
            bind(XMLExportService.class).to(XStreamXMLExporter.class);
            bind(XMLImportService.class).to(XStreamXMLImporter.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /*
    @Provides
    public Context initContext() {
        try {
            Properties props = new Properties();
            props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
            props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
            props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

            //Optional.  Defaults to localhost.  Only needed if web server is running on a different host than the appserver
            props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");

            //Optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
            props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

            Context iContext = new InitialContext(props);
            
            NamingEnumeration<NameClassPair> list = iContext.list("");
            while(list.hasMore()) {
                System.out.println("Available JNDI Name For Guice: " + list.next().getName());
            }

            return new InitialContext();
        } catch (NamingException ne) {
            throw new RuntimeException("Unable to initialize context.", ne);
        }
    }
     *
     */
}