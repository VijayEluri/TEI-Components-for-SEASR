package edu.brown.seasr.filesystemenumeratorcomponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.util.logging.Level;

/**
 * User: mdellabitta
 * Date: 2011-03-24
 * Time: 8:33 AM
 */

@SuppressWarnings( { "WeakerAccess" })
@Component(
        creator = "Michael Della Bitta",
        description = "",
        name = "Filesystem Enumerator",
        tags = "file, directory, uri",
        dependency = { "protobuf-java-2.2.0.jar" },
        baseURL = "meandre://brown.edu/seasr/tei/components/",
        firingPolicy = Component.FiringPolicy.all,
        mode = Component.Mode.compute,
        rights = Component.Licenses.Other)

public class FilesystemEnumeratorComponent extends AbstractExecutableComponent {

    @ComponentOutput(description = "The list of filesystem URIs.<br>TYPE: Strings.", name = "uris")
    final static String OUT_URIS = "uris";

    @ComponentProperty(name = "regexp", description = "Optional regular expression in java regexp syntax to filter the names of the files returned.", defaultValue = "")
    final static String PROP_REGEXP = "regexp";

    @ComponentProperty(name = "recurse", description = "Whether the component should decend through the sub directory tree or not. 'true' or 'false'.", defaultValue = "false")
    final static String PROP_RECURSE = "recurse";

    @ComponentProperty(name = "directory", description = "Absolute path to the parent directory.", defaultValue = "")
    final static String PROP_DIR = "directory";

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the pushed message be wrapped as a stream.",
            defaultValue = "false"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
    
    private String regexp = null;
    private boolean recurse = false;
    private String directory = null;
    private boolean wrap = false;

    @Override
    public void disposeCallBack(ComponentContextProperties componentContextProperties) throws Exception {
        console.fine("IN dispose");
    }

    @Override
    public void initializeCallBack(ComponentContextProperties componentContextProperties) throws Exception {
        console.fine("IN initialize");
        String regexpProp = componentContextProperties.getProperty(PROP_REGEXP);
        String recurseProp = componentContextProperties.getProperty(PROP_RECURSE);
        String wrapProp = componentContextProperties.getProperty(PROP_WRAP_STREAM);
        String directoryProp = componentContextProperties.getProperty(PROP_DIR);

        if (recurseProp != null && "true".equals(recurseProp.toLowerCase())) {
            recurse = true;
        }

        if (wrapProp != null && "true".equals(wrapProp.toLowerCase())) {
            wrap = true;
        }

        regexp = regexpProp;

        if (directoryProp == null || "".equals(directoryProp)) throw new Exception("No directory specified.");
        directory = directoryProp;
    }

    public FilesystemEnumerator getFilesystemEnumerator() {
        return new FilesystemEnumerator();
    }

    @Override
    public void executeCallBack(ComponentContext componentContext) throws Exception {
        console.fine("IN execute");
        try {
            FilesystemEnumerator filesystemEnumerator = getFilesystemEnumerator();
            String[] uris = filesystemEnumerator.getURIs(directory, regexp, recurse);
            BasicDataTypes.Strings output = BasicDataTypesTools.stringToStrings(uris);

            if (wrap) {
                pushInitiator(new File(directory).toURI().toString());
            }
            componentContext.pushDataComponentToOutput(OUT_URIS, output);

            if (wrap) {
                pushTerminator(new File(directory).toURI().toString());
            }

        } catch (Exception e) {
            outputError(e, Level.SEVERE);
        }
    }

    private void pushInitiator(String sLoc) throws Exception {
        console.fine("Pushing " + StreamInitiator.class.getSimpleName());

        StreamInitiator si = new StreamInitiator();
        si.put(PROP_DIR, sLoc);
        componentContext.pushDataComponentToOutput(OUT_URIS, si);
    }

    private void pushTerminator(String sLoc) throws Exception {
        console.fine("Pushing " + StreamTerminator.class.getSimpleName());

        StreamTerminator st = new StreamTerminator();
        st.put(PROP_DIR, sLoc);
        componentContext.pushDataComponentToOutput(OUT_URIS, st);
    }
}
