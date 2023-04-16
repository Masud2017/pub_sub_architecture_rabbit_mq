package org.networking_project.services;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.checkerframework.checker.units.qual.A;
import org.networking_project.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Producers {
    private String CostQueryExchange = "CostQueryExchange";
    private String ExpensiveStateListExchange = "ExpensiveStateListExchange";
    private String EconomicStateListExchange = "EconomicStateListChange";
    private String HighestGrowthRateListExchange = "HighestGrowthRateListExchange";
    private String AggragateRegionsOverallExpenseExchange = "AggragateRegionsOverallExpenseExchange";

    private EduCostService eduCostService;

    public Producers () throws IOException {
        this.eduCostService = new EduCostService();
    }

    public void startCostQueryProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(this.CostQueryExchange, "topic");

        CostQueryRequest query = new CostQueryRequest();
        query.setYear("2013");
        query.setType("Private");
        query.setLength("4-year");
        query.setExpense("Fees/Tuition");
        query.setState("Alabama");

        CostQuery response = this.eduCostService.queryCost(query);

        Gson jsonSerializer = new Gson();

        String message = jsonSerializer.toJson(response);
        String routingKey = "CostQuery.firstResponse";

        channel.basicPublish(this.CostQueryExchange, routingKey, null, message.getBytes());

        // Close connection
        channel.close();
        connection.close();
    }

    public void startExpensiveStateListProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(this.ExpensiveStateListExchange, "topic");

        ExpensiveStateQuery query = new ExpensiveStateQuery();

        query.setYear("2013");
        query.setType("Private");
        query.setLength("4-year");

        ExpensiveStateResponse response = this.eduCostService.queryExpensiveStateList(query);

        Gson jsonSerializer = new Gson();

        String message = jsonSerializer.toJson(response);
        String routingKey = "ExpensiveStateList.firstResponse";

        channel.basicPublish(this.ExpensiveStateListExchange, routingKey, null, message.getBytes());

        // Close connection
        channel.close();
        connection.close();
    }

    public void startEconomicStateListProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(this.EconomicStateListExchange, "topic");

        EconomicStateQuery query = new EconomicStateQuery();

        query.setLength("4-year");
        query.setYear("2013");
        query.setType("Private");

        EconomicStateResponseList response = this.eduCostService.queryEconomicStateList(query);

        Gson jsonSerializer = new Gson();

        String message = jsonSerializer.toJson(response);
        String routingKey = "EconomicStateList.firstResponse";

        channel.basicPublish(this.EconomicStateListExchange, routingKey, null, message.getBytes());

        channel.close();
        connection.close();
    }

    public void startHighestGrowthRateListProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(this.HighestGrowthRateListExchange, "topic");

        HighestGrowthRateQuery query = new HighestGrowthRateQuery();
        List<String> years = new ArrayList<>();
        years.add("2013");
        years.add("2014");
        years.add("2015");

        query.setYears(years);
        query.setType("Public In-State");
        query.setLength("4-year");

        HighestGrowthRateResponseList response = this.eduCostService.queryHighestGrowthRateList(query);

        Gson jsonSerializer = new Gson();

        String message = jsonSerializer.toJson(response);
        String routingKey = "HighestGrowthRateList.firstResponse";

        channel.basicPublish(this.HighestGrowthRateListExchange, routingKey, null, message.getBytes());

        channel.close();
        connection.close();
    }

    public void startAggragateRegionsOverallExpenseProducer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(this.AggragateRegionsOverallExpenseExchange, "topic");

        AggragateRegionsOverallExpenseQuery query = new AggragateRegionsOverallExpenseQuery();
        query.setYear("2013");
        query.setType("Public In-State");
        query.setRegion("South");
        query.setLength("4-year");
        AggragateRegionsOverallExpenseResponse response = this.eduCostService.queryAggragateRegionsOverallExpense(query);

        Gson jsonSerializer = new Gson();

        String message = jsonSerializer.toJson(response);
        String routingKey = "AggragateRegionsOverallExpense.firstResponse";

        channel.basicPublish(this.AggragateRegionsOverallExpenseExchange, routingKey, null, message.getBytes());

        // Close connection
        channel.close();
        connection.close();
    }
}
