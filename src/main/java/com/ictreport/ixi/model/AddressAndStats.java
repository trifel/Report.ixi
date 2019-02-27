package com.ictreport.ixi.model;

public class AddressAndStats {
    private Address address;
    private Stats stats;

    public AddressAndStats(Address address) {
        this.address = address;
        this.stats = new Stats();
    }

    public AddressAndStats(Address address, Stats stats) {
        this.address = address;
        this.stats = stats;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    @Override
    public String toString() {
        return "AddressAndStats{" +
                "address=" + address +
                ", stats=" + stats +
                '}';
    }
}