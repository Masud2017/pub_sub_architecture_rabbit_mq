package org.networking_project.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.networking_project.models.*;
import org.networking_project.utils.Environment;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>RpcMongoDataRepository</h1>
 * A class that handles data query operations for mongoDb.
 * */
public class RpcMongoDataRepository {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection mongoCollection;

    /**
     * Constructor.
     * @param dbName - Name of the database
     * @param collectionName - name of the collection name
     * */
    public RpcMongoDataRepository(final String dbName,
                                  final String collectionName)
            throws IOException {
        this.mongoClient = MongoDBSingleton.getInstance(Integer
                        .parseInt(Environment
                                .getInstance()
                                .getByKey("db.port")),
                true);
        this.mongoDatabase = this.mongoClient.getDatabase(dbName);
        this.mongoCollection = this.mongoDatabase.getCollection(collectionName);
    }

    public CostQuery getEduCost(String year, String state, String type,
                                String length, String expense) {

        Document doc = new Document();
        doc.append("Year",year);
        doc.append("Stat",state);
        doc.append("Type",type);
        doc.append("Length",length);
        doc.append("Expense",expense);
        Document result = (Document) this.mongoCollection.find(doc).first();

        CostQuery response = new CostQuery();
        response.setCostQuery(result.getString("Value"));
        return response;
    }

    public ExpensiveStateResponse getExpensiveStatList(String year, String type, String length) {

        Document doc = new Document();
        doc.append("Length",length);
        doc.append("Year",year);
        doc.append("Type",type);

        FindIterable<Document> resultList = this.mongoCollection.find(doc);



        List<String> statList = new ArrayList<>();
        for (Document documentItem : resultList) {
//            org.rpcproject.compiled_protobuf_classes.EduCost tempEducost = org.rpcproject.compiled_protobuf_classes.EduCost.newBuilder()
//                    .setExpense(documentItem.getString("Expense"))
//                    .setYear(documentItem.getString("Year"))
//                    .setStat(documentItem.getString("Stat"))
//                    .setType(documentItem.getString("Type"))
//                    .setLength(documentItem.getString("Length"))
//                    .setValue(documentItem.getString("Value"))
//                    .build();

            statList.add(documentItem.getString("Stat"));
        }

//        ExpensiveStateResponse.Builder response = org.rpcproject.compiled_protobuf_classes.ExpensiveStateResponse
//                .newBuilder()
//                .addAllStates(statList);
        ExpensiveStateResponse response = new ExpensiveStateResponse();
        response.setStates(statList);

        return response;
    }

    public List<EconomicStateResponse> queryEconomicStat(String year, String type, String length) {
        List<EconomicStateResponse> economicStateResponseList = new ArrayList<>();


        Document filter = new Document();
        filter.append("Year",year);
        filter.append("Type",type);
        filter.append("Length", length);
        Iterator cursor = this.mongoCollection.find(filter).limit(5).iterator();

        Integer expense = 0;
        while(cursor.hasNext()) {
            Document doc = (Document) cursor.next();
            expense += Integer.parseInt(doc.getString("Value"));

            EconomicStateResponse economicStateResponse = new EconomicStateResponse();

            economicStateResponse.setLength(length);
            economicStateResponse.setType(type);
            economicStateResponse.setYear(year);
            economicStateResponse.setExpense(String.valueOf(expense));

            economicStateResponseList.add(economicStateResponse);
        }


        return economicStateResponseList;
    }


    public List<HighestGrowthRateResponse> queryHighestGrowthRateList(String[] yearsStr, String type, String length) {
        List<HighestGrowthRateResponse> highestGrowthRateResponseArrayList = new ArrayList<>();

        List<Document> docs = (List<Document>) this.mongoCollection.find(
                Filters.and(
                        Filters.in("Year", yearsStr),
                        Filters.eq("Type", type),
                        Filters.eq("Length", length)
                )
        ).into(new ArrayList<>());

        Map<String, List<Integer>> expensesByState = new HashMap<>();
        for (Document doc : docs) {
            String state = doc.getString("Stat");
            int year = Integer.parseInt(doc.getString("Year"));
            int expense = Integer.parseInt(doc.getString("Value"));
            if (!expensesByState.containsKey(state)) {
                expensesByState.put(state, new ArrayList<>());
            }
            expensesByState.get(state).add(expense);
        }

        Map<String, Double> growthRatesByState = new HashMap<>();
        for (String state : expensesByState.keySet()) {
            List<Integer> expenses = expensesByState.get(state);
            int baseYearExpense = expenses.get(expenses.size() - 1);
            double growthRate = (expenses.get(0) - baseYearExpense) / (double) baseYearExpense;
            growthRatesByState.put(state, growthRate);
        }

        List<Map.Entry<String, Double>> sortedGrowthRates = growthRatesByState.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .collect(Collectors.toList());

        for (Map.Entry<String, Double> entry : sortedGrowthRates) {
            String state = entry.getKey();
            double averageGrowthRate = entry.getValue();

            HighestGrowthRateResponse highestGrowthRateResponse = new HighestGrowthRateResponse();

            highestGrowthRateResponse.setState(state);
            highestGrowthRateResponse.setExpense(String.valueOf(averageGrowthRate));

            highestGrowthRateResponseArrayList.add(highestGrowthRateResponse);
        }

        return highestGrowthRateResponseArrayList;
    }


    public AggragateRegionsOverallExpenseResponse queryRegionsAverageExpense(String type,
                                                                             String length,
                                                                             String region,
                                                                             String year) {
        List<String> statList = this.getRegionInfo().get(region);

        List<Document> resultList = new ArrayList<>();

        for(String statItem : statList) {
            Document tempdoc = new Document();
            tempdoc.append("State",statItem);
            Document filterInfo = new Document();
            filterInfo.append("Year",year);
            filterInfo.append("Length",length);
            filterInfo.append("Type",type);

            Document result = (Document) this.mongoCollection.find(tempdoc).filter(filterInfo).first();
            resultList.add(result);
        }

        AggragateRegionsOverallExpenseResponse respone = new AggragateRegionsOverallExpenseResponse();

        respone.setYear(year);
        respone.setOverallExpense(this.getOverAllExpenseFromRegionDataList(resultList));

        return respone;
    }

    public boolean createAndSaveDataToNewCollection(String collectionName, Document collectionData) {
        try {
            // if not exist
            this.mongoDatabase.createCollection(collectionName);
        } catch(MongoCommandException e) {

        }
        MongoCollection collection = this.mongoDatabase.getCollection(collectionName);

        collection.insertOne(collectionData);


        return false;
    }

    protected String getOverAllExpenseFromRegionDataList(List<Document> resultList) {
        Integer expense = 0;
        for (Document documentItem : resultList) {
            expense += Integer.parseInt(documentItem.getString("Value"));
        }

        return String.valueOf(expense);
    }

    protected Map<String,List<String>> getRegionInfo() {
        Map<String,List<String>> map = new HashMap<>();
        List<String> regionWestList = Arrays.asList( "California", "Colorado", "Hawaii", "Nevada", "Oregon", "Washington");
        map.put("West",regionWestList);

        List<String> regionNortheastList = Arrays.asList("Connecticut", "Maine", "Massachusetts", "New Hampshire", "New Jersey", "New York", "Pennsylvania", "Rhode Island", "Vermont");
        map.put("Northeast",regionNortheastList);

        List<String> regionSouthList = Arrays.asList("Alabama",
                "Arkansas", "Delaware", "District of Columbia", "Florida", "Georgia", "Kentucky", "Louisiana", "Maryland", "Mississippi", "North Carolina", "Oklahoma", "South Carolina", "Tennessee", "Texas", "Virginia", "West Virginia");
        map.put("South",regionSouthList);

        List<String> regionMidwestList = Arrays.asList( "Illinois", "Indiana", "Iowa", "Kansas", "Michigan", "Minnesota", "Missouri", "Nebraska", "North Dakota", "Ohio", "South Dakota", "Wisconsin");
        map.put("Midwest",regionMidwestList);

        return map;
    }

//    public static void main (String[] args) throws IOException {
//        RpcMongoDataRepository repository = new RpcMongoDataRepository("rpc_communication","EduCostStat");
//        org.rpcproject.compiled_protobuf_classes
//                .AggragateRegionsOverallExpenseResponse response = repository
//                .queryRegionsAverageExpense("Public In-State","4-year","South","2013");
//        System.out.println(response.getYear() + "  "+response.getOverallExpense());
////
////
////
////        Document tempDoc = new Document();
////        tempDoc.append("Name","Jack2");
////        tempDoc.append("Email","jack2@yopmail.com");
////
////        Document uniqueDoc = new Document();
////        uniqueDoc.append("Email","jack2@yopmail.com");
////        repository.createAndSaveDataToNewCollection("test2",tempDoc,uniqueDoc);
//
//
//        org.rpcproject.compiled_protobuf_classes.ResponseToQuery res = repository.getEduCost("2013","Alabama","Private","4-year","Fees/Tuition");
//        System.out.println(res.getCost());
//
//    }
}
