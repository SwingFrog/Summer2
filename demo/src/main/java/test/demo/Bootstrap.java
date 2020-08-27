package test.demo;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.starter.Summer;
import com.swingfrog.summer2.starter.annotation.SummerApplication;
import com.swingfrog.summer2.starter.event.SummerContext;
import com.swingfrog.summer2.starter.event.SummerListener;
import test.demo.component.BaseComponent;
import test.demo.component.ComponentA;

/**
 * @author: toke
 */
@SummerApplication
public class Bootstrap implements SummerListener {

    @Autowire
    private ComponentA componentA;

    @Override
    public void onStart(SummerContext context) {
        componentA.a();
        System.out.println();
        context.listBean(BaseComponent.class).forEach(BaseComponent::b);
    }

    @Override
    public void onStop(SummerContext context) {

    }

    public static void main(String[] args) {
        Summer.hot(Bootstrap.class);
    }

}
