package com.akumaya.clientes24.web;

import com.akumaya.clientes24.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @PostMapping("/excel")
    public ResponseEntity<String> importar(@RequestParam("file") MultipartFile file) throws Exception {
        int count = service.importarDesdeExcel(file.getInputStream());
        return ResponseEntity.ok("Registros importados: " + count);
    }
}
