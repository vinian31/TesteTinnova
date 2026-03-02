package org.tinnova.teste.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.tinnova.teste.dto.VeiculoDTO;
import org.tinnova.teste.entity.Veiculo;
import org.tinnova.teste.exception.PlacaDuplicadaException;
import org.tinnova.teste.exception.VeiculoNaoEncontradoException;
import org.tinnova.teste.repository.VeiculoRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço VeiculoService")
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private DolarService dolarService;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;
    private Veiculo veiculoDois;
    private VeiculoDTO veiculoDTO;

    @BeforeEach
    void setUp() {
        veiculo = Veiculo.builder()
                .id(1L)
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2023)
                .placa("ABC1234")
                .cor("Preto")
                .preco(new BigDecimal("50000.00"))
                .ativo(true)
                .build();

        veiculoDois = Veiculo.builder()
                .id(2L)
                .marca("Honda")
                .modelo("Civic")
                .ano(2022)
                .placa("XYZ5678")
                .cor("Branco")
                .preco(new BigDecimal("45000.00"))
                .ativo(true)
                .build();

        veiculoDTO = VeiculoDTO.builder()
                .id(1L)
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2023)
                .placa("ABC1234")
                .cor("Preto")
                .preco(new BigDecimal("50000.00"))
                .ativo(true)
                .build();

        // Mock lenient da taxa de dólar - usado quando resultado é convertido em DTO
        lenient().when(dolarService.obterTaxaDolar()).thenReturn(new BigDecimal("5.00"));
    }


    @Nested
    @DisplayName("Testes de Criação de Veículos")
    class TestsCriarVeiculo {

        @Test
        @DisplayName("Deve criar um veículo com sucesso quando a placa não existe")
        void testCriarVeiculo() {
            when(veiculoRepository.findByPlacaAndAtivoTrue("ABC1234")).thenReturn(Optional.empty());
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

            VeiculoDTO resultado = veiculoService.criar(veiculoDTO);

            assertNotNull(resultado);
            assertEquals("ABC1234", resultado.getPlaca());
            assertEquals("Toyota", resultado.getMarca());
            verify(veiculoRepository, times(1)).findByPlacaAndAtivoTrue("ABC1234");
            verify(veiculoRepository, times(1)).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve lançar PlacaDuplicadaException quando a placa já existe")
        void testCriarVeiculoComPlacaDuplicada() {
            when(veiculoRepository.findByPlacaAndAtivoTrue("ABC1234")).thenReturn(Optional.of(veiculo));

            PlacaDuplicadaException exception = assertThrows(
                    PlacaDuplicadaException.class,
                    () -> veiculoService.criar(veiculoDTO)
            );

            assertTrue(exception.getMessage().contains("ABC1234"));
            verify(veiculoRepository, times(1)).findByPlacaAndAtivoTrue("ABC1234");
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve validar duplicidade de placa mesmo com soft delete")
        void testValidarDuplicidadePlacaSoftDelete() {
            when(veiculoRepository.findByPlacaAndAtivoTrue("ABC1234")).thenReturn(Optional.empty());
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

            // Deve permitir criar pois verifica apenas ativos
            VeiculoDTO resultado = veiculoService.criar(veiculoDTO);

            assertNotNull(resultado);
            verify(veiculoRepository, times(1)).findByPlacaAndAtivoTrue("ABC1234");
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Veículos")
    class TestsAtualizarVeiculo {

        @Test
        @DisplayName("Deve atualizar um veículo com sucesso via PUT")
        void testAtualizarVeiculoPut() {
            VeiculoDTO atualizacao = VeiculoDTO.builder()
                    .marca("Honda")
                    .modelo("Civic")
                    .ano(2024)
                    .placa("NEW1234")
                    .cor("Branco")
                    .preco(new BigDecimal("55000.00"))
                    .build();

            Veiculo veiculoOriginal = veiculo;
            Veiculo veiculoAtualizado = Veiculo.builder()
                    .id(veiculoOriginal.getId())
                    .marca("Honda")
                    .modelo("Civic")
                    .ano(2024)
                    .placa("NEW1234")
                    .cor("Branco")
                    .preco(new BigDecimal("55000.00"))
                    .ativo(true)
                    .build();

            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.findByPlacaAndAtivoTrue("NEW1234")).thenReturn(Optional.empty());
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoAtualizado);

            VeiculoDTO resultado = veiculoService.atualizar(1L, atualizacao);

            assertNotNull(resultado);
            assertEquals("Honda", resultado.getMarca());
            assertEquals("NEW1234", resultado.getPlaca());
            verify(veiculoRepository, times(1)).findByIdAndAtivoTrue(1L);
            verify(veiculoRepository, times(1)).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve falhar ao atualizar com placa duplicada via PUT")
        void testAtualizarVeiculoComPlacaDuplicadaPut() {
            VeiculoDTO atualizacao = VeiculoDTO.builder()
                    .marca("Honda")
                    .modelo("Civic")
                    .ano(2024)
                    .placa("XYZ5678") // Placa do veiculoDois
                    .cor("Branco")
                    .preco(new BigDecimal("55000.00"))
                    .build();

            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.findByPlacaAndAtivoTrue("XYZ5678")).thenReturn(Optional.of(veiculoDois));

            PlacaDuplicadaException exception = assertThrows(
                    PlacaDuplicadaException.class,
                    () -> veiculoService.atualizar(1L, atualizacao)
            );

            assertTrue(exception.getMessage().contains("XYZ5678"));
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve permitir manter a mesma placa no PUT")
        void testAtualizarVeiculoComMesmaPlaca() {
            VeiculoDTO atualizacao = VeiculoDTO.builder()
                    .marca("Toyota")
                    .modelo("Corolla")
                    .ano(2024)
                    .placa("ABC1234") // Mesma placa
                    .cor("Branco")
                    .preco(new BigDecimal("52000.00"))
                    .build();

            Veiculo veiculoAtualizado = Veiculo.builder()
                    .id(veiculo.getId())
                    .marca("Toyota")
                    .modelo("Corolla")
                    .ano(2024)
                    .placa("ABC1234")
                    .cor("Branco")
                    .preco(new BigDecimal("52000.00"))
                    .ativo(true)
                    .build();

            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoAtualizado);

            VeiculoDTO resultado = veiculoService.atualizar(1L, atualizacao);

            assertNotNull(resultado);
            assertEquals("ABC1234", resultado.getPlaca());
            verify(veiculoRepository, times(1)).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve falhar ao atualizar um veículo inexistente via PUT")
        void testAtualizarVeiculoInexistentePut() {
            when(veiculoRepository.findByIdAndAtivoTrue(999L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException exception = assertThrows(
                    VeiculoNaoEncontradoException.class,
                    () -> veiculoService.atualizar(999L, veiculoDTO)
            );

            assertTrue(exception.getMessage().contains("999"));
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve atualizar parcialmente um veículo via PATCH")
        void testAtualizarParcialVeiculo() {
            VeiculoDTO atualizacao = VeiculoDTO.builder()
                    .cor("Azul")
                    .preco(new BigDecimal("52000.00"))
                    .build();

            Veiculo veiculoAtualizado = Veiculo.builder()
                    .id(veiculo.getId())
                    .marca(veiculo.getMarca())
                    .modelo(veiculo.getModelo())
                    .ano(veiculo.getAno())
                    .placa(veiculo.getPlaca())
                    .cor("Azul")
                    .preco(new BigDecimal("52000.00"))
                    .ativo(true)
                    .build();

            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoAtualizado);

            VeiculoDTO resultado = veiculoService.atualizarParcial(1L, atualizacao);

            assertNotNull(resultado);
            assertEquals("Azul", resultado.getCor());
            verify(veiculoRepository, times(1)).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve falhar ao atualizar parcialmente com placa duplicada via PATCH")
        void testAtualizarParcialVeiculoComPlacaDuplicadaPatch() {
            VeiculoDTO atualizacao = VeiculoDTO.builder()
                    .placa("XYZ5678") // Placa do veiculoDois
                    .build();

            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.findByPlacaAndAtivoTrue("XYZ5678")).thenReturn(Optional.of(veiculoDois));

            PlacaDuplicadaException exception = assertThrows(
                    PlacaDuplicadaException.class,
                    () -> veiculoService.atualizarParcial(1L, atualizacao)
            );

            assertTrue(exception.getMessage().contains("XYZ5678"));
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve falhar ao atualizar parcialmente um veículo inexistente via PATCH")
        void testAtualizarParcialVeiculoInexistentePatch() {
            when(veiculoRepository.findByIdAndAtivoTrue(999L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException exception = assertThrows(
                    VeiculoNaoEncontradoException.class,
                    () -> veiculoService.atualizarParcial(999L, veiculoDTO)
            );

            assertTrue(exception.getMessage().contains("999"));
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }
    }

    @Nested
    @DisplayName("Testes de Listagem e Filtros")
    class TestsListagemeFiltros {

        @Test
        @DisplayName("Deve listar todos os veículos com paginação")
        void testListarTodos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo, veiculoDois), pageable, 2);

            when(veiculoRepository.findByAtivoTrue(pageable)).thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.listarTodos(pageable);

            assertNotNull(resultado);
            assertEquals(2, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findByAtivoTrue(pageable);
        }

        @Test
        @DisplayName("Deve filtrar veículos por marca")
        void testFiltrarPorMarca() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo), pageable, 1);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar("Toyota", null, null, null, null, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar veículos por ano")
        void testFiltrarPorAno() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo), pageable, 1);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar(null, 2023, null, null, null, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar veículos por cor")
        void testFiltrarPorCor() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo), pageable, 1);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar(null, null, "Preto", null, null, pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar veículos por range de preço")
        void testFiltrarPorRangePreco() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo), pageable, 1);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar(
                    null, null, null,
                    new BigDecimal("40000"),
                    new BigDecimal("60000"),
                    pageable
            );

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar veículos com múltiplos critérios combinados")
        void testFiltrarComCritériosCombinados() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(veiculo), pageable, 1);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar(
                    "Toyota", 2023, "Preto",
                    new BigDecimal("40000"),
                    new BigDecimal("60000"),
                    pageable
            );

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando nenhum veículo corresponde aos filtros")
        void testFiltrarSemResultados() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> page = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);

            Page<VeiculoDTO> resultado = veiculoService.filtrar(
                    "MarcaInexistente", null, null, null, null, pageable
            );

            assertNotNull(resultado);
            assertEquals(0, resultado.getTotalElements());
            verify(veiculoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Testes de Busca por ID")
    class TestsBuscaPorID {

        @Test
        @DisplayName("Deve obter um veículo por ID quando ele existe")
        void testObterPorIdEncontrado() {
            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));

            VeiculoDTO resultado = veiculoService.obterPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("ABC1234", resultado.getPlaca());
            verify(veiculoRepository, times(1)).findByIdAndAtivoTrue(1L);
        }

        @Test
        @DisplayName("Deve lançar VeiculoNaoEncontradoException quando ID não existe")
        void testObterPorIdNaoEncontrado() {
            when(veiculoRepository.findByIdAndAtivoTrue(999L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException exception = assertThrows(
                    VeiculoNaoEncontradoException.class,
                    () -> veiculoService.obterPorId(999L)
            );

            assertTrue(exception.getMessage().contains("999"));
            verify(veiculoRepository, times(1)).findByIdAndAtivoTrue(999L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar um veículo inativo")
        void testObterPorIdVeiculoInativo() {
            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException exception = assertThrows(
                    VeiculoNaoEncontradoException.class,
                    () -> veiculoService.obterPorId(1L)
            );

            assertTrue(exception.getMessage().contains("1"));
            verify(veiculoRepository, times(1)).findByIdAndAtivoTrue(1L);
        }
    }

    @Nested
    @DisplayName("Testes de Deleção")
    class TestsDeletarVeiculo {

        @Test
        @DisplayName("Deve deletar (soft delete) um veículo com sucesso")
        void testDeletarVeiculo() {
            when(veiculoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculo));
            when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

            veiculoService.deletar(1L);

            verify(veiculoRepository, times(1)).findByIdAndAtivoTrue(1L);
            verify(veiculoRepository, times(1)).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("Deve falhar ao deletar um veículo inexistente")
        void testDeletarVeiculoInexistente() {
            when(veiculoRepository.findByIdAndAtivoTrue(999L)).thenReturn(Optional.empty());

            VeiculoNaoEncontradoException exception = assertThrows(
                    VeiculoNaoEncontradoException.class,
                    () -> veiculoService.deletar(999L)
            );

            assertTrue(exception.getMessage().contains("999"));
            verify(veiculoRepository, never()).save(any(Veiculo.class));
        }
    }
}

