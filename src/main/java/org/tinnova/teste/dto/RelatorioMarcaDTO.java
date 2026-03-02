package org.tinnova.teste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioMarcaDTO {
    private String marca;
    private Long quantidade;
}