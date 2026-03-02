package org.tinnova.teste.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tinnova.teste.entity.Usuario;
import org.tinnova.teste.entity.Veiculo;
import org.tinnova.teste.repository.UsuarioRepository;
import org.tinnova.teste.repository.VeiculoRepository;
import org.tinnova.teste.service.DolarService;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
@Configuration
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DolarService dolarService;

    @Bean
    public CommandLineRunner initializeUsers() {
        return args -> {
            log.info("🔄 Verificando usuários iniciais...");

            // Criar usuário ADMIN se não existir
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = Usuario.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@tinnova.com")
                        .ativo(true)
                        .roles(Arrays.asList("ADMIN", "USER"))
                        .build();

                usuarioRepository.save(admin);
                log.info("✅ Usuário ADMIN criado com sucesso!");
                log.info("   Username: admin");
                log.info("   Password: admin123");
            } else {
                log.info("ℹ️  Usuário ADMIN já existe");
            }

            // Criar usuário USER se não existir
            if (usuarioRepository.findByUsername("user").isEmpty()) {
                Usuario user = Usuario.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user123"))
                        .email("user@tinnova.com")
                        .ativo(true)
                        .roles(Arrays.asList("USER"))
                        .build();

                usuarioRepository.save(user);
                log.info("✅ Usuário USER criado com sucesso!");
                log.info("   Username: user");
                log.info("   Password: user123");
            } else {
                log.info("ℹ️  Usuário USER já existe");
            }

            log.info("🎉 Inicialização de dados concluída!");
        };
    }

    @Bean
    public CommandLineRunner initializeVehicles() {
        return args -> {
            log.info("🔄 Verificando veículos iniciais...");

            // Obter taxa de dólar uma vez para todas as conversões
            BigDecimal taxaDolar = dolarService.obterTaxaDolar();
            log.info("Taxa de câmbio USD/BRL: {}", taxaDolar);

            // Fiat Uno 2022 - Branco - R$ 35.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FIA2022A").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("35000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo fiat1 = Veiculo.builder()
                        .marca("Fiat")
                        .modelo("Uno")
                        .ano(2022)
                        .placa("FIA2022A")
                        .cor("Branco")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(fiat1);
                log.info("✅ Fiat Uno 2022 criado! R$ 35.000 = ${}", precoEmDolar);
            }

            // Fiat Palio 2020 - Preto - R$ 30.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FIA2020P").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("30000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo fiat2 = Veiculo.builder()
                        .marca("Fiat")
                        .modelo("Palio")
                        .ano(2020)
                        .placa("FIA2020P")
                        .cor("Preto")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(fiat2);
                log.info("✅ Fiat Palio 2020 criado! R$ 30.000 = ${}", precoEmDolar);
            }

            // Toyota Corolla 2023 - Prata - R$ 85.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("TOY2023A").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("85000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo toyota1 = Veiculo.builder()
                        .marca("Toyota")
                        .modelo("Corolla")
                        .ano(2023)
                        .placa("TOY2023A")
                        .cor("Prata")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(toyota1);
                log.info("✅ Toyota Corolla 2023 criado! R$ 85.000 = ${}", precoEmDolar);
            }

            // Toyota Camry 2022 - Cinza - R$ 120.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("TOY2022C").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("120000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo toyota2 = Veiculo.builder()
                        .marca("Toyota")
                        .modelo("Camry")
                        .ano(2022)
                        .placa("TOY2022C")
                        .cor("Cinza")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(toyota2);
                log.info("✅ Toyota Camry 2022 criado! R$ 120.000 = ${}", precoEmDolar);
            }

            // Toyota RAV4 2023 - Azul - R$ 145.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("TOY2023B").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("145000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo toyota3 = Veiculo.builder()
                        .marca("Toyota")
                        .modelo("RAV4")
                        .ano(2023)
                        .placa("TOY2023B")
                        .cor("Azul")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(toyota3);
                log.info("✅ Toyota RAV4 2023 criado! R$ 145.000 = ${}", precoEmDolar);
            }

            // Ford Fiesta 2021 - Vermelho - R$ 40.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FOR2021R").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("40000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo ford1 = Veiculo.builder()
                        .marca("Ford")
                        .modelo("Fiesta")
                        .ano(2021)
                        .placa("FOR2021R")
                        .cor("Vermelho")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(ford1);
                log.info("✅ Ford Fiesta 2021 criado! R$ 40.000 = ${}", precoEmDolar);
            }

            // Ford Focus 2022 - Branco - R$ 65.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FOR2022B").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("65000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo ford2 = Veiculo.builder()
                        .marca("Ford")
                        .modelo("Focus")
                        .ano(2022)
                        .placa("FOR2022B")
                        .cor("Branco")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(ford2);
                log.info("✅ Ford Focus 2022 criado! R$ 65.000 = ${}", precoEmDolar);
            }

            // Ford Ranger 2023 - Preta - R$ 175.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FOR2023P").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("175000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo ford3 = Veiculo.builder()
                        .marca("Ford")
                        .modelo("Ranger")
                        .ano(2023)
                        .placa("FOR2023P")
                        .cor("Preta")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(ford3);
                log.info("✅ Ford Ranger 2023 criado! R$ 175.000 = ${}", precoEmDolar);
            }

            // Ford Fusion 2021 - Cinza - R$ 95.000
            if (veiculoRepository.findByPlacaAndAtivoTrue("FOR2021C").isEmpty()) {
                BigDecimal precoEmDolar = new BigDecimal("95000.00").divide(taxaDolar, 2, BigDecimal.ROUND_HALF_UP);
                Veiculo ford4 = Veiculo.builder()
                        .marca("Ford")
                        .modelo("Fusion")
                        .ano(2021)
                        .placa("FOR2021C")
                        .cor("Cinza")
                        .preco(precoEmDolar)
                        .ativo(true)
                        .build();
                veiculoRepository.save(ford4);
                log.info("✅ Ford Fusion 2021 criado! R$ 95.000 = ${}", precoEmDolar);
            }

            log.info("🎉 Inicialização de veículos de teste concluída!");
            log.info("   2 Fiat, 3 Toyota e 4 Ford criados com sucesso em USD!");
        };
    }
}
