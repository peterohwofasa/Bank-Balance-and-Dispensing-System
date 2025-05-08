package com.bank.balancedispense;

import org.springframework.boot.SpringApplication;

public class TestBalanceDispenseApplication {

    public static void main(String[] args) {
        SpringApplication.from(BalanceDispenseApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
