package br.com.joselucas.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joselucas.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity createTask(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUSer((UUID) idUser);

    var currentData = LocalDateTime.now();
    // var initialDate = currentData.isAfter(taskModel.getStartAt());
    // var finalDate = currentData.isAfter(taskModel.getStartAt());
    if (currentData.isAfter(taskModel.getStartAt()) || currentData.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("As datas devem ser maior que a data atual!!");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de término deve ser maior que data atual!!");
    }

    var newTask = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(newTask);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var listTasks = this.taskRepository.findByIdUSer((UUID) idUser);
    return listTasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity updateList(@RequestBody TaskModel taskModel, @PathVariable UUID id,
      HttpServletRequest request) {
    var task = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Tarefa não encontrada!!");
    }

    var idUser = request.getAttribute("idUser");

    if(!task.getIdUSer().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Usuário não tem permissão para alterar esta tarefa!!"); 
    }

    Utils.copyNonNUllProperties(taskModel, task);
    var taskUpdate = this.taskRepository.save(task);
    return ResponseEntity.ok().body(taskUpdate);
  }

}
