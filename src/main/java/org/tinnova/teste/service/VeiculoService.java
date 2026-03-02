package org.tinnova.teste.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tinnova.teste.dto.RelatorioMarcaDTO;
import org.tinnova.teste.dto.VeiculoDTO;
import org.tinnova.teste.entity.Veiculo;
import org.tinnova.teste.exception.PlacaDuplicadaException;
import org.tinnova.teste.exception.VeiculoNaoEncontradoException;
import org.tinnova.teste.repository.VeiculoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private DolarService dolarService;

    @Transactional(readOnly = true)
    public Page<VeiculoDTO> listarTodos(Pageable pageable) {
        log.info("Listando todos os veículos com paginação: {}", pageable);
        return veiculoRepository.findByAtivoTrue(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<VeiculoDTO> filtrar(String marca, Integer ano, String cor, BigDecimal minPreco,
                                     BigDecimal maxPreco, Pageable pageable) {
        log.info("Filtrando veículos: marca={}, ano={}, cor={}, minPreco={}, maxPreco={}",
                 marca, ano, cor, minPreco, maxPreco);

        Specification<Veiculo> spec = Specification.where((root, query, cb) ->
            cb.equal(root.get("ativo"), true));

        if (marca != null && !marca.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("marca")),
                    "%" + marca.toLowerCase() + "%"));
        }

        if (ano != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ano"), ano));
        }

        if (cor != null && !cor.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("cor")),
                    "%" + cor.toLowerCase() + "%"));
        }

        if (minPreco != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("preco"), minPreco));
        }

        if (maxPreco != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("preco"), maxPreco));
        }

        return veiculoRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public VeiculoDTO obterPorId(Long id) {
        log.info("Buscando veículo com ID: {}", id);
        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado com ID: " + id));
        return toDTO(veiculo);
    }

    @Transactional
    public VeiculoDTO criar(VeiculoDTO dto) {
        log.info("Criando novo veículo: {} - Preço em USD: ${}", dto.getPlaca(), dto.getPreco());

        if (veiculoRepository.findByPlacaAndAtivoTrue(dto.getPlaca()).isPresent()) {
            throw new PlacaDuplicadaException("Já existe um veículo com a placa: " + dto.getPlaca());
        }

        Veiculo veiculo = toEntity(dto);
        veiculo.setAtivo(true);

        veiculo = veiculoRepository.save(veiculo);

        log.info("Veículo criado com sucesso: ID={} - Preço em USD: ${}", veiculo.getId(), veiculo.getPreco());
        return toDTO(veiculo);
    }

    @Transactional
    public VeiculoDTO atualizar(Long id, VeiculoDTO dto) {
        log.info("Atualizando veículo: ID={} - Novo preço em USD: ${}", id, dto.getPreco());

        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado com ID: " + id));

        if (!veiculo.getPlaca().equals(dto.getPlaca()) &&
            veiculoRepository.findByPlacaAndAtivoTrue(dto.getPlaca()).isPresent()) {
            throw new PlacaDuplicadaException("Já existe um veículo com a placa: " + dto.getPlaca());
        }

        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAno(dto.getAno());
        veiculo.setPlaca(dto.getPlaca());
        veiculo.setCor(dto.getCor());

        veiculo.setPreco(dto.getPreco());

        veiculo = veiculoRepository.save(veiculo);

        log.info("Veículo atualizado com sucesso: ID={} - Preço em USD: ${}", veiculo.getId(), veiculo.getPreco());
        return toDTO(veiculo);
    }

    @Transactional
    public VeiculoDTO atualizarParcial(Long id, VeiculoDTO dto) {
        log.info("Atualizando parcialmente veículo: ID={}", id);

        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado com ID: " + id));

        if (dto.getMarca() != null) {
            veiculo.setMarca(dto.getMarca());
        }
        if (dto.getModelo() != null) {
            veiculo.setModelo(dto.getModelo());
        }
        if (dto.getAno() != null) {
            veiculo.setAno(dto.getAno());
        }
        if (dto.getPlaca() != null) {
            if (!veiculo.getPlaca().equals(dto.getPlaca()) &&
                veiculoRepository.findByPlacaAndAtivoTrue(dto.getPlaca()).isPresent()) {
                throw new PlacaDuplicadaException("Já existe um veículo com a placa: " + dto.getPlaca());
            }
            veiculo.setPlaca(dto.getPlaca());
        }
        if (dto.getCor() != null) {
            veiculo.setCor(dto.getCor());
        }
        if (dto.getPreco() != null) {
            veiculo.setPreco(dto.getPreco());
            log.info("Preço atualizado para ${}", dto.getPreco());
        }

        veiculo = veiculoRepository.save(veiculo);

        log.info("Veículo atualizado parcialmente com sucesso: ID={}", veiculo.getId());
        return toDTO(veiculo);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando veículo (soft delete): ID={}", id);

        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado com ID: " + id));

        veiculo.setAtivo(false);
        veiculoRepository.save(veiculo);

        log.info("Veículo deletado com sucesso: ID={}", id);
    }

    @Transactional(readOnly = true)
    public List<RelatorioMarcaDTO> obterRelatorioPorMarca() {
        log.info("Gerando relatório de veículos por marca");

        return veiculoRepository.findAll().stream()
                .filter(Veiculo::getAtivo)
                .collect(Collectors.groupingBy(
                        Veiculo::getMarca,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> RelatorioMarcaDTO.builder()
                        .marca(entry.getKey())
                        .quantidade(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private VeiculoDTO toDTO(Veiculo veiculo) {
        BigDecimal taxaDolar = dolarService.obterTaxaDolar();
        BigDecimal precoEmReal = veiculo.getPreco().multiply(taxaDolar).setScale(2, RoundingMode.HALF_UP);

        String precoEmRealFormatado = String.format("R$ %,.2f", precoEmReal).replace(",", "_").replace(".", ",").replace("_", ".");

        return VeiculoDTO.builder()
                .id(veiculo.getId())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .placa(veiculo.getPlaca())
                .cor(veiculo.getCor())
                .preco(veiculo.getPreco())
                .precoEmReal(precoEmRealFormatado)
                .ativo(veiculo.getAtivo())
                .dataCriacao(veiculo.getDataCriacao())
                .dataAtualizacao(veiculo.getDataAtualizacao())
                .build();
    }

    private Veiculo toEntity(VeiculoDTO dto) {
        return Veiculo.builder()
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .ano(dto.getAno())
                .placa(dto.getPlaca())
                .cor(dto.getCor())
                .preco(dto.getPreco())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }
}

