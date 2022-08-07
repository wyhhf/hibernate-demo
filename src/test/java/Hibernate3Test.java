import com.wyh.hibernatedemo.entity.n21both.Customer;
import com.wyh.hibernatedemo.entity.n21both.Order;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Hibernate3Test {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addClass(Customer.class);
        configuration.addClass(Order.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        session = sessionFactory.openSession();

        transaction = session.beginTransaction();
    }

    @AfterEach
    public void destroy() {
        transaction.commit();
        session.close();
        sessionFactory.close();
    }

    @Test
    public void testCascade() {
        Customer customer = session.get(Customer.class, 2);
        customer.getOrders().clear();
    }

    @Test
    public void testDelete() {
        Customer customer = session.get(Customer.class, 1);
        session.delete(customer);
    }

    @Test
    public void testUpdate2() {
        Customer customer = session.get(Customer.class, 1);
        customer.getOrders().iterator().next().setOrderName("GGG");
    }

    @Test
    public void testOne2ManyGet() {
        // 1.对n的一端的集合使用延迟加载
        Customer customer = session.get(Customer.class, 3);
        System.out.println(customer.getCustomerName());
        System.out.println(customer.getOrders());
        // 2.返回的多的一端的集合是Hibernate内置的集合对象。
        // 该类型具有延迟加载和存放代理对象的功能
        System.out.println(customer.getOrders().getClass());
        // 3.可能抛出 LazyInitializationException 异常
        // 4.在需要使用集合中元素的时候进行初始化。
    }

    @Test
    public void testOneToManyBothSave() {
        Customer customer = new Customer();
        customer.setCustomerName("ceshi");

        Order order1 = new Order();
        order1.setOrderName("order3");
        Order order2 = new Order();
        order2.setOrderName("order4");

        order1.setCustomer(customer);
        order2.setCustomer(customer);


        customer.getOrders().add(order1);
        customer.getOrders().add(order2);
        // 三条insert，两条update
        // 因为1的一端和n的一端都维护关联关系，所以会多出update
        // 可以在1的一端的set节点指定inverse=true，来使1的一端放弃维护关联关系
        // 建议设定set的inverse=true，建议先插入1的一段，后插入多的一端
        session.save(customer);
        session.save(order1);
        session.save(order2);
    }
}