package org.appfuse.service.impl;

import static org.junit.Assert.assertTrue;

import org.gbif.provider.dao.UniversalDao;
import org.gbif.provider.model.User;
import org.gbif.provider.service.impl.UniversalManagerImpl;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.AssertThrows;

/**
 * This class tests the generic UniversalManager and UniversalManagerImpl implementation.
 */
public class UniversalManagerTest extends BaseManagerMockTestCase {
    protected UniversalManagerImpl manager = new UniversalManagerImpl();
    protected UniversalDao dao;

    @Before
    public void setUp() throws Exception {
        dao = context.mock(UniversalDao.class);
        manager.setDao(dao);
    }

    @After
    public void tearDown() throws Exception {
        manager = null;
        dao = null;
    }

    /**
     * Simple test to verify BaseDao works.
     */
    @Test
    public void testCreate() {
        final User user = createUser();
        context.checking(new Expectations() {{
            one(dao).save(with(same(user)));
            will(returnValue(user));
        }});

        manager.save(user);
    }

    @Test
    public void testRetrieve() {
        final User user = createUser();
        context.checking(new Expectations() {{
            one(dao).get(User.class, "foo");
            will(returnValue(user));
        }});

        User user2 = (User) manager.get(User.class, user.getUsername());
        assertTrue(user2.getUsername().equals("foo"));
    }

    @Test
    public void testUpdate() {
        context.checking(new Expectations() {{
            one(dao).save(createUser());
        }});

        User user = createUser();
        manager.save(user);
    }

    @Test
    public void testDelete() {
        final Exception ex = new ObjectRetrievalFailureException(User.class, "foo");

        context.checking(new Expectations() {{
            one(dao).remove(User.class, "foo");
            one(dao).get(User.class, "foo");
            will(throwException(ex));
        }});

        manager.remove(User.class, "foo");
        new AssertThrows(ObjectRetrievalFailureException.class) {
            public void test() {
                manager.get(User.class, "foo");
            }
        }.runTest();
    }
    
    private User createUser() {
        User user = new User();
        // set required fields
        user.setUsername("foo");
        return user;
    }
}
