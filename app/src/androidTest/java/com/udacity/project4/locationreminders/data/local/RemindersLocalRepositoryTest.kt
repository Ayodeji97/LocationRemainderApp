package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {

        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
        )
    }

    @After
    fun cleanUp() = database.close()


    @Test
    fun check_if_reminder_is_save_to_database () = runBlockingTest{
        val reminder = ReminderDTO("title", "description",
                "location", 6.454202, 3.599068)

        database.reminderDao().saveReminder(reminder)

        val getReminder = database.reminderDao().getReminders()

        assertThat(getReminder, `is`(notNullValue()))
    }


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

}