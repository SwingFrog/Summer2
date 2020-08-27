package test.demo.component;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Bean;
import com.swingfrog.summer2.core.ioc.annotation.Component;

/**
 * @author: toke
 */
@Component
public class ComponentD extends AbstractComponent {

    private final ComponentA componentA;

    @Autowire
    public ComponentD(ComponentA componentA) {
        this.componentA = componentA;
    }

    @Bean
    public ComponentE createComponentE() {
        return new ComponentE(componentA);
    }

}
