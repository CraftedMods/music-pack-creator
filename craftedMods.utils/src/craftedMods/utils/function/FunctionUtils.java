package craftedMods.utils.function;

import java.util.function.*;

public class FunctionUtils
{
    public static <T> Consumer<T> emptyConsumer ()
    {
        return param ->
        {
        };
    }

    public static <T> Supplier<T> nullSupplier ()
    {
        return () -> null;
    }

}
