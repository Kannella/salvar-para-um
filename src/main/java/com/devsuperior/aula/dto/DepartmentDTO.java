package com.devsuperior.aula.dto;

import com.devsuperior.aula.entities.Department;

public class DepartmentDTO {
    private Long id;
    private String name;

    public DepartmentDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public DepartmentDTO(Department entity) {
        this.id = entity.getId();  // Neste ponto acessamos o banco
        this.name = entity.getName(); // Neste ponto acessamos o banco
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
