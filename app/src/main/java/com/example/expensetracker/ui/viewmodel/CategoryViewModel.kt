package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Category Management screen.
 */
data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val categoryToEdit: Category? = null // For handling edit dialog state
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            expenseRepository.getAllCategories()
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Error loading categories: ${exception.message}")
                    }
                }
                .collect { categoryList ->
                    _uiState.update {
                        it.copy(isLoading = false, categories = categoryList)
                    }
                }
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Category name cannot be empty.") }
            return
        }
        // Optional: Check if category name already exists (though DB has unique constraint)
        // val existing = _uiState.value.categories.any { it.name.equals(name, ignoreCase = true) }
        // if (existing) { ... }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(errorMessage = null) } // Clear previous error
                expenseRepository.insertCategory(Category(name = name.trim()))
                // No need to manually update state, Flow should trigger reload via collect
            } catch (e: Exception) {
                // Handle potential errors (e.g., unique constraint violation if check wasn't done)
                 _uiState.update { it.copy(errorMessage = "Error adding category: ${e.message}") }
            }
        }
    }

    fun updateCategory(category: Category) {
         if (category.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Category name cannot be empty.") }
            return
        }
         // Prevent editing the default 'Uncategorized' category if needed
         if (category.id == Category.UNCATEGORIZED.id) {
             _uiState.update { it.copy(errorMessage = "Cannot edit the default 'Uncategorized' category.") }
             return
         }

        viewModelScope.launch {
             try {
                 _uiState.update { it.copy(errorMessage = null, categoryToEdit = null) } // Clear error and edit state
                 expenseRepository.updateCategory(category.copy(name = category.name.trim()))
             } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Error updating category: ${e.message}") }
             }
        }
    }

     fun deleteCategory(category: Category) {
         // Prevent deleting the default 'Uncategorized' category
         if (category.id == Category.UNCATEGORIZED.id) {
             _uiState.update { it.copy(errorMessage = "Cannot delete the default 'Uncategorized' category.") }
             return
         }
         // TODO: Consider implications - what happens to expenses in this category?
         // The current DB schema sets category_id to default (1 - Uncategorized) on delete.
         // Confirm this is the desired behavior or add checks/warnings.

         viewModelScope.launch {
             try {
                 _uiState.update { it.copy(errorMessage = null) }
                 expenseRepository.deleteCategory(category)
             } catch (e: Exception) {
                 _uiState.update { it.copy(errorMessage = "Error deleting category: ${e.message}") }
             }
         }
     }

    // Functions to manage edit dialog state
    fun setCategoryToEdit(category: Category?) {
        _uiState.update { it.copy(categoryToEdit = category, errorMessage = null) }
    }
}