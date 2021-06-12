package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
//import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    // Execute each task synchronously using architectural component
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database : RemindersDatabase

    @Before
    fun initDb () {

        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insert_task_get_task_by_Id () = runBlockingTest {

        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description", "location", 6.454202, 3.599068)
        database.reminderDao().saveReminder(reminder)

        //WHEN - load a reminder by its id
       val loaded =  database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded reminder contains the expected value
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is` (reminder.id))

        assertThat(loaded.title, `is`(reminder.title))

        assertThat(loaded.description, `is`(reminder.description))

        assertThat(loaded.latitude, `is`(reminder.latitude))

        assertThat(loaded.longitude, `is`(reminder.longitude))

        assertThat(loaded.location, `is`(reminder.location))

    }

    // test deleting all reminder

    @Test
    fun deleteAll_reminders_in_database () = runBlockingTest {

        // Given : Insert into database
        val reminder = ReminderDTO("title", "description", "location", 6.454202, 3.599068)
        val reminder2 = ReminderDTO("title", "description", "location", 6.454202, 3.599068)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)

        // When : delete the reminder from database
        database.reminderDao().deleteAllReminders()
        // Then : check if the database is empty
        val getReminder = database.reminderDao().getReminders()

        assertThat(getReminder, `is` (emptyList()))

    }

    // Test if a list of reminders are being saved into the database

    @Test
    fun check_if_reminders_are_saved_to_database () = runBlockingTest{

        // Given
        val reminder = ReminderDTO("title", "description", "location", 6.454202, 3.599068)
        val reminder2 = ReminderDTO("title", "description", "location", 6.454202, 3.599068)
        val reminder3 = ReminderDTO("title", "description", "location", 6.454202, 3.599068)

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // When
        val getReminders = database.reminderDao().getReminders()

        // Then
        assertThat(getReminders, `is`(notNullValue()))

    }
    

}