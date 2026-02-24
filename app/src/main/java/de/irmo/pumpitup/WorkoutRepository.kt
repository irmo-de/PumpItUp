package de.irmo.pumpitup

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pumpitup_workouts", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveWorkout(workout: Workout) {
        val workouts = getWorkouts().toMutableList()
        workouts.add(workout)
        saveList(workouts)
    }

    fun getWorkouts(): List<Workout> {
        val json = prefs.getString("workouts", null) ?: return emptyList()
        val type = object : TypeToken<List<Workout>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteWorkout(id: String) {
        val workouts = getWorkouts().filter { it.id != id }
        saveList(workouts)
    }

    fun updateWorkout(id: String, newCount: Int) {
        val workouts = getWorkouts().map {
            if (it.id == id) it.copy(count = newCount) else it
        }
        saveList(workouts)
    }

    private fun saveList(workouts: List<Workout>) {
        val json = gson.toJson(workouts)
        prefs.edit().putString("workouts", json).apply()
    }
}
