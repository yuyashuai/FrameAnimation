package com.yuyashuai.frameanimationmaster

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author yuyashuai   2019-05-30.
 */
@RunWith(AndroidJUnit4::class)
class TestActivityTest {
    @get:Rule
    val mainRule = ActivityTestRule<TestActivity>(TestActivity::class.java)

    @Test
    fun fastClick() {
        repeat(1000) {
            onView(withId(R.id.btn1_test)).perform(click())
            //Thread.sleep(20)
            if (it % 10 == 0) {
                Thread.sleep(2000)
            }
            onView(withId(R.id.btn2_test)).perform(click())
            if (it % 10 == 1) {
                Thread.sleep(2000)
            }
            if (it % 10 == 2) {
                onView(withId(R.id.btn3_test)).perform(click())
            }
        }
    }
}