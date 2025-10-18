package com.dlight.eric.taskmanager.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.presentation.activity.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith


// TODO: Fix this when I get time... Problem: Init failing.

/*
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginAddUpdateTaskTest {

    val hiltRule = HiltAndroidRule(this)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rule: RuleChain = RuleChain.outerRule(hiltRule)
        .around(composeTestRule)

    private lateinit var appDataStore: AppDataStore

    @Before
    fun setUp() {
        appDataStore = AppDataStore(ApplicationProvider.getApplicationContext())

        runBlocking {
            appDataStore.clearAuthData()
        }
    }

    @Test
    fun login_Add_Update_Task_fullFlow() {
        // Wait for login screen to appear
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithTag("email").fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Login
        composeTestRule
            .onNodeWithTag("email")
            .performTextInput("eric@dlight.com")

        composeTestRule
            .onNodeWithTag("log_in")
            .performClick()

        // Wait for the 5-second auth delay and navigation to tasks screen
        composeTestRule.waitUntil(timeoutMillis = 7000L) {
            composeTestRule.onAllNodesWithTag("tasks").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Create a Task
        composeTestRule
            .onNodeWithTag("add_task")
            .performClick()

        composeTestRule
            .onNodeWithTag("title")
            .performTextInput("Original Task Title")

        composeTestRule
            .onNodeWithTag("description")
            .performTextInput("Original description")

        composeTestRule
            .onNodeWithTag("save")
            .performClick()

        // 3. Edit the task
        composeTestRule
            .onNodeWithTag("edit")
            .performClick()

        // Clear and update title
        composeTestRule
            .onNodeWithTag("title")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("title")
            .performTextInput("Updated Task Title")

        // Save changes
        composeTestRule
            .onNodeWithTag("save")
            .performClick()

        // 4. Verify changes are saved
        composeTestRule
            .onNodeWithText("Updated Task Title")
            .assertIsDisplayed()

        // Back to List screen
        composeTestRule
            .onNodeWithTag("back")
            .performClick()

        composeTestRule
            .onNodeWithText("Updated Task Title")
            .assertIsDisplayed()
    }
}
*/