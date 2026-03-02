package org.tinnova.teste.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.tinnova.teste.entity.Veiculo;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {
    Optional<Veiculo> findByPlacaAndAtivoTrue(String placa);

    Optional<Veiculo> findByIdAndAtivoTrue(Long id);

    Page<Veiculo> findByAtivoTrue(Pageable pageable);
}

