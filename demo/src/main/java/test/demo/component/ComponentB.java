package test.demo.component;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Component;

/**
 * @author: toke
 */
@Component
public class ComponentB extends AbstractComponent {

    @Autowire
    private ComponentC componentC;

    @Override
    public void a() {
        super.a();
        componentC.a();
    }

}
