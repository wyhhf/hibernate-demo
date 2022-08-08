import com.wyh.hibernate.one2one.primary.Department;
import com.wyh.hibernate.one2one.primary.Manager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HibernateTest1 {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addClass(Department.class);
        configuration.addClass(Manager.class);

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
    public void testGet() {
        // 1.默认情况下对关联属性使用懒加载
        Department department = session.get(Department.class, 1);
        System.out.println(department.getDeptName());
    }

    @Test
    public void testSave() {
        Department department = new Department();
        department.setDeptName("AA");
        Manager manager = new Manager();
        manager.setMgrName("A");
        // 设定关联关系
        department.setManager(manager);
        manager.setDepartment(department);
        // 保存操作
        // 先插入哪一个都不会有多余的update。都是先插入manager再插入department
        session.save(department);
        session.save(manager);
    }
}