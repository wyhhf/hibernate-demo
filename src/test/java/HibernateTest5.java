import com.wyh.hibernate.union.subclass.Person;
import com.wyh.hibernate.union.subclass.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class HibernateTest5 {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addClass(Person.class);

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
    public void testUpdate() {
        String hql = "UPDATE Person p SET p.age = 20";
        session.createQuery(hql).executeUpdate();
    }
    /**
     * 优点：
     * 1.不需要使用辨别者列。
     * 2.子类独有的字段能添加非空约束。
     *
     * 缺点：
     * 1.存在冗余的字段
     * 2.若更新父表的字段，更新的效率较低。
     */
    /**
     * 查询：
     * 1.查询父类记录，需把父表和子表记录汇总到一起再做查询。性能稍差。
     * 2.对于子类记录，只需要查询一张数据表。
     */
    @Test
    public void testQuery() {
        List<Person> persons = session.createQuery("from Person").list();
        System.out.println(persons.size());

        List<Student> students = session.createQuery("from Student").list();
        System.out.println(students.size());
    }

    /**
     * 插入操作：
     * 1.对于子类对象只需把记录插入到一张数据表中。
     */
    @Test
    public void testSave() {
        Person person = new Person();
        person.setName("AA");
        person.setAge(12);

        Student student = new Student();
        student.setName("BB");
        student.setAge(22);
        student.setSchool("GuiGu");

        session.save(person);
        session.save(student);
    }
}