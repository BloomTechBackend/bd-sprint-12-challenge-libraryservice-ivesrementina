package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.models.Checkout;
import com.bloomtech.library.models.Library;
import com.bloomtech.library.models.LibraryCard;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import com.bloomtech.library.repositories.LibraryRepository;
import com.fasterxml.jackson.module.afterburner.util.ClassName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks
    @Autowired
    private CheckableService checkableService;

    @MockBean
    private CheckableRepository checkableRepository;
    private List<Checkable> checkables;
    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }
    //TODO: Write Unit Tests for all CheckableService methods and possible Exceptions
    @Test
    void getAll() {
        Mockito.when(checkableRepository.findAll()).thenReturn(checkables);
        List<Checkable> checkables = checkableService.getAll();
        assertEquals(8, checkables.size());
    }
    @Test
    void getByIsbn_findsExistingISBN() {
        Mockito.when(checkableRepository.findByIsbn(any(String.class))).thenReturn(Optional.of(checkables.get(0)));
        Checkable checkable = checkableService.getByIsbn("1-0");
        assertEquals("1-0", checkable.getIsbn());
    }

    @Test
    void getByIsbn_nonISBN() {
        Mockito.when(checkableRepository.findByIsbn(any(String.class))).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, ()->{
            checkableService.getByIsbn("Non-Existent Checkables");
        });
    }

    @Test
    void getByType_findsExistingType() {
        Mockito.when(checkableRepository.findByType(any(Class.class))).thenReturn(Optional.of(checkables.get(0)));
        Checkable checkable = checkableService.getByType(ClassName.class);
        assertEquals("1-0", checkable.getIsbn());
    }

    @Test
    void getByType_nonType() {
        Mockito.when(checkableRepository.findByType(any(Class.class))).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, ()->{
            checkableService.getByType(ClassName.class);
        });
    }

    @Test
    void save() {
        when(checkableRepository.findAll()).thenReturn(checkables);
        checkableService.save(new Checkable("New ISBN", "New Title"));
        Mockito.verify(checkableRepository).save(any(Checkable.class));
    }

    @Test
    void save_existingISBN_throwsResourceExistsException() {
        when(checkableRepository.findAll()).thenReturn(checkables);
        assertThrows(ResourceExistsException.class, ()->{
            checkableService.save(new Checkable("1-0", "The White Whale"));
        });
        verify(checkableRepository, never()).save(any(Checkable.class));
    }
}