package test.demo.component;

/**
 * @author: toke
 */
public abstract class AbstractComponent implements BaseComponent {

    @Override
    public void a() {
        System.out.println(this.getClass().getSimpleName() + ".a");
    }

    @Override
    public void b() {
        System.out.println(this.getClass().getSimpleName() + ".b");
    }

}
