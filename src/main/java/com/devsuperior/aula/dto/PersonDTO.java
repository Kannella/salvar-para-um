package com.devsuperior.aula.dto;

import com.devsuperior.aula.entities.Person;

public class PersonDTO {

    private Long id;
    private String name;
    private Double salary;
    //A classe PersonDTO tem um atributo que vai vir direto para o objeto PersonDTO e nao mais um atributo com uma referencia para o objeto DepartmentDTO. E para salvar isso no banco eu vou ter que instanciar uma entidade apontando para o departamento  que vai ter esse id e depois eu salvo
    private Long departmentId;

    public PersonDTO(Long id, String name, Double salary, Long departmentId) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.departmentId = departmentId;
    }

    public PersonDTO(Person entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.salary = entity.getSalary();
        this.departmentId = entity.getDepartment().getId();  //Pego somente o id do departamento por meio da referencia de Department que a entidade Person tem.
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getSalary() {
        return salary;
    }

    public Long getDepartmentId() {
        return departmentId;
    }
}
