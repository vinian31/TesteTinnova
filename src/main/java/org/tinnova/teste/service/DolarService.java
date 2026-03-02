package org.tinnova.teste.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.tinnova.teste.exception.DolarAPIException;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DolarService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "dolar_taxa";
    private static final long CACHE_TTL_MINUTES = 60;
    private static final String PRIMARY_API = "https://economia.awesomeapi.com.br/json/last/USD-BRL";
    private static final String FALLBACK_API = "https://api.frankfurter.app/latest?from=USD&to=BRL";

    public BigDecimal obterTaxaDolar() {
        try {
            Object cachedTaxa = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedTaxa != null) {
                return new BigDecimal(cachedTaxa.toString());
            }

            try {
                BigDecimal taxa = obterTaxaDaPrimaryAPI();
                salvarNoCache(taxa);
                return taxa;
            } catch (Exception e) {
                log.warn("Erro ao obter taxa da API primária, tentando fallback: {}", e.getMessage());

                BigDecimal taxa = obterTaxaDaFallbackAPI();
                salvarNoCache(taxa);
                return taxa;
            }
        } catch (Exception e) {
            log.error("Erro ao obter taxa de dólar: {}", e.getMessage(), e);
            throw new DolarAPIException("Falha ao obter taxa de dólar. Tente novamente mais tarde.");
        }
    }

    private BigDecimal obterTaxaDaPrimaryAPI() {
        try {
            String response = restTemplate.getForObject(PRIMARY_API, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode usdBrlNode = rootNode.get("USDBRL");
            String bid = usdBrlNode.get("bid").asText();
            return new BigDecimal(bid);
        } catch (Exception e) {
            throw new DolarAPIException("Erro ao chamar API primária: " + e.getMessage(), e);
        }
    }

    private BigDecimal obterTaxaDaFallbackAPI() {
        try {
            String response = restTemplate.getForObject(FALLBACK_API, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode ratesNode = rootNode.get("rates");
            String brlRate = ratesNode.get("BRL").asText();
            return new BigDecimal(brlRate);
        } catch (Exception e) {
            throw new DolarAPIException("Erro ao chamar API de fallback: " + e.getMessage(), e);
        }
    }

    private void salvarNoCache(BigDecimal taxa) {
        redisTemplate.opsForValue().set(CACHE_KEY, taxa.toString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Taxa de dólar salva em cache: {}", taxa);
    }

    public void invalidarCache() {
        redisTemplate.delete(CACHE_KEY);
        log.info("Cache de dólar invalidado");
    }
}

