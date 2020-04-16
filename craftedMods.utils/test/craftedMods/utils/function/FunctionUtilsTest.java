package craftedMods.utils.function;

import org.junit.*;

public class FunctionUtilsTest
{

    @Test
    public void testIsEmptyConsumerNotNull ()
    {
        Assert.assertNotNull (FunctionUtils.emptyConsumer ());
    }

    @Test
    public void testIsNullSupplierNotNull ()
    {
        Assert.assertNotNull (FunctionUtils.nullSupplier ());
    }

    @Test
    public void testDoesNullSupplierSupplyNull ()
    {
        Assert.assertNull (FunctionUtils.nullSupplier ().get ());
    }

}
