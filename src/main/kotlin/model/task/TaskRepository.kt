package com.ksuzuki.model.task

object TaskRepository {

    private val tasks = mutableListOf(
        Task("cleaning", "Clean the house", Priority.LOW),
        Task("gardening", "Mow the lawn", Priority.MEDIUM),
        Task("shopping", "Buy the groceries", Priority.HIGH),
        Task("painting", "Paint the fence", Priority.VITAL),
        Task("cleaning", "Clean the house", Priority.LOW),
        Task("gardening", "Mow the lawn", Priority.MEDIUM),
        Task("shopping", "Buy the groceries", Priority.HIGH),
        Task("painting", "Paint the fence", Priority.VITAL)
    )

    fun allTasks(): List<Task> = tasks.toList()

    fun tasksByPriority(priority: Priority): List<Task> =
        tasks.filter { it.priority == priority }

    fun taskByName(name: String) = tasks.find {
        it.name.equals(name, ignoreCase = true)
    }

    fun addTask(task: Task) {
        if (taskByName(task.name) != null) {
            throw IllegalStateException("Cannot duplicate task names!")
        }
        tasks.add(task)
    }
}
