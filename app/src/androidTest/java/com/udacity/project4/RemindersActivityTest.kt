package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel

import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
  //  private val dataBindingIdlingResource = DataBindingIdlingResource()


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */

//    @Before
//    fun registerIdlingResource () {
//
//        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
//
//    }


    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
//    @After
//    fun unRegisterIdlingResource () {
//
//        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
//
//
//    }


    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


//    TODO: add End to End testing to the app

    @Test
    fun editTask () = runBlocking {
//
//        var reminder = ReminderDTO("Title", "Description",
//                "Location", 6.454202, 3.599068
//        )
//        repository.saveReminder(reminder)

        // Start up reminder screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)


      //  dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(R.string.err_enter_title)))

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))

        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())

        onView(withId(R.id.position_click)).perform(longClick())



         onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))

        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(getActivity(activityScenario)!!.window.decorView))).check(matches(isDisplayed()))




//        onView(withId(com.google.android.material.R.id.snackbar_text))
//                .check(matches(withText(R.string.err_select_location)))
//

        //close the activity before resetting the db
        activityScenario.close()

    }

}
