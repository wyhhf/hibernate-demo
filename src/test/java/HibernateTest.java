import com.wyh.hibernatedemo.entity.News;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class HibernateTest {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addClass(News.class);

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
    public void testDoWork() {
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                System.out.println(connection);
                // 调用存储过程
            }
        });
    }

    /**
     * evict：从session缓存中把指定的持久化对象移除
     */
    @Test
    public void testEvict() {
        News news1 = session.get(News.class, 8);
        News news2 = session.get(News.class, 9);

        news1.setTitle("AA");
        news2.setTitle("BB");

        session.evict(news1);
    }

    /**
     * delete：执行删除操作，只要 OID 和数据表中一条记录对应，就会准备执行delete操作
     * 若 OID 在数据表中没有对应的记录，则抛出异常
     * 可以通过设置hibernate配置文件 se_identifier_rollback 属性为true，
     * 使删除对象后，把其OID置为null
     */
    @Test
    public void testDelete() {

//        News news = new News();
//        news.setId(11);

        News news = session.get(News.class, 7);
        session.delete(news);

        System.out.println(news);
    }

    /*merger方法未讲*/

    /**
     * 注意：
     * 1.若OID不为null，但数据表中还没有和其对应的记录。会抛出异常
     * 2.了解：OID值等于 id 的 unsaved-value 属性值的对象，也被认为是一个游离对象
     */
    @Test
    public void testSaveOrUpdate() {
        News news = new News("FF", "ff", new Date());
        news.setId(11);

        session.saveOrUpdate(news);
    }

    /**
     * update:
     * 1.若更新一个持久化对象，不需要显示的调用update方法。以为在调用Transaction
     * 的commit()方法时，会先执行session的flush方法。
     * 2.更新一个游离对象，需要显示的调用session的update方法。可以把一个游离对象变为持久化对象（加载到Session缓存中）。
     * <p>
     * 之后尝试执行update语句
     * <p>
     * 需要注意的：
     * 1.无论要更新的游离对象和数据表的记录是否一致，都会发送UPDATE语句。
     * 如何能让update方法不再盲目的发出update语句呢？ 在 .hbm.xml文件的class节点配置
     * select-before-update=true(默认为false)，但通常不需要设置该属性。
     * <p>
     * 2.若数据表中没有对应的记录，但还调用了update方法，会抛出异常
     * <p>
     * 3.当update()方法关联一个游离对象时，
     * 如果在Session的缓存中已经存在相同OID的持久化对象，会抛出异常。因为在session缓存中不能有两个OID相同的对象。
     * NonUniqueObjectException
     */
    @Test
    public void testUpdate() {
        News news = session.get(News.class, 1);

        transaction.commit();
        session.close();

//        news.setId(10);

        session = sessionFactory.openSession();
        transaction = session.beginTransaction();

        //  news对象此时是游离对象
//        news.setAuthor("王怡贺");

        News news1 = session.get(News.class, 1);

        session.update(news);
    }

    /**
     * get和load：
     * 1.执行 get方法： 会立即加载对象。
     * 而执行load方法，若不使用该对象，则不会立即执行查询操作，而返回一个代理对象
     * get是立即检索，load是延迟检索
     * 2.load方法可能会抛出懒加载异常。LazyInitializationException：在需要初始化代理对象之前，已经关闭了session
     * 3.如果在数据表中没有对应的记录且session也没有被关闭，同时需要使用对象时，get返回null，load方法，若不使用给对象的任何属性，没问题；若需要初始化了，抛出异常
     */
    @Test
    public void testLoad() {
        News news = session.load(News.class, 10);
        System.out.println(news.getClass().getName());

//        session.close();

//        System.out.println(news);
    }

    @Test
    public void testGet() {
        News news = session.get(News.class, 1);
//        session.close();
        System.out.println(news);
    }

    /**
     * persist()：也会执行INSERT操作。
     * 和save的区别
     * 在调用persist方法之前，若对象已经有id了，则不会执行INSERT，而抛出异常
     */
    @Test
    public void testPersist() {
        News news = new News();
        news.setTitle("CC");
        news.setAuthor("cc");
        news.setDate(new Date());
        news.setId(200);

        session.persist(news);
    }


    /**
     * 1.save方法
     * 1).使一个临时对象变为持久化对象
     * 2).为对象分配id
     * 3).在flush缓存时会发送一条insert语句
     * 4).id是用来唯一标识这个对象和数据表之间的一个关系，在save方法之前的id是无效的
     * 5).持久化之后对象的id是不能被修改的
     * setId()并不会生效
     */
    @Test
    public void testSave() {
        News news = new News();
        news.setTitle("AA");
        news.setAuthor("aa");
        news.setDate(new Date());

        System.out.println(news);
        session.save(news);
        System.out.println(news);
    }


    /**
     * 清理缓存
     */
    @Test
    public void testClear() {
        News news = session.get(News.class, 1);

        session.clear();

        News news1 = session.get(News.class, 1);
    }

    /**
     * refresh():会强制发送SELECT语句，以使Session缓存中对象的状态和数据表中对应的记录保持一致。
     */
    @Test
    public void testRefresh() {
        News news = session.get(News.class, 1);
        System.out.println(news);

        session.refresh(news);
        System.out.println(news);
    }

    /**
     * flush:使数据表中的记录和Session缓存中的对象的状态保持一致。为了保存一致，则可能发送对应的sql语句
     * 1.调用Transaction的commit方法：先调用session的flush方法，再提交事务
     * 2.flush方法可能会发送sql语句（不一致情况下），不会提交事务
     * 3.注意：在未提交事务或显示的调用session.flush()方法之前，也有可能会进行flush()操作
     * 1).执行HQL或QBC查询，会先进行flush()操作，以得到数据表的最新记录
     * 2).若记录的ID是由底层数据库使用自增的方式生成的，则在调用save()方法时，就会立即发送INSERT语句
     * 因为save方法后，必须保证对象的ID是存在的。
     */
    @Test
    public void testSessionFlush() {
        News news = session.get(News.class, 8);
        news.setAuthor("王怡贺");

        News news1 = (News) session.createCriteria(News.class).uniqueResult();
        System.out.println(news1);
    }

    @Test
    public void testSessionFlush2() {
        News news = new News("Java", "wyh", new Date());
        session.save(news);
    }

    @Test
    @DisplayName("测试SessionCache")
    public void testSessionCache() {
        News news = (News) session.get(News.class, 1);
        System.out.println(news);

        News news1 = (News) session.get(News.class, 1);
        System.out.println(news1);
    }
}