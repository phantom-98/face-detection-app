package com.facecool.facecoolalert;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;

@RunWith(AndroidJUnit4.class)
public class UserGenerationTest {

    private SubjectDao subjectDao;
    private TestSubjectGenerator testSubjectGenerator;
    private int originalNumberOfUsers;
    private static final int NUMBER_OF_USERS_TO_GENERATE = 200;
    private static final String TAG = "UserGenerationTest";

    @Before
    public void setUp() throws Exception {
        subjectDao = MyDatabase.getInstance(getApplicationContext()).subjectDao();
        testSubjectGenerator = new TestSubjectGenerator(subjectDao);
        originalNumberOfUsers = subjectDao.getAllSubjects().size();

//        subjectDao.deleteAll();
    }

    @Test
    public void testUserGeneration() {
        testSubjectGenerator.setupUsers(NUMBER_OF_USERS_TO_GENERATE);

        int newNumberOfUsersInDatabase = subjectDao.getAllSubjects().size();
        int increaseInUsers = newNumberOfUsersInDatabase - originalNumberOfUsers;
        assertEquals(NUMBER_OF_USERS_TO_GENERATE, increaseInUsers);
    }

    @Test
    public void testUsersExistence() {
        int numberOfUsersInDatabase = subjectDao.getAllSubjects().size();
        Log.e(TAG, "Current number of users in database: " + numberOfUsersInDatabase);
        assertTrue("There should be users in the database", numberOfUsersInDatabase > 0);
    }

    @After
    public void tearDown() throws Exception {
//        subjectDao.deleteAll();
    }
}
