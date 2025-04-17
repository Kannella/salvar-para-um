package com.devsuperior.aula.Services;

import com.devsuperior.aula.dto.PersonDepartmentDTO;
import com.devsuperior.aula.entities.Department;
import com.devsuperior.aula.entities.Person;
import com.devsuperior.aula.repositories.DepartmentRepository;
import com.devsuperior.aula.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service //Para registrar o componente
public class PersonService {

    //PersonService depende de PersonRepository
    @Autowired
    private PersonRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public PersonDepartmentDTO insert(PersonDepartmentDTO dto){

        //Dentro da memoria eu criei um objeto Person com os atributos id, name, salary, department
        Person entity = new Person();

        //O objeto dto que chegou de argumento tambem tem como atributos id, name, salary, department

        //O objeto que chega por post quando chegar no controlador vai ser instanciado um dto do java correspondente a esse JSON
        //Entao no caso do Department, vai ser criado pelo framework um objeto PersonDepartmentDTO a partir do JSON recebido na requisicao
        //que vai ter id e nome e no atributo Department (do tipo DepartmentDTO) da classe PersonDepartmentDTO vai ser criado uma referencia para o objeto DepartmentDTO
        //com as informacoes do objeto departamento que chegaram por JSON, o que voce nao informar para o json vai ficar como null na hora de instanciar os DTOs.

        //Convertendo os dados vindo do dto para nossa entidade
        entity.setName(dto.getName());
        entity.setSalary(dto.getSalary());

        //Criando uma entidade departamento e agora esse objeto eh uma entidade gerenciada pela JPA
        /*
        Com o getReferenceById
        Você está pedindo para o JPA te dar uma referência "preguiçosa" (lazy) para a entidade Department, sem ir ao banco na hora.

        O que o getReferenceById() faz?
            Ele não busca do banco imediatamente.
            Ele cria um "proxy" — um objeto que parece ser um Department, mas que ainda não tem os dados carregados.
            Esse proxy só acessa o banco quando você realmente chamar algo como dept.getName().
        Isso é chamado de lazy loading. Muito útil pra performance em algumas situações. Mas mesmo assim, dept já é uma entidade gerenciada!
        Mesmo sem buscar os dados no banco, o JPA já está rastreando esse dept. É como se ele dissesse:
        "Esse Department existe sim, com esse ID, e quando você precisar dos detalhes, eu vou buscar."

        Quando usar getReferenceById()?
        Use quando:
            Você só precisa do ID ou só vai associar a outra entidade.
            Não precisa acessar os campos agora.
            Quer evitar uma consulta desnecessária no banco naquele momento.

        */
        // A partir do momento que voce faz com que o objeto department seja gerenciado pela JPA, o Hibernate que vai posteriormente fazer as respectivas consultar para retornar o id e o nome do departamento
        // Entao enquanto houver a sessao da JPA, se eu precisar de algum dado dela ela vai no banco e busca. Se fosse so com uma entidade transient (como na explicacao minha la embaixo) nao funcionaria dessa forma, ela so pegaria o id e salvaria no banco
        Department dept = departmentRepository.getReferenceById((dto.getDepartment().getId()));
        //dept.setId(dto.getDepartment().getId());
        //dept.setName(dto.getDepartment().getName());

        //Associando o departamento passado no corpo da requisicao com a entidade pessoa (associando os dois objetos)
        entity.setDepartment(dept); // aqui ainda não acessou os dados do dept. Vai acessar os dados do deparment somente dentrod o contrutor do PersonDepartmentDTO

        //Salvar uma entidade no banco de dados usando o Repository (dependencia dessa classe)
        //Salvo essa entidade (que eu criei acima com o new) no banco e armazeno essa referencia para essa entidade nova e salvo nessa mesma variavel
        entity = repository.save(entity);// Salvo no banco a entidade nova passada pelo corpo da requisicao

        //Reconverto a entidade que eu acabei de salvar no banco para ProductDTO para passar para o Controller
        return new PersonDepartmentDTO(entity); //Retorno um novo PersonDepartmentDTO passando a entidade que eu acabei de criar para o construtor do PersonDepartmentDTO e la no construtor de PersonDepartmentDTO voce vai criar um novo objeto DepartmentDTO e acessar os dados dentro do DepartmentDTO do respectivo departamento
    }
}

/*
-----------------------------------------------------------------------------------------
Por que veio null no nome do departamento?

Atual:

public class PersonService {

    //PersonService depende de PersonRepository
    @Autowired
    private PersonRepository repository;

    public PersonDepartmentDTO insert(PersonDepartmentDTO dto){

        Person entity = new Person();

        entity.setName(dto.getName());
        entity.setSalary(dto.getSalary());

        Department dept = new Department();
        dept.setId(dto.getDepartment().getId());
        dept.setName(dto.getDepartment().getName());

        entity.setDepartment(dept);

        entity = repository.save(entity);

        return new PersonDepartmentDTO(entity);
    }
}

Porque o JSON não incluiu o nome do departamento, e você não buscou o departamento no banco. Você criou um Department novo com o id e name = null.
Então, esse Department que você associou ao Person é uma entidade "transiente", ou seja, um objeto criado na mão, que não está sendo gerenciado pelo JPA, e pior: nem buscado do banco.
O nome não veio porque:

    O JSON não trouxe o nome.
    Você não fez um findById no banco para preencher o nome.
    E você esperava que o nome "aparecesse sozinho" por mágica, o que não vai rolar 😅

Como resolver?
Se o departamento já existe no banco (e só o ID foi enviado), você deve buscar o Department
com esse ID no banco em vez de criar um novo manualmente.

Solução:
@Autowired
private DepartmentRepository departmentRepository;

...

// Buscar o departamento real do banco
Department dept = departmentRepository.findById(dto.getDepartment().getId())
        .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

entity.setDepartment(dept); //aqui nesse caso o dept eh uma entidade monitorada pelo jpa

E então o metodo completo ficaria assim:
public PersonDepartmentDTO insert(PersonDepartmentDTO dto){
    Person entity = new Person();
    entity.setName(dto.getName());
    entity.setSalary(dto.getSalary());

    // Buscar do banco em vez de criar manualmente
    Department dept = departmentRepository.findById(dto.getDepartment().getId())
            .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

    entity.setDepartment(dept);
    entity = repository.save(entity);

    return new PersonDepartmentDTO(entity);
}

Conclusão
O erro foi:
    Esperar que o nome do departamento fosse preenchido mesmo sem estar no JSON
    Criar um Department na mão, que não é o mesmo que está no banco (e o JPA não tem como saber disso sozinho)
    O certo é buscar o departamento no banco pelo ID e associá-lo ao Person.

-----------------------------------------------------------------------------------------

O que é uma entidade gerenciada pelo JPA?
Uma entidade gerenciada é aquela que está sendo controlada/monitorada pelo EntityManager do JPA. Ou seja, ela:
    Foi buscada do banco de dados (findById, getReferenceById, etc)
    Ou foi persistida e sincronizada (save, persist, etc)

Essas entidades estão dentro do "contexto de persistência" do JPA. Isso significa que:
    Se você modificar os dados dessa entidade, o JPA detecta a mudança e propaga para o banco automaticamente (flush).
    O JPA entende os relacionamentos corretamente.
    O JPA consegue gerar SQL correto quando você salvar ou atualizar outra entidade que referencia ela

O que é uma entidade transiente?
Uma entidade transiente é um objeto comum do Java. Ela não foi carregada do banco, nem está sendo controlada pelo JPA.
Ela é só um objeto instanciado com new, como nesse trecho seu:
    Department dept = new Department(); // isso é uma entidade transiente
    dept.setId(dto.getDepartment().getId());
    entity.setDepartment(dept);

Nesse caso acima, dept é uma entidade transiente porque:
    Você criou com new
    Ela não foi persistida (salva com save)
    Ela não foi carregada do banco
    O EntityManager não está ciente dela

E aí o JPA fica meio “perdido” — ele sabe que você está tentando associar algo,
mas como não conhece esse objeto Department, pode gerar SQL incompleto ou até nem gerar o join corretamente.

O JPA não sabe que esse Department existe no banco. Como o JPA não
reconhece essa entidade como gerenciada, ele pode:
    Tentar inserir um novo Department (e falhar por PK duplicada)
    Salvar um dado com um id de departamento que nao existe
    Salvar com campos nulos, pois não conhece os detalhes dessa entidade
Por isso, mesmo o id sendo 1 (de algo que existe no banco), o nome não veio junto,
porque o JPA não buscou o department real — você só passou uma “casca” de entidade com id e name = null.

Solução:
@Autowired
private DepartmentRepository departmentRepository;

...

// Buscar o departamento real do banco
Department dept = departmentRepository.findById(dto.getDepartment().getId())
        .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

entity.setDepartment(dept); //aqui nesse caso o dept eh uma entidade monitorada pelo jpa

Neste caso dept é uma entidade gerenciada pelo JPA, porque:

    Foi carregada do banco de dados via findById()
    Está associada ao contexto de persistência do EntityManager
    Está apta para fazer parte de relacionamentos, sem risco de inconsistência

O que acontece nos bastidores:
1. O metodo findById() do Spring Data JPA usa o EntityManager por trás para buscar a entidade.
2. Ao buscar do banco, o Department que ele retorna é um objeto do tipo Department, mas com um detalhe especial:
    Ele está vinculado ao contexto de persistência atual do JPA.
    Isso significa que qualquer mudança que você fizer nesse objeto será rastreada pelo JPA.
3. Esse dept é uma entidade gerenciada (ou seja, "monitorada").

E quando você faz isso acima e logo apos:
entity.setDepartment(dept);
Você está associando à sua entidade Person um Department que o JPA conhece,
que tem todos os dados carregados corretamente (id, name, etc), e que vai ser tratado corretamente
nas operações de persistência, como:

    persist(entity)
    merge(entity)
    flush()
    Geração de SQL de relacionamento (JOIN, FOREIGN KEY, etc)

Como testar se dept está sendo monitorado?
@PersistenceContext
private EntityManager em;

...

System.out.println(em.contains(dept)); // true se dept é gerenciado
Se retornar true, o JPA está "de olho" nesse objeto. Se for um new Department(), isso daria false.

Como eu faco pra associar uma entidade a um contexto de persistencia do EntityManager?
 Associar uma entidade ao contexto de persistência do EntityManager significa fazer com que
 ela seja "gerenciada" pelo JPA — ou seja, que o EntityManager esteja rastreando suas alterações
 para sincronizá-las automaticamente com o banco de dados.

 1. Buscando do banco (find, findById, getReferenceById)
     Department dept = entityManager.find(Department.class, 1L);
     // ou usando Spring Data JPA
     Department dept = departmentRepository.findById(1L).get();

     Isso associa automaticamente dept ao contexto de persistência.
 2. Salvando a entidade (persist ou save)
    entityManager.persist(dept); // agora o JPA começa a monitorar dept
 Ou com Spring Data JPA:
    departmentRepository.save(dept);
    Se o dept era uma entidade nova (id == null), ela vira gerenciada após persist() ou save().

 3. Etc

 Importante: O que não associa automaticamente?
    Department dept = new Department();
    dept.setId(1L);

  Isso não é gerenciado. É só um POJO (Plain Old Java Object). Eh somente um transient
  O JPA não sabe nada sobre ele até que você use merge, persist ou o busque com find.
*/
