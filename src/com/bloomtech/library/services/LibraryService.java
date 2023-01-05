package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    //TODO: Implement behavior described by the unit tests in tst.com.bloomtech.library.services.LibraryService

    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private CheckableService checkableService;
    private LibraryService libraryService;

    public List<Library> getLibraries() {
        List<Library> libraries = libraryRepository.findAll();
        return libraries;
    }

    public Library getLibraryByName(String name) throws LibraryNotFoundException {
        Optional<Library> library = libraryRepository.findByName(name);
        if (library.isEmpty()) {
            throw new LibraryNotFoundException("Library did not Exist!");
        }
        return library.get();
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        Library library = getLibraryByName(libraryName);
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);
        List<CheckableAmount> checkableAmountList = library.getCheckables();
        for (CheckableAmount checkableAmount : checkableAmountList){
            if (checkable.equals(checkableAmount.getCheckable())){
                return checkableAmount;
            }  else {
                return new CheckableAmount(checkable, 0);
            }
        }
        return new CheckableAmount(checkable, 2);
    }
    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        List<LibraryAvailableCheckouts> available = new ArrayList<>();
        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> libraries = libraryRepository.findAll();
        for(Library library : libraries){
            for (CheckableAmount checkableAmount : library.getCheckables()){
                checkableAmount.getCheckable();
                if (checkable.equals(checkableAmount.getCheckable())){
                    available.add(new LibraryAvailableCheckouts(checkableAmount.getAmount(), library.getName()));
                }
            }
        }
        return available;
    }
    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();
        Library library = getLibraryByName(libraryName);
        Set<LibraryCard> libraryCards = library.getLibraryCards();
        LocalDateTime now = LocalDateTime.now();
        for(LibraryCard libraryCard: libraryCards) {
            List<Checkout> checkoutList = libraryCard.getCheckouts();
            for (Checkout checkout : checkoutList){
                if (now.isAfter(checkout.getDueDate())) {
                    overdueCheckouts.add(new OverdueCheckout(libraryCard.getPatron(),checkout));
                }
            }
        }
        return overdueCheckouts;
    }
}
