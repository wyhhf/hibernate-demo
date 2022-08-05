import com.wyh.hibernatedemo.entity.n21.Customer;
import com.wyh.hibernatedemo.entity.n21.Order;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class Hibernate2Test {
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

    // 删除
    @Test
    public void testDelete() {
        // 在不设定级联关系的情况下，且1这一端的对象有n的对象在引用，则不能直接删除1这一端的对象
        Customer customer = session.get(Customer.class, 1);
        session.delete(customer);
    }

    // 修改
    @Test
    public void testUpdate() {
        Order order = session.get(Order.class, 1);
        order.getCustomer().setCustomerName("AAA");
    }

    // 查询
    @Test
    public void testMany2OneGet() {
        // 1.若查询多的一端的一个对象，则默认情况下，只查多的一端的对象，而没有查询关联的1的那一端的对象
        Order order = session.get(Order.class, 1);
        System.out.println(order.getOrderName());

        System.out.println(order.getCustomer().getClass().getName());
        session.close();

        // 2.在需要使用到关联对象的时候，才发送对应的SQL语句
        Customer customer = order.getCustomer();
        System.out.println(customer.getCustomerName());

        // 3.在查询Customer对象时，由多的一端导航到1的一端时，
        // 若此时session已被关闭，则默认情况下
        // 会发生懒加载异常。LazyInitializationException

        // 4.获取Order对象时，默认情况下，其关联的Customer对象是一个代理对象！
    }

    // 保存
    @Test
    public void testMany2OneSave() {
        Customer customer = new Customer();
        customer.setCustomerName("AA");

        Order order1 = new Order();
        order1.setOrderName("order_1");

        Order order2 = new Order();
        order2.setOrderName("order_2");

        // 设定关联关系
        order1.setCustomer(customer);
        order2.setCustomer(customer);

        // 执行save操作：先插入Customer，再插入Order，3条Insert
        // 先插入1的一端，再插入n的一端，只用Insert语句
//        session.save(customer);
//        session.save(order1);
//        session.save(order2);

        // 先插入Order，再插入Customer. 3条Insert,2条update
        // 先插入n的一端，再插入一的一端，会多出Update语句
        // 因为在插入多的一端时，无法确定1的一端的外键，所以只能等1的一端插入后，再额外发送Update语句
        // 推荐先插入1的一端，再插入n的一端
        Customer customer1 = new Customer();
        customer1.setCustomerName("BB");

        Order order3 = new Order();
        order3.setOrderName("order_3");
        order3.setCustomer(customer1);

        Order order4 = new Order();
        order4.setOrderName("order_4");
        order4.setCustomer(customer1);

        session.save(order3);
        session.save(order4);
        session.save(customer1);
    }
}