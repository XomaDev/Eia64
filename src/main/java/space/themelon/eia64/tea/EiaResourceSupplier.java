package space.themelon.eia64.tea;

import org.teavm.classlib.ResourceSupplier;
import org.teavm.classlib.ResourceSupplierContext;

public class EiaResourceSupplier implements ResourceSupplier {
  @Override
  public String[] supplyResources(ResourceSupplierContext resourceSupplierContext) {
    return new String[] { "a.txt" };
  }
}
