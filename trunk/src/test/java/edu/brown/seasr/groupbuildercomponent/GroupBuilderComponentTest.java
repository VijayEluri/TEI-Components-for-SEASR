package edu.brown.seasr.groupbuildercomponent;

import edu.brown.seasr.SEASRComponentTest;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Ignore;
import org.junit.Test;
import org.meandre.core.ComponentContext;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.BasicDataTypesTools;

import java.util.Arrays;
import java.util.List;

import static edu.brown.seasr.groupbuildercomponent.GroupBuilderComponent.*;
import static org.hamcrest.Matchers.allOf;

/**
 * User: mdellabitta
 * Date: 2011-02-15
 * Time: 6:08 PM
 */
@SuppressWarnings("unchecked")
public class GroupBuilderComponentTest extends SEASRComponentTest {

    final BasicDataTypes.Strings oneStrings;
    final BasicDataTypes.Strings threeStrings;
    final BasicDataTypes.Strings fourStrings;
    final BasicDataTypes.Strings sixStrings;
    final BasicDataTypes.Strings secondHalf;

    StreamInitiator si;
    StreamTerminator st;

    {
        oneStrings = strings("eat");
        threeStrings = strings("eat", "at", "joe's");
        fourStrings = strings("eat", "at", "joe's", "now");
        sixStrings = strings("eat", "at", "joe's", "or", "else", "!");
        secondHalf = strings("or", "else", "!");

        Mockery context = utils.getMockery();
        si = new StreamInitiator();
        st = new StreamTerminator();
    }

    @Override
    protected ExecutableComponent getComponent() {
        return new GroupBuilderComponent();
    }

    @Test
    public void testOne() throws Exception {
        test(Arrays.asList(threeStrings));
    }

    @Test
    public void testTwo() throws Exception {
        test(Arrays.asList(threeStrings), Arrays.asList(secondHalf));
    }

    @Test
    public void testLots() throws Exception {
        test(Arrays.asList(threeStrings, secondHalf), Arrays.asList(fourStrings, sixStrings), Arrays.asList(secondHalf));
    }

    private void test(List<BasicDataTypes.Strings>... stringseses) throws Exception {
        Mockery context = utils.getMockery();
        c = getComponent();

        final ComponentContext cc = utils.getComponentContext();

        utils.initializeComponent(c, properties);

        for (final List<BasicDataTypes.Strings> stringses : stringseses) {

            context.checking(new Expectations() {{
                oneOf(cc).isInputAvailable(IN_DOCUMENTS);
                will(returnValue(true));

                oneOf(cc).getDataComponentFromInput(IN_DOCUMENTS);
                will(returnValue(si));

                for (BasicDataTypes.Strings strings : stringses) {
                    oneOf(cc).isInputAvailable(IN_DOCUMENTS);
                    will(returnValue(true));

                    oneOf(cc).getDataComponentFromInput(IN_DOCUMENTS);
                    will(returnValue(strings));
                }

                oneOf(cc).isInputAvailable(IN_DOCUMENTS);
                will(returnValue(true));

                oneOf(cc).getDataComponentFromInput(IN_DOCUMENTS);
                will(returnValue(st));

            }});

            c.execute(cc);

            for (BasicDataTypes.Strings strings : stringses) {
                c.execute(cc);
            }

            c.execute(cc);
        }
        
        context.assertIsSatisfied();
    }

    private BasicDataTypes.Strings strings(String... strings) {
        return BasicDataTypesTools.stringToStrings(strings);
    }
}
