package be.vinci.pae.api.filters;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import jakarta.ws.rs.NameBinding;

@NameBinding
@Retention(RUNTIME)
public @interface AuthorizeBoss {

}
