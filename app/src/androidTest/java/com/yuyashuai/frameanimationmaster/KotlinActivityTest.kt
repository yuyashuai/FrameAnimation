package com.yuyashuai.frameanimationmaster

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author yuyashuai   2019-05-31.
 */
@RunWith(AndroidJUnit4::class)
class KotlinActivityTest{

    @get:Rule
    val mainRules=ActivityTestRule<KotlinActivity>(KotlinActivity::class.java)

    @Test
    fun click(){
        repeat(500){
            onView(withId(R.id.btn_start)).perform(ViewActions.click())
        }
    }
}