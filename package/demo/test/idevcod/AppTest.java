/**@author:idevcod
 */
package idevcod;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AppTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    App app;

    @Before
    public void setUp()
    {
        app = new App();
    }

    @Test
    public void test001()
    {
        assertTrue(app.init());
    }

    @Test
    public void test002()
    {
        app.exception();
    }
    
    @Test
    public void test003() {
        assertFalse(app.init());
    }
}
