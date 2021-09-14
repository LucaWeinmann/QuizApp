package com.example.quizapp.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.ui.fragments.quizscreen.FragmentQuizQuestion

class VpaQuiz(fragment: Fragment, questions : List<Question>) : FragmentStateAdapter(fragment) {

    private val fragments = Array(questions.size) {
        FragmentQuizQuestion.newInstance(questions[it])
    }

    fun getFragment(position: Int) = fragments[position]

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}