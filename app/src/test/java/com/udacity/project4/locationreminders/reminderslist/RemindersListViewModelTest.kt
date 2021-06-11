package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import com.google.common.truth.Truth.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var reminderViewModel : RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var reminderRepository: FakeDataSource

    /**
     * This rules all related arch component background Job in the same thread
     * */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel () {

        reminderRepository = FakeDataSource()

        reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }



    @Test
    fun loadingReminder_loading() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        // pause dispatcher so you can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // load the task from viewModel
        reminderViewModel.loadReminders()

        // THEN : Assert that progress indicator is shown
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is` (true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is` (false))

    }


    @Test
    fun unAvailableReminders_loadErrorMessage () = mainCoroutineRule.runBlockingTest{

        reminderRepository.setReturnError(true)

        reminderViewModel.loadReminders()

        assertThat(reminderViewModel.showSnackBar.getOrAwaitValue(), `is` ("Error getting reminders"))

    }

    @Test
    fun deleteReminder_check_if_list_isEmpty() = mainCoroutineRule.runBlockingTest {

        // Given
        reminderRepository.deleteAllReminders()

        // When
        reminderViewModel.loadReminders()

        // Then
        assertThat(reminderViewModel.showNoData.getOrAwaitValue(), `is` (true))

    }

    @Test
    fun save_to_database_check_if_view_isNotEmpty() = mainCoroutineRule.runBlockingTest {

        val firstReminder = ReminderDTO(
            "Chicken Republic", "Get Snack", "Austria", 6.454202, 3.599068
        )

        reminderRepository.saveReminder(firstReminder)

        reminderViewModel.loadReminders()

        assertThat(reminderViewModel.remindersList.getOrAwaitValue().isNotEmpty())


    }

}