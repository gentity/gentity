import com.github.gentity.test.Test0h_base_table_standard_types;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestH {

    Test0h_base_table_standard_types t;
    @Before
    public void before() {
        t = new Test0h_base_table_standard_types();
        t.beforeTest();
    }

    @After
    public void after() {
        t.afterTest();
    }

    @Test
    public void test() throws NoSuchFieldException {
        t.test();
    }
}
