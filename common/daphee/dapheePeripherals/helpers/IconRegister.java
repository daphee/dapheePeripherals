package daphee.dapheePeripherals.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IconRegister {
    public static Map<Integer, IconRequest> requests = new HashMap<Integer, IconRequest>();
    public static AtomicInteger counter = new AtomicInteger();
}
