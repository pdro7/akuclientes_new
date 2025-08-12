package com.akumaya.clientes24.service;

import com.akumaya.clientes24.domain.Cliente;
import com.akumaya.clientes24.repo.ClienteRepository;
import com.akumaya.clientes24.search.ClienteDoc;
import com.akumaya.clientes24.search.ClienteSearchRepo;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class ImportService {
    private final ClienteRepository repo;
    private final ClienteSearchRepo searchRepo;

    public ImportService(ClienteRepository repo, ClienteSearchRepo searchRepo) {
        this.repo = repo; this.searchRepo = searchRepo;
    }

    @Transactional
    public int importarDesdeExcel(InputStream excel) throws Exception {
        Workbook wb = WorkbookFactory.create(excel);
        Sheet sheet = wb.getSheetAt(0);
        int rows = 0;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i); if (r == null) continue;

            Cliente c = new Cliente();
            // Columna 0: fecha/hora
            OffsetDateTime odt = (r.getCell(0) != null && r.getCell(0).getCellType() == CellType.NUMERIC)
                    ? r.getCell(0).getLocalDateTimeCellValue().atOffset(ZoneOffset.UTC)
                    : OffsetDateTime.now(ZoneOffset.UTC);
            c.setHoraRegistro(odt);

            c.setNombreTutor(getString(r,1));
            c.setCiudad(getString(r,2));
            c.setDepartamento(getString(r,3));
            c.setNombreHijo(getString(r,4));
            c.setEdadHijo(parseIntSafe(r.getCell(5)));
            c.setComoNosConocio(getString(r,6));
            String news = String.valueOf(getString(r,7)).toLowerCase();
            c.setAceptaNewsletter(news.contains("si") || news.contains("sí") || news.contains("yes"));

            repo.save(c);

            ClienteDoc d = new ClienteDoc();
            d.setId(c.getId());
            d.setHoraRegistro(c.getHoraRegistro());
            d.setNombreTutor(c.getNombreTutor());
            d.setCiudad(c.getCiudad());
            d.setDepartamento(c.getDepartamento());
            d.setNombreHijo(c.getNombreHijo());
            d.setEdadHijo(c.getEdadHijo());
            d.setComoNosConocio(c.getComoNosConocio());
            d.setAceptaNewsletter(c.isAceptaNewsletter());
            searchRepo.save(d);

            rows++;
        }
        return rows;
    }

    private static String getString(Row r, int idx) {
        Cell cell = r.getCell(idx); if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
    private static Integer parseIntSafe(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int)cell.getNumericCellValue();
                case STRING -> Integer.parseInt(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (Exception e) { return null; }
    }
}
