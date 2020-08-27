package test.demo.component;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Component;

/**
 * @author: toke
 */
@Component
public class ComponentA extends AbstractComponent {

    private final ComponentB componentB;

    @Autowire
    public ComponentA(ComponentB componentB) {
        this.componentB = componentB;
    }

    @Override
    public void a() {
        super.a();
        componentB.a();
    }

}
