package org.networking_project.services;

import org.bson.Document;
import org.networking_project.dao.RpcMongoDataRepository;
import org.networking_project.models.*;
import org.networking_project.utils.Environment;

import java.io.IOException;
import java.util.List;

public class EduCostService {
    private RpcMongoDataRepository repository = new RpcMongoDataRepository(Environment.getInstance().getByKey("db.name"),
            Environment.getInstance().getByKey("db.collection_name"));

    public EduCostService() throws IOException {
    }


    public CostQuery queryCost(CostQueryRequest request) {
        CostQuery res = this.repository.getEduCost(request.getYear(),request.getState(),request.getType(), request.getLength()
                , request.getExpense());

        return res;
    }

    public ExpensiveStateResponse queryExpensiveStateList(ExpensiveStateQuery request) {
        ExpensiveStateResponse res = this.repository.getExpensiveStatList(request.getYear(), request.getType(), request.getLength());

        return res;
    }

    public EconomicStateResponseList queryEconomicStateList(EconomicStateQuery request) {
        List<EconomicStateResponse> economicStateResponseList = this.repository
                .queryEconomicStat(request.getYear(), request.getType(), request.getLength());

        EconomicStateResponseList responseList = new EconomicStateResponseList();
        responseList.setEconomicStateResponseList(economicStateResponseList);

        return responseList;
    }

    public HighestGrowthRateResponseList queryHighestGrowthRateList(HighestGrowthRateQuery request) {
        String[] yearArr = new String[request.getYears().size()];
        int idx = 0;
        for (String yearItem : request.getYears()) {
            yearArr[idx] = yearItem;
            idx++;
        }
        List<HighestGrowthRateResponse> responseList = this.repository
                .queryHighestGrowthRateList(yearArr, request.getType(), request.getLength());


        HighestGrowthRateResponseList responseList1 = new HighestGrowthRateResponseList();
        responseList1.setHighestGrowthRateResponseList(responseList);


        return responseList1;
    }

    public AggragateRegionsOverallExpenseResponse queryAggragateRegionsOverallExpense(AggragateRegionsOverallExpenseQuery request) {
        Document doc = new Document();
        doc.append("Expense",this.repository.queryRegionsAverageExpense(request.getType(), request.getLength(), request.getRegion(), request.getYear()));

        return this.repository.queryRegionsAverageExpense(request.getType(), request.getLength(), request.getRegion(), request.getYear());
    }
}
