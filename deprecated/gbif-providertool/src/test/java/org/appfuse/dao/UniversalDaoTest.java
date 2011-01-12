package org.appfuse.dao;

import org.gbif.provider.dao.UniversalDao;
import org.gbif.provider.model.User;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * This class tests the generic GenericDao and BaseDao implementation.
 */
public class UniversalDaoTest extends BaseDaoTestCase {
    protected UniversalDao universalDao;

    /**
     * This method is used instead of setUniversalDao b/c setUniversalDao uses
     * autowire byType <code>setPopulateProtectedVariables(true)</code> can also
     * be used, but it's a little bit slower.
     */
    public void onSetUpBeforeTransaction() throws Exception {
        universalDao = (UniversalDao) applicationContext.getBean("universalDao");
    }

    public void onTearDownAfterTransaction() throws Exception {
        universalDao = null;
    }

    /**
     * Simple test to verify CRUD works.
     */
    public void testCRUD() {
        User user = new User();
        // set required fields
        user.setUsername("foo");
        user.setPassword("bar");
        user.setFirstName("first");
        user.setLastName("last");
        user.setEmail("foo@bar.com");

        // create
        user = (User)universalDao.save(user);
        flush();
        assertNotNull(user.getId());

        // retrieve
        user = (User) universalDao.get(User.class, user.getId());
        assertNotNull(user);
        assertEquals("last", user.getLastName());

        // update
        universalDao.save(user);
        flush();

        user = (User) universalDao.get(User.class, user.getId());

        // delete
        universalDao.remove(User.class, user.getId());
        flush();
        try {
            universalDao.get(User.class, user.getId());
            fail("User 'foo' found in database");
        } catch (ObjectRetrievalFailureException e) {
            assertNotNull(e.getMessage());
        } catch (InvalidDataAccessApiUsageException e) { // Spring 2.0 throws this one
            assertNotNull(e.getMessage());
        }
    }
}
