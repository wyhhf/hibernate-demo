import com.wyh.hibernate.n2n.Category;
import com.wyh.hibernate.n2n.Item;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class HibernateTest2 {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addClass(Category.class);
        configuration.addClass(Item.class);

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
        Category category = session.get(Category.class, 1);
        System.out.println(category.getName());

        // 需要连接中间表
        Set<Item> items = category.getItems();
        System.out.println(items.size());
    }

    @Test
    public void testSave() {
        Category category1 = new Category();
        category1.setName("category_1");

        Category category2 = new Category();
        category2.setName("category_2");

        Item item1 = new Item();
        item1.setName("item_1");
        Item item2 = new Item();
        item2.setName("item_2");
        // 设定关联关系
        category1.getItems().add(item1);
        category1.getItems().add(item2);

        item1.getCategories().add(category1);
        item1.getCategories().add(category2);

        category2.getItems().add(item1);
        category2.getItems().add(item2);

        item2.getCategories().add(category1);
        item2.getCategories().add(category2);

        // 保存操作
        session.save(category1);
        session.save(category2);

        session.save(item1);
        session.save(item2);
    }
}