package org.tinnova.teste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tinnova.teste.dto.RelatorioMarcaDTO;
import org.tinnova.teste.dto.VeiculoDTO;
import org.tinnova.teste.service.VeiculoService;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @GetMapping
    @Operation(summary = "Listar veículos", description = "Lista todos os veículos com filtros opcionais")
    public ResponseEntity<Page<VeiculoDTO>> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) BigDecimal minPreco,
            @RequestParam(required = false) BigDecimal maxPreco,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("GET /veiculos - Listando veículos com filtros");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<VeiculoDTO> veiculos;
        if (marca != null || ano != null || cor != null || minPreco != null || maxPreco != null) {
            veiculos = veiculoService.filtrar(marca, ano, cor, minPreco, maxPreco, pageable);
        } else {
            veiculos = veiculoService.listarTodos(pageable);
        }

        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter veículo por ID", description = "Obtém os detalhes de um veículo específico pelo ID")
    public ResponseEntity<VeiculoDTO> obterPorId(@PathVariable Long id) {
        log.info("GET /veiculos/{} - Obtendo detalhes do veículo", id);
        VeiculoDTO veiculo = veiculoService.obterPorId(id);
        return ResponseEntity.ok(veiculo);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar veículo", description = "Cria um novo veículo")
    public ResponseEntity<VeiculoDTO> criar(@Valid @RequestBody VeiculoDTO dto) {
        log.info("POST /veiculos - Criando novo veículo: {}", dto.getPlaca());
        VeiculoDTO veiculo = veiculoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar veículo", description = "Atualiza os dados de um veículo existente")
    public ResponseEntity<VeiculoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoDTO dto) {
        log.info("PUT /veiculos/{} - Atualizando veículo", id);
        VeiculoDTO veiculo = veiculoService.atualizar(id, dto);
        return ResponseEntity.ok(veiculo);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar veículo parcialmente", description = "Atualiza parcialmente os dados de um veículo")
    public ResponseEntity<VeiculoDTO> atualizarParcial(@PathVariable Long id, @RequestBody VeiculoDTO dto) {
        log.info("PATCH /veiculos/{} - Atualizando parcialmente veículo", id);
        VeiculoDTO veiculo = veiculoService.atualizarParcial(id, dto);
        return ResponseEntity.ok(veiculo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar veículo", description = "Remove um veículo do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /veiculos/{} - Deletando veículo", id);
        veiculoService.deletar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/relatorios/por-marca")
    @Operation(summary = "Relatório por marca", description = "Obtém um relatório de veículos agrupados por marca")
    public ResponseEntity<List<RelatorioMarcaDTO>> obterRelatorioPorMarca() {
        log.info("GET /veiculos/relatorios/por-marca - Obtendo relatório");
        List<RelatorioMarcaDTO> relatorio = veiculoService.obterRelatorioPorMarca();
        return ResponseEntity.ok(relatorio);
    }
}
