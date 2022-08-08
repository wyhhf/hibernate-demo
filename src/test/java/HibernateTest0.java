import com.wyh.hibernate.one2one.foreign.Department;
import com.wyh.hibernate.one2one.foreign.Manager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HibernateTest0 {
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
    public void testGet2() {
        // 在查询没有外键的实体对象时，使用左外连接查询，一并查询出其关联的对象
        // 并已经进行初始化
        Manager manager = session.get(Manager.class, 1);
        System.out.println(manager.getMgrName());
        System.out.println(manager.getDepartment().getDeptName());
//        Hibernate:
//        select
//        manager0_.mgr_id as mgr_id1_1_0_,
//                manager0_.mgr_name as mgr_name2_1_0_,
//        department1_.dept_id as dept_id1_0_1_,
//                department1_.dept_name as dept_nam2_0_1_,
//        department1_.manager_id as manager_3_0_1_
//                from
//        manager manager0_
//        left outer join
//        department department1_
//        on manager0_.mgr_id=department1_.manager_id
//        where
//        manager0_.mgr_id=?
    }

    @Test
    public void testGet() {
        // 1.默认情况下对关联属性使用懒加载
        // 2.会出现懒加载异常的问题
        Department department = session.get(Department.class, 1);
        System.out.println(department.getDeptName());

//        session.close();
        // 没有出现异常
//        System.out.println(department.getManager().getClass());
        // 懒加载异常：org.hibernate.LazyInitializationException: could not initialize proxy [com.wyh.hibernate.one2one.foreign.Manager#1] - no Session
//        System.out.println(department.getManager().getMgrName());
        // 3.查询manager对象的连接条件应该是department.manager_id = manager.mgr_id
        // 而不是下面的语句。
        Manager manager = department.getManager();
        System.out.println(manager.getMgrName());
        // Hibernate:
        //    select
        //        manager0_.mgr_id as mgr_id1_1_0_,
        //        manager0_.mgr_name as mgr_name2_1_0_,
        //        department1_.dept_id as dept_id1_0_1_,
        //        department1_.dept_name as dept_nam2_0_1_,
        //        department1_.manager_id as manager_3_0_1_
        //    from
        //        manager manager0_
        //    left outer join
        //        department department1_
        //            on manager0_.mgr_id=department1_.dept_id
        //    where
        //        manager0_.mgr_id=?
//        加入property-ref属性之后的sql语句
//        Hibernate:
//        select
//        manager0_.mgr_id as mgr_id1_1_0_,
//                manager0_.mgr_name as mgr_name2_1_0_,
//        department1_.dept_id as dept_id1_0_1_,
//                department1_.dept_name as dept_nam2_0_1_,
//        department1_.manager_id as manager_3_0_1_
//                from
//        manager manager0_
//        left outer join
//        department department1_
//        on manager0_.mgr_id=department1_.manager_id
//        where
//        manager0_.mgr_id=?

    }

    @Test
    public void testSave() {
        Department department = new Department();
        department.setDeptName("测试部");
        Manager manager = new Manager();
        manager.setMgrName("李经理");
        // 设定关联关系
        department.setManager(manager);
        manager.setDepartment(department);
        // 保存操作
        // 建议先保存没有外键列的对象，这样会减少update语句
        session.save(department);
        session.save(manager);
    }
}