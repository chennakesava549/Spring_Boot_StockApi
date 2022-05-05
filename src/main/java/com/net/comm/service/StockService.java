package com.net.comm.service;

import com.net.comm.exception.IncorrectRequestException;
import com.net.comm.exception.StockAlreadyExistsException;
import com.net.comm.exception.StockNotFoundException;
import com.net.comm.model.Stock;
import com.net.comm.repository.StockRepository;
import com.net.comm.request.NewStockRequest;
import com.sun.istack.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Service to perform business logic on {@link Stock} entities.
 */
@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    //Get a list of stocks
    @Transactional
    @NonNull
    public Collection<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
    
    //Get a list of stocks using Paging 
    @Transactional
    @NonNull
    public Page<Stock> getAllStocks(int page) {
    	int pageSize=4;
    	Pageable pageable = PageRequest.of(pageSize - 1, pageSize);
        return stockRepository.findAll(pageable);
    }

    //Get one stock from the list
    @Transactional
    @NonNull
    public Stock lookupStock(long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock with id " + id + " not found"));
    }

    //create a new stock
    @Transactional
    @NonNull
    public Stock addNewStock(@NonNull NewStockRequest newStockRequest) {
        Stock newStock = new Stock();
        newStock.setCurrentPrice(validatePrice(newStockRequest.getPrice()));
        newStock.setName(validateName(newStockRequest.getName()));
        return saveStock(newStock);
    }

    //Update the price of a single stock
    @Transactional
    @NonNull
    public Stock updateStockPrice(long id, @Nullable Double price) {
        validatePrice(price);
        Stock stock = lookupStock(id);
        stock.setCurrentPrice(price);
        return saveStock(stock);
    }

    //Delete a single stock
    @Transactional
    @NonNull
    private Stock saveStock(@NonNull Stock stock) {
        Instant lastUpdate = Instant.now();
        stock.setLastUpdate(lastUpdate);
        Stock savedStock = stockRepository.save(stock);

        return savedStock;
    }

    /**
     * Validates that price is greater than 0.
     *
     * @param price - price to validate.
     * @return input price.
     * @throws IncorrectRequestException when price is 0 or less.
     */
    private static double validatePrice(@Nullable Double price) {
        if (price == null || price <= 0) {
            throw new IncorrectRequestException("Stock price should be greater than 0");
        }
        return price;
    }

    /**
     * Validates that name is:
     * - not null
     * - not empty
     * - unique among all Stocks.
     *
     * @param name - name to validate.
     * @return input name without leading and trailing whitespaces.
     */
    @NonNull
    private String validateName(@Nullable String name) {
        if (name == null || name.isEmpty() || name.trim().isEmpty()) {
            throw new IncorrectRequestException("Stock name can't be empty");
        }
        String cleanedName = name.trim();
        Optional<Stock> actualStock = stockRepository.findByName(cleanedName);
        if (actualStock.isPresent()) {
            throw new StockAlreadyExistsException("Stock already exists with name: " + cleanedName);
        }
        return cleanedName;
    }
}

