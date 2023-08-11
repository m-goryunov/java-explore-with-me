package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryControllerAdmin {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto saveCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryService.saveCategory(newCategoryDto));
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid NewCategoryDto categoryDto) {
        return CategoryMapper.toCategoryDto(categoryService.updateCategory(catId, categoryDto));
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }
}
