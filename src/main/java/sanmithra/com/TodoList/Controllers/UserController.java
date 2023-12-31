package sanmithra.com.TodoList.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import sanmithra.com.TodoList.Entity.Login;
import sanmithra.com.TodoList.Entity.TodoList;
import sanmithra.com.TodoList.Entity.Users;
import sanmithra.com.TodoList.Repository.TodoListRepository;
import sanmithra.com.TodoList.Repository.UsersRepository;
import sanmithra.com.TodoList.Services.*;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/users")
public class UserController {
@Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private CaptchaValidator validator;

    @GetMapping("/{userId}")
    public Users getUserById(@PathVariable Integer userId){
        return usersRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
    }

    @PostMapping("/adduser")
    public Users addUser(@RequestBody Users users,@RequestParam("g-recaptcha-response")String captcha) throws UsernameAlreadyExistsException {
        Users existingUser = usersRepository.findByUserName(users.getUserName());
        if (existingUser != null) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        else if(validator.isValid(captcha)) {
            users.setUserName(users.getUserName());
            users.setUserPassword(users.getUserPassword());
            return usersRepository.save(users);
        }
        else {
            throw new CaptchaValidationException("CAPTCHA validation failed");
        }
    }
    @PostMapping("/login")
    public Users loginUser(@RequestBody Login login,@RequestParam("g-recaptcha-response")String captcha) {
        Users user = usersRepository.findByUserName(login.getUserName());
        if (validator.isValid(captcha)) {
            if (user != null && user.getUserPassword().equals(login.getUserPassword())) {
                // Successful login
                //return ResponseEntity.ok("Login successful");
                return user;
            } else {
                //return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
                throw new RuntimeException();
            }
        }
        else {
            throw new CaptchaValidationException("CAPTCHA validation failed");
        }
    }


    @PostMapping("/{userId}/todos")
    public void addTodo(@PathVariable Integer userId, @RequestBody TodoList todoList){
        Users user = usersRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        TodoList todo = new TodoList();
        todo.setTasks(todoList.getTasks());
        user.getTodoList().add(todo);
        usersRepository.save(user);
    }

    @PostMapping("/todos/{todoId}")
    public void toggleTodoCompleted( @PathVariable Integer todoId){
        TodoList todoList = todoListRepository.findById(todoId).orElseThrow(() -> new NoSuchElementException());
        todoList.setCompleted(!todoList.getCompleted());
        todoListRepository.save(todoList);
    }


    @DeleteMapping("{userId}/todos/{todoId}")
    public void deleteTodo(@PathVariable Integer userId, @PathVariable Integer todoId) {
        Users user = usersRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        TodoList todo = todoListRepository.findById(todoId).orElseThrow(() -> new NoSuchElementException());
        user.getTodoList().remove(todo);
        todoListRepository.delete(todo);
    }
    @PutMapping("/{userId}/todos/{todoId}")
    public TodoList editTodo(
            @PathVariable Integer userId,
            @PathVariable Integer todoId,
            @RequestBody TodoList updatedTodo
    ) {
        Users user = usersRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        TodoList todo = todoListRepository.findById(todoId).orElseThrow(() -> new NoSuchElementException());

        // Update the task's details with the provided data
        todo.setTasks(updatedTodo.getTasks());
        todo.setCompleted(updatedTodo.getCompleted());

        // Save the updated task
        todoListRepository.save(todo);

        return todo;
    }



    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId){
        Users users = usersRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        usersRepository.delete(users);
    }
    @PutMapping("/users/{userId}/todos/{taskId}")
    public ResponseEntity<TodoList> updateTaskStatus(
            @PathVariable("userId") int userId,
            @PathVariable("taskId") int taskId,
            @RequestBody TodoList updatedTask) {
        // Check if the task belongs to the specified user (you should have a way to verify this)

        Optional<TodoList> taskOptional = todoListRepository.findById(taskId);

        if (taskOptional.isPresent()) {
            TodoList existingTask = taskOptional.get();
            existingTask.setCompleted(updatedTask.isCompleted());
            todoListRepository.save(existingTask);

            return ResponseEntity.ok(existingTask);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}

//    @PostMapping("/login")
//    public String loginUser(@RequestBody Login login) {
//        Users temp = usersRepository.findByUserName(login.getUserName());
//        Users temp2 = usersRepository.findByUserPassword(login.getUserPassword());
//        if (temp.getUserPassword() == temp2.getUserPassword()) {
//            return "success";
//        } else {
//            return "failed";
//        }
//    }
