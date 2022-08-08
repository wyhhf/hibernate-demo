import com.wyh.hibernate.joined.subclass.Person;
import com.wyh.hibernate.joined.subclass.Student;
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

class HibernateTest4 {
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
     * 优点：
     * 1.不需要使用辨别者列。
     * 2.子类独有的字段能添加非空约束。
     * 3.没有冗余字段。
     */
    /**
     * 查询：
     * 1.查询父类记录，做一个左外连接查询
     * 2.对于子类记录，做一个内连接查询
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
     * 1.对于子类对象至少要插入到两张数据表中。
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