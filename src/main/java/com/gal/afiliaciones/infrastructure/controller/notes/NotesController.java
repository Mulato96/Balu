package com.gal.afiliaciones.infrastructure.controller.notes;

import com.gal.afiliaciones.application.service.affiliate.NotesService;
import com.gal.afiliaciones.domain.model.Notes;
import com.gal.afiliaciones.infrastructure.dto.NotesDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/notes")
public class NotesController {

    private final NotesService notesService;

    @PostMapping("/saveNote")
    public ResponseEntity<NotesDTO> saveNote(@RequestBody Notes note){
        return ResponseEntity.ok().body(notesService.create(note));
    }

    @GetMapping("/findNoteByAffiliation/{filedNumber}")
    public ResponseEntity<List<NotesDTO>> findByAffiliation(@PathVariable String filedNumber){
        return ResponseEntity.ok().body(notesService.findByAffiliation(filedNumber));
    }

}
