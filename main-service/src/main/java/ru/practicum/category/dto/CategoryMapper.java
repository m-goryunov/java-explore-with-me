package ru.practicum.category.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.category.Category;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(NewCategoryDto newCategoryDto) {
        return new Category(newCategoryDto.getName());
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public List<CategoryDto> toCategoryDto(List<Category> cats) {
        return cats.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }
}
