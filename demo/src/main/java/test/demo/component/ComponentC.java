package test.demo.component;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Component;

/**
 * @author: toke
 */
@Component
public class ComponentC extends AbstractComponent {

    private ComponentD componentD;

    @Autowire
    public void setComponentD(ComponentD componentD) {
        this.componentD = componentD;
    }

    @Override
    public void a() {
        super.a();
        componentD.a();
    }

}
