package com.net.comm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.net.comm.exception.IncorrectRequestException;
import com.net.comm.exception.StockAlreadyExistsException;
import com.net.comm.exception.StockNotFoundException;
import com.net.comm.model.Stock;
import com.net.comm.repository.StockRepository;
import com.net.comm.request.NewStockRequest;
import com.net.comm.request.PriceUpdateRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= {com.net.comm.StockControllerTests.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StockControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockRepository stockRepository;
    

    private static void assertException(Exception exception, Class<? extends Throwable> exceptionClass, String message) {
        assertThat(exception).isNotNull().isExactlyInstanceOf(exceptionClass);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testGetAllStocks() throws Exception {
        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"id\":1,\"name\":\"Stock1.L\",\"currentPrice\":2.0,\"lastUpdate\":\"2020-05-01T22:56:04Z\"}," +
                                "{\"id\":2,\"name\":\"Stock2.N\",\"currentPrice\":1.92,\"lastUpdate\":\"2020-05-02T21:08:47Z\"}" +
                                "]",
                        true));
    }

    @Test
    void testGetStockById() throws Exception {
        mockMvc.perform(get("/api/stocks/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"id\":2,\"name\":\"Stock2.N\",\"currentPrice\":1.92,\"lastUpdate\":\"2020-05-02T21:08:47Z\"}",
                        true));
    }

    @Test
    void testGetIncorrectId() throws Exception {
        Exception exception = mockMvc.perform(get("/api/stocks/3"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();
        assertException(exception, StockNotFoundException.class, "Stock with id 3 not found");
    }

    @Test
    void testGetUnparsedId() throws Exception {
        Exception exception = mockMvc.perform(
                get("/api/stocks/number"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, MethodArgumentTypeMismatchException.class, "Failed to convert value of type 'java.lang.String' to required type 'long'; nested exception is java.lang.NumberFormatException: For input string: \"number\"");
    }

    @Test
    void testGetHistory() throws Exception {
        mockMvc.perform(
                get("/api/stocks/1/history"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"price\":2.0,\"startDate\":\"2020-05-01T22:56:04Z\"}," +
                                "{\"price\":1.99,\"startDate\":\"2020-05-01T21:54:03Z\"}" +
                                "]",
                        true
                ));
    }

    @Test
    void testGetHistoryIncorrectId() throws Exception {
        Exception exception = mockMvc.perform(
                get("/api/stocks/3/history"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();
        assertException(exception, StockNotFoundException.class, "Stock with id 3 not found");
    }

    @Test
    void testPostNewStock() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("   Stock3.H ");
        newStockRequest.setPrice(0.23d);

        Instant timestamp = Instant.now();

        mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("New stock was created"))
                .andExpect(header().string("Location", equalTo("http://localhost/api/stocks/3")))
                .andExpect(redirectedUrl("http://localhost/api/stocks/3"));

        Optional<Stock> expectedStock = stockRepository.findByName("Stock3.H");
        assertThat(expectedStock)
                .isPresent()
                .hasValueSatisfying(stock -> Assertions.assertAll(
                        () -> assertThat(stock.getId()).isEqualTo(3),
                        () -> assertThat(stock.getName()).isEqualTo("Stock3.H"),
                        () -> assertThat(stock.getCurrentPrice()).isEqualTo(0.23d),
                        () -> assertThat(stock.getLastUpdate()).isAfter(timestamp)));
    }

    @Test
    void testPostNewStockZeroPrice() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("Stock4.N");
        newStockRequest.setPrice(0d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than 0");
    }

    @Test
    void testPostNewStockEmptyName() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("     ");
        newStockRequest.setPrice(1.1d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, IncorrectRequestException.class, "Stock name can't be empty");
    }

    @Test
    void testPostNewStockAlreadyExists() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("Stock1.L");
        newStockRequest.setPrice(1.1d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, StockAlreadyExistsException.class, "Stock already exists with name: Stock1.L");
    }

    @Test
    void testPutNewPrice() throws Exception {
        double newPrice = 3.1;

        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(newPrice);

        Optional<Stock> expectedStock = stockRepository.findById(2L);
        assertThat(expectedStock).isPresent();
        Stock expectedStockValue = expectedStock.get();
        String originalStockName = expectedStockValue.getName();
        Instant timestampBeforeUpdate = expectedStockValue.getLastUpdate();

        mockMvc.perform(
                put("/api/stocks/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isOk());

        Optional<Stock> updatedStock = stockRepository.findById(2L);
        assertThat(updatedStock)
                .isPresent()
                .hasValueSatisfying(stock -> Assertions.assertAll(
                        () -> assertThat(stock.getName()).isEqualTo(originalStockName),
                        () -> assertThat(stock.getCurrentPrice()).isEqualTo(newPrice),
                        () -> assertThat(stock.getLastUpdate()).isAfter(timestampBeforeUpdate)                   
                ));
    }


    @Test
    void testPutNewPriceIncorrectId() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(3d);

        Exception exception = mockMvc.perform(
                put("/api/stocks/3")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();
        assertException(exception, StockNotFoundException.class, "Stock with id 3 not found");
    }

    @Test
    void testPutNewPriceNegative() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(-3d);

        Exception exception = mockMvc.perform(
                put("/api/stocks/1")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than 0");
    }

    @Test
    void testPutNewPriceNull() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();

        Exception exception = mockMvc.perform(
                put("/api/stocks/1")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than 0");
    }
}
