package com.softserve.itacademy.service;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.repository.ToDoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final ToDoRepository toDoRepository;

    public boolean isTodoOwnerOrCollaborator(long todoId, long userId) {
        ToDo todo = toDoRepository.findById(todoId)
                .orElseThrow(() -> new EntityNotFoundException("ToDo with id " + todoId + " not found"));
        return todo.getOwner().getId() == userId || 
               todo.getCollaborators().stream().anyMatch(c -> c.getId() == userId);
    }
}
