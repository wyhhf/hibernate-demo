import com.wyh.hibernate.subclass.Person;
import com.wyh.hibernate.subclass.Student;
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

class HibernateTest3 {
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
    /**
     * 缺点：
     * 1.使用了辨别者列。
     * 2.子类独有的字段不能添加非空约束。
     * 3.若继承层次较深，则数据表的字段也会较多。
     */
    /**
     * 查询：
     * 1.查询父类记录，只需要查询一张数据表
     * 2.对于子类记录，也只需要查询一张数据表
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
     * 1.对于子类对象只需把记录插入到一张数据表中，
     * 2.辨别者列由Hibernate自动维护。
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