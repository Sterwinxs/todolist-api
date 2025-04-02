package br.com.gfdcastro.todolist.task;

import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gfdcastro.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/tasks")
public class TaskCrontoller {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    System.out.println("Chegou no controller"+request.getAttribute("idUser"));
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser(java.util.UUID.fromString(idUser.toString()));

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio / fim não deve ser maior que a data atual");
    }

    if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio não deve ser maior que a data de fim");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
      var idUser = java.util.UUID.fromString(request.getAttribute("idUser").toString());
      var tasks = this.taskRepository.findByIdUser(idUser);
      return tasks;
  }

  @PutMapping("/{id}")
  public TaskModel update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var idUser = UUID.fromString(request.getAttribute("idUser").toString());

    // Busca a tarefa existente no banco de dados
    var existingTask = this.taskRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

    // Garante que apenas o dono da tarefa pode atualizá-la
    if (!existingTask.getIdUser().equals(idUser)) {
        throw new RuntimeException("Usuário não autorizado para esta tarefa");
    }

    var task = this.taskRepository.findById(id).orElse(null);

    Utils.copyNonNullProperties(taskModel, task);

    // Mantém o ID original e o idUser correto
    taskModel.setId(id);
    taskModel.setIdUser(idUser);

    return this.taskRepository.save(task);
  }
  
}