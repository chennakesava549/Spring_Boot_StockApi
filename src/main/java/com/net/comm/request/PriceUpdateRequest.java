package com.net.comm.request;

/**
 * Class containing data about incoming requests to update price for particular stock.
 */

public class PriceUpdateRequest {

    private Double price;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PriceUpdateRequest{" +
                "price=" + price +
                '}';
    }
}
