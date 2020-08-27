package test.demo.component;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;

/**
 * @author: toke
 */
public class ComponentE extends AbstractComponent {

    private final ComponentA componentA;

    @Autowire
    private ComponentB componentB;

    public ComponentE(ComponentA componentA) {
        this.componentA = componentA;
    }

    @Override
    public void a() {
        super.a();
        componentA.a();
        System.out.println(componentB);
    }

}
