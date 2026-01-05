package de.demo.lending.procurement.application.ports.out;

import de.demo.lending.procurement.domain.SupplierInfo;

public interface ProcurementClient {
    SupplierInfo searchBook(String query);

    String orderBook(String externalBookId);
}
