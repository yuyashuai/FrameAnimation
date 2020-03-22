package com.yuyashuai.frameanimationmaster

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
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
class ActivityTest {
    @get:Rule
    val mainRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun fastClick() {
        repeat(1000) {
            onView(withId(R.id.btn_start)).perform(click())
            if (it % 10 == 0) {
                Thread.sleep(2000)
            }
            onView(withId(R.id.btn_stop)).perform(click())
            if (it % 10 == 1) {
                Thread.sleep(2000)
            }
        }
    }

    /**
     * #issue 34
     */
    @Test
    fun jump100000(){
        repeat(100000){
            onView(withId(R.id.btn_start)).perform(click())
            onView(withId(R.id.btn_jump)).perform(click())
            Thread.sleep(1000)
            pressBack()
        }
    }
}