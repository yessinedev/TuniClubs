package com.tuniclubs.app.services;

import com.tuniclubs.app.dto.CategoryDTO;
import com.tuniclubs.app.exceptions.ResourceNotFoundException;
import com.tuniclubs.app.models.Category;
import com.tuniclubs.app.repository.CategoryRepository;
import com.tuniclubs.app.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ClubRepository clubRepository;

    public CategoryService(final CategoryRepository categoryRepository, ClubRepository clubRepository) {
        this.categoryRepository = categoryRepository;
        this.clubRepository = clubRepository;
    }



    public CategoryDTO createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return new CategoryDTO(savedCategory);
    }
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::new).toList();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found", id.toString()));
    }

}
