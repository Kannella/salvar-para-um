package com.devsuperior.aula.dto;

import com.devsuperior.aula.entities.Department;
import com.devsuperior.aula.entities.Person;

public class PersonDepartmentDTO {
    private Long id;
    private String name;
    private Double salary;

    //Como eu estou no DTO eu nunca posso usar a entidade diretamente como: private Department department;
    //A classe PersonDepartmentDTO tem uma referencia para DepartmentDTO. Entao o objeto PersonDepartmentDTO no atributo department apornta para o objeto DepartmentDTO
    private DepartmentDTO department;

    public PersonDepartmentDTO(Long id, String name, Double salary, DepartmentDTO department) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.department = department;
    }

    //Passando a entidade Person para o construtor na hora de criar um objeto e colocar os dados vindos do corpo da requisicao de forma correta
    public PersonDepartmentDTO(Person entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.salary = entity.getSalary();
        this.department = new DepartmentDTO(entity.getDepartment()); //Criei um DepartmentDTO passando para o construtor do DepartmentDTO uma entidade Department. E la na entidade Department o acesso aos dados do departamento eh feito
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

    public DepartmentDTO getDepartment() {
        return department;
    }
}
