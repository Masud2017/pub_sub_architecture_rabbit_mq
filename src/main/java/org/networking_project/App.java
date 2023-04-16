package org.networking_project;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.networking_project.services.Producers;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Hello world!
 *
 */
public class App 
{
    private static String EXCHANGE = "EduCostQueue";
    public static void main( String[] args ) throws IOException, TimeoutException {
        Producers producers = new Producers();

        producers.startCostQueryProducer();
        producers.startExpensiveStateListProducer();
        producers.startEconomicStateListProducer();
        producers.startHighestGrowthRateListProducer();
        producers.startAggragateRegionsOverallExpenseProducer();
    }
}
